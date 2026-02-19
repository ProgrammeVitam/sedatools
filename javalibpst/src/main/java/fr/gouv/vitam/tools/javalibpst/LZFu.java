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
package fr.gouv.vitam.tools.javalibpst;

import java.io.UnsupportedEncodingException;

/**
 * An implementation of the LZFu algorithm to decompress RTF content
 *
 * @author Richard Johnson
 */
public class LZFu {

    public static final String LZFU_HEADER =
        "{\\rtf1\\ansi\\mac\\deff0\\deftab720{\\fonttbl;}{\\f0\\fnil \\froman \\fswiss \\fmodern \\fscript \\fdecor MS Sans SerifSymbolArialTimes New RomanCourier{\\colortbl\\red0\\green0\\blue0\n\r\\par \\pard\\plain\\f0\\fs20\\b\\i\\u\\tab\\tx";

    public static String decode(final byte[] data) throws PSTException {
        @SuppressWarnings("unused")
        final int compressedSize = (int) PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
        final int uncompressedSize = (int) PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
        final int compressionSig = (int) PSTObject.convertLittleEndianBytesToLong(data, 8, 12);
        @SuppressWarnings("unused")
        final int compressedCRC = (int) PSTObject.convertLittleEndianBytesToLong(data, 12, 16);

        if (compressionSig == 0x75465a4c) {
            // we are compressed...
            final byte[] output = new byte[uncompressedSize];
            int outputPosition = 0;
            final byte[] lzBuffer = new byte[4096];
            // preload our buffer.
            try {
                final byte[] bytes = LZFU_HEADER.getBytes("US-ASCII");
                System.arraycopy(bytes, 0, lzBuffer, 0, LZFU_HEADER.length());
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int bufferPosition = LZFU_HEADER.length();
            int currentDataPosition = 16;

            // next byte is the flags,
            while (currentDataPosition < data.length - 2 && outputPosition < output.length) {
                int flags = data[currentDataPosition++] & 0xFF;
                for (int x = 0; x < 8 && outputPosition < output.length; x++) {
                    final boolean isRef = ((flags & 1) == 1);
                    flags >>= 1;
                    if (isRef) {
                        // get the starting point for the buffer and the
                        // length to read
                        final int refOffsetOrig = data[currentDataPosition++] & 0xFF;
                        final int refSizeOrig = data[currentDataPosition++] & 0xFF;
                        final int refOffset = (refOffsetOrig << 4) | (refSizeOrig >>> 4);
                        final int refSize = (refSizeOrig & 0xF) + 2;
                        // refOffset &= 0xFFF;
                        try {
                            // copy the data from the buffer
                            int index = refOffset;
                            for (int y = 0; y < refSize && outputPosition < output.length; y++) {
                                output[outputPosition++] = lzBuffer[index];
                                lzBuffer[bufferPosition] = lzBuffer[index];
                                bufferPosition++;
                                bufferPosition %= 4096;
                                ++index;
                                index %= 4096;
                            }
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // copy the byte over
                        lzBuffer[bufferPosition] = data[currentDataPosition];
                        bufferPosition++;
                        bufferPosition %= 4096;
                        output[outputPosition++] = data[currentDataPosition++];
                    }
                }
            }

            if (outputPosition != uncompressedSize) {
                throw new PSTException(
                    String.format(
                        "Error decompressing RTF! Expected %d bytes, got %d bytes\n",
                        uncompressedSize,
                        outputPosition
                    )
                );
            }
            return new String(output).trim();
        } else if (compressionSig == 0x414c454d) {
            // we are not compressed!
            // just return the rest of the contents as a string
            final byte[] output = new byte[data.length - 16];
            System.arraycopy(data, 16, output, 0, data.length - 16);
            return new String(output).trim();
        }

        return "";
    }
}
