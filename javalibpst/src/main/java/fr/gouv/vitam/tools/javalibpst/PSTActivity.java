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
import java.util.Date;
import java.util.HashMap;

/**
 * PSTActivity represents Journal entries
 *
 * @author Richard Johnson
 */
public class PSTActivity extends PSTMessage {

    /**
     * Instantiates a new Pst activity.
     *
     * @param theFile             the the file
     * @param descriptorIndexNode the descriptor index node
     * @throws PSTException the pst exception
     * @throws IOException  the io exception
     */
    public PSTActivity(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * Instantiates a new Pst activity.
     *
     * @param theFile              the the file
     * @param folderIndexNode      the folder index node
     * @param table                the table
     * @param localDescriptorItems the local descriptor items
     */
    public PSTActivity(
        final PSTFile theFile,
        final DescriptorIndexNode folderIndexNode,
        final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems
    ) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * Type
     *
     * @return the log type
     */
    public String getLogType() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008700, PSTFile.PSETID_Log));
    }

    /**
     * Start
     *
     * @return the log start
     */
    public Date getLogStart() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008706, PSTFile.PSETID_Log));
    }

    /**
     * Duration
     *
     * @return the log duration
     */
    public int getLogDuration() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008707, PSTFile.PSETID_Log));
    }

    /**
     * End
     *
     * @return the log end
     */
    public Date getLogEnd() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008708, PSTFile.PSETID_Log));
    }

    /**
     * LogFlags
     *
     * @return the log flags
     */
    public int getLogFlags() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x0000870c, PSTFile.PSETID_Log));
    }

    /**
     * DocPrinted
     *
     * @return the boolean
     */
    public boolean isDocumentPrinted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x0000870e, PSTFile.PSETID_Log)));
    }

    /**
     * DocSaved
     *
     * @return the boolean
     */
    public boolean isDocumentSaved() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x0000870f, PSTFile.PSETID_Log)));
    }

    /**
     * DocRouted
     *
     * @return the boolean
     */
    public boolean isDocumentRouted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008710, PSTFile.PSETID_Log)));
    }

    /**
     * DocPosted
     *
     * @return the boolean
     */
    public boolean isDocumentPosted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008711, PSTFile.PSETID_Log)));
    }

    /**
     * Type Description
     *
     * @return the log type desc
     */
    public String getLogTypeDesc() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008712, PSTFile.PSETID_Log));
    }

    @Override
    public String toString() {
        return (
            "Type ASCII or Unicode string: " +
            this.getLogType() +
            "\n" +
            "Start Filetime: " +
            this.getLogStart() +
            "\n" +
            "Duration Integer 32-bit signed: " +
            this.getLogDuration() +
            "\n" +
            "End Filetime: " +
            this.getLogEnd() +
            "\n" +
            "LogFlags Integer 32-bit signed: " +
            this.getLogFlags() +
            "\n" +
            "DocPrinted Boolean: " +
            this.isDocumentPrinted() +
            "\n" +
            "DocSaved Boolean: " +
            this.isDocumentSaved() +
            "\n" +
            "DocRouted Boolean: " +
            this.isDocumentRouted() +
            "\n" +
            "DocPosted Boolean: " +
            this.isDocumentPosted() +
            "\n" +
            "TypeDescription ASCII or Unicode string: " +
            this.getLogTypeDesc()
        );
    }
}
