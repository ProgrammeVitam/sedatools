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
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Generic table item.
 * Provides some basic string functions
 *
 * @author Richard Johnson
 */
class PSTTableItem {

    public static final int VALUE_TYPE_PT_UNICODE = 0x1f;
    public static final int VALUE_TYPE_PT_STRING8 = 0x1e;
    public static final int VALUE_TYPE_PT_BIN = 0x102;

    public int itemIndex = 0;
    public int entryType = 0;
    public int entryValueType = 0;
    public int entryValueReference = 0;
    public byte[] data = new byte[0];
    public boolean isExternalValueReference = false;

    public long getLongValue() {
        if (this.data.length > 0) {
            return PSTObject.convertLittleEndianBytesToLong(this.data);
        }
        return -1;
    }

    /**
     * Gets a string value of the data for a given codepage (charset name)
     *
     * @param codepage the codepage
     * @return the string value
     */
    public String getStringValue(String codepage) {
        return this.getStringValue(this.entryValueType, codepage);
    }

    /**
     * Gets a string value of the data
     *
     * @param stringType the string type
     * @param codepage   the codepage
     * @return string value
     */
    public String getStringValue(final int stringType, String codepage) {
        if (stringType == VALUE_TYPE_PT_UNICODE) {
            // we are a nice little-endian unicode string.
            try {
                if (this.isExternalValueReference) {
                    return "External string reference!";
                }
                return new String(this.data, "UTF-16LE").trim();
            } catch (final UnsupportedEncodingException e) {
                if (PSTFile.isPrintErrors()) System.err.println("Error decoding string: " + this.data.toString());
                return "";
            }
        }

        if (stringType == VALUE_TYPE_PT_STRING8) {
            // System.out.println("Warning! decoding string8 without charset:
            // "+this.entryType + " - "+ Integer.toHexString(this.entryType));
            return new String(this.data, Charset.forName(codepage)).trim();
        }

        final StringBuffer outputBuffer = new StringBuffer();
        /*
         * if ( stringType == VALUE_TYPE_PT_BIN) {
         * int theChar;
         * for (int x = 0; x < data.length; x++) {
         * theChar = data[x] & 0xFF;
         * outputBuffer.append((char)theChar);
         * }
         * }
         * else
         * /
         **/
        {
            // we are not a normal string, give a hexish sort of output
            final StringBuffer hexOut = new StringBuffer();
            for (final byte element : this.data) {
                final int valueChar = element & 0xff;
                if (Character.isLetterOrDigit((char) valueChar)) {
                    outputBuffer.append((char) valueChar);
                    outputBuffer.append(" ");
                } else {
                    outputBuffer.append(". ");
                }
                final String hexValue = Long.toHexString(valueChar);
                hexOut.append(hexValue);
                hexOut.append(" ");
                if (hexValue.length() > 1) {
                    outputBuffer.append(" ");
                }
            }
            outputBuffer.append("\n");
            outputBuffer.append("	");
            outputBuffer.append(hexOut);
        }

        return new String(outputBuffer);
    }

    @Override
    public String toString() {
        final String ret = PSTFile.getPropertyDescription(this.entryType, this.entryValueType);

        if (this.entryValueType == 0x000B) {
            return ret + (this.entryValueReference == 0 ? "false" : "true");
        }

        if (this.isExternalValueReference) {
            // Either a true external reference, or entryValueReference contains
            // the actual data
            return ret + String.format("0x%08X (%d)", this.entryValueReference, this.entryValueReference);
        }

        if (this.entryValueType == 0x0005 || this.entryValueType == 0x0014) {
            // 64bit data
            if (this.data == null) {
                return ret + "no data";
            }
            if (this.data.length == 8) {
                final long l = PSTObject.convertLittleEndianBytesToLong(this.data, 0, 8);
                return String.format("%s0x%016X (%d)", ret, l, l);
            } else {
                return String.format("%s invalid data length: %d", ret, this.data.length);
            }
        }

        if (this.entryValueType == 0x0040) {
            // It's a date...
            final int high = (int) PSTObject.convertLittleEndianBytesToLong(this.data, 4, 8);
            final int low = (int) PSTObject.convertLittleEndianBytesToLong(this.data, 0, 4);

            final Date d = PSTObject.filetimeToDate(high, low);
            this.dateFormatter.setTimeZone(utcTimeZone);
            return ret + this.dateFormatter.format(d);
        }

        if (this.entryValueType == 0x001F) {
            // Unicode string
            String s;
            try {
                s = new String(this.data, "UTF-16LE");
            } catch (final UnsupportedEncodingException e) {
                if (PSTFile.isPrintErrors()) System.err.println("Error decoding string: " + this.data.toString());
                s = "";
            }

            if (s.length() >= 2 && s.charAt(0) == 0x0001) {
                return String.format(
                    "%s [%04X][%04X]%s",
                    ret,
                    (short) s.charAt(0),
                    (short) s.charAt(1),
                    s.substring(2)
                );
            }

            return ret + s;
        }

        return ret + this.getStringValue("UTF-8");
    }

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    private static SimpleTimeZone utcTimeZone = new SimpleTimeZone(0, "UTC");
}
