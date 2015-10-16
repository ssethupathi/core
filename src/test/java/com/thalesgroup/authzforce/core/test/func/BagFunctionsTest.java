/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.thalesgroup.authzforce.core.test.func;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xacml.attr.DNSNameAttributeValue;
import com.sun.xacml.attr.IPAddressAttributeValue;
import com.thalesgroup.authzforce.core.attr.AnyURIAttributeValue;
import com.thalesgroup.authzforce.core.attr.AttributeValue;
import com.thalesgroup.authzforce.core.attr.Base64BinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.BooleanAttributeValue;
import com.thalesgroup.authzforce.core.attr.DatatypeConstants;
import com.thalesgroup.authzforce.core.attr.DateAttributeValue;
import com.thalesgroup.authzforce.core.attr.DateTimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.DayTimeDurationAttributeValue;
import com.thalesgroup.authzforce.core.attr.DoubleAttributeValue;
import com.thalesgroup.authzforce.core.attr.HexBinaryAttributeValue;
import com.thalesgroup.authzforce.core.attr.IntegerAttributeValue;
import com.thalesgroup.authzforce.core.attr.RFC822NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.StringAttributeValue;
import com.thalesgroup.authzforce.core.attr.TimeAttributeValue;
import com.thalesgroup.authzforce.core.attr.X500NameAttributeValue;
import com.thalesgroup.authzforce.core.attr.YearMonthDurationAttributeValue;
import com.thalesgroup.authzforce.core.eval.Bag;
import com.thalesgroup.authzforce.core.eval.Expression;
import com.thalesgroup.authzforce.core.eval.Expression.Value;

@RunWith(Parameterized.class)
public class BagFunctionsTest extends GeneralFunctionTest
{

	public BagFunctionsTest(String functionName, List<Expression<?>> inputs, Value<?> expectedResult)
	{
		super(functionName, inputs, expectedResult);
	}

	private static final String NAME_STRING_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
	private static final String NAME_BOOLEAN_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only";
	private static final String NAME_INTEGER_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
	private static final String NAME_DOUBLE_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:double-one-and-only";
	private static final String NAME_TIME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:time-one-and-only";
	private static final String NAME_DATE_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:date-one-and-only";
	private static final String NAME_DATETIME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only";
	private static final String NAME_ANYURI_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only";
	private static final String NAME_HEXBINARY_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only";
	private static final String NAME_BASE64BINARY_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only";
	private static final String NAME_DAYTIMEDURATION_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only";
	private static final String NAME_YEARMONTHDURATION_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only";
	private static final String NAME_X500NAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only";
	private static final String NAME_RFC822NAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only";
	private static final String NAME_IPADDRESS_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only";
	private static final String NAME_DNSNAME_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only";
	private static final String NAME_STRING_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:string-bag-size";
	private static final String NAME_BOOLEAN_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size";
	private static final String NAME_INTEGER_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:integer-bag-size";
	private static final String NAME_DOUBLE_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:double-bag-size";
	private static final String NAME_TIME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:time-bag-size";
	private static final String NAME_DATE_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:date-bag-size";
	private static final String NAME_DATETIME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size";
	private static final String NAME_ANYURI_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size";
	private static final String NAME_HEXBINARY_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size";
	private static final String NAME_BASE64BINARY_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size";
	private static final String NAME_DAYTIMEDURATION_BAG_SIZE = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size";
	private static final String NAME_YEARMONTHDURATION_BAG_SIZE = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size";
	private static final String NAME_X500NAME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size";
	private static final String NAME_RFC822NAME_BAG_SIZE = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size";
	private static final String NAME_IPADDRESS_BAG_SIZE = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size";
	private static final String NAME_DNSNAME_BAG_SIZE = "urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size";
	private static final String NAME_STRING_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:string-is-in";
	private static final String NAME_BOOLEAN_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:boolean-is-in";
	private static final String NAME_INTEGER_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:integer-is-in";
	private static final String NAME_DOUBLE_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:double-is-in";
	private static final String NAME_TIME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:time-is-in";
	private static final String NAME_DATE_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:date-is-in";
	private static final String NAME_DATETIME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in";
	private static final String NAME_ANYURI_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in";
	private static final String NAME_HEXBINARY_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in";
	private static final String NAME_BASE64BINARY_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in";
	private static final String NAME_DAYTIMEDURATION_IS_IN = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in";
	private static final String NAME_YEARMONTHDURATION_IS_IN = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in";
	private static final String NAME_X500NAME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in";
	private static final String NAME_RFC822NAME_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in";
	/*
	 * The two following functions are not officially listed in conformance table of XACML 3.0 core
	 * specification. However, we consider they should be there like for the other types.
	 */
	private static final String NAME_IPADDRESS_IS_IN = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in";
	private static final String NAME_DNSNAME_IS_IN = "urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in";

	private static final String NAME_STRING_BAG = "urn:oasis:names:tc:xacml:1.0:function:string-bag";
	private static final String NAME_BOOLEAN_BAG = "urn:oasis:names:tc:xacml:1.0:function:boolean-bag";
	private static final String NAME_INTEGER_BAG = "urn:oasis:names:tc:xacml:1.0:function:integer-bag";
	private static final String NAME_DOUBLE_BAG = "urn:oasis:names:tc:xacml:1.0:function:double-bag";
	private static final String NAME_TIME_BAG = "urn:oasis:names:tc:xacml:1.0:function:time-bag";
	private static final String NAME_DATE_BAG = "urn:oasis:names:tc:xacml:1.0:function:date-bag";
	private static final String NAME_DATETIME_BAG = "urn:oasis:names:tc:xacml:1.0:function:dateTime-bag";
	private static final String NAME_ANYURI_BAG = "urn:oasis:names:tc:xacml:1.0:function:anyURI-bag";
	private static final String NAME_HEXBINARY_BAG = "urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag";
	private static final String NAME_BASE64BINARY_BAG = "urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag";
	private static final String NAME_DAYTIMEDURATION_BAG = "urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag";
	private static final String NAME_YEARMONTHDURATION_BAG = "urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag";
	private static final String NAME_X500NAME_BAG = "urn:oasis:names:tc:xacml:1.0:function:x500Name-bag";
	private static final String NAME_RFC822NAME_BAG = "urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag";
	private static final String NAME_IPADDRESS_BAG = "urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag";
	private static final String NAME_DNSNAME_BAG = "urn:oasis:names:tc:xacml:2.0:function:dnsName-bag";

	/**
	 * *-one-and-only function test parameters. For each, we test with a valid one-value bag
	 * parameter, then with an invalid parameter that is an empty bag, then an invalid parameter
	 * that is a two-value bag.
	 */
	private static <AV extends AttributeValue<AV>> Collection<Object[]> newOneAndOnlyFunctionTestParams(String oneAndOnlyFunctionId, DatatypeConstants<AV> typeParam, AV primitiveValue)
	{
		Collection<Object[]> params = new ArrayList<>();

		// case 1: empty bag {}
		// one-and-only({}) -> Indeterminate
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(typeParam.EMPTY_BAG), null });

		// one-and-only({primitiveValue}) -> primitiveValue
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(Bag.singleton(typeParam.BAG_TYPE, primitiveValue)), primitiveValue });

		// one-and-only({primitiveValue, primitiveValue}) -> Indeterminate
		params.add(new Object[] { oneAndOnlyFunctionId, Arrays.asList(Bag.getInstance(typeParam.BAG_TYPE, Collections.nCopies(2, primitiveValue))), null });

		return params;
	}

	private static final IntegerAttributeValue ZERO_AS_INT = new IntegerAttributeValue("0");
	private static final IntegerAttributeValue ONE_AS_INT = new IntegerAttributeValue("1");
	private static final IntegerAttributeValue TWO_AS_INT = new IntegerAttributeValue("2");

	/**
	 * *-bag-size function test parameters. For each, we test with an empty bag parameter, then with
	 * an one-value bag, then a two-value bag.
	 */
	private static <AV extends AttributeValue<AV>> Collection<Object[]> newBagSizeFunctionTestParams(String bagSizeFunctionId, DatatypeConstants<AV> typeParam, AV primitiveValue)
	{
		Collection<Object[]> params = new ArrayList<>();

		// bag-size({}) -> 0
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(typeParam.EMPTY_BAG), ZERO_AS_INT });

		// bag-size({primitiveValue}) -> 1
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(Bag.singleton(typeParam.BAG_TYPE, primitiveValue)), ONE_AS_INT });

		// bag-size({primitiveValue, primitiveValue}) -> 2
		params.add(new Object[] { bagSizeFunctionId, Arrays.asList(Bag.getInstance(typeParam.BAG_TYPE, Collections.nCopies(2, primitiveValue))), TWO_AS_INT });
		return params;
	}

	/**
	 * *-is-in function test parameters. We want to test the following cases: 1) the first argument
	 * is not in the empty bag as second argument, 2) the first argument is in the non-empty bag as
	 * second argument, 3) the first argument is not in the non-empty bag as second argument.
	 * <p>
	 * Parameters primitiveValue1 and primitiveValue2 MUST be different values.
	 */
	private static <AV extends AttributeValue<AV>> Collection<Object[]> newIsInFunctionTestParams(String isInFunctionId, DatatypeConstants<AV> typeParam, AV primitiveValue1, AV primitiveValue2)
	{
		Collection<Object[]> params = new ArrayList<>();

		// is-in(val, {}) -> false
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue1, typeParam.EMPTY_BAG), BooleanAttributeValue.FALSE });

		// is-in(primitiveValue2, {primitiveValue1, primitiveValue2}) -> true
		Bag<AV> twoValBag = Bag.getInstance(typeParam.BAG_TYPE, Arrays.asList(primitiveValue1, primitiveValue2));
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, twoValBag), BooleanAttributeValue.TRUE });

		// is-in(primitiveValue2, {primitiveValue1, primitiveValue1}) -> false
		Bag<AV> twoValBag2 = Bag.getInstance(typeParam.BAG_TYPE, Collections.nCopies(2, primitiveValue1));
		params.add(new Object[] { isInFunctionId, Arrays.asList(primitiveValue2, twoValBag2), BooleanAttributeValue.FALSE });
		return params;
	}

	/**
	 * *-bag function test parameters.
	 */
	private static <AV extends AttributeValue<AV>> Collection<Object[]> newBagOfFunctionTestParams(String bagOfFunctionId, DatatypeConstants<AV> typeParam, AV primitiveValue1, AV primitiveValue2)
	{
		Collection<Object[]> params = new ArrayList<>();

		// bag(primitiveValue1, primitiveValue2) -> {primitiveValue1, primitiveValue2}
		Bag<AV> twoValBag = Bag.getInstance(typeParam.BAG_TYPE, Arrays.asList(primitiveValue1, primitiveValue2));
		params.add(new Object[] { bagOfFunctionId, Arrays.asList(primitiveValue1, primitiveValue2), twoValBag });
		return params;
	}

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> params() throws Exception
	{
		Collection<Object[]> params = new ArrayList<>();

		// *-one-and-only functions
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_STRING_ONE_AND_ONLY, DatatypeConstants.STRING, new StringAttributeValue("Test")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BOOLEAN_ONE_AND_ONLY, DatatypeConstants.BOOLEAN, BooleanAttributeValue.FALSE));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_INTEGER_ONE_AND_ONLY, DatatypeConstants.INTEGER, new IntegerAttributeValue("3")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DOUBLE_ONE_AND_ONLY, DatatypeConstants.DOUBLE, new DoubleAttributeValue("3.14")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_ANYURI_ONE_AND_ONLY, DatatypeConstants.ANYURI, new AnyURIAttributeValue("http://www.example.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_HEXBINARY_ONE_AND_ONLY, DatatypeConstants.HEXBINARY, new HexBinaryAttributeValue("0FB7")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_BASE64BINARY_ONE_AND_ONLY, DatatypeConstants.BASE64BINARY, new Base64BinaryAttributeValue("RXhhbXBsZQ==")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_TIME_ONE_AND_ONLY, DatatypeConstants.TIME, new TimeAttributeValue("09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATE_ONE_AND_ONLY, DatatypeConstants.DATE, new DateAttributeValue("2002-09-24")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DATETIME_ONE_AND_ONLY, DatatypeConstants.DATETIME, new DateTimeAttributeValue("2002-09-24T09:30:15")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DAYTIMEDURATION_ONE_AND_ONLY, DatatypeConstants.DAYTIMEDURATION, new DayTimeDurationAttributeValue("P1DT2H")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_YEARMONTHDURATION_ONE_AND_ONLY, DatatypeConstants.YEARMONTHDURATION, new YearMonthDurationAttributeValue("P1Y2M")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_X500NAME_ONE_AND_ONLY, DatatypeConstants.X500NAME, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_RFC822NAME_ONE_AND_ONLY, DatatypeConstants.RFC822NAME, new RFC822NameAttributeValue("Anderson@sun.com")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_IPADDRESS_ONE_AND_ONLY, DatatypeConstants.IPADDRESS, new IPAddressAttributeValue("192.168.1.10")));
		params.addAll(newOneAndOnlyFunctionTestParams(NAME_DNSNAME_ONE_AND_ONLY, DatatypeConstants.DNSNAME, new DNSNameAttributeValue("example.com")));

		// *-bag-size functions
		params.addAll(newBagSizeFunctionTestParams(NAME_STRING_BAG_SIZE, DatatypeConstants.STRING, new StringAttributeValue("Test")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BOOLEAN_BAG_SIZE, DatatypeConstants.BOOLEAN, BooleanAttributeValue.FALSE));
		params.addAll(newBagSizeFunctionTestParams(NAME_INTEGER_BAG_SIZE, DatatypeConstants.INTEGER, new IntegerAttributeValue("1")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DOUBLE_BAG_SIZE, DatatypeConstants.DOUBLE, new DoubleAttributeValue("3.14")));
		params.addAll(newBagSizeFunctionTestParams(NAME_ANYURI_BAG_SIZE, DatatypeConstants.ANYURI, new AnyURIAttributeValue("http://www.example.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_HEXBINARY_BAG_SIZE, DatatypeConstants.HEXBINARY, new HexBinaryAttributeValue("0FB7")));
		params.addAll(newBagSizeFunctionTestParams(NAME_BASE64BINARY_BAG_SIZE, DatatypeConstants.BASE64BINARY, new Base64BinaryAttributeValue("RXhhbXBsZQ==")));
		params.addAll(newBagSizeFunctionTestParams(NAME_TIME_BAG_SIZE, DatatypeConstants.TIME, new TimeAttributeValue("09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATE_BAG_SIZE, DatatypeConstants.DATE, new DateAttributeValue("2002-09-24")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DATETIME_BAG_SIZE, DatatypeConstants.DATETIME, new DateTimeAttributeValue("2002-09-24T09:30:15")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DAYTIMEDURATION_BAG_SIZE, DatatypeConstants.DAYTIMEDURATION, new DayTimeDurationAttributeValue("P1DT2H")));
		params.addAll(newBagSizeFunctionTestParams(NAME_YEARMONTHDURATION_BAG_SIZE, DatatypeConstants.YEARMONTHDURATION, new YearMonthDurationAttributeValue("P1Y2M")));
		params.addAll(newBagSizeFunctionTestParams(NAME_X500NAME_BAG_SIZE, DatatypeConstants.X500NAME, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US")));
		params.addAll(newBagSizeFunctionTestParams(NAME_RFC822NAME_BAG_SIZE, DatatypeConstants.RFC822NAME, new RFC822NameAttributeValue("Anderson@sun.com")));
		params.addAll(newBagSizeFunctionTestParams(NAME_IPADDRESS_BAG_SIZE, DatatypeConstants.IPADDRESS, new IPAddressAttributeValue("192.168.1.10")));
		params.addAll(newBagSizeFunctionTestParams(NAME_DNSNAME_BAG_SIZE, DatatypeConstants.DNSNAME, new DNSNameAttributeValue("example.com")));

		// *-is-in functions
		params.addAll(newIsInFunctionTestParams(NAME_STRING_IS_IN, DatatypeConstants.STRING, new StringAttributeValue("Test1"), new StringAttributeValue("Test2")));
		params.addAll(newIsInFunctionTestParams(NAME_BOOLEAN_IS_IN, DatatypeConstants.BOOLEAN, BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE));
		params.addAll(newIsInFunctionTestParams(NAME_INTEGER_IS_IN, DatatypeConstants.INTEGER, new IntegerAttributeValue("1"), new IntegerAttributeValue("2")));
		params.addAll(newIsInFunctionTestParams(NAME_DOUBLE_IS_IN, DatatypeConstants.DOUBLE, new DoubleAttributeValue("-4.21"), new DoubleAttributeValue("3.14")));
		params.addAll(newIsInFunctionTestParams(NAME_ANYURI_IS_IN, DatatypeConstants.ANYURI, new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example1.com")));
		params.addAll(newIsInFunctionTestParams(NAME_HEXBINARY_IS_IN, DatatypeConstants.HEXBINARY, new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")));
		params.addAll(newIsInFunctionTestParams(NAME_BASE64BINARY_IS_IN, DatatypeConstants.BASE64BINARY, new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")));
		params.addAll(newIsInFunctionTestParams(NAME_TIME_IS_IN, DatatypeConstants.TIME, new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DATE_IS_IN, DatatypeConstants.DATE, new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")));
		params.addAll(newIsInFunctionTestParams(NAME_DATETIME_IS_IN, DatatypeConstants.DATETIME, new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")));
		params.addAll(newIsInFunctionTestParams(NAME_DAYTIMEDURATION_IS_IN, DatatypeConstants.DAYTIMEDURATION, new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("-P1DT3H")));
		params.addAll(newIsInFunctionTestParams(NAME_YEARMONTHDURATION_IS_IN, DatatypeConstants.YEARMONTHDURATION, new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("-P1Y3M")));
		params.addAll(newIsInFunctionTestParams(NAME_X500NAME_IS_IN, DatatypeConstants.X500NAME, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newIsInFunctionTestParams(NAME_RFC822NAME_IS_IN, DatatypeConstants.RFC822NAME, new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")));
		params.addAll(newIsInFunctionTestParams(NAME_IPADDRESS_IS_IN, DatatypeConstants.IPADDRESS, new IPAddressAttributeValue("192.168.1.10"), new IPAddressAttributeValue("192.168.1.11")));
		params.addAll(newIsInFunctionTestParams(NAME_DNSNAME_IS_IN, DatatypeConstants.DNSNAME, new DNSNameAttributeValue("example.com"), new DNSNameAttributeValue("example1.com")));

		// *-bag functions
		params.addAll(newBagOfFunctionTestParams(NAME_STRING_BAG, DatatypeConstants.STRING, new StringAttributeValue("Test1"), new StringAttributeValue("Test2")));
		params.addAll(newBagOfFunctionTestParams(NAME_BOOLEAN_BAG, DatatypeConstants.BOOLEAN, BooleanAttributeValue.FALSE, BooleanAttributeValue.TRUE));
		params.addAll(newBagOfFunctionTestParams(NAME_INTEGER_BAG, DatatypeConstants.INTEGER, new IntegerAttributeValue("1"), new IntegerAttributeValue("2")));
		params.addAll(newBagOfFunctionTestParams(NAME_DOUBLE_BAG, DatatypeConstants.DOUBLE, new DoubleAttributeValue("-4.21"), new DoubleAttributeValue("3.14")));
		params.addAll(newBagOfFunctionTestParams(NAME_ANYURI_BAG, DatatypeConstants.ANYURI, new AnyURIAttributeValue("http://www.example.com"), new AnyURIAttributeValue("http://www.example1.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_HEXBINARY_BAG, DatatypeConstants.HEXBINARY, new HexBinaryAttributeValue("0FB7"), new HexBinaryAttributeValue("0FB8")));
		params.addAll(newBagOfFunctionTestParams(NAME_BASE64BINARY_BAG, DatatypeConstants.BASE64BINARY, new Base64BinaryAttributeValue("RXhhbXBsZQ=="), new Base64BinaryAttributeValue("T3RoZXI=")));
		params.addAll(newBagOfFunctionTestParams(NAME_TIME_BAG, DatatypeConstants.TIME, new TimeAttributeValue("09:30:15"), new TimeAttributeValue("17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATE_BAG, DatatypeConstants.DATE, new DateAttributeValue("2002-09-24"), new DateAttributeValue("2003-10-25")));
		params.addAll(newBagOfFunctionTestParams(NAME_DATETIME_BAG, DatatypeConstants.DATETIME, new DateTimeAttributeValue("2002-09-24T09:30:15"), new DateTimeAttributeValue("2003-10-25T17:18:19")));
		params.addAll(newBagOfFunctionTestParams(NAME_DAYTIMEDURATION_BAG, DatatypeConstants.DAYTIMEDURATION, new DayTimeDurationAttributeValue("P1DT2H"), new DayTimeDurationAttributeValue("-P1DT3H")));
		params.addAll(newBagOfFunctionTestParams(NAME_YEARMONTHDURATION_BAG, DatatypeConstants.YEARMONTHDURATION, new YearMonthDurationAttributeValue("P1Y2M"), new YearMonthDurationAttributeValue("-P1Y3M")));
		params.addAll(newBagOfFunctionTestParams(NAME_X500NAME_BAG, DatatypeConstants.X500NAME, new X500NameAttributeValue("cn=John Smith, o=Medico Corp, c=US"), new X500NameAttributeValue("cn=John Smith, o=Other Corp, c=US")));
		params.addAll(newBagOfFunctionTestParams(NAME_RFC822NAME_BAG, DatatypeConstants.RFC822NAME, new RFC822NameAttributeValue("Anderson@sun.com"), new RFC822NameAttributeValue("Smith@sun.com")));
		params.addAll(newBagOfFunctionTestParams(NAME_IPADDRESS_BAG, DatatypeConstants.IPADDRESS, new IPAddressAttributeValue("192.168.1.10"), new IPAddressAttributeValue("192.168.1.11")));
		params.addAll(newBagOfFunctionTestParams(NAME_DNSNAME_BAG, DatatypeConstants.DNSNAME, new DNSNameAttributeValue("example.com"), new DNSNameAttributeValue("example1.com")));

		return params;
	}

}
