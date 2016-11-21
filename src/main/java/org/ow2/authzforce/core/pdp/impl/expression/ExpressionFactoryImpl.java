/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.expression;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableDefinition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.VariableReferenceType;

import org.ow2.authzforce.core.pdp.api.EnvironmentProperties;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.ExpressionFactory;
import org.ow2.authzforce.core.pdp.api.expression.FunctionExpression;
import org.ow2.authzforce.core.pdp.api.expression.VariableReference;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.core.pdp.impl.CloseableAttributeProvider;
import org.ow2.authzforce.core.pdp.impl.func.FunctionRegistry;
import org.ow2.authzforce.xmlns.pdp.ext.AbstractAttributeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.UnknownIdentifierException;

/**
 * Implementation of ExpressionFactory that supports the Expressions defined in VariableDefinitions in order to resolve VariableReferences. In particular, it makes sure the depth of recursivity of
 * VariableDefinition does not exceed a value (to avoid inconveniences such as stackoverflow or very negative performance impact) defined by {@code maxVarRefDef} parameter to
 * {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, List, int, boolean, boolean, EnvironmentProperties)}. Note that reference loops are avoided by the fact that a
 * VariableReference can reference only a VariableDefinition defined previously to the VariableReference in this implementation.
 *
 * 
 * @version $Id: $
 */
public final class ExpressionFactoryImpl implements ExpressionFactory
{
	/**
	 * Base class for evaluating XACML VariableReferences that holds the variable ID and longest VariableReference chain found in the referenced variable's expression, in order to detect abuse of such
	 * chains, which is unsafe.
	 *
	 * @param <V>
	 *            evaluation's return type
	 * 
	 * @version $Id: $
	 */
	private static abstract class BaseVariableReference<V extends Value> implements VariableReference<V>
	{

		protected final String variableId;
		private final transient Deque<String> longestVariableReferenceChain;

		/**
		 * Constructor that takes a variable identifier
		 *
		 * @param varId
		 *            variable ID
		 * @param longestVarRefChain
		 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ... -> Vn, where "V1 -> V2" means VariableReference V1's expression contains one or more
		 *            VariableReferences to V2)
		 */
		private BaseVariableReference(final String varId, final Deque<String> longestVarRefChain)
		{
			this.variableId = varId;
			this.longestVariableReferenceChain = longestVarRefChain;
		}

		/** {@inheritDoc} */
		@Override
		public final String getVariableId()
		{
			return this.variableId;
		}

		/** {@inheritDoc} */
		@Override
		public final JAXBElement<VariableReferenceType> getJAXBElement()
		{
			return JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createVariableReference(new VariableReferenceType(variableId));
		}

		/**
		 * <p>
		 * Getter for the field <code>longestVariableReferenceChain</code>.
		 * </p>
		 *
		 * @return the longestVariableReferenceChain
		 */
		private final Deque<String> getLongestVariableReferenceChain()
		{
			return longestVariableReferenceChain;
		}
	}

	private static final class ConstantVariableReference<V extends Value> extends BaseVariableReference<V>
	{
		private final transient V varValue;
		private final transient Datatype<V> varDatatype;

		/**
		 * Constructor that takes a variable identifier
		 *
		 * @param varId
		 *            variable ID
		 * @param varValue
		 *            constant value (for constant variable)
		 * @param varDatatype
		 *            variable datatype
		 * @param longestVarRefChain
		 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ... -> Vn, where "V1 -> V2" means VariableReference V1's expression contains one or more
		 *            VariableReferences to V2)
		 */
		public ConstantVariableReference(final String varId, final V varValue, final Datatype<V> varDatatype, final Deque<String> longestVarRefChain)
		{
			super(varId, longestVarRefChain);
			this.varValue = varValue;
			this.varDatatype = varDatatype;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Returns the type of the referenced expression.
		 */
		@Override
		public Datatype<V> getReturnType()
		{
			return this.varDatatype;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
		 */
		/** {@inheritDoc} */
		@Override
		public V getValue()
		{
			return this.varValue;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Evaluates the referenced expression using the given context, and either returns an error or a resulting value. If this doesn't reference an evaluatable expression (eg, a single Function)
		 * then this will throw an exception.
		 */
		@Override
		public V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			return this.varValue;
		}
	}

	private static final class DynamicVariableReference<V extends Value> extends BaseVariableReference<V>
	{
		private final transient Expression<V> expression;
		private final transient IndeterminateEvaluationException nullContextException;

		/**
		 * Constructor that takes a variable identifier
		 *
		 * @param varId
		 *            input VariableReference from XACML model
		 * @param varExpr
		 *            Expression of referenced VariableDefinition
		 * @param longestVarRefChain
		 *            longest chain of VariableReference Reference in <code>expr</code> (V1 -> V2 -> ... -> Vn, where "V1 -> V2" means VariableReference V1's expression contains one or more
		 *            VariableReferences to V2)
		 */
		public DynamicVariableReference(final String varId, final Expression<V> varExpr, final Deque<String> longestVarRefChain)
		{
			super(varId, longestVarRefChain);
			this.expression = varExpr;
			this.nullContextException = new IndeterminateEvaluationException("VariableReference[VariableId='" + this.variableId
					+ "']: evaluate(context = null) not allowed because the variable requires context for evaluation (not constant)", StatusHelper.STATUS_PROCESSING_ERROR);
		}

		/**
		 * {@inheritDoc}
		 *
		 * Returns the type of the referenced expression.
		 */
		@Override
		public Datatype<V> getReturnType()
		{
			return expression.getReturnType();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.thalesgroup.authzforce.core.eval.Expression#isStatic()
		 */
		/** {@inheritDoc} */
		@Override
		public V getValue()
		{
			return null;
		}

		/**
		 * {@inheritDoc}
		 *
		 * Evaluates the referenced expression using the given context, and either returns an error or a resulting value. If this doesn't reference an evaluatable expression (eg, a single Function)
		 * then this will throw an exception.
		 */
		@Override
		public V evaluate(final EvaluationContext context) throws IndeterminateEvaluationException
		{
			if (context == null)
			{
				throw nullContextException;
			}

			final V ctxVal = context.getVariableValue(this.variableId, expression.getReturnType());
			if (ctxVal != null)
			{
				return ctxVal;
			}

			// ctxVal == null: not evaluated yet in this context -> evaluate now
			final V result = expression.evaluate(context);
			context.putVariableIfAbsent(this.variableId, result);
			return result;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionFactoryImpl.class);

	private static final IllegalArgumentException MISSING_ATTRIBUTE_DESIGNATOR_ISSUER_EXCEPTION = new IllegalArgumentException(
			"Missing Issuer that is required on AttributeDesignators by PDP configuration");

	private static final IllegalArgumentException UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION = new IllegalArgumentException("Unsupported Expression type (optional XACML feature): AttributeSelector");

	private static final IllegalArgumentException NULL_FUNCTION_REGISTRY_EXCEPTION = new IllegalArgumentException("Undefined function registry");

	private static final IllegalArgumentException NULL_ATTRIBUTE_DATATYPE_REGISTRY_EXCEPTION = new IllegalArgumentException("Undefined attribute datatype registry");

	private static final IllegalArgumentException UNSUPPORTED_ATTRIBUTE_DESIGNATOR_OR_SELECTOR_BECAUSE_OF_NULL_ATTRIBUTE_PROVIDER_EXCEPTION = new IllegalArgumentException(
			"Unsupported Expression type 'AttributeDesignator' and 'AttributeSelector' because no attribute Provider defined");

	private static final int UNLIMITED_MAX_VARIABLE_REF_DEPTH = -1;

	private final DatatypeFactoryRegistry datatypeFactoryRegistry;
	private final FunctionRegistry functionRegistry;
	private final CloseableAttributeProvider attributeProvider;
	private final int maxVariableReferenceDepth;
	// the map from identifiers to internal data
	private final Map<String, BaseVariableReference<?>> idToVariableMap = HashCollections.newMutableMap();
	private final boolean allowAttributeSelectors;

	private final boolean issuerRequiredOnAttributeDesignators;

	/**
	 * Maximum VariableReference depth allowed for VariableDefinitions to be managed. Examples:
	 * <ul>
	 * <li>A VariableDefinition V1 that does not use any VariableReference has a reference depth of 0.</li>
	 * <li>A VariableDefinition V1 that uses a VariableReference to VariableDefinition V2 with no further VariableReference, has a reference depth of 1</li>
	 * <li>etc.</li>
	 * </ul>
	 *
	 * @param attributeFactory
	 *            attribute value factory (not null)
	 * @param functionRegistry
	 *            function registry (not null)
	 * @param jaxbAttributeProviderConfs
	 *            XML/JAXB configurations of Attribute Providers for AttributeDesignator/AttributeSelector evaluation; may be null for static expression evaluation (out of context), in which case
	 *            AttributeSelectors/AttributeDesignators are not supported
	 * @param maxVarRefDepth
	 *            max depth of VariableReference chaining: VariableDefinition -> VariableDefinition ->... ('->' represents a VariableReference); strictly negative value means unlimited
	 * @param allowAttributeSelectors
	 *            allow use of AttributeSelectors (experimental, not for production, use with caution)
	 * @param strictAttributeIssuerMatch
	 *            true iff we want strict Attribute Issuer matching and we require that all AttributeDesignators set the Issuer field.
	 *            <p>
	 *            "Strict Attribute Issuer matching" means that an AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0,
	 *            §5.29, in the case that the Issuer is not present in the Attribute Designator, but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice).
	 *            Indeed, the XACML 3.0 Attribute Evaluation section §5.29 says: "If the Issuer is not present in the AttributeDesignator, then the matching of the attribute to the named attribute
	 *            SHALL be governed by AttributeId and DataType attributes alone." Therefore, if {@code strictAttributeIssuerMatch} is false, since policies may use AttributeDesignators without
	 *            Issuer, if the requests are using matching Attributes but with none, one or more different Issuers, this PDP engine has to gather all the values from all the attributes with matching
	 *            Category/AttributeId but with any Issuer or no Issuer. Therefore, in order to stay compliant with §5.29 and still enforce best practice, when {@code strictAttributeIssuerMatch} is
	 *            true, we also require that all AttributeDesignators set the Issuer field. AttributeDesignators set the Issuer field.
	 * @param environmentProperties
	 *            global PDP configuration environment properties
	 * @throws java.lang.IllegalArgumentException
	 *             If any of attribute Provider modules created from {@code jaxbAttributeProviderConfs} does not provide any attribute; or it is in conflict with another one already registered to
	 *             provide the same or part of the same attributes.
	 * @throws java.io.IOException
	 *             error closing the attribute Provider modules created from {@code jaxbAttributeProviderConfs}, when and before an {@link IllegalArgumentException} is raised
	 */
	public ExpressionFactoryImpl(final DatatypeFactoryRegistry attributeFactory, final FunctionRegistry functionRegistry, final List<AbstractAttributeProvider> jaxbAttributeProviderConfs,
			final int maxVarRefDepth, final boolean allowAttributeSelectors, final boolean strictAttributeIssuerMatch, final EnvironmentProperties environmentProperties)
			throws IllegalArgumentException, IOException
	{
		if (attributeFactory == null)
		{
			throw NULL_ATTRIBUTE_DATATYPE_REGISTRY_EXCEPTION;
		}

		if (functionRegistry == null)
		{
			throw NULL_FUNCTION_REGISTRY_EXCEPTION;
		}

		this.datatypeFactoryRegistry = attributeFactory;
		this.functionRegistry = functionRegistry;
		this.maxVariableReferenceDepth = maxVarRefDepth < 0 ? UNLIMITED_MAX_VARIABLE_REF_DEPTH : maxVarRefDepth;
		// finally create the global attribute Provider used to resolve
		// AttributeDesignators
		this.attributeProvider = CloseableAttributeProvider.getInstance(jaxbAttributeProviderConfs, attributeFactory, strictAttributeIssuerMatch, environmentProperties);
		this.allowAttributeSelectors = allowAttributeSelectors;
		this.issuerRequiredOnAttributeDesignators = strictAttributeIssuerMatch;
	}

	private static <V extends Value> BaseVariableReference<?> newVariableReference(final String variableId, final Expression<V> variableExpression, final Deque<String> longestVarRefChainInExpression)
	{
		final V constant = variableExpression.getValue();
		if (constant != null)
		{
			/*
			 * Variable expression is constant
			 */
			if (LOGGER.isWarnEnabled() && !(variableExpression instanceof ConstantExpression))
			{
				LOGGER.warn("Expression of Variable {} is constant '{}', therefore should be replaced with a equivalent AttributeValue.", variableId, constant);
			}

			variableExpression.getReturnType();
			return new ConstantVariableReference<>(variableId, constant, variableExpression.getReturnType(), longestVarRefChainInExpression);
		}

		return new DynamicVariableReference<>(variableId, variableExpression, longestVarRefChainInExpression);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference<?> addVariable(final VariableDefinition varDef, final XPathCompiler xPathCompiler, final Deque<String> inoutLongestVarRefChain) throws IllegalArgumentException
	{
		final String varId = varDef.getVariableId();
		/*
		 * Initialize the longest variable reference chain from this VariableDefinition (varDef -> VarDef2 -> ..., where "v1 -> v2" means: v1's expression contains a VariableReference to v2) as empty
		 * for later update by this#getVariable() when resolving a VariableReference within this varDef's expression (being parsed just after). The goal is to detect chains longer than
		 * this.maxVariableReferenceDepth to limit abuse of VariableReferences. There may be multiple VariableReferences in a VariableDefinition's expression, such as an Apply, and each may be
		 * referencing a different VariableDefinition; but we are interested only in the one with the longest chain of references. Note that circular references are prevented by the fact that, in our
		 * implementation, a VariableReference can only reference a Variable previously declared.
		 */
		/*
		 * inoutLongestVarRefChain == null means that the longest VarRef chain will not be computed nor checked
		 */
		final Deque<String> longestVarRefChainInCurrentVarExpression = inoutLongestVarRefChain == null ? null : new ArrayDeque<>();
		final Expression<?> varExpr = getInstance(varDef.getExpression().getValue(), xPathCompiler, longestVarRefChainInCurrentVarExpression);
		/*
		 * if not null, longestVarRefChainInCurrentVarExpression has now been updated to longest VariableReference chain in varExpr
		 */
		if (inoutLongestVarRefChain != null && longestVarRefChainInCurrentVarExpression != null)
		{
			/*
			 * Check size only if there is a limit
			 */
			if (maxVariableReferenceDepth != UNLIMITED_MAX_VARIABLE_REF_DEPTH && longestVarRefChainInCurrentVarExpression.size() > this.maxVariableReferenceDepth)
			{
				throw new IllegalArgumentException("Max allowed VariableReference depth (" + this.maxVariableReferenceDepth + ") exceeded by length ("
						+ longestVarRefChainInCurrentVarExpression.size() + ") of longest VariableReference Reference chain found in Expression of Variable '" + varId + "': "
						+ longestVarRefChainInCurrentVarExpression);
			}

			/*
			 * Update inoutLongestVarRefChain if this variable's expression constains bigger chain
			 */
			if (longestVarRefChainInCurrentVarExpression.size() > inoutLongestVarRefChain.size())
			{
				// current longest is no longer the longest, so replace with new
				// longest's content
				inoutLongestVarRefChain.clear();
				/*
				 * This is a VariableDefinition we are looking at here, but there may not be any VariableReference to it; if there is, it will taken into account in #getVariable(). So at this point,
				 * we do not include it in the chain.
				 */
				inoutLongestVarRefChain.addAll(longestVarRefChainInCurrentVarExpression);
			}

		}

		final BaseVariableReference<?> var = newVariableReference(varId, varExpr, longestVarRefChainInCurrentVarExpression);
		return idToVariableMap.put(varId, var);
	}

	/** {@inheritDoc} */
	@Override
	public VariableReference<?> removeVariable(final String varId)
	{
		return idToVariableMap.remove(varId);
	}

	/**
	 * Resolves a VariableReference to the corresponding VariableReference(Definition) and validates the depth of VariableReference, i.e. the length of VariableReference chain. A chain of variable
	 * references is a list of VariableIds, such that V1 -> V2 ->... -> Vn, where 'V1 -> V2' means: V1's Expression contains a VariableReference to V2.
	 * 
	 * @param jaxbVarRef
	 *            the JAXB/XACML VariableReference with merely identifying a VariableDefinition by its VariableId
	 * 
	 * @return VariableReference containing the resolved VariableDefinition's expression referenced by <code>jaxbVarRef</code> as known by this factory, or null if unknown
	 * @param inoutLongestVarRefChain
	 *            If we are resolving a VariableReference in a VariableDefinition's expression (may be null if not), this is the longest chain of VariableReferences starting from a one in this
	 *            VariableDefinition. If we are not resolving a VariableReference in a VariableDefinition's expression, this may be null.This is used to detect exceeding reference depth (see
	 *            {@link #ExpressionFactoryImpl(int)} for the limit. In a Expression such as an Apply, we can have multiple VariableReferences referencing different VariableDefinitions. So we can have
	 *            different depths of VariableReference references. We compare the length of the current longest chain with the one we would get by adding the longest one in the referenced
	 *            VariableDefinition and <code>jaxbVarRef</code>'s VariableId. If the latter is longer, its content becomes the content <code>longestVarRefChain</code>.
	 * @throws UnknownIdentifierException
	 *             if VariableReference's VariableId is unknown by this factory
	 */
	private BaseVariableReference<?> getVariable(final VariableReferenceType jaxbVarRef, final Deque<String> inoutLongestVarRefChain) throws IllegalArgumentException
	{
		final String varId = jaxbVarRef.getVariableId();
		final BaseVariableReference<?> var = idToVariableMap.get(varId);
		if (var == null)
		{
			throw new IllegalArgumentException("VariableReference's VariableId=" + varId + " unknown in the current context, i.e. does not match any prior VariableDefinition's VariableId");
		}

		/*
		 * inoutLongestVarRefChain == null means that the longest VarRef chain will not be computed nor checked
		 */
		if (inoutLongestVarRefChain != null)
		{
			/*
			 * Check whether the chain formed by [the Variable's Expression's longest VariableReference chain, and the VariableReference that we currently resolving in this method] is not too big
			 */
			final Deque<String> referencedVarLongestRefChain = var.getLongestVariableReferenceChain();
			/*
			 * Make sure longestVarRefChain is set to the biggest one
			 */
			if (referencedVarLongestRefChain != null && referencedVarLongestRefChain.size() + 1 > inoutLongestVarRefChain.size())
			{
				// current longest is no longer the longest, so replace with new
				// longest's content
				inoutLongestVarRefChain.clear();
				inoutLongestVarRefChain.add(varId);
				inoutLongestVarRefChain.addAll(referencedVarLongestRefChain);
			}

			if (maxVariableReferenceDepth != UNLIMITED_MAX_VARIABLE_REF_DEPTH && inoutLongestVarRefChain.size() > this.maxVariableReferenceDepth)
			{
				throw new IllegalArgumentException("Max allowed VariableReference depth (" + this.maxVariableReferenceDepth + ") exceeded by length (" + inoutLongestVarRefChain.size()
						+ ") of VariableReference Reference chain: " + inoutLongestVarRefChain);
			}
		}

		return var;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a function instance using the function registry passed as parameter to
	 * {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, List, int, boolean, boolean, EnvironmentProperties)} .
	 */
	@Override
	public FunctionExpression getFunction(final String functionId)
	{
		final Function<?> f = this.functionRegistry.getFunction(functionId);
		if (f == null)
		{
			return null;
		}

		return new FunctionExpression(f);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a function instance using the function registry passed as parameter to
	 * {@link #ExpressionFactoryImpl(DatatypeFactoryRegistry, FunctionRegistry, List, int, boolean, boolean, EnvironmentProperties)} .
	 */
	@Override
	public FunctionExpression getFunction(final String functionId, final Datatype<?> subFunctionReturnType) throws IllegalArgumentException
	{
		if (subFunctionReturnType == null)
		{
			return getFunction(functionId);
		}

		final DatatypeFactory<?> subFuncReturnTypeFactory = this.datatypeFactoryRegistry.getExtension(subFunctionReturnType.getId());
		if (subFuncReturnTypeFactory == null)
		{
			throw new IllegalArgumentException("Invalid sub-function's return type specified: unknown/unsupported ID: " + subFunctionReturnType.getId());
		}

		final Function<?> f = this.functionRegistry.getFunction(functionId, subFuncReturnTypeFactory);
		if (f == null)
		{
			return null;
		}

		return new FunctionExpression(f);
	}

	/** {@inheritDoc} */
	@Override
	public Expression<?> getInstance(final ExpressionType expr, final XPathCompiler xPathCompiler, final Deque<String> longestVarRefChain) throws IllegalArgumentException
	{
		final Expression<?> expression;
		/*
		 * We check all types of Expression: <Apply>, <AttributeSelector>, <AttributeValue>, <Function>, <VariableReference> and <AttributeDesignator>
		 */
		if (expr instanceof ApplyType)
		{
			expression = ApplyExpression.getInstance((ApplyType) expr, xPathCompiler, this, longestVarRefChain);
		}
		else if (expr instanceof AttributeDesignatorType)
		{
			if (this.attributeProvider == null)
			{
				throw UNSUPPORTED_ATTRIBUTE_DESIGNATOR_OR_SELECTOR_BECAUSE_OF_NULL_ATTRIBUTE_PROVIDER_EXCEPTION;
			}

			final AttributeDesignatorType jaxbAttrDes = (AttributeDesignatorType) expr;
			if (this.issuerRequiredOnAttributeDesignators && jaxbAttrDes.getIssuer() == null)
			{
				throw MISSING_ATTRIBUTE_DESIGNATOR_ISSUER_EXCEPTION;
			}

			final DatatypeFactory<?> attrFactory = datatypeFactoryRegistry.getExtension(jaxbAttrDes.getDataType());
			if (attrFactory == null)
			{
				throw new IllegalArgumentException("Unsupported Datatype used in AttributeDesignator: " + jaxbAttrDes.getDataType());
			}

			expression = new AttributeDesignatorExpression<>(jaxbAttrDes, attrFactory.getBagDatatype(), attributeProvider);
		}
		else if (expr instanceof AttributeSelectorType)
		{
			if (!allowAttributeSelectors)
			{
				throw UNSUPPORTED_ATTRIBUTE_SELECTOR_EXCEPTION;
			}

			if (this.attributeProvider == null)
			{
				throw UNSUPPORTED_ATTRIBUTE_DESIGNATOR_OR_SELECTOR_BECAUSE_OF_NULL_ATTRIBUTE_PROVIDER_EXCEPTION;
			}

			final AttributeSelectorType jaxbAttrSelector = (AttributeSelectorType) expr;
			final DatatypeFactory<?> attrFactory = datatypeFactoryRegistry.getExtension(jaxbAttrSelector.getDataType());
			if (attrFactory == null)
			{
				throw new IllegalArgumentException("Unsupported Datatype used in AttributeSelector: " + jaxbAttrSelector.getDataType());
			}

			// Check whether default XPath compiler/version specified for the
			// XPath evaluator
			if (xPathCompiler == null)
			{
				throw new IllegalArgumentException("AttributeSelector found but missing Policy(Set)Defaults/XPathVersion required for XPath evaluation in AttributeSelector");
			}

			try
			{
				expression = new AttributeSelectorExpression<>(jaxbAttrSelector, xPathCompiler, attributeProvider, attrFactory);
			}
			catch (final XPathExpressionException e)
			{
				throw new IllegalArgumentException("Invalid AttributeSelector's Path='" + jaxbAttrSelector.getPath() + "' into a XPath expression", e);
			}
		}
		else if (expr instanceof AttributeValueType)
		{
			expression = getInstance((AttributeValueType) expr, xPathCompiler);
		}
		else if (expr instanceof FunctionType)
		{
			final FunctionType jaxbFunc = (FunctionType) expr;
			final FunctionExpression funcExp = getFunction(jaxbFunc.getFunctionId());
			if (funcExp != null)
			{
				expression = funcExp;
			}
			else
			{
				throw new IllegalArgumentException("Function " + jaxbFunc.getFunctionId()
						+ " is not supported (at least) as standalone Expression: either a generic higher-order function supported only as Apply FunctionId, or function completely unknown.");
			}
		}
		else if (expr instanceof VariableReferenceType)
		{
			final VariableReferenceType varRefElt = (VariableReferenceType) expr;
			expression = getVariable(varRefElt, longestVarRefChain);
		}
		else
		{
			throw new IllegalArgumentException("Expressions of type " + expr.getClass().getSimpleName()
					+ " are not supported. Expected: one of Apply, AttributeDesignator, AttributeSelector, AttributeValue, Function or VariableReference.");
		}

		return expression;
	}

	/** {@inheritDoc} */
	@Override
	public ConstantExpression<? extends AttributeValue> getInstance(final AttributeValueType jaxbAttrVal, final XPathCompiler xPathCompiler) throws IllegalArgumentException
	{
		return this.datatypeFactoryRegistry.newExpression(jaxbAttrVal, xPathCompiler);
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException
	{
		if (attributeProvider != null)
		{
			attributeProvider.close();
		}
	}

}
