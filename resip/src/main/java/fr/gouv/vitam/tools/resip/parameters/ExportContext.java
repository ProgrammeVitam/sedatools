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

/**
 * The Class ExportContext.
 */
public class ExportContext {

	/**
	 * The export nature.
	 */
	public static final int SIP=1;
	public static final int MANIFEST=2;
	public static final int DISK=3;
	
	// prefs elements
	/** The hierarchical archive units. */
	private boolean hierarchicalArchiveUnits;

	/** The indented. */
	private boolean indented;

	/** The reindex DataObjectPackage elements before export. */
	private boolean reindex;

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

	// Inner Object

	/**
	 * The Constant CURRENT_SERIALIZATION_VERSION.
	 */
	static final String CURRENT_SERIALIZATION_VERSION = "1.0";

	/**
	 * The version of this object used for to distinct serialization in prefs or on
	 * disk.
	 */
	private String serializationVersion;

	/**
	 * Instantiates a new global metadata context.
	 */
	public ExportContext() {
		this.hierarchicalArchiveUnits = true;
		this.indented = true;
		this.reindex = false;
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
	 * @param onDiskOutput              the on disk output
	 * @param managementMetadataXmlData the management metadata xml data
	 * @param metadataFilterFlag        the metadata filter flag
	 * @param keptMetadataList          the kept metadata list
	 */
	public ExportContext(GlobalMetadata globalMetadata,
			boolean hierarchicalArchiveUnits, boolean indented, boolean reindex, String onDiskOutput, String managementMetadataXmlData,
						 boolean metadataFilterFlag, List<String> keptMetadataList) {
		this.hierarchicalArchiveUnits = hierarchicalArchiveUnits;
		this.indented = indented;
		this.reindex = indented;
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
	 * @param contextNode the node in which preferences
	 */
	public ExportContext(Preferences contextNode) {
		Preferences node;
		setArchiveTransferGlobalMetadata(new GlobalMetadata());
		node = contextNode.node("ExportContext");
		serializationVersion = node.get("serializationVersion", CURRENT_SERIALIZATION_VERSION);
		hierarchicalArchiveUnits = node.getBoolean("hierarchicalArchiveUnits", true);
		indented = node.getBoolean("indented", true);
		reindex = node.getBoolean("reindex", false);
		managementMetadataXmlData=nullIfEmpty(node.get("managementMetadataXmlData", ""));
		metadataFilterFlag = node.getBoolean("metadataFilterFlag", false);
		String keptMetadataString = node.get("keptMetadataList", "");
		if (keptMetadataString.isEmpty())
			keptMetadataList = new ArrayList<String>();
		else
			keptMetadataList = Arrays.asList(keptMetadataString.split("\\s*\n\\s*"))
					.stream().map(String::trim).collect(Collectors.toList());
		node = node.node("globalMetadata");
		getArchiveTransferGlobalMetadata().comment=nullIfEmpty(node.get("comment", ""));
		getArchiveTransferGlobalMetadata().date=nullIfEmpty(node.get("date", ""));
		getArchiveTransferGlobalMetadata().setNowFlag(node.getBoolean("nowFlag", true));
		getArchiveTransferGlobalMetadata().messageIdentifier=nullIfEmpty(node.get("messageIdentifier", ""));
		getArchiveTransferGlobalMetadata().archivalAgreement=nullIfEmpty(node.get("archivalAgreement", ""));
		getArchiveTransferGlobalMetadata()
				.codeListVersionsXmlData=nullIfEmpty(node.get("codeListVersionsXmlData", ""));
		getArchiveTransferGlobalMetadata()
				.transferRequestReplyIdentifier=nullIfEmpty(node.get("transferRequestReplyIdentifier", ""));
		getArchiveTransferGlobalMetadata()
				.archivalAgencyIdentifier=nullIfEmpty(node.get("archivalAgencyIdentifier", ""));
		getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData=
				nullIfEmpty(node.get("archivalAgencyOrganizationDescriptiveMetadataXmlData", ""));
		getArchiveTransferGlobalMetadata()
				.transferringAgencyIdentifier=nullIfEmpty(node.get("transferringAgencyIdentifier", ""));
		getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData=
				nullIfEmpty(node.get("transferringAgencyOrganizationDescriptiveMetadataXmlData", ""));
	}

	/**
	 * To prefs.
	 *
	 * @param globalNode the global node
	 * @throws BackingStoreException the backing store exception
	 */
	public void toPrefs(Preferences globalNode) throws BackingStoreException {
		Preferences contextNode = globalNode.node("ExportContext");
		contextNode.put("serializationVersion", serializationVersion);
		contextNode.putBoolean("hierarchicalArchiveUnits", hierarchicalArchiveUnits);
		contextNode.putBoolean("indented", indented);
		contextNode.putBoolean("reindex", reindex);
		contextNode.put("managementMetadataXmlData", (managementMetadataXmlData == null ? ""
				: managementMetadataXmlData));
		contextNode.putBoolean("metadataFilterFlag", metadataFilterFlag);
		contextNode.put("keptMetadataList", String.join("\n", keptMetadataList));

		contextNode = contextNode.node("globalMetadata");
		contextNode.put("comment", (getArchiveTransferGlobalMetadata().comment == null ? ""
				: getArchiveTransferGlobalMetadata().comment));
		contextNode.put("date", (getArchiveTransferGlobalMetadata().date == null ? ""
				: getArchiveTransferGlobalMetadata().date));
		contextNode.putBoolean("nowFlag", getArchiveTransferGlobalMetadata().isNowFlag());
		contextNode.put("messageIdentifier", (getArchiveTransferGlobalMetadata().messageIdentifier == null ? ""
				: getArchiveTransferGlobalMetadata().messageIdentifier));
		contextNode.put("archivalAgreement", (getArchiveTransferGlobalMetadata().archivalAgreement == null ? ""
				: getArchiveTransferGlobalMetadata().archivalAgreement));
		contextNode.put("codeListVersionsXmlData",
				(getArchiveTransferGlobalMetadata().codeListVersionsXmlData == null ? ""
						: getArchiveTransferGlobalMetadata().codeListVersionsXmlData));
		contextNode.put("transferRequestReplyIdentifier",
				(getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier == null ? ""
						: getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier));
		contextNode.put("archivalAgencyIdentifier",
				(getArchiveTransferGlobalMetadata().archivalAgencyIdentifier == null ? ""
						: getArchiveTransferGlobalMetadata().archivalAgencyIdentifier));
		contextNode.put("archivalAgencyOrganizationDescriptiveMetadataXmlData",
				(getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData == null ? ""
						: getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData));
		contextNode.put("transferringAgencyIdentifier",
				(getArchiveTransferGlobalMetadata().transferringAgencyIdentifier == null ? ""
						: getArchiveTransferGlobalMetadata().transferringAgencyIdentifier));
		contextNode.put("transferringAgencyOrganizationDescriptiveMetadataXmlData",
				(getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData == null ? ""
						: getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData));
		contextNode.flush();
	}

	/**
	 * Sets the default prefs.
	 */
	public void setDefaultPrefs() {
		serializationVersion = CURRENT_SERIALIZATION_VERSION;
		this.hierarchicalArchiveUnits = true;
		this.indented = true;
		this.reindex = false;
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
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getSerializationVersion() {
		return serializationVersion;
	}

	/**
	 * Sets the version.
	 *
	 * @param serializationVersion the new serialization version
	 */
	public void setSerializationVersion(String serializationVersion) {
		this.serializationVersion = serializationVersion;
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
