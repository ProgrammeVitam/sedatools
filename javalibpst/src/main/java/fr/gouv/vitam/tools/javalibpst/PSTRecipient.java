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

// import java.util.Date;
import java.util.HashMap;

/**
 * Class containing recipient information
 *
 * @author Orin Eman
 *
 *
 */
public class PSTRecipient {

    private final HashMap<Integer, PSTTable7CItem> details;

    public static final int MAPI_TO = 1;
    public static final int MAPI_CC = 2;
    public static final int MAPI_BCC = 3;

    private PSTMessage message;

    PSTRecipient(PSTMessage message, final HashMap<Integer, PSTTable7CItem> recipientDetails) {
        this.message = message;
        this.details = recipientDetails;
    }

    public String getDisplayName() {
        return this.getString(0x3001);
    }

    public int getRecipientType() {
        return this.getInt(0x0c15);
    }

    public String getEmailAddressType() {
        return this.getString(0x3002);
    }

    public String getEmailAddress() {
        return this.getString(0x3003);
    }

    public int getRecipientFlags() {
        return this.getInt(0x5ffd);
    }

    public int getRecipientOrder() {
        return this.getInt(0x5fdf);
    }

    public String getSmtpAddress() {
        // If the recipient address type is SMTP,
        // we can simply return the recipient address.
        final String addressType = this.getEmailAddressType();
        if (addressType != null && addressType.equalsIgnoreCase("smtp")) {
            final String addr = this.getEmailAddress();
            if (addr != null && addr.length() != 0) {
                return addr;
            }
        }
        // Otherwise, we have to hope the SMTP address is
        // present as the PidTagPrimarySmtpAddress property.
        return this.getString(0x39FE);
    }

    private String getString(final int id) {
        if (this.details.containsKey(id)) {
            final PSTTable7CItem item = this.details.get(id);
            return item.getStringValue(message.getStringCodepage());
        }

        return "";
    }

    /*
     * private boolean getBoolean(int id) {
     * if ( details.containsKey(id) ) {
     * PSTTable7CItem item = details.get(id);
     * if ( item.entryValueType == 0x000B )
     * {
     * return (item.entryValueReference & 0xFF) == 0 ? false : true;
     * }
     * }
     *
     * return false;
     * }
     */
    private int getInt(final int id) {
        if (this.details.containsKey(id)) {
            final PSTTable7CItem item = this.details.get(id);
            if (item.entryValueType == 0x0003) {
                return item.entryValueReference;
            }

            if (item.entryValueType == 0x0002) {
                final short s = (short) item.entryValueReference;
                return s;
            }
        }

        return 0;
    }
    /*
     * private Date getDate(int id) {
     * long lDate = 0;
     *
     * if ( details.containsKey(id) ) {
     * PSTTable7CItem item = details.get(id);
     * if ( item.entryValueType == 0x0040 ) {
     * int high = (int)PSTObject.convertLittleEndianBytesToLong(item.data, 4,
     * 8);
     * int low = (int)PSTObject.convertLittleEndianBytesToLong(item.data, 0, 4);
     *
     * return PSTObject.filetimeToDate(high, low);
     * }
     * }
     * return new Date(lDate);
     * }
     */
}
