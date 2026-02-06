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

/**
 * DescriptorIndexNode is a leaf item from the Descriptor index b-tree
 * It is like a pointer to an element in the PST file, everything has one...
 * 
 * @author Richard Johnson
 */
public class DescriptorIndexNode {
    public int descriptorIdentifier;
    public long dataOffsetIndexIdentifier;
    public long localDescriptorsOffsetIndexIdentifier;
    public int parentDescriptorIndexIdentifier;
    public int itemType;

    // PSTFile.PSTFileBlock dataBlock = null;

    /**
     * parse the data out into something meaningful
     *
     * @param data        the data
     * @param pstFileType the pst file type
     */
    DescriptorIndexNode(final byte[] data, final int pstFileType) {
        // parse it out
        // first 4 bytes
        if (pstFileType == PSTFile.PST_TYPE_ANSI) {
            this.descriptorIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
            this.dataOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
            this.localDescriptorsOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 8, 12);
            this.parentDescriptorIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 12, 16);
            // itemType = (int)PSTObject.convertLittleEndianBytesToLong(data,
            // 28, 32);
        } else {
            this.descriptorIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
            this.dataOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 8, 16);
            this.localDescriptorsOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 16, 24);
            this.parentDescriptorIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 24, 28);
            this.itemType = (int) PSTObject.convertLittleEndianBytesToLong(data, 28, 32);
        }
    }

    /*
     * void readData(PSTFile file)
     * throws IOException, PSTException
     * {
     * if ( dataBlock == null ) {
     * dataBlock = file.readLeaf(dataOffsetIndexIdentifier);
     * }
     * }
     *
     */

    PSTNodeInputStream getNodeInputStream(final PSTFile pstFile) throws IOException, PSTException {
        return new PSTNodeInputStream(pstFile, pstFile.getOffsetIndexNode(this.dataOffsetIndexIdentifier));
    }

    @Override
    public String toString() {

        return "DescriptorIndexNode\n" + "Descriptor Identifier: " + this.descriptorIdentifier + " (0x"
            + Long.toHexString(this.descriptorIdentifier) + ")\n" + "Data offset identifier: "
            + this.dataOffsetIndexIdentifier + " (0x" + Long.toHexString(this.dataOffsetIndexIdentifier) + ")\n"
            + "Local descriptors offset index identifier: " + this.localDescriptorsOffsetIndexIdentifier + " (0x"
            + Long.toHexString(this.localDescriptorsOffsetIndexIdentifier) + ")\n"
            + "Parent Descriptor Index Identifier: " + this.parentDescriptorIndexIdentifier + " (0x"
            + Long.toHexString(this.parentDescriptorIndexIdentifier) + ")\n" + "Item Type: " + this.itemType + " (0x"
            + Long.toHexString(this.itemType) + ")";
    }
}
