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

import org.springframework.stereotype.Component;

/**
 * A check-digit validator based on the Damm algorithm (H. M. Damm, 2004). Like Verhoeff it detects
 * all single-digit errors and all adjacent transpositions, but it uses a single totally
 * anti-symmetric quasigroup operation table (no permutation tables or per-position weights) and
 * places no restriction on the length of the identifier. The check digit is the result of walking
 * the operation table over the digits starting from 0; an identifier is valid when the same walk
 * over the digits plus the check digit ends back at 0.
 * <p>
 * See: H. Michael Damm, "Total anti-symmetrische Quasigruppen" (2004), and
 * https://en.wikipedia.org/wiki/Damm_algorithm
 */
@Component
public class DammIdentifierValidator extends BaseHyphenatedIdentifierValidator {

	private static final String ALLOWED_CHARS = "0123456789";

	private static final String DAMM_NAME = "Damm Check Digit Validator.";

	/**
	 * Standard Damm operation table: a totally anti-symmetric quasigroup of order 10. The main diagonal
	 * is all zeros, which is what makes the trailing check digit come out to 0 for a correct
	 * identifier.
	 */
	private static final int[][] OPERATION_TABLE = { { 0, 3, 1, 7, 5, 9, 8, 6, 4, 2 }, { 7, 0, 9, 2, 1, 5, 4, 8, 6, 3 },
	        { 4, 2, 0, 6, 8, 7, 1, 3, 5, 9 }, { 1, 7, 5, 0, 9, 8, 3, 4, 2, 6 }, { 6, 1, 2, 3, 0, 4, 5, 9, 7, 8 },
	        { 3, 6, 7, 4, 2, 0, 9, 5, 8, 1 }, { 5, 8, 6, 9, 7, 2, 0, 1, 3, 4 }, { 8, 9, 4, 5, 3, 6, 2, 0, 1, 7 },
	        { 9, 4, 3, 8, 6, 1, 7, 2, 0, 5 }, { 2, 5, 8, 1, 4, 3, 6, 7, 9, 0 } };

	/**
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#getAllowedCharacters()
	 */
	@Override
	public String getAllowedCharacters() {
		return ALLOWED_CHARS;
	}

	/**
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#getName()
	 */
	@Override
	public String getName() {
		return DAMM_NAME;
	}

	/**
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#getCheckDigit(java.lang.String)
	 */
	@Override
	protected int getCheckDigit(String undecoratedIdentifier) {
		// The base class has already verified that the identifier contains only allowed
		// characters (the digits 0-9), so charAt(i) - '0' is always in range here.
		int interim = 0;
		for (int i = 0; i < undecoratedIdentifier.length(); i++) {
			int digit = undecoratedIdentifier.charAt(i) - '0';
			interim = OPERATION_TABLE[interim][digit];
		}
		return interim;
	}
}
