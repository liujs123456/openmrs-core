/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.patient.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link DammIdentifierValidator}. The identifiers below are arbitrary numeric strings;
 * the expected check digits/letters were computed independently from the Damm operation table (and
 * "572" -> 4 is the published worked example for the algorithm).
 */
public class DammIdentifierValidatorTest {

	private DammIdentifierValidator validator = new DammIdentifierValidator();

	// Note the last identifier is 11 digits long: unlike Verhoeff, Damm has no length restriction.
	private String[] allowedIdentifiers = { "572", "12345678", "87654321", "11111111", "64537218", "00000000",
	        "43881234567" };

	private char[] allowedIdentifiersCheckDigits = { 'E', 'G', 'H', 'B', 'I', 'A', 'J' };

	private char unusedCheckDigit = 'C';

	private String[] invalidIdentifiers = { "", " ", "12 34", "12a34", "abc", "!234", "12.3", " 572", "572 " };

	/**
	 * "572" is the worked example in the algorithm's documentation: its Damm check digit is 4 (rendered
	 * here as the letter E).
	 *
	 * @see DammIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getCheckDigit_shouldComputeTheKnownDammCheckDigit() {
		assertEquals("572-E", validator.getValidIdentifier("572"));
	}

	/**
	 * @see DammIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldGetValidIdentifier() {
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertEquals(allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigits[i],
			    validator.getValidIdentifier(allowedIdentifiers[i]));
		}
	}

	/**
	 * @see DammIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldFailWithInvalidIdentifiers() {
		for (String invalidIdentifier : invalidIdentifiers) {
			assertThrows(Exception.class, () -> validator.getValidIdentifier(invalidIdentifier),
			    "Identifier " + invalidIdentifier + " should have failed.");
		}
	}

	/**
	 * @see DammIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldPassWithValidSuffixes() {
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertTrue(validator.isValid(allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigits[i]));
			// the validator must accept what it itself produces
			assertTrue(validator.isValid(validator.getValidIdentifier(allowedIdentifiers[i])));
		}
	}

	/**
	 * @see DammIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldFailWithIncorrectlyCalculatedSuffixes() {
		for (String allowedIdentifier : allowedIdentifiers) {
			assertFalse(validator.isValid(allowedIdentifier + "-" + unusedCheckDigit));
		}
	}

	/**
	 * @see DammIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldFailWithInvalidIdentifiers() {
		for (String invalidIdentifier : invalidIdentifiers) {
			assertThrows(Exception.class, () -> validator.isValid(invalidIdentifier));
		}
	}

	/**
	 * The Damm algorithm detects every single-digit error, so changing any one digit must change the
	 * check digit.
	 *
	 * @see DammIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void checkDigit_shouldChangeWhenASingleDigitIsChanged() {
		for (String allowedIdentifier : allowedIdentifiers) {
			String expected = validator.getValidIdentifier(allowedIdentifier);
			for (int pos = 0; pos < allowedIdentifier.length(); pos++) {
				for (char d = '0'; d <= '9'; d++) {
					if (d != allowedIdentifier.charAt(pos)) {
						String modified = allowedIdentifier.substring(0, pos) + d + allowedIdentifier.substring(pos + 1);
						assertNotEquals(expected, validator.getValidIdentifier(modified),
						    "single-digit change " + allowedIdentifier + " -> " + modified + " was not detected");
					}
				}
			}
		}
	}

	/**
	 * The Damm algorithm detects every adjacent transposition, so swapping two neighbouring (distinct)
	 * digits must change the check digit. Mirrors the equivalent Verhoeff test.
	 *
	 * @see DammIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void checkDigit_shouldChangeWhenAdjacentCharsAreTransposed() {
		int failures = 0;
		StringBuilder failureMsg = new StringBuilder();
		for (String allowedIdentifier : allowedIdentifiers) {
			String expected = validator.getValidIdentifier(allowedIdentifier);
			for (int j = 1; j < allowedIdentifier.length(); j++) {
				char c = allowedIdentifier.charAt(j - 1);
				char d = allowedIdentifier.charAt(j);
				if (c != d) {
					String transposed = allowedIdentifier.substring(0, j - 1) + d + c + allowedIdentifier.substring(j + 1);
					if (expected.equals(validator.getValidIdentifier(transposed))) {
						failureMsg.append("Check digits for '").append(allowedIdentifier).append("' and '")
						        .append(transposed).append("' should be different.\n");
						failures++;
					}
				}
			}
		}
		assertEquals(0, failures, "transposed digits were not detected:\n" + failureMsg);
	}
}
