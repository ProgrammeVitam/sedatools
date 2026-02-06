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
package fr.gouv.vitam.tools.sedalib.inout;

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.CSVMetadataToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.ManagementMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

/**
 * The Class SIPBuilder.
 * <p>
 * Class for easily constructing SIP from any java application.
 * <p>
 * For code samples, consult <b>sedalib-samples</b> module
 */
public class SIPBuilder implements AutoCloseable {

    /**
     * The progress logger..
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * The archive transfer.
     */
    private ArchiveTransfer archiveTransfer;

    /**
     * The management metadata.
     */
    private ManagementMetadata managementMetadata;

    /**
     * The sip path string.
     */
    private String sipPathString;

    /**
     * Instantiates a new SIP builder.
     *
     * @param sipPathString         the sip path string
     * @param sedaLibProgressLogger the progress logger
     */
    public SIPBuilder(String sipPathString, SEDALibProgressLogger sedaLibProgressLogger) {
        GlobalMetadata gm = new GlobalMetadata();
        ManagementMetadata mm = new ManagementMetadata();

        this.sipPathString = sipPathString;
        this.archiveTransfer = new ArchiveTransfer();
        gm.comment = "SIPBuilder generated";
        gm.messageIdentifier = "SIP " + java.util.UUID.randomUUID().toString();
        gm.setNowFlag(true);
        gm.archivalAgreement = null;
        gm.transferRequestReplyIdentifier = null;
        gm.archivalAgencyIdentifier = null;
        gm.archivalAgencyOrganizationDescriptiveMetadataXmlData = null;
        gm.transferringAgencyIdentifier = null;
        gm.transferringAgencyOrganizationDescriptiveMetadataXmlData = null;
        gm.codeListVersionsXmlData = "  <CodeListVersions>\n" +
        "    <ReplyCodeListVersion>ReplyCodeListVersion</ReplyCodeListVersion>\n" +
        "    <MessageDigestAlgorithmCodeListVersion>MessageDigestAlgorithmCodeListVersion</MessageDigestAlgorithmCodeListVersion>\n" +
        "    <MimeTypeCodeListVersion>MimeTypeCodeListVersion</MimeTypeCodeListVersion>\n" +
        "    <EncodingCodeListVersion>EncodingCodeListVersion</EncodingCodeListVersion>\n" +
        "    <FileFormatCodeListVersion>FileFormatCodeListVersion</FileFormatCodeListVersion>\n" +
        "    <CompressionAlgorithmCodeListVersion>CompressionAlgorithmCodeListVersion</CompressionAlgorithmCodeListVersion>\n" +
        "    <DataObjectVersionCodeListVersion>DataObjectVersionCodeListVersion</DataObjectVersionCodeListVersion>\n" +
        "    <StorageRuleCodeListVersion>StorageRuleCodeListVersion</StorageRuleCodeListVersion>\n" +
        "    <AppraisalRuleCodeListVersion>AppraisalRuleCodeListVersion</AppraisalRuleCodeListVersion>\n" +
        "    <AccessRuleCodeListVersion>AccessRuleCodeListVersion</AccessRuleCodeListVersion>\n" +
        "    <DisseminationRuleCodeListVersion>DisseminationRuleCodeListVersion</DisseminationRuleCodeListVersion>\n" +
        "    <ReuseRuleCodeListVersion>ReuseRuleCodeListVersion</ReuseRuleCodeListVersion>\n" +
        "    <ClassificationRuleCodeListVersion>ClassificationRuleCodeListVersion</ClassificationRuleCodeListVersion>\n" +
        "    <AuthorizationReasonCodeListVersion>AuthorizationReasonCodeListVersion</AuthorizationReasonCodeListVersion>\n" +
        "    <RelationshipCodeListVersion>RelationshipCodeListVersion</RelationshipCodeListVersion>\n" +
        "  </CodeListVersions>";
        this.archiveTransfer.setGlobalMetadata(gm);
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.managementMetadata = mm;
    }

    /**
     * Gets the ArchiveTransfer in construction.
     *
     * @return the ArchiveTransfer
     */
    public ArchiveTransfer getArchiveTransfer() {
        return archiveTransfer;
    }

    /**
     * Sets the comment.
     *
     * @param comment the new comment
     */
    public void setComment(String comment) {
        this.archiveTransfer.getGlobalMetadata().comment = comment;
    }

    /**
     * Sets the message identifier.
     *
     * @param messageIdentifier the new message identifier
     */
    public void setMessageIdentifier(String messageIdentifier) {
        this.archiveTransfer.getGlobalMetadata().messageIdentifier = messageIdentifier;
    }

    /**
     * Sets all the agencies.
     *
     * @param archivalAgencyIdentifier     the archival agency identifier
     * @param transferringAgencyIdentifier the transferring agency identifier
     * @param originatingAgencyIdentifier  the originating agency identifier
     * @param submissionAgencyIdentifier   the submission agency identifier
     */
    public void setAgencies(
        String archivalAgencyIdentifier,
        String transferringAgencyIdentifier,
        String originatingAgencyIdentifier,
        String submissionAgencyIdentifier
    ) {
        archiveTransfer.getGlobalMetadata().archivalAgencyIdentifier = archivalAgencyIdentifier;
        archiveTransfer.getGlobalMetadata().transferringAgencyIdentifier = transferringAgencyIdentifier;
        try {
            managementMetadata.addNewMetadata("OriginatingAgencyIdentifier", originatingAgencyIdentifier);
            managementMetadata.addNewMetadata("SubmissionAgencyIdentifier", submissionAgencyIdentifier);
        } catch (SEDALibException ignored) {
            // ignored
        }
        archiveTransfer.getDataObjectPackage().setManagementMetadataXmlData(managementMetadata.toString());
    }

    /**
     * Sets the archival agreement.
     *
     * @param archivalAgreement the new archival agreement
     */
    public void setArchivalAgreement(String archivalAgreement) {
        this.archiveTransfer.getGlobalMetadata().archivalAgreement = archivalAgreement;
    }

    /**
     * Creates a root archive unit.
     *
     * @param archiveUnitID the archive unit ID
     * @return the archive unit
     * @throws SEDALibException     when the ArchiveUnit has a defined UniqId which is already in the DataObjectPackage
     */
    public ArchiveUnit createRootArchiveUnit(String archiveUnitID) throws SEDALibException {
        ArchiveUnit au;
        au = new ArchiveUnit();
        au.setInDataObjectPackageId(archiveUnitID);
        au.setDataObjectPackage(archiveTransfer.getDataObjectPackage());
        archiveTransfer.getDataObjectPackage().addArchiveUnit(au);
        archiveTransfer.getDataObjectPackage().addRootAu(au);
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS,
            "sedalib: création d'une ArchiveUnit racine [" + archiveUnitID + "]",
            null
        );
        return au;
    }

    /**
     * Creates a root archive unit in the SIP.
     *
     * @param archiveUnitID    the archive unit ID
     * @param descriptionLevel the description level
     * @param title            the title
     * @param description      the description
     * @return the archive unit
     * @throws SEDALibException     when the ArchiveUnit has a defined UniqId which is already in the SIP
     */
    public ArchiveUnit createRootArchiveUnit(
        String archiveUnitID,
        String descriptionLevel,
        String title,
        String description
    ) throws SEDALibException {
        ArchiveUnit au = createRootArchiveUnit(archiveUnitID);

        Content c = new Content();
        try {
            c.addNewMetadata("DescriptionLevel", descriptionLevel);
            c.addNewMetadata("Title", title);
            c.addNewMetadata("Description", description);
        } catch (SEDALibException ignored) {
            // ignored
        }
        au.setContent(c);

        return au;
    }

    /**
     * Creates a root archive unit in the SIP which is in fact an existing archive unit in the archiving system.
     * This archive unit is defined by its uniq systemId.
     *
     * @param archiveUnitID the archive unit ID
     * @param systemId      the system id
     * @return the archive unit
     * @throws SEDALibException     when the ArchiveUnit has a defined UniqId which is already in the SIP
     */
    public ArchiveUnit createSystemExistingRootArchiveUnit(String archiveUnitID, String systemId)
        throws SEDALibException {
        ArchiveUnit au = createRootArchiveUnit(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");

        Management m = new Management();
        m.addNewMetadata("UpdateOperation", systemId);
        au.setManagement(m);

        return au;
    }

    /**
     * Creates a root archive unit in the SIP which is in fact an existing archive unit in the archiving system.
     * This archive unit is defined by a value of a specific metadata which determines a uniq archive unit in
     * the archiving system.
     *
     * @param archiveUnitID    the archive unit ID
     * @param metadataName     the metadata name
     * @param metadataValue    the metadata value
     * @param descriptionLevel the description level
     * @param title            the title
     * @return the archive unit
     * @throws SEDALibException     when the ArchiveUnit has a defined UniqId which is already in the SIP
     */
    public ArchiveUnit createSystemExistingRootArchiveUnit(
        String archiveUnitID,
        String metadataName,
        String metadataValue,
        String descriptionLevel,
        String title
    ) throws SEDALibException {
        ArchiveUnit au = createRootArchiveUnit(archiveUnitID);

        Management m = new Management();
        m.addNewMetadata("UpdateOperation", metadataName, metadataValue);
        au.setManagement(m);

        Content c = new Content();
        c.addNewMetadata("DescriptionLevel", descriptionLevel);
        c.addNewMetadata("Title", title);
        au.setContent(c);

        return au;
    }

    /**
     * Adds a sub archive unit.
     *
     * @param archiveUnitID      the archive unit ID
     * @param childArchiveUnitID the child archive unit ID
     * @return the archive unit
     * @throws SEDALibException if no identified ArchiveUnit
     */
    public ArchiveUnit addSubArchiveUnit(String archiveUnitID, String childArchiveUnitID) throws SEDALibException {
        ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (parentAU == null) throw new SEDALibException(
            "Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]"
        );
        ArchiveUnit childAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(childArchiveUnitID);
        if (childAU == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        parentAU.addChildArchiveUnit(childAU);
        return childAU;
    }

    /**
     * Adds a sub archive unit.
     *
     * @param archiveUnitID      the archive unit ID
     * @param childArchiveUnitID the child archive unit ID
     * @param descriptionLevel   the description level
     * @param title              the title
     * @param description        the description
     * @return the archive unit
     * @throws SEDALibException     if no identified ArchiveUnit, or when there is already an ArchiveUnit with the same                          UniqID
     */
    public ArchiveUnit addNewSubArchiveUnit(
        String archiveUnitID,
        String childArchiveUnitID,
        String descriptionLevel,
        String title,
        String description
    ) throws SEDALibException {
        ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (parentAU == null) throw new SEDALibException(
            "Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]"
        );

        ArchiveUnit au;
        au = new ArchiveUnit();
        au.setInDataObjectPackageId(childArchiveUnitID);
        archiveTransfer.getDataObjectPackage().addArchiveUnit(au);
        Content c = new Content();
        c.addNewMetadata("DescriptionLevel", descriptionLevel);
        c.addNewMetadata("Title", title);
        c.addNewMetadata("Description", description);
        au.setContent(c);
        parentAU.addChildArchiveUnit(au);
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS,
            "sedalib: création d'une sous ArchiveUnit [" + childArchiveUnitID + "] de [" + archiveUnitID + "]",
            null
        );
        return au;
    }

    /**
     * Adds an archive unit containing the sub-tree of an other archive unit.
     *
     * @param archiveUnitID     the archive unit ID
     * @param fromArchiveUnitID the from archive unit ID
     * @throws SEDALibException     if no identified ArchiveUnit
     */
    public void addArchiveUnitSubTree(String archiveUnitID, String fromArchiveUnitID) throws SEDALibException {
        ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (parentAU == null) throw new SEDALibException(
            "Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]"
        );
        ArchiveUnit fromAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(fromArchiveUnitID);
        if (fromAU == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        for (ArchiveUnit au : fromAU.getChildrenAuList().getArchiveUnitList()) parentAU.addChildArchiveUnit(au);
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS,
            "sedalib: ajout d'un sous-arbre à [" + archiveUnitID + "]",
            null
        );
    }

    /**
     * Adds a sub archive unit with the given file as BinaryMaster_1.
     *
     * @param archiveUnitID      the archive unit ID
     * @param onDiskPath         the on disk path
     * @param childArchiveUnitID the child archive unit ID
     * @param descriptionLevel   the description level
     * @param title              the title
     * @param description        the description
     * @return the archive unit
     * @throws SEDALibException     if no identified ArchiveUnit, file access problem , or when there is already an                          ArchiveUnit with the same UniqID
     */
    public ArchiveUnit addFileSubArchiveUnit(
        String archiveUnitID,
        String onDiskPath,
        String childArchiveUnitID,
        String descriptionLevel,
        String title,
        String description
    ) throws SEDALibException {
        ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (parentAU == null) throw new SEDALibException(
            "Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]"
        );
        ArchiveUnit au;
        BinaryDataObject bdo;

        au = new ArchiveUnit();
        au.setInDataObjectPackageId(childArchiveUnitID);
        archiveTransfer.getDataObjectPackage().addArchiveUnit(au);
        Content c = new Content();
        c.addNewMetadata("DescriptionLevel", descriptionLevel);
        c.addNewMetadata("Title", title);
        if (description != null) c.addNewMetadata("Description", description);
        au.setContent(c);
        parentAU.addChildArchiveUnit(au);
        Path path = Paths.get(onDiskPath);
        bdo = new BinaryDataObject(
            archiveTransfer.getDataObjectPackage(),
            path,
            path.getFileName().toString(),
            "BinaryMaster_1"
        );
        bdo.extractTechnicalElements(sedaLibProgressLogger);
        au.addDataObjectById(bdo.getInDataObjectPackageId());
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS,
            "sedalib: création d'une sous ArchiveUnit [" + childArchiveUnitID + "] de [" + archiveUnitID + "]",
            null
        );
        return au;
    }

    /**
     * Adds the tree representation of a disk hierarchy to the given archive unit.
     *
     * @param archiveUnitID      the archive unit ID
     * @param onDiskPathString   the on disk path string
     * @param ignorePatterString the ignore patter string
     * @throws SEDALibException     if no identified ArchiveUnit
     */
    public void addDiskSubTree(String archiveUnitID, String onDiskPathString, String... ignorePatterString)
        throws SEDALibException {
        addDiskSubTree(archiveUnitID, onDiskPathString, false, null, ignorePatterString);
    }

    /**
     * Adds the tree representation of a disk hierarchy to the given archive unit.
     * <p>
     * It will take into account two options:
     * <ul>
     * <li>noLinkFlag: determine if the windows shortcut or windows/linux symbolic link are ignored</li>
     * <li>extractTitleFromFileNameFunction: define the function used to extract Title from file name (if null simpleCopy is used)</li>
     * </ul>
     *
     * @param archiveUnitID                    the archive unit ID
     * @param onDiskPathString                 the on disk path string
     * @param noLinkFlag                       the no link flag
     * @param extractTitleFromFileNameFunction the extract title from file name function
     * @param ignorePatterString               the ignore patter string
     * @throws SEDALibException     if no identified ArchiveUnit
     */
    public void addDiskSubTree(
        String archiveUnitID,
        String onDiskPathString,
        boolean noLinkFlag,
        Function<String, String> extractTitleFromFileNameFunction,
        String... ignorePatterString
    ) throws SEDALibException {
        ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (parentAU == null) throw new SEDALibException(
            "Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]"
        );
        DiskToDataObjectPackageImporter di = new DiskToDataObjectPackageImporter(
            onDiskPathString,
            noLinkFlag,
            extractTitleFromFileNameFunction,
            sedaLibProgressLogger
        );

        for (String ip : ignorePatterString) di.addIgnorePattern(ip);
        try {
            di.doImport();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        parentAU.getDataObjectPackage().moveContentFromDataObjectPackage(di.getDataObjectPackage(), parentAU);
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS,
            "sedalib: ajout d'un sous-arbre à [" + archiveUnitID + "]",
            null
        );
    }

    /**
     * Adds the tree representation of a set of files with hierarchy and metadata defined in a csv file to the given archive unit.
     *
     * @param archiveUnitID    the archive unit ID
     * @param encoding         the encoding
     * @param separator        the separator
     * @param onDiskPathString the on disk path string
     * @throws SEDALibException     if no identified ArchiveUnit
     */
    public void addCSVMetadataSubTree(String archiveUnitID, String encoding, char separator, String onDiskPathString)
        throws SEDALibException {
        ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (parentAU == null) throw new SEDALibException(
            "Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]"
        );
        CSVMetadataToDataObjectPackageImporter cmi = new CSVMetadataToDataObjectPackageImporter(
            onDiskPathString,
            encoding,
            separator,
            sedaLibProgressLogger
        );

        try {
            cmi.doImport();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        parentAU.getDataObjectPackage().moveContentFromDataObjectPackage(cmi.getDataObjectPackage(), parentAU);
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS,
            "sedalib: ajout d'un sous-arbre à [" + archiveUnitID + "]",
            null
        );
    }

    /**
     * Adds a file as the usage_version BinaryDataObject de l'archive unit.
     *
     * @param archiveUnitID    the ArchiveUnit id
     * @param onDiskPathString the file path string
     * @param usageVersion     the usageVersion metadata in "usage_version" format
     * @throws SEDALibException     if no identified ArchiveUnit or file acces problem
     */
    public void addFileToArchiveUnit(String archiveUnitID, String onDiskPathString, String usageVersion)
        throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");

        Path path = Paths.get(onDiskPathString);
        String filename = path.getFileName().toString();
        BinaryDataObject bdo = new BinaryDataObject(
            archiveTransfer.getDataObjectPackage(),
            path,
            filename,
            usageVersion
        );
        bdo.extractTechnicalElements(sedaLibProgressLogger);
        au.addDataObjectById(bdo.getInDataObjectPackageId());
    }

    /**
     * Gets the value of a simple descriptive metadata (String, Text or DateTime type), in archive unit Content,
     * determined only by a metadata name in String format or null if not found
     *
     * @param archiveUnitID the ArchiveUnit id
     * @param metadataName  the metadata name
     * @return the String formatted metadata value
     * @throws SEDALibException if no identified ArchiveUnit, XML read exception or inappropriate xml in rawContenteData
     */
    public String findArchiveUnitSimpleDescriptiveMetadata(String archiveUnitID, String metadataName)
        throws SEDALibException {
        Content c = getContent(archiveUnitID);
        if (c == null) return null;
        return c.getSimpleMetadata(metadataName);
    }

    /**
     * Gets an ArchiveUnit identified by a simple descriptive metadata (String, Text or DateTime type) being equal
     * to a given value. It return a compliant ArchiveUnit (if many exists it's a random one among the compliant), or null.
     *
     * @param metadataName  the metadata name
     * @param metadataValue the metadata value
     * @return the ArchiveUnit or null
     * @throws SEDALibException if no identified ArchiveUnit
     */
    public ArchiveUnit findArchiveUnitBySimpleDescriptiveMetadata(String metadataName, String metadataValue)
        throws SEDALibException {
        for (ArchiveUnit au : archiveTransfer.getDataObjectPackage().getAuInDataObjectPackageIdMap().values()) {
            String auValue = au.getContent().getSimpleMetadata(metadataName);
            if ((auValue != null) && (auValue.equals(metadataValue))) return au;
        }
        return null;
    }

    /**
     * Gets the ArchiveUnit identified by an id.
     *
     * @param archiveUnitID the ArchiveUnit id
     * @return the ArchiveUnit or null
     * @throws SEDALibException if no identified ArchiveUnit
     */
    public ArchiveUnit findArchiveUnit(String archiveUnitID) throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        return au;
    }

    /**
     * Gets the ArchiveUnit ArchiveUnitProfile metadata.
     *
     * @param archiveUnitID the archive unit ID
     * @return the content
     * @throws SEDALibException if no identified ArchiveUnit, XML read exception or inappropriate xml in                          rawArchiveUnitProfileData
     */
    public ArchiveUnitProfile getArchiveUnitProfile(String archiveUnitID) throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        ArchiveUnitProfile aup = au.getArchiveUnitProfile();
        if (aup == null) {
            aup = new ArchiveUnitProfile();
            au.setArchiveUnitProfile(aup);
        }
        return aup;
    }

    /**
     * Sets the ArchiveUnit ArchiveUnitProfile metadata.
     *
     * @param archiveUnitID      the archive unit ID
     * @param archiveUnitProfile the ArchiveUnitProfile
     * @throws SEDALibException if no identified ArchiveUnit
     */
    public void setArchiveUnitProfile(String archiveUnitID, ArchiveUnitProfile archiveUnitProfile)
        throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        au.setArchiveUnitProfile(archiveUnitProfile);
    }

    /**
     * Gets the ArchiveUnit content metadata.
     *
     * @param archiveUnitID the archive unit ID
     * @return the content
     * @throws SEDALibException if no identified ArchiveUnit, XML read exception or inappropriate xml in rawContentData
     */
    public Content getContent(String archiveUnitID) throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        Content c = au.getContent();
        if (c == null) {
            c = new Content();
            au.setContent(c);
        }
        return c;
    }

    /**
     * Sets the ArchiveUnit content metadata.
     *
     * @param archiveUnitID the archive unit ID
     * @param content       the Content
     * @throws SEDALibException the seda lib exception
     */
    public void setContent(String archiveUnitID, Content content) throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        au.setContent(content);
    }

    /**
     * Gets the ArchiveUnit management metadata.
     *
     * @param archiveUnitID the archive unit ID
     * @return the management
     * @throws SEDALibException if no identified ArchiveUnit, XML read exception or inappropriate xml in rawData
     */
    public Management getManagement(String archiveUnitID) throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        Management m = au.getManagement();
        if (m == null) {
            m = new Management();
            au.setManagement(m);
        }
        return m;
    }

    /**
     * Sets the ArchiveUnit management metadata.
     *
     * @param archiveUnitID the archive unit ID
     * @param management    the Management
     * @throws SEDALibException if no identified ArchiveUnit, XML read exception or inappropriate xml in                          rawManagementData
     */
    public void setManagement(String archiveUnitID, Management management) throws SEDALibException {
        ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById(archiveUnitID);
        if (au == null) throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
        au.setManagement(management);
    }

    /**
     * Adds the new content metadata in archive unit.
     *
     * @param archiveUnitID the archive unit ID
     * @param elementName   the metadata XML element name
     * @param args          the args used in constructor
     * @throws SEDALibException if no identified ArchiveUnit, XML read exception or inappropriate xml in rawContentData
     */
    public void addNewContentMetadataInArchiveUnit(String archiveUnitID, String elementName, Object... args)
        throws SEDALibException {
        Content c = getContent(archiveUnitID);
        c.addNewMetadata(elementName, args);
        setContent(archiveUnitID, c);
    }

    /**
     * Adds the new management metadata in archive unit.
     *
     * @param archiveUnitID the archive unit ID
     * @param elementName   the metadata XML element name
     * @param args          the args used in constructor
     * @throws SEDALibException if no identified ArchiveUnit, XML read exception or inappropriate xml in                          rawManagementData
     */
    public void addNewManagementMetadataInArchiveUnit(String archiveUnitID, String elementName, Object... args)
        throws SEDALibException {
        Management m = getManagement(archiveUnitID);
        m.addNewMetadata(elementName, args);
        setManagement(archiveUnitID, m);
    }

    /**
     * Validate SEDA schema conformity for global SEDA2Version .
     *
     * @throws SEDALibException if any validation problem occurs with a descriptive message
     */
    public void sedaSchemaValidate() throws SEDALibException {
        try {
            archiveTransfer.sedaSchemaValidate(sedaLibProgressLogger);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Validate SEDA Profile, either RNG or XSD file, conformity.
     *
     * @param profileFileName the profile file name
     * @throws SEDALibException if any validation problem occurs with a descriptive message
     */
    public void sedaProfileValidate(String profileFileName) throws SEDALibException {
        try {
            archiveTransfer.sedaProfileValidate(profileFileName, sedaLibProgressLogger);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Verify context.
     *
     * @throws SEDALibException the SEDA lib exception
     */
    private void verifyContext() throws SEDALibException {
        if (archiveTransfer.getGlobalMetadata().archivalAgencyIdentifier == null) throw new SEDALibException(
            "Element ArchivalAgencyIdentifier non défini"
        );
        if (archiveTransfer.getGlobalMetadata().transferringAgencyIdentifier == null) throw new SEDALibException(
            "Element TransferringAgencyIdentifier non défini"
        );
        if (managementMetadata.isMetadataLacking("OriginatingAgencyIdentifier")) throw new SEDALibException(
            "Element OriginatingAgencyIdentifier non défini"
        );
        if (managementMetadata.isMetadataLacking("SubmissionAgencyIdentifier")) throw new SEDALibException(
            "Element SubmissionAgencyIdentifier non défini"
        );
        if (archiveTransfer.getGlobalMetadata().archivalAgreement == null) throw new SEDALibException(
            "Element ArchivalAgreement non défini"
        );
    }

    /**
     * Generate SIP.
     *
     * @throws SEDALibException     the SEDA lib exception
     */
    public void generateSIP() throws SEDALibException {
        generateSIP(false, false);
    }

    /**
     * Generate SIP.
     *
     * @param hierarchicalArchiveUnitsFlag the hierarchical archive units flag
     * @param indentedFlag                 the indented flag
     * @throws SEDALibException     the SEDA lib exception
     */
    public void generateSIP(boolean hierarchicalArchiveUnitsFlag, boolean indentedFlag) throws SEDALibException {
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.GLOBAL,
            "sedalib: lancement de la génération du SIP",
            null
        );
        try {
            archiveTransfer.getDataObjectPackage().vitamNormalize(sedaLibProgressLogger);
            verifyContext();
            archiveTransfer.getDataObjectPackage().regenerateContinuousIds();
        } catch (SEDALibException e) {
            throw new SEDALibException("Le paquet SIP n'est pas constructible", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SEDALibException("La construction du paquet SIP est interrompue", e);
        }

        ArchiveTransferToSIPExporter sm = new ArchiveTransferToSIPExporter(archiveTransfer, sedaLibProgressLogger);
        try {
            sm.doExportToSEDASIP(sipPathString, hierarchicalArchiveUnitsFlag, indentedFlag);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        doProgressLogWithoutInterruption(
            sedaLibProgressLogger,
            SEDALibProgressLogger.GLOBAL,
            "sedalib: fichier sauvegardé (" +
            SEDALibProgressLogger.readableFileSize(new File(sipPathString).length()) +
            ")",
            null
        );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        // mandatory for closable, can be used for extension
    }
}
