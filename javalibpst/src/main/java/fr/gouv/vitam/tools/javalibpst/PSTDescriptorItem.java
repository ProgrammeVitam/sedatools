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
 * The descriptor items contain information that describes a PST object.
 * This is like extended table entries, usually when the data cannot fit in a
 * traditional table item.
 *
 * This is an entry of type SLENTRY or SIENTRY
 * see [MS-PST]: Outlook Personal Folders (.pst) File Format
 *
 * @author Richard Johnson
 */
class PSTDescriptorItem {

    PSTDescriptorItem(final byte[] data, final int offset, final PSTFile pstFile, int entryType) {
        this.pstFile = pstFile;

        if (pstFile.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
            this.descriptorIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, offset, offset + 4);
            this.offsetIndexIdentifier = ((int) PSTObject.convertLittleEndianBytesToLong(
                    data,
                    offset + 4,
                    offset + 8
                )) &
            0xfffffffe;
            if (entryType == PSTFile.SLBLOCK_ENTRY) this.subNodeOffsetIndexIdentifier =
                (int) PSTObject.convertLittleEndianBytesToLong(data, offset + 8, offset + 12) & 0xfffffffe;
            else this.subNodeOffsetIndexIdentifier = 0;
        } else {
            this.descriptorIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, offset, offset + 4);
            this.offsetIndexIdentifier = ((int) PSTObject.convertLittleEndianBytesToLong(
                    data,
                    offset + 8,
                    offset + 16
                )) &
            0xfffffffe;
            if (entryType == PSTFile.SLBLOCK_ENTRY) this.subNodeOffsetIndexIdentifier =
                (int) PSTObject.convertLittleEndianBytesToLong(data, offset + 16, offset + 24) & 0xfffffffe;
            else this.subNodeOffsetIndexIdentifier = 0;
        }
    }

    public byte[] getData() throws IOException, PSTException {
        if (this.dataBlockData != null) {
            return this.dataBlockData;
        }

        final PSTNodeInputStream in = this.pstFile.readLeaf(this.offsetIndexIdentifier);
        final byte[] out = new byte[(int) in.length()];
        in.readCompletely(out);
        this.dataBlockData = out;
        return this.dataBlockData;
    }

    public int[] getBlockOffsets() throws IOException, PSTException {
        if (this.dataBlockOffsets != null) {
            return this.dataBlockOffsets;
        }
        final Long[] offsets = this.pstFile.readLeaf(this.offsetIndexIdentifier).getBlockOffsets();
        final int[] offsetsOut = new int[offsets.length];
        for (int x = 0; x < offsets.length; x++) {
            offsetsOut[x] = offsets[x].intValue();
        }
        return offsetsOut;
    }

    public int getDataSize() throws IOException, PSTException {
        return this.pstFile.getLeafSize(this.offsetIndexIdentifier);
    }

    // Public data
    int descriptorIdentifier;
    int offsetIndexIdentifier;
    int subNodeOffsetIndexIdentifier;

    // These are private to ensure that getData()/getBlockOffets() are used
    // private PSTFile.PSTFileBlock dataBlock = null;
    byte[] dataBlockData = null;
    int[] dataBlockOffsets = null;
    private final PSTFile pstFile;

    @Override
    public String toString() {
        return (
            "PSTDescriptorItem\n" +
            "   descriptorIdentifier: " +
            this.descriptorIdentifier +
            "\n" +
            "   offsetIndexIdentifier: " +
            this.offsetIndexIdentifier +
            "\n" +
            "   subNodeOffsetIndexIdentifier: " +
            this.subNodeOffsetIndexIdentifier +
            "\n"
        );
    }
}
