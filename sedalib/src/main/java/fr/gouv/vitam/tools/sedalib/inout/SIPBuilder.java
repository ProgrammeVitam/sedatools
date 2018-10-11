/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.inout;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.Content;
import fr.gouv.vitam.tools.sedalib.metadata.Management;
import fr.gouv.vitam.tools.sedalib.metadata.ManagementMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

/**
 * The Class SIPBuilder.
 * <p>
 * Class for easily constructing SIP from any java application.
 * <p>
 * This has to be improved with usage cases...
 */
public class SIPBuilder implements AutoCloseable {

	/** The archive transfer. */
	private ArchiveTransfer archiveTransfer;

	/** The management metadata. */
	private ManagementMetadata managementMetadata;

	/** The sip path string. */
	private String sipPathString;

	/** The logger. */
	private Logger logger;

	/** The progress logger. */
	private SEDALibProgressLogger progressLogger;

	/**
	 * Instantiates a new SIP builder.
	 *
	 * @param sipPathString the sip path string
	 * @param logger        the logger
	 */
	public SIPBuilder(String sipPathString, Logger logger) {
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
		gm.codeListVersionsXmlData = "  <CodeListVersions>\n"
				+ "    <ReplyCodeListVersion>ReplyCodeListVersion</ReplyCodeListVersion>\n"
				+ "    <MessageDigestAlgorithmCodeListVersion>MessageDigestAlgorithmCodeListVersion</MessageDigestAlgorithmCodeListVersion>\n"
				+ "    <MimeTypeCodeListVersion>MimeTypeCodeListVersion</MimeTypeCodeListVersion>\n"
				+ "    <EncodingCodeListVersion>EncodingCodeListVersion</EncodingCodeListVersion>\n"
				+ "    <FileFormatCodeListVersion>FileFormatCodeListVersion</FileFormatCodeListVersion>\n"
				+ "    <CompressionAlgorithmCodeListVersion>CompressionAlgorithmCodeListVersion</CompressionAlgorithmCodeListVersion>\n"
				+ "    <DataObjectVersionCodeListVersion>DataObjectVersionCodeListVersion</DataObjectVersionCodeListVersion>\n"
				+ "    <StorageRuleCodeListVersion>StorageRuleCodeListVersion</StorageRuleCodeListVersion>\n"
				+ "    <AppraisalRuleCodeListVersion>AppraisalRuleCodeListVersion</AppraisalRuleCodeListVersion>\n"
				+ "    <AccessRuleCodeListVersion>AccessRuleCodeListVersion</AccessRuleCodeListVersion>\n"
				+ "    <DisseminationRuleCodeListVersion>DisseminationRuleCodeListVersion</DisseminationRuleCodeListVersion>\n"
				+ "    <ReuseRuleCodeListVersion>ReuseRuleCodeListVersion</ReuseRuleCodeListVersion>\n"
				+ "    <ClassificationRuleCodeListVersion>ClassificationRuleCodeListVersion</ClassificationRuleCodeListVersion>\n"
				+ "    <AuthorizationReasonCodeListVersion>AuthorizationReasonCodeListVersion</AuthorizationReasonCodeListVersion>\n"
				+ "    <RelationshipCodeListVersion>RelationshipCodeListVersion</RelationshipCodeListVersion>\n"
				+ "  </CodeListVersions>";
		this.archiveTransfer.setGlobalMetadata(gm);

		managementMetadata = mm;

		this.logger = logger;
		this.progressLogger = new SEDALibProgressLogger(logger);
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
	 * @throws SEDALibException the SEDA lib exception
	 */
	public void setAgencies(String archivalAgencyIdentifier, String transferringAgencyIdentifier,
			String originatingAgencyIdentifier, String submissionAgencyIdentifier) throws SEDALibException {
		archiveTransfer.getGlobalMetadata().archivalAgencyIdentifier = archivalAgencyIdentifier;
		archiveTransfer.getGlobalMetadata().transferringAgencyIdentifier = transferringAgencyIdentifier;
		managementMetadata.addNewMetadata("OriginatingAgencyIdentifier", originatingAgencyIdentifier);
		managementMetadata.addNewMetadata("SubmissionAgencyIdentifier", submissionAgencyIdentifier);
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
	 * @throws SEDALibException the SEDA lib exception
	 */
	public ArchiveUnit createRootArchiveUnit(String archiveUnitID) throws SEDALibException {
		ArchiveUnit au;
		au = new ArchiveUnit();
		au.setInDataObjectPackageId("SIPBuilder" + archiveUnitID);
		au.setDataObjectPackage(archiveTransfer.getDataObjectPackage());
		archiveTransfer.getDataObjectPackage().addArchiveUnit(au);
		archiveTransfer.getDataObjectPackage().addRootAu(au);
		logger.fine("Creation d'une ArchiveUnit racine [" + archiveUnitID + "]");
		return au;
	}

	/**
	 * Creates an existing system root archive unit from systemId.
	 *
	 * @param archiveUnitID the archive unit ID
	 * @param systemId      the system id
	 * @return the archive unit
	 * @throws SEDALibException the SEDA lib exception
	 */
	public ArchiveUnit createSystemExistingRootArchiveUnit(String archiveUnitID, String systemId)
			throws SEDALibException {
		ArchiveUnit au = createRootArchiveUnit(archiveUnitID);

		Management m = new Management();
		m.addNewMetadata("UpdateOperation", systemId);
		au.managementXmlData = m.toString();

		return au;
	}

	/**
	 * Creates an existing system root archive unit form metadata name/value.
	 *
	 * @param archiveUnitID    the archive unit ID
	 * @param metadataName     the metadata name
	 * @param metadataValue    the metadata value
	 * @param descriptionLevel the description level
	 * @param title            the title
	 * @return the archive unit
	 * @throws SEDALibException the SEDA lib exception
	 */
	public ArchiveUnit createSystemExistingRootArchiveUnit(String archiveUnitID, String metadataName,
			String metadataValue, String descriptionLevel, String title) throws SEDALibException {
		ArchiveUnit au = createRootArchiveUnit(archiveUnitID);

		Management m = new Management();
		m.addNewMetadata("UpdateOperation", metadataName, metadataValue);
		au.managementXmlData = m.toString();
		
		Content c = new Content();
		c.addNewMetadata("DescriptionLevel", descriptionLevel);
		c.addNewMetadata("Title", title);
		au.contentXmlData = c.toString();

		return au;
	}

	/**
	 * Creates a root archive unit.
	 *
	 * @param archiveUnitID    the archive unit ID
	 * @param descriptionLevel the description level
	 * @param title            the title
	 * @param description      the description
	 * @return the archive unit
	 * @throws SEDALibException the SEDA lib exception
	 */
	public ArchiveUnit createRootArchiveUnit(String archiveUnitID, String descriptionLevel, String title,
			String description) throws SEDALibException {
		ArchiveUnit au = createRootArchiveUnit(archiveUnitID);

		Content c = new Content();
		c.addNewMetadata("DescriptionLevel", descriptionLevel);
		c.addNewMetadata("Title", title);
		c.addNewMetadata("Description", description);
		au.contentXmlData = c.toString();

		return au;
	}

	/**
	 * Adds a sub archive unit.
	 *
	 * @param archiveUnitID      the archive unit ID
	 * @param childArchiveUnitID the child archive unit ID
	 * @return the archive unit
	 */
	public ArchiveUnit addSubArchiveUnit(String archiveUnitID, String childArchiveUnitID) {
		ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		ArchiveUnit childAU = archiveTransfer.getDataObjectPackage()
				.getArchiveUnitById("SIPBuilder" + childArchiveUnitID);
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
	 * @throws SEDALibException the SEDA lib exception
	 */
	public ArchiveUnit addNewSubArchiveUnit(String archiveUnitID, String childArchiveUnitID, String descriptionLevel,
			String title, String description) throws SEDALibException {
		ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		ArchiveUnit au;
		au = new ArchiveUnit();
		au.setInDataObjectPackageId("SIPBuilder" + childArchiveUnitID);
		au.setDataObjectPackage(archiveTransfer.getDataObjectPackage());
		archiveTransfer.getDataObjectPackage().addArchiveUnit(au);
		Content c = new Content();
		c.addNewMetadata("DescriptionLevel", descriptionLevel);
		c.addNewMetadata("Title", title);
		c.addNewMetadata("Description", description);
		au.contentXmlData = c.toString();
		parentAU.addChildArchiveUnit(au);
		logger.fine("Creation d'une sous ArchiveUnit [" + childArchiveUnitID + "] de [" + archiveUnitID + "]");
		return au;
	}

	/**
	 * Adds an archive unit sub tree.
	 *
	 * @param archiveUnitID     the archive unit ID
	 * @param fromArchiveUnitID the from archive unit ID
	 */
	public void addArchiveUnitSubTree(String archiveUnitID, String fromArchiveUnitID) {
		ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		ArchiveUnit fromAU = archiveTransfer.getDataObjectPackage()
				.getArchiveUnitById("SIPBuilder" + fromArchiveUnitID);
		for (ArchiveUnit au : fromAU.getChildrenAuList().getArchiveUnitList())
			parentAU.addChildArchiveUnit(au);
	}

	/**
	 * Adds a sub archive unit with the given file as BinaryMaster.
	 *
	 * @param archiveUnitID      the archive unit ID
	 * @param onDiskPath         the on disk path
	 * @param childArchiveUnitID the child archive unit ID
	 * @param descriptionLevel   the description level
	 * @param title              the title
	 * @param description        the description
	 * @return the archive unit
	 * @throws SEDALibException the SEDA lib exception
	 */
	public ArchiveUnit addFileSubArchiveUnit(String archiveUnitID, String onDiskPath, String childArchiveUnitID,
			String descriptionLevel, String title, String description) throws SEDALibException {
		ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		ArchiveUnit au;
		BinaryDataObject bdo;

		au = new ArchiveUnit();
		au.setInDataObjectPackageId("SIPBuilder" + childArchiveUnitID);
		au.setDataObjectPackage(archiveTransfer.getDataObjectPackage());
		archiveTransfer.getDataObjectPackage().addArchiveUnit(au);
		Content c = new Content();
		c.addNewMetadata("DescriptionLevel", descriptionLevel);
		c.addNewMetadata("Title", title);
		c.addNewMetadata("Description", description);
		au.contentXmlData = c.toString();
		parentAU.addChildArchiveUnit(au);
		Path path = Paths.get(onDiskPath);
		bdo = new BinaryDataObject(archiveTransfer.getDataObjectPackage(), path, path.getFileName().toString(),
				"BinaryMaster_1");
		bdo.extractTechnicalElements(progressLogger);
		au.addDataObjectById(bdo.getInDataObjectPackageId());
		logger.fine("Creation d'une sous ArchiveUnit [" + childArchiveUnitID + "] de [" + archiveUnitID + "]");
		return au;
	}

	/**
	 * Adds the disk sub tree.
	 *
	 * @param archiveUnitID      the archive unit ID
	 * @param onDiskPathString   the on disk path string
	 * @param ignorePatterString the ignore patter string
	 * @throws SEDALibException the SEDA lib exception
	 */
	public void addDiskSubTree(String archiveUnitID, String onDiskPathString, String... ignorePatterString)
			throws SEDALibException {
		DiskToDataObjectPackageImporter di = new DiskToDataObjectPackageImporter(onDiskPathString, progressLogger);

		for (String ip : ignorePatterString)
			di.addIgnorePattern(ip);
		try {
			di.doImport();
		} catch (InterruptedException e) {
			// impossible
		}

		ArchiveUnit parentAU = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		parentAU.getDataObjectPackage().moveContentFromDataObjectPackage(di.getDataObjectPackage(), parentAU);
	}

	/**
	 * Gets the ArchiveUnit ArchiveUnitProfile metadata.
	 *
	 * @param archiveUnitID the archive unit ID
	 * @return the content
	 * @throws SEDALibException if no identified ArchiveUnit, XML read exception or                          inappropriate xml in rawData
	 */
	public ArchiveUnitProfile getArchiveUnitProfile(String archiveUnitID) throws SEDALibException {
		ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		if (au == null)
			throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
		if (au.archiveUnitProfileXmlData == null)
			return new ArchiveUnitProfile();
		else
			return (ArchiveUnitProfile) SEDAMetadata.fromString(au.archiveUnitProfileXmlData, ArchiveUnitProfile.class);
	}

	/**
	 * Sets the ArchiveUnit ArchiveUnitProfile metadata.
	 *
	 * @param archiveUnitID      the archive unit ID
	 * @param archiveUnitProfile the ArchiveUnitProfile
	 */
	public void setArchiveUnitProfile(String archiveUnitID, ArchiveUnitProfile archiveUnitProfile) {
		ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		au.archiveUnitProfileXmlData = archiveUnitProfile.toString();
	}

	/**
	 * Gets the ArchiveUnit content metadata.
	 *
	 * @param archiveUnitID the archive unit ID
	 * @return the content
	 * @throws SEDALibException if no identified ArchiveUnit, XML read exception or                          inappropriate xml in rawData
	 */
	public Content getContent(String archiveUnitID) throws SEDALibException {
		ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		if (au == null)
			throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
		if (au.contentXmlData == null)
			return new Content();
		else
			return (Content) SEDAMetadata.fromString(au.contentXmlData, Content.class);
	}

	/**
	 * Sets the ArchiveUnit content metadata.
	 *
	 * @param archiveUnitID the archive unit ID
	 * @param content       the Content
	 */
	public void setContent(String archiveUnitID, Content content) {
		ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		au.contentXmlData = content.toString();
	}

	/**
	 * Gets the ArchiveUnit management metadata.
	 *
	 * @param archiveUnitID the archive unit ID
	 * @return the management
	 * @throws SEDALibException if no identified ArchiveUnit, XML read exception or                          inappropriate xml in rawData
	 */
	public Management getManagement(String archiveUnitID) throws SEDALibException {
		ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		if (au == null)
			throw new SEDALibException("Pas d'ArchiveUnit avec l'identifiant [" + archiveUnitID + "]");
		if (au.managementXmlData == null)
			return new Management();
		else
			return (Management) SEDAMetadata.fromString(au.managementXmlData, Management.class);
	}

	/**
	 * Sets the ArchiveUnit management metadata.
	 *
	 * @param archiveUnitID the archive unit ID
	 * @param management    the Management
	 */
	public void setManagement(String archiveUnitID, Management management) {
		ArchiveUnit au = archiveTransfer.getDataObjectPackage().getArchiveUnitById("SIPBuilder" + archiveUnitID);
		au.managementXmlData = management.toString();
	}

	/**
	 * Adds the new content metadata in archive unit.
	 *
	 * @param archiveUnitID the archive unit ID
	 * @param elementName   the metadata XML element name
	 * @param args          the args used in constructor
	 * @throws SEDALibException the SEDA lib exception
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
	 * @throws SEDALibException the SEDA lib exception
	 */
	public void addNewManagementMetadataInArchiveUnit(String archiveUnitID, String elementName, Object... args)
			throws SEDALibException {
		Management m = getManagement(archiveUnitID);
		m.addNewMetadata(elementName, args);
		setManagement(archiveUnitID, m);
	}

	/**
	 * Verify context.
	 *
	 * @throws SEDALibException the SEDA lib exception
	 */
	private void verifyContext() throws SEDALibException {
		if (archiveTransfer.getGlobalMetadata().archivalAgencyIdentifier == null)
			throw new SEDALibException("Element ArchivalAgencyIdentifier non défini");
		if (archiveTransfer.getGlobalMetadata().transferringAgencyIdentifier == null)
			throw new SEDALibException("Element TransferringAgencyIdentifier non défini");
		if (managementMetadata.isMetadataLacking("OriginatingAgencyIdentifier"))
			throw new SEDALibException("Element OriginatingAgencyIdentifier non défini");
		if (managementMetadata.isMetadataLacking("SubmissionAgencyIdentifier"))
			throw new SEDALibException("Element SubmissionAgencyIdentifier non défini");
		if (archiveTransfer.getGlobalMetadata().archivalAgreement == null)
			throw new SEDALibException("Element ArchivalAgreement non défini");
	}

	/**
	 * Generate SIP.
	 *
	 * @throws SEDALibException the SEDA lib exception
	 */
	public void generateSIP() throws SEDALibException {
		generateSIP(false, false);
	}

	/**
	 * Generate SIP.
	 *
	 * @param hierarchicalArchiveUnitsFlag the hierarchical archive units flag
	 * @param indentedFlag                 the indented flag
	 * @throws SEDALibException the SEDA lib exception
	 */
	public void generateSIP(boolean hierarchicalArchiveUnitsFlag, boolean indentedFlag) throws SEDALibException {
		try {
			archiveTransfer.getDataObjectPackage().vitamNormalize();
			verifyContext();
			archiveTransfer.getDataObjectPackage().regenerateContinuousIds();
		} catch (SEDALibException e) {
			throw new SEDALibException("Le paquet SIP n'est pas constructible\n->" + e.getMessage());
		}
		ArchiveTransferToSIPExporter sm = new ArchiveTransferToSIPExporter(archiveTransfer, progressLogger);
		try {
			sm.doExportToSEDASIP(sipPathString, hierarchicalArchiveUnitsFlag, indentedFlag);
		} catch (InterruptedException e) {
			// impossible
		}
		logger.info("Fichier sauvegardé (" + SEDALibProgressLogger.readableFileSize(new File(sipPathString).length())
				+ ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
	}

}
