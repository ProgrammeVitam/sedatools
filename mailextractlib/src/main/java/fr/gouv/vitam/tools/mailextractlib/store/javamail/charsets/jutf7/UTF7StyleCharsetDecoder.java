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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * <p>The CharsetDecoder used to decode both variants of the UTF-7 charset and the 
 * modified-UTF-7 charset.</p>
 * 
 * @author Jaap Beetstra
 */
class UTF7StyleCharsetDecoder extends CharsetDecoder {
	private final Base64Util base64;
	private final byte shift;
	private final byte unshift;
	private final boolean strict;
	private boolean base64mode;
	private int bitsRead;
	private int tempChar;
	private boolean justShifted;
	private boolean justUnshifted;

	UTF7StyleCharsetDecoder(UTF7StyleCharset cs, Base64Util base64, boolean strict) {
		super(cs, 0.6f, 1.0f);
		this.base64 = base64;
		this.strict = strict;
		this.shift = cs.shift();
		this.unshift = cs.unshift();
	}

	/* (non-Javadoc)
	 * @see java.nio.charset.CharsetDecoder#decodeLoop(java.nio.ByteBuffer, java.nio.CharBuffer)
	 */
	protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
		while (in.hasRemaining()) {
			byte b = in.get();
			if (base64mode) {
				if (b == unshift) {
					if (base64bitsWaiting())
						return malformed(in);
					if (justShifted) {
						if (!out.hasRemaining())
							return overflow(in);
						out.put((char) shift);
					} else
						justUnshifted = true;
					setUnshifted();
				} else {
					if (!out.hasRemaining())
						return overflow(in);
					CoderResult result = handleBase64(in, out, b);
					if (result != null)
						return result;
				}
				justShifted = false;
			} else {
				if (b == shift) {
					base64mode = true;
					if (justUnshifted && strict)
						return malformed(in);
					justShifted = true;
					continue;
				}
				if (!out.hasRemaining())
					return overflow(in);
				out.put((char) b);
				justUnshifted = false;
			}
		}
		return CoderResult.UNDERFLOW;
	}

	private CoderResult overflow(ByteBuffer in) {
		in.position(in.position() - 1);
		return CoderResult.OVERFLOW;
	}

	/**
	 * <p>Decodes a byte in <i>base 64 mode</i>. Will directly write a character to the output 
	 * buffer if completed.</p>
	 * 
	 * @param in The input buffer
	 * @param out The output buffer
	 * @param lastRead Last byte read from the input buffer
	 * @return CoderResult.malformed if a non-base 64 character was encountered in strict 
	 *   mode, null otherwise
	 */
	private CoderResult handleBase64(ByteBuffer in, CharBuffer out, byte lastRead) {
		CoderResult result = null;
		int sextet = base64.getSextet(lastRead);
		if (sextet >= 0) {
			bitsRead += 6;
			if (bitsRead < 16) {
				tempChar += sextet << (16 - bitsRead);
			} else {
				bitsRead -= 16;
				tempChar += sextet >> (bitsRead);
				out.put((char) tempChar);
				tempChar = (sextet << (16 - bitsRead)) & 0xFFFF;
			}
		} else {
			if (strict)
				return malformed(in);
			out.put((char) lastRead);
			if (base64bitsWaiting())
				result = malformed(in);
			setUnshifted();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.nio.charset.CharsetDecoder#implFlush(java.nio.CharBuffer)
	 */
	protected CoderResult implFlush(CharBuffer out) {
		if ((base64mode && strict) || base64bitsWaiting())
			return CoderResult.malformedForLength(1);
		return CoderResult.UNDERFLOW;
	}

	/* (non-Javadoc)
	 * @see java.nio.charset.CharsetDecoder#implReset()
	 */
	protected void implReset() {
		setUnshifted();
		justUnshifted = false;
	}

	/**
	 * <p>Resets the input buffer position to just before the last byte read, and returns
	 * a result indicating to skip the last byte.</p>
	 * 
	 * @param in The input buffer
	 * @return CoderResult.malformedForLength(1);
	 */
	private CoderResult malformed(ByteBuffer in) {
		in.position(in.position() - 1);
		return CoderResult.malformedForLength(1);
	}

	/**
	 * @return True if there are base64 encoded characters waiting to be written
	 */
	private boolean base64bitsWaiting() {
		return tempChar != 0 || bitsRead >= 6;
	}

	/**
	 * <p>Updates internal state to reflect the decoder is no longer in <i>base 64 
	 * mode</i></p>
	 */
	private void setUnshifted() {
		base64mode = false;
		bitsRead = 0;
		tempChar = 0;
	}
}