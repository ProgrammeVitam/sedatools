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

import java.io.IOException;
import java.util.UUID;

/**
 * Object that represents the message store.
 * Not much use other than to get the "name" of the PST file.
 *
 * @author Richard Johnson
 */
public class PSTMessageStore extends PSTObject {

    PSTMessageStore(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * Get the tag record key, unique to this pst
     *
     * @return the tag record key as uuid
     */
    public UUID getTagRecordKeyAsUUID() {
        // attempt to find in the table.
        final int guidEntryType = 0x0ff9;
        if (this.items.containsKey(guidEntryType)) {
            final PSTTableBCItem item = this.items.get(guidEntryType);
            final int offset = 0;
            final byte[] bytes = item.data;
            final long mostSigBits =
                (PSTObject.convertLittleEndianBytesToLong(bytes, offset, offset + 4) << 32) |
                (PSTObject.convertLittleEndianBytesToLong(bytes, offset + 4, offset + 6) << 16) |
                PSTObject.convertLittleEndianBytesToLong(bytes, offset + 6, offset + 8);
            final long leastSigBits = PSTObject.convertBigEndianBytesToLong(bytes, offset + 8, offset + 16);
            return new UUID(mostSigBits, leastSigBits);
        }
        return null;
    }

    /**
     * get the message store display name
     */
    @Override
    public String getDisplayName() {
        // attempt to find in the table.
        final int displayNameEntryType = 0x3001;
        if (this.items.containsKey(displayNameEntryType)) {
            return this.getStringItem(displayNameEntryType);
            // PSTTableBCItem item =
            // (PSTTableBCItem)this.items.get(displayNameEntryType);
            // return new String(item.getStringValue());
        }
        return "";
    }

    public String getDetails() {
        return this.items.toString();
    }

    /**
     * Is this pst file is password protected.
     *
     * @throws PSTException
     *             on corrupted pst
     * @throws IOException
     *             on bad read
     * @return - true if protected,false otherwise
     *         pstfile has the password stored against identifier 0x67FF.
     *         if there is no password the value stored is 0x00000000.
     */
    public boolean isPasswordProtected() throws PSTException, IOException {
        return (this.getLongItem(0x67FF) != 0);
    }
}
