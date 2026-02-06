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
/*
 *
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Class CompactContext.
 */
public class CompactContext extends CreationContext {

    // prefs elements
    /**
     * The max size of a document metadata size.
     */
    private int maxMetadataSize;

    /**
     * The max number of documents in a document pack.
     */
    private int maxDocumentNumber;

    /**
     * The descriptive metadata filter during compact flag.
     */
    private boolean metadataFilterFlag;

    /**
     * The descriptive metadata kept in compacted content.
     */
    private List<String> keptMetadataList;

    /**
     * The data object version kept in compacted document.
     */
    private List<String> documentKeptDataObjectVersionList;

    /**
     * The data object version kept in compacted sub document.
     */
    private List<String> subDocumentKeptDataObjectVersionList;

    /**
     * Are the Document files in the pack deflated flag
     */
    private boolean deflatedFlag;

    /**
     * Instantiates a new compact context.
     */
    public CompactContext() {
        this(null, 0, 0, false, null, null, null, false);
    }

    /**
     * Instantiates a new compact context.
     *
     * @param workDir                              the work directory
     * @param maxMetadataSize                      the max metadata size
     * @param maxDocumentNumber                    the max document number
     * @param metadataFilterFlag                   the metadata filter flag
     * @param keptMetadataList                     the kept metadata list
     * @param documentKeptDataObjectVersionList    the document kept data object version list
     * @param subDocumentKeptDataObjectVersionList the sub document kept data object version list
     * @param deflatedFlag                         the deflated flag
     */
    public CompactContext(
        String workDir,
        int maxMetadataSize,
        int maxDocumentNumber,
        boolean metadataFilterFlag,
        List<String> keptMetadataList,
        List<String> documentKeptDataObjectVersionList,
        List<String> subDocumentKeptDataObjectVersionList,
        boolean deflatedFlag
    ) {
        super(null, workDir);
        this.maxMetadataSize = maxMetadataSize;
        this.maxDocumentNumber = maxDocumentNumber;
        this.metadataFilterFlag = metadataFilterFlag;
        if (keptMetadataList == null) this.keptMetadataList = new ArrayList<>();
        else this.keptMetadataList = keptMetadataList;
        if (documentKeptDataObjectVersionList == null) this.documentKeptDataObjectVersionList = new ArrayList<>();
        else this.documentKeptDataObjectVersionList = documentKeptDataObjectVersionList;
        if (subDocumentKeptDataObjectVersionList == null) this.subDocumentKeptDataObjectVersionList = new ArrayList<>();
        else this.subDocumentKeptDataObjectVersionList = subDocumentKeptDataObjectVersionList;
        this.deflatedFlag = deflatedFlag;
    }

    /**
     * Instantiates a new compact context from the preferences.
     *
     * @param preferences the prefs
     */
    public CompactContext(Preferences preferences) {
        super(preferences);
        try {
            maxMetadataSize = Integer.parseInt(
                preferences.getPrefProperties().getProperty("compactContext.general.maxMetadataSize", "0")
            );
        } catch (NumberFormatException e) {
            maxMetadataSize = 1000000;
        }
        try {
            maxDocumentNumber = Integer.parseInt(
                preferences.getPrefProperties().getProperty("compactContext.general.maxDocumentNumber", "0")
            );
        } catch (NumberFormatException e) {
            maxDocumentNumber = 1000;
        }
        metadataFilterFlag = Boolean.parseBoolean(
            preferences.getPrefProperties().getProperty("compactContext.general.metadataFilterFlag", "false")
        );
        String keptMetadataString = preferences
            .getPrefProperties()
            .getProperty("compactContext.general.keptMetadataList", "");
        if (keptMetadataString.isEmpty()) setDefaultPrefs();
        else keptMetadataList = Arrays.asList(keptMetadataString.split("\\s*\n\\s*"))
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
        String documentKeptDataObjectVersionString = preferences
            .getPrefProperties()
            .getProperty("compactContext.general.documentKeptDataObjectVersionList", "");
        if (documentKeptDataObjectVersionString.isEmpty()) setDefaultPrefs();
        else documentKeptDataObjectVersionList = Arrays.asList(documentKeptDataObjectVersionString.split("\\s*\n\\s*"))
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
        String subDocumentKeptDataObjectVersionString = preferences
            .getPrefProperties()
            .getProperty("compactContext.general.subDocumentKeptDataObjectVersionList", "");
        if (subDocumentKeptDataObjectVersionString.isEmpty()) setDefaultPrefs();
        else subDocumentKeptDataObjectVersionList = Arrays.asList(
            subDocumentKeptDataObjectVersionString.split("\\s*\n\\s*")
        )
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
        deflatedFlag = Boolean.parseBoolean(
            preferences.getPrefProperties().getProperty("compactContext.general.deflatedFlag", "false")
        );
    }

    /**
     * To prefs.
     *
     * @param preferences the prefs
     */
    @Override
    public void toPrefs(Preferences preferences) {
        preferences
            .getPrefProperties()
            .setProperty("compactContext.general.maxMetadataSize", Integer.toString(maxMetadataSize));
        preferences
            .getPrefProperties()
            .setProperty("compactContext.general.maxDocumentNumber", Integer.toString(maxDocumentNumber));
        preferences
            .getPrefProperties()
            .setProperty("compactContext.general.metadataFilterFlag", Boolean.toString(metadataFilterFlag));
        preferences
            .getPrefProperties()
            .setProperty("compactContext.general.keptMetadataList", String.join("\n", keptMetadataList));
        preferences
            .getPrefProperties()
            .setProperty(
                "compactContext.general.documentKeptDataObjectVersionList",
                String.join("\n", documentKeptDataObjectVersionList)
            );
        preferences
            .getPrefProperties()
            .setProperty(
                "compactContext.general.subDocumentKeptDataObjectVersionList",
                String.join("\n", subDocumentKeptDataObjectVersionList)
            );
        preferences
            .getPrefProperties()
            .setProperty("compactContext.general.deflatedFlag", Boolean.toString(deflatedFlag));
    }

    /**
     * Sets the default prefs.
     */
    @Override
    public void setDefaultPrefs() {
        super.setDefaultPrefs();
        this.maxMetadataSize = 1000000;
        this.maxDocumentNumber = 1000;
        this.metadataFilterFlag = false;
        String keptMetadataString =
            "DescriptionLevel\nTitle\n" +
            "FilePlanPosition\nSystemId\nOriginatingSystemId\nOriginatingSystemIdReplyTo\n" +
            "ArchivalAgencyArchiveUnitIdentifier\nOriginatingAgencyArchiveUnitIdentifier\n" +
            "TransferringAgencyArchiveUnitIdentifier\n" +
            "Description\nCustodialHistory\nType\nDocumentType\nLanguage\nDescriptionLanguage\n" +
            "Status\nVersion\nTag\nKeyword\nCoverage\nOriginatingAgency\nSubmissionAgency\n" +
            "AuthorizedAgent\nWriter\nAddressee\nRecipient\nTransmitter\nSender\nSource\nRelatedObjectReference\n" +
            "CreatedDate\nTransactedDate\nAcquiredDate\nSentDate\nReceivedDate\nRegisteredDate\nStartDate\n" +
            "EndDate\nEvent\nSignature\nGps\nTextContent:1000";
        this.keptMetadataList = Arrays.asList(keptMetadataString.split("\\s*\n\\s*"))
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
        String documentKeptDataObjectVersionString = "BinaryMaster\nTextContent";
        this.documentKeptDataObjectVersionList = Arrays.asList(documentKeptDataObjectVersionString.split("\\s*\n\\s*"))
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
        String subDocumentKeptDataObjectVersionString = "BinaryMaster";
        this.subDocumentKeptDataObjectVersionList = Arrays.asList(
            subDocumentKeptDataObjectVersionString.split("\\s*\n\\s*")
        )
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
        this.deflatedFlag = false;
    }

    /**
     * Gets max metadata size.
     *
     * @return the max metadata size
     */
    public int getMaxMetadataSize() {
        return maxMetadataSize;
    }

    /**
     * Sets max metadata size.
     *
     * @param maxMetadataSize the max metadata size
     */
    public void setMaxMetadataSize(int maxMetadataSize) {
        this.maxMetadataSize = maxMetadataSize;
    }

    /**
     * Gets max document number.
     *
     * @return the max document number
     */
    public int getMaxDocumentNumber() {
        return maxDocumentNumber;
    }

    /**
     * Sets max document number.
     *
     * @param maxDocumentNumber the max document number
     */
    public void setMaxDocumentNumber(int maxDocumentNumber) {
        this.maxDocumentNumber = maxDocumentNumber;
    }

    /**
     * Is metadata filter flag boolean.
     *
     * @return the boolean
     */
    public boolean isMetadataFilterFlag() {
        return metadataFilterFlag;
    }

    /**
     * Sets metadata filter flag.
     *
     * @param metadataFilterFlag the metadata filter flag
     */
    public void setMetadataFilterFlag(boolean metadataFilterFlag) {
        this.metadataFilterFlag = metadataFilterFlag;
    }

    /**
     * Gets kept metadata list.
     *
     * @return the kept metadata list
     */
    public List<String> getKeptMetadataList() {
        return keptMetadataList;
    }

    /**
     * Sets kept metadata list.
     *
     * @param keptMetadataList the kept metadata list
     */
    public void setKeptMetadataList(List<String> keptMetadataList) {
        this.keptMetadataList = keptMetadataList;
    }

    /**
     * Gets document kept data object version list.
     *
     * @return the document kept data object version list
     */
    public List<String> getDocumentKeptDataObjectVersionList() {
        return documentKeptDataObjectVersionList;
    }

    /**
     * Sets document kept data object version list.
     *
     * @param documentKeptDataObjectVersionList the document kept data object version list
     */
    public void setDocumentKeptDataObjectVersionList(List<String> documentKeptDataObjectVersionList) {
        this.documentKeptDataObjectVersionList = documentKeptDataObjectVersionList;
    }

    /**
     * Gets sub document kept data object version list.
     *
     * @return the sub document kept data object version list
     */
    public List<String> getSubDocumentKeptDataObjectVersionList() {
        return subDocumentKeptDataObjectVersionList;
    }

    /**
     * Sets sub document kept data object version list.
     *
     * @param subDocumentKeptDataObjectVersionList the sub document kept data object version list
     */
    public void setSubDocumentKeptDataObjectVersionList(List<String> subDocumentKeptDataObjectVersionList) {
        this.subDocumentKeptDataObjectVersionList = subDocumentKeptDataObjectVersionList;
    }

    /**
     * Is deflated flag boolean.
     *
     * @return the boolean
     */
    public boolean isDeflatedFlag() {
        return deflatedFlag;
    }

    /**
     * Sets deflated flag.
     *
     * @param deflatedFlag the deflated flag
     */
    public void setDeflatedFlag(boolean deflatedFlag) {
        this.deflatedFlag = deflatedFlag;
    }
}
