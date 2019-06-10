/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import static fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter.*;

/**
 * The Class ExportContext.
 */
public class ExportContext {

	// prefs elements
	/** The hierarchical archive units in SEDA manifest. */
	private boolean hierarchicalArchiveUnits;

	/** The indented xml in SEDA manifest. */
	private boolean indented;

	/** The DataObjectPackage elements reindex before export flag. */
	private boolean reindex;

	/** The csv export mode for usage_version object selection. */
	private int usageVersionSelectionMode;

	/** The csv export max name size for directories. */
	private int maxNameSize;

	/** The ManagementMetadata. */
	private String managementMetadataXmlData;

	/** The archive transfer global metadata. */
	private GlobalMetadata globalMetadata;

	/** The descriptive metadata filter flag. */
	private boolean metadataFilterFlag;

	/** The descriptive metadata kept in content. */
	private List<String> keptMetadataList;

	/** The on disk output. */
	private String onDiskOutput;

	/**
	 * Instantiates a new global metadata context.
	 */
	public ExportContext() {
		this.hierarchicalArchiveUnits = true;
		this.indented = true;
		this.reindex = false;
		this.usageVersionSelectionMode = LAST_DATAOBJECT;
		this.maxNameSize = 32;
		this.globalMetadata=new GlobalMetadata();
		this.managementMetadataXmlData=null;
		this.onDiskOutput=null;
		this.metadataFilterFlag=false;
		this.keptMetadataList=new ArrayList<String>();
	}

	/**
	 * Instantiates a new global metadata context.
	 *
	 * @param globalMetadata            the archive transfer global metadata
	 * @param hierarchicalArchiveUnits  the hierarchical archive units
	 * @param indented                  the indented
	 * @param reindex                   the reindex
	 * @param usageVersionSelectionMode the usage version selection mode
	 * @param maxNameSize               the max name size
	 * @param onDiskOutput              the on disk output
	 * @param managementMetadataXmlData the management metadata xml data
	 * @param metadataFilterFlag        the metadata filter flag
	 * @param keptMetadataList          the kept metadata list
	 */
	public ExportContext(GlobalMetadata globalMetadata,
			boolean hierarchicalArchiveUnits, boolean indented, boolean reindex, int usageVersionSelectionMode,
						 int maxNameSize, String onDiskOutput, String managementMetadataXmlData,
						 boolean metadataFilterFlag, List<String> keptMetadataList) {
		this.hierarchicalArchiveUnits = hierarchicalArchiveUnits;
		this.indented = indented;
		this.reindex = indented;
		this.usageVersionSelectionMode = usageVersionSelectionMode;
		this.maxNameSize = maxNameSize;
		this.globalMetadata=globalMetadata;
		this.managementMetadataXmlData=managementMetadataXmlData;
		this.setOnDiskOutput(onDiskOutput);
		this.metadataFilterFlag=metadataFilterFlag;
		if (keptMetadataList == null)
			this.keptMetadataList = new ArrayList<String>();
		else
			this.keptMetadataList = keptMetadataList;
	}

	/**
	 * Instantiates a new ExportContext from a file.
	 *
	 * @param sipExportFileName the sipExportFileName
	 * @throws SEDALibException the SEDALibException
	 */
	public ExportContext(String sipExportFileName) throws SEDALibException {
		ObjectMapper mapper = new ObjectMapper();

		try (FileInputStream fis = new FileInputStream(new File(sipExportFileName))) {
			JsonParser jsonParser = mapper.getFactory().createParser(fis);
			jsonParser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
			ExportContext sec=mapper.readValue(jsonParser, ExportContext.class);
			this.hierarchicalArchiveUnits = sec.hierarchicalArchiveUnits;
			this.indented = sec.indented;
			this.reindex = sec.reindex;
			this.usageVersionSelectionMode = sec.usageVersionSelectionMode;
			this.maxNameSize = sec.maxNameSize;
			this.globalMetadata=sec.globalMetadata;
			this.managementMetadataXmlData=sec.managementMetadataXmlData;
			this.metadataFilterFlag=sec.metadataFilterFlag;
			this.keptMetadataList=sec.keptMetadataList;
		} catch (IOException e) {
			throw new SEDALibException("Resip.InOut: Le fichier [" + sipExportFileName
					+ "] de contexte est illisible\n->" + e.getMessage());
		}
	}

	/**
	 * Null if empty.
	 *
	 * @param s the s
	 * @return the string
	 */
	private static String nullIfEmpty(String s) {
		if (s.isEmpty())
			return null;
		return s;
	}

	/**
	 * Instantiates a new GlobalMetadata from the preferences.
	 *
	 * @param prefs the prefs
	 */
	public ExportContext(Prefs prefs) {
		setArchiveTransferGlobalMetadata(new GlobalMetadata());
		hierarchicalArchiveUnits = Boolean.parseBoolean(prefs.getPrefProperties().getProperty("exportContext.general.hierarchicalArchiveUnits", "true"));
		indented = Boolean.parseBoolean(prefs.getPrefProperties().getProperty("exportContext.general.indented", "true"));
		reindex = Boolean.parseBoolean(prefs.getPrefProperties().getProperty("exportContext.general.reindex", "false"));
		try {
			usageVersionSelectionMode=Integer.parseInt(prefs.getPrefProperties().getProperty("exportContext.csvExport.usageVersionSelectionMode",Integer.toString(LAST_DATAOBJECT)));
		}
		catch (NumberFormatException e){
			usageVersionSelectionMode=LAST_DATAOBJECT;
		}
		if ((usageVersionSelectionMode<FIRST_DATAOBJECT) || (usageVersionSelectionMode>ALL_DATAOBJECTS)) usageVersionSelectionMode=LAST_DATAOBJECT;
		try {
			maxNameSize=Integer.parseInt(prefs.getPrefProperties().getProperty("exportContext.csvExport.maxNameSize","32"));
		}
		catch (NumberFormatException e){
			maxNameSize=32;
		}
		if (maxNameSize<0) maxNameSize=0;
		managementMetadataXmlData=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.general.managementMetadataXmlData", ""));
		metadataFilterFlag = Boolean.parseBoolean(prefs.getPrefProperties().getProperty("exportContext.general.metadataFilterFlag", "false"));
		String keptMetadataString = prefs.getPrefProperties().getProperty("exportContext.general.keptMetadataList", "");
		if (keptMetadataString.isEmpty())
			keptMetadataList = new ArrayList<String>();
		else
			keptMetadataList = Arrays.asList(keptMetadataString.split("\\s*\n\\s*"))
					.stream().map(String::trim).collect(Collectors.toList());

		getArchiveTransferGlobalMetadata().comment=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.comment", ""));
		getArchiveTransferGlobalMetadata().date=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.date", ""));
		getArchiveTransferGlobalMetadata().setNowFlag(Boolean.parseBoolean(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.nowFlag", "true")));
		getArchiveTransferGlobalMetadata().messageIdentifier=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.messageIdentifier", ""));
		getArchiveTransferGlobalMetadata().archivalAgreement=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.archivalAgreement", ""));
		getArchiveTransferGlobalMetadata()
				.codeListVersionsXmlData=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.codeListVersionsXmlData", ""));
		getArchiveTransferGlobalMetadata()
				.transferRequestReplyIdentifier=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.transferRequestReplyIdentifier", ""));
		getArchiveTransferGlobalMetadata()
				.archivalAgencyIdentifier=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.archivalAgencyIdentifier", ""));
		getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData=
				nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.archivalAgencyOrganizationDescriptiveMetadataXmlData", ""));
		getArchiveTransferGlobalMetadata()
				.transferringAgencyIdentifier=nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.transferringAgencyIdentifier", ""));
		getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData=
				nullIfEmpty(prefs.getPrefProperties().getProperty("exportContext.globalMetadata.transferringAgencyOrganizationDescriptiveMetadataXmlData", ""));
	}

	/**
	 * To prefs.
	 *
	 * @param prefs the prefs
	 */
	public void toPrefs(Prefs prefs) {
		prefs.getPrefProperties().setProperty("exportContext.general.hierarchicalArchiveUnits", Boolean.toString(hierarchicalArchiveUnits));
		prefs.getPrefProperties().setProperty("exportContext.general.indented", Boolean.toString(indented));
		prefs.getPrefProperties().setProperty("exportContext.general.reindex", Boolean.toString(reindex));
		prefs.getPrefProperties().setProperty("exportContext.csvExport.usageVersionSelectionMode", Integer.toString(usageVersionSelectionMode));
		prefs.getPrefProperties().setProperty("exportContext.csvExport.maxNameSize", Integer.toString(maxNameSize));
		prefs.getPrefProperties().setProperty("exportContext.general.managementMetadataXmlData", (managementMetadataXmlData == null ? ""
				: managementMetadataXmlData));
		prefs.getPrefProperties().setProperty("exportContext.general.metadataFilterFlag", Boolean.toString(metadataFilterFlag));
		prefs.getPrefProperties().setProperty("exportContext.general.keptMetadataList", String.join("\n", keptMetadataList));

		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.comment", (getArchiveTransferGlobalMetadata().comment == null ? ""
				: getArchiveTransferGlobalMetadata().comment));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.date", (getArchiveTransferGlobalMetadata().date == null ? ""
				: getArchiveTransferGlobalMetadata().date));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.nowFlag", Boolean.toString(getArchiveTransferGlobalMetadata().isNowFlag()));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.messageIdentifier", (getArchiveTransferGlobalMetadata().messageIdentifier == null ? ""
				: getArchiveTransferGlobalMetadata().messageIdentifier));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.archivalAgreement", (getArchiveTransferGlobalMetadata().archivalAgreement == null ? ""
				: getArchiveTransferGlobalMetadata().archivalAgreement));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.codeListVersionsXmlData",
				(getArchiveTransferGlobalMetadata().codeListVersionsXmlData == null ? ""
						: getArchiveTransferGlobalMetadata().codeListVersionsXmlData));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.transferRequestReplyIdentifier",
				(getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier == null ? ""
						: getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.archivalAgencyIdentifier",
				(getArchiveTransferGlobalMetadata().archivalAgencyIdentifier == null ? ""
						: getArchiveTransferGlobalMetadata().archivalAgencyIdentifier));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.archivalAgencyOrganizationDescriptiveMetadataXmlData",
				(getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData == null ? ""
						: getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.transferringAgencyIdentifier",
				(getArchiveTransferGlobalMetadata().transferringAgencyIdentifier == null ? ""
						: getArchiveTransferGlobalMetadata().transferringAgencyIdentifier));
		prefs.getPrefProperties().setProperty("exportContext.globalMetadata.transferringAgencyOrganizationDescriptiveMetadataXmlData",
				(getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData == null ? ""
						: getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData));
	}

	/**
	 * Sets the default prefs.
	 */
	public void setDefaultPrefs() {
		this.hierarchicalArchiveUnits = true;
		this.indented = true;
		this.reindex = false;
		this.usageVersionSelectionMode = LAST_DATAOBJECT;
		this.maxNameSize = 32;
		this.managementMetadataXmlData="    <ManagementMetadata>\n"
				+ "      <AcquisitionInformation>Acquisition Information</AcquisitionInformation>\n"
				+ "      <LegalStatus>Public Archive</LegalStatus>\n"
				+ "      <OriginatingAgencyIdentifier>Service_producteur</OriginatingAgencyIdentifier>\n"
				+ "      <SubmissionAgencyIdentifier>Service_versant</SubmissionAgencyIdentifier>\n"
				+ "    </ManagementMetadata>";
		this.metadataFilterFlag=false;
		String keptMetadataString="DescriptionLevel\nTitle\n" +
				"FilePlanPosition\nSystemId\nOriginatingSystemId\n" +
				"ArchivalAgencyArchiveUnitIdentifier\nOriginatingAgencyArchiveUnitIdentifier\n"+
				"TransferringAgencyArchiveUnitIdentifier\n"+
				"Description\nCustodialHistory\nType\nDocumentType\nLanguage\nDescriptionLanguage\n"+
				"Status\nVersion\nTag\nKeyword\nCoverage\nOriginatingAgency\nSubmissionAgency\n"+
				"AuthorizedAgent\nWriter\nAddressee\nRecipient\nTransmitter\nSender\nSource\nRelatedObjectReference\n"+
				"CreatedDate\nTransactedDate\nAcquiredDate\nSentDate\nReceivedDate\nRegisteredDate\nStartDate\n"+
				"EndDate\nEvent\nSignature\nGps";
		this.keptMetadataList = Arrays.asList(keptMetadataString.split("\\s*\n\\s*"))
					.stream().map(String::trim).collect(Collectors.toList());
		if (getArchiveTransferGlobalMetadata() == null)
			setArchiveTransferGlobalMetadata(new GlobalMetadata());
		getArchiveTransferGlobalMetadata().comment="Avec valeurs utilisables sur environnement de démo Vitam";
		getArchiveTransferGlobalMetadata().setNowFlag(true);
		getArchiveTransferGlobalMetadata().messageIdentifier="SIP SEDA de test";
		getArchiveTransferGlobalMetadata().archivalAgreement="ArchivalAgreement0";
		getArchiveTransferGlobalMetadata().codeListVersionsXmlData="  <CodeListVersions>\n"
				+ "    <ReplyCodeListVersion>ReplyCodeListVersion0</ReplyCodeListVersion>\n"
				+ "    <MessageDigestAlgorithmCodeListVersion>MessageDigestAlgorithmCodeListVersion0</MessageDigestAlgorithmCodeListVersion>\n"
				+ "    <MimeTypeCodeListVersion>MimeTypeCodeListVersion0</MimeTypeCodeListVersion>\n"
				+ "    <EncodingCodeListVersion>EncodingCodeListVersion0</EncodingCodeListVersion>\n"
				+ "    <FileFormatCodeListVersion>FileFormatCodeListVersion0</FileFormatCodeListVersion>\n"
				+ "    <CompressionAlgorithmCodeListVersion>CompressionAlgorithmCodeListVersion0</CompressionAlgorithmCodeListVersion>\n"
				+ "    <DataObjectVersionCodeListVersion>DataObjectVersionCodeListVersion0</DataObjectVersionCodeListVersion>\n"
				+ "    <StorageRuleCodeListVersion>StorageRuleCodeListVersion0</StorageRuleCodeListVersion>\n"
				+ "    <AppraisalRuleCodeListVersion>AppraisalRuleCodeListVersion0</AppraisalRuleCodeListVersion>\n"
				+ "    <AccessRuleCodeListVersion>AccessRuleCodeListVersion0</AccessRuleCodeListVersion>\n"
				+ "    <DisseminationRuleCodeListVersion>DisseminationRuleCodeListVersion0</DisseminationRuleCodeListVersion>\n"
				+ "    <ReuseRuleCodeListVersion>ReuseRuleCodeListVersion0</ReuseRuleCodeListVersion>\n"
				+ "    <ClassificationRuleCodeListVersion>ClassificationRuleCodeListVersion0</ClassificationRuleCodeListVersion>\n"
				+ "    <AuthorizationReasonCodeListVersion>AuthorizationReasonCodeListVersion0</AuthorizationReasonCodeListVersion>\n"
				+ "    <RelationshipCodeListVersion>RelationshipCodeListVersion0</RelationshipCodeListVersion>\n"
				+ "  </CodeListVersions>";
		getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier="Identifier3";
		getArchiveTransferGlobalMetadata().archivalAgencyIdentifier="Identifier4";
		getArchiveTransferGlobalMetadata().transferringAgencyIdentifier="Identifier5";
	}

	/**
	 * Gets the archive transfer global metadata.
	 *
	 * @return the archive transfer global metadata
	 */
	public GlobalMetadata getArchiveTransferGlobalMetadata() {
		return globalMetadata;
	}

	/**
	 * Sets the archive transfer global metadata.
	 *
	 * @param globalMetadata the new archive transfer global metadata
	 */
	public void setArchiveTransferGlobalMetadata(GlobalMetadata globalMetadata) {
		this.globalMetadata = globalMetadata;
	}

	/**
	 * Checks if is hierarchical archive units.
	 *
	 * @return true, if is hierarchical archive units
	 */
	public boolean isHierarchicalArchiveUnits() {
		return hierarchicalArchiveUnits;
	}

	/**
	 * Sets the hierarchical archive units.
	 *
	 * @param hierarchicalArchiveUnits the new hierarchical archive units
	 */
	public void setHierarchicalArchiveUnits(boolean hierarchicalArchiveUnits) {
		this.hierarchicalArchiveUnits = hierarchicalArchiveUnits;
	}

	/**
	 * Checks if is indented.
	 *
	 * @return true, if is indented
	 */
	public boolean isIndented() {
		return indented;
	}

	/**
	 * Sets the indented.
	 *
	 * @param indented the new indented
	 */
	public void setIndented(boolean indented) {
		this.indented = indented;
	}

	/**
	 * Is reindex boolean.
	 *
	 * @return the boolean
	 */
	public boolean isReindex() {
		return reindex;
	}

	/**
	 * Sets reindex.
	 *
	 * @param reindex the reindex
	 */
	public void setReindex(boolean reindex) {
		this.reindex = reindex;
	}

	/**
	 * Gets usage version selection mode.
	 *
	 * @return the usage version selection mode
	 */
	public int getUsageVersionSelectionMode() {
		return usageVersionSelectionMode;
	}

	/**
	 * Sets usage version selection mode.
	 *
	 * @param usageVersionSelectionMode the usage version selection mode
	 */
	public void setUsageVersionSelectionMode(int usageVersionSelectionMode) {
		this.usageVersionSelectionMode = usageVersionSelectionMode;
	}

	/**
	 * Gets max name size.
	 *
	 * @return the max name size
	 */
	public int getMaxNameSize() {
		return maxNameSize;
	}

	/**
	 * Sets max name size.
	 *
	 * @param maxNameSize the max name size
	 */
	public void setMaxNameSize(int maxNameSize) {
		this.maxNameSize = maxNameSize;
	}

	/**
	 * Gets the management metadata xml data.
	 *
	 * @return the management metadata xml data
	 */
	public String getManagementMetadataXmlData() {
		return managementMetadataXmlData;
	}

	/**
	 * Sets the management metadata xml data.
	 *
	 * @param managementMetadataXmlData the new management metadata xml data
	 */
	public void setManagementMetadataXmlData(String managementMetadataXmlData) {
		this.managementMetadataXmlData = managementMetadataXmlData;
	}

	/**
	 * Gets on disk output.
	 *
	 * @return the on disk output
	 */
	public String getOnDiskOutput() {
		return onDiskOutput;
	}

	/**
	 * Sets on disk output.
	 *
	 * @param onDiskOutput the on disk output
	 */
	public void setOnDiskOutput(String onDiskOutput) {
		this.onDiskOutput = onDiskOutput;
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
}
