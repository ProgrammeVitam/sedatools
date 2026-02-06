/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets.jutf7;

import java.util.Arrays;

/**
 * <p>Represent a base 64 mapping. The 64 characters used in the encoding can be specified, 
 * since modified-UTF-7 uses other characters than UTF-7 (',' instead of '/').</p>
 * 
 * <p>The exact type of the arguments and result values is adapted to the needs of the 
 * encoder and decoder, as opposed to following a strict interpretation of base 64.</p> 
 * <p>Base 64, as specified in RFC 2045, is an encoding used to encode bytes as characters. 
 * In (modified-)UTF-7 however, it is used to encode characters as bytes, using some 
 * intermediate steps:</p>
 * <ol>
 * <li>Encode all characters as a 16-bit (UTF-16) integer value</li>
 * <li>Write this as stream of bytes (most-significant first)</li> 
 * <li>Encode these bytes using (modified) base 64 encoding</li>
 * <li>Write the thus formed stream of characters as a stream of bytes, using ASCII encoding</li>
 * </ol>
 * 
 * @author Jaap Beetstra
 */
class Base64Util {
	private static final int ALPHABET_LENGTH = 64;
	private final char[] alphabet;
	private final int[] inverseAlphabet;

	/**
	 * Initializes the class with the specified encoding/decoding alphabet. 
	 * 
	 * @param alphabet
	 * @throws IllegalArgumentException if alphabet is not 64 characters long or 
	 *  contains characters which are not 7-bit ASCII
	 */
	Base64Util(final String alphabet) {
		this.alphabet = alphabet.toCharArray();
		if (alphabet.length() != ALPHABET_LENGTH)
			throw new IllegalArgumentException("alphabet has incorrect length (should be 64, not "
					+ alphabet.length() + ")");
		inverseAlphabet = new int[128];
		Arrays.fill(inverseAlphabet, -1);
		for (int i = 0; i < this.alphabet.length; i++) {
			final char ch = this.alphabet[i];
			if (ch >= 128)
				throw new IllegalArgumentException("invalid character in alphabet: " + ch);
			inverseAlphabet[ch] = i;
		}
	}

	/**
	 * Returns the integer value of the six bits represented by the specified character.
	 * 
	 * @param ch The character, as a ASCII encoded byte 
	 * @return The six bits, as an integer value, or -1 if the byte is not in the alphabet
	 */
	int getSextet(final byte ch) {
		if (ch >= 128)
			return -1;
		return inverseAlphabet[ch];
	}

	/**
	 * Tells whether the alphabet contains the specified character.
	 * 
	 * @param ch The character 
	 * @return true if the alphabet contains <code>ch</code>, false otherwise
	 */
	boolean contains(final char ch) {
		if (ch >= 128)
			return false;
		return inverseAlphabet[ch] >= 0;
	}

	/**
	 * Encodes the six bit group as a character.
	 * 
	 * @param sextet The six bit group to be encoded
	 * @return The ASCII value of the character
	 */
	byte getChar(final int sextet) {
		return (byte) alphabet[sextet];
	}
}
