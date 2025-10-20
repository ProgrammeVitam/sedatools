/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata.content;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.compacted.DocumentContainer;
import fr.gouv.vitam.tools.sedalib.metadata.compacted.DocumentPack;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class Content.
 * <p>
 * Class for SEDA element Content.
 * <p>
 * A ArchiveUnit metadata.
 * <p>
 * Standard quote: "Métadonnées de description associées à un ArchiveUnit"
 */
public class Content extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true, sedaVersion = { SedaVersion.V2_1 })
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();//NOSONAR public mandatory for ComplexlistType mechanism
        metadataMap.put("DescriptionLevel", new ComplexListMetadataKind(EnumType.class, false));
        metadataMap.put("Title", new ComplexListMetadataKind(TextType.class, true));
        metadataMap.put("FilePlanPosition", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("SystemId", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("OriginatingSystemId", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("ArchivalAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("OriginatingAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("TransferringAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("Description", new ComplexListMetadataKind(TextType.class, true));
        metadataMap.put("CustodialHistory",
                new ComplexListMetadataKind(CustodialHistory.class, false));
        metadataMap.put("Type", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("DocumentType", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("Language", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("DescriptionLanguage", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("Status", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("Version", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("Tag", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("Keyword", new ComplexListMetadataKind(Keyword.class, true));
        metadataMap.put("Coverage", new ComplexListMetadataKind(Coverage.class, false));
        metadataMap.put("OriginatingAgency",
                new ComplexListMetadataKind(AgencyType.class, false));
        metadataMap.put("SubmissionAgency",
                new ComplexListMetadataKind(AgencyType.class, false));
        // can't implement AgentAbstract (abstract in middle of th list...)
        metadataMap.put("AuthorizedAgent", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap.put("Writer", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap.put("Addressee", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap.put("Recipient", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap.put("Transmitter", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap.put("Sender", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap.put("Source", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("RelatedObjectReference", new ComplexListMetadataKind(RelatedObjectReference.class, false));
        metadataMap.put("CreatedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("TransactedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("AcquiredDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("SentDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("ReceivedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("RegisteredDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("StartDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("EndDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("Event", new ComplexListMetadataKind(Event.class, true));
        metadataMap.put("Signature", new ComplexListMetadataKind(Signature.class, true));
        metadataMap.put("Gps", new ComplexListMetadataKind(Gps.class, false));
        // Vitam extensions
        metadataMap.put("OriginatingSystemIdReplyTo", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("TextContent", new ComplexListMetadataKind(StringType.class, true));
        // Experimental Compact extensions
        metadataMap.put("DocumentContainer", new ComplexListMetadataKind(DocumentContainer.class, false));
        metadataMap.put("DocumentPack", new ComplexListMetadataKind(DocumentPack.class, false));
    }

    @ComplexListMetadataMap(isExpandable = true, sedaVersion = { SedaVersion.V2_2 })
    public static final Map<String, ComplexListMetadataKind> metadataMap_v2;

    static {
        metadataMap_v2 = new LinkedHashMap<>();//NOSONAR public mandatory for ComplexlistType mechanism
        metadataMap_v2.put("DescriptionLevel", new ComplexListMetadataKind(EnumType.class, false));
        metadataMap_v2.put("Title", new ComplexListMetadataKind(TextType.class, true));
        metadataMap_v2.put("FilePlanPosition", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("SystemId", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("OriginatingSystemId", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("ArchivalAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("OriginatingAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("TransferringAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("Description", new ComplexListMetadataKind(TextType.class, true));
        metadataMap_v2.put("CustodialHistory",
                new ComplexListMetadataKind(CustodialHistory.class, false));
        metadataMap_v2.put("Type", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("DocumentType", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("Language", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("DescriptionLanguage", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("Status", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("Version", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("Tag", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("Keyword", new ComplexListMetadataKind(Keyword.class, true));
        metadataMap_v2.put("Coverage", new ComplexListMetadataKind(Coverage.class, false));
        metadataMap_v2.put("OriginatingAgency",
                new ComplexListMetadataKind(AgencyType.class, false));
        metadataMap_v2.put("SubmissionAgency",
                new ComplexListMetadataKind(AgencyType.class, false));
        // specific in Seda 2.2
        metadataMap_v2.put("Agent", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v2.put("AuthorizedAgent", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v2.put("Writer", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v2.put("Addressee", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v2.put("Recipient", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v2.put("Transmitter", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v2.put("Sender", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v2.put("Source", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("RelatedObjectReference", new ComplexListMetadataKind(RelatedObjectReference.class, false));
        metadataMap_v2.put("CreatedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("TransactedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("AcquiredDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("SentDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("ReceivedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("RegisteredDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("StartDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("EndDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("DateLitteral", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("Event", new ComplexListMetadataKind(Event.class, true));
        metadataMap_v2.put("Signature", new ComplexListMetadataKind(Signature.class, true));
        metadataMap_v2.put("Gps", new ComplexListMetadataKind(Gps.class, false));
        // specific in Seda 2.2 (was in Vitam before)
        metadataMap_v2.put("OriginatingSystemIdReplyTo", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("TextContent", new ComplexListMetadataKind(StringType.class, true));
        // Experimental Compact extensions
        metadataMap_v2.put("DocumentContainer", new ComplexListMetadataKind(DocumentContainer.class, false));
        metadataMap_v2.put("DocumentPack", new ComplexListMetadataKind(DocumentPack.class, false));
    }

    @ComplexListMetadataMap(isExpandable = true, sedaVersion = { SedaVersion.V2_3 })
    public static final Map<String, ComplexListMetadataKind> metadataMap_v3;

    static {
        metadataMap_v3 = new LinkedHashMap<>();//NOSONAR public mandatory for ComplexlistType mechanism
        metadataMap_v3.put("DescriptionLevel", new ComplexListMetadataKind(EnumType.class, false));
        metadataMap_v3.put("Title", new ComplexListMetadataKind(TextType.class, true));
        metadataMap_v3.put("FilePlanPosition", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("SystemId", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("OriginatingSystemId", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("ArchivalAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("OriginatingAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("TransferringAgencyArchiveUnitIdentifier",
                new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("Description", new ComplexListMetadataKind(TextType.class, true));
        metadataMap_v3.put("CustodialHistory",
                new ComplexListMetadataKind(CustodialHistory.class, false));
        metadataMap_v3.put("Type", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("DocumentType", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("Language", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("DescriptionLanguage", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("Status", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("Version", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("Tag", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("Keyword", new ComplexListMetadataKind(Keyword.class, true));
        metadataMap_v3.put("Coverage", new ComplexListMetadataKind(Coverage.class, false));
        metadataMap_v3.put("OriginatingAgency",
                new ComplexListMetadataKind(AgencyType.class, false));
        metadataMap_v3.put("SubmissionAgency",
                new ComplexListMetadataKind(AgencyType.class, false));
        metadataMap_v3.put("Agent", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v3.put("AuthorizedAgent", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v3.put("Writer", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v3.put("Addressee", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v3.put("Recipient", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v3.put("Transmitter", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v3.put("Sender", new ComplexListMetadataKind(AgentType.class, true));
        metadataMap_v3.put("Source", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("RelatedObjectReference", new ComplexListMetadataKind(RelatedObjectReference.class, false));
        metadataMap_v3.put("CreatedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("TransactedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("AcquiredDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("SentDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("ReceivedDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("RegisteredDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("StartDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("EndDate", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v3.put("DateLitteral", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("Event", new ComplexListMetadataKind(Event.class, true));
        // deprecated in Seda 2.3 metadataMap_v3.put("Signature", new ComplexListMetadataKind(Signature.class, true));
        metadataMap_v3.put("SigningInformation", new ComplexListMetadataKind(SigningInformation.class, false));
        metadataMap_v3.put("Gps", new ComplexListMetadataKind(Gps.class, false));
        metadataMap_v3.put("OriginatingSystemIdReplyTo", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("TextContent", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v3.put("PersistentIdentifier", new ComplexListMetadataKind(PersistentIdentifier.class, true));
        // Experimental Compact extensions
        metadataMap_v3.put("DocumentContainer", new ComplexListMetadataKind(DocumentContainer.class, false));
        metadataMap_v3.put("DocumentPack", new ComplexListMetadataKind(DocumentPack.class, false));
    }

    /**
     * Instantiates a new Content.
     */
    public Content() {
        super("Content");
    }

    /**
     * Return the XML export form as the String representation, but filtered by a list of authorized inner metadata.
     *
     * @param keptMetadataList the kept metadata list
     * @return the indented XML form String
     */
    public String filteredToString(List<String> keptMetadataList) {
        String result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
            xmlWriter.writeStartElement(elementName);
            for (SEDAMetadata sm : metadataList) {
                if (keptMetadataList.contains(sm.getXmlElementName()))
                    sm.toSedaXml(xmlWriter);
            }
            xmlWriter.writeEndElement();
            xmlWriter.flush();
            result = baos.toString(StandardCharsets.UTF_8);
            if (result.startsWith("\n"))
                result = result.substring(1);
        } catch (XMLStreamException | IOException | SEDALibException e) {
            result = super.toString();
        }
        return result;
    }

    /**
     * Export the Content metadata to csv List for the csv metadata file, but filtered by a list of authorized inner metadata.
     * <p>
     * In the HashMap result, the key is a metadata path of a leaf and the value is the leaf of the metadata value.
     *
     * @param keptMetadataList the kept metadata list
     * @return the linked hash map with header title as key and metadata value as value
     * @throws SEDALibException the seda lib exception
     */
    public LinkedHashMap<String, String> externToCsvList(List<String> keptMetadataList) throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        String previousXMLElementName = null;
        int count = 0;
        for (SEDAMetadata sm : metadataList) {
            if ((keptMetadataList != null) && (!keptMetadataList.contains(sm.getXmlElementName())))
                continue;
            if (!sm.getXmlElementName().equals(previousXMLElementName)) {
                previousXMLElementName = sm.getXmlElementName();
                count = 0;
            } else count++;
            final String addedName;
            if (isAMultiValuedMetadata(sm.getXmlElementName()))
                addedName = sm.getXmlElementName() + "." + count;
            else
                addedName = sm.getXmlElementName();
            LinkedHashMap<String, String> smCsvList = sm.toCsvList();
            smCsvList.entrySet().stream().forEach(e -> result.put("Content." + addedName + (e.getKey().isEmpty() ? "" : "." + e.getKey()), e.getValue()));
        }
        return result;
    }
}
