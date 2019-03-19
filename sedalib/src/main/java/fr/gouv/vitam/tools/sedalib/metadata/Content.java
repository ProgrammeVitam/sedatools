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
package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    /** Init the metadata possibilities. */ {
        initMetadataOrderedList();
        initMetadataMap();
    }

    /**
     * The metadata ordered list.
     */
    protected static List<String> metadataOrderedList;

    /**
     * The metadata map.
     */
    protected static HashMap<String, MetadataKind> metadataMap;

    /**
     * Instantiates a new Content.
     */
    public Content() {
        super("Content");
    }

    /**
     * Import the Content in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read Content
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static Content fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        Content content = new Content();
        content = (Content) fromSedaXmlInObject(xmlReader, content);
        return content;
    }

    // Init

    /**
     * Init metadata ordered list.
     */
    protected void initMetadataOrderedList() {
        metadataOrderedList = new ArrayList<String>();
        metadataOrderedList.add("DescriptionLevel");
        metadataOrderedList.add("Title");
        metadataOrderedList.add("FilePlanPosition");
        metadataOrderedList.add("SystemId");
        metadataOrderedList.add("OriginatingSystemId");
        metadataOrderedList.add("ArchivalAgencyArchiveUnitIdentifier");
        metadataOrderedList.add("OriginatingAgencyArchiveUnitIdentifier");
        metadataOrderedList.add("TransferringAgencyArchiveUnitIdentifier");
        metadataOrderedList.add("Description");
        metadataOrderedList.add("CustodialHistory");
        metadataOrderedList.add("Type");
        metadataOrderedList.add("DocumentType");
        metadataOrderedList.add("Language");
        metadataOrderedList.add("DescriptionLanguage");
        metadataOrderedList.add("Status");
        metadataOrderedList.add("Version");
        metadataOrderedList.add("Tag");
        metadataOrderedList.add("Keyword");
        metadataOrderedList.add("Coverage");
        metadataOrderedList.add("OriginatingAgency");
        metadataOrderedList.add("SubmissionAgency");
        // can't implement AgentAbstract (abstract in middle of th list...)
        metadataOrderedList.add("AuthorizedAgent");
        metadataOrderedList.add("Writer");
        metadataOrderedList.add("Addressee");
        metadataOrderedList.add("Recipient");
        metadataOrderedList.add("Transmitter");
        metadataOrderedList.add("Sender");
        metadataOrderedList.add("Source");
        // RelatedObjectReference not implemented in Vitam
        metadataOrderedList.add("CreatedDate");
        metadataOrderedList.add("TransactedDate");
        metadataOrderedList.add("AcquiredDate");
        metadataOrderedList.add("SentDate");
        metadataOrderedList.add("ReceivedDate");
        metadataOrderedList.add("RegisteredDate");
        metadataOrderedList.add("StartDate");
        metadataOrderedList.add("EndDate");
        metadataOrderedList.add("Event");
        metadataOrderedList.add("Signature");
        metadataOrderedList.add("Gps");
    }

    /**
     * Init metadata map.
     */
    protected void initMetadataMap() {
        metadataMap = new HashMap<String, ComplexListType.MetadataKind>();
        metadataMap.put("DescriptionLevel", new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("Title", new ComplexListType.MetadataKind(TextType.class, true));
        metadataMap.put("FilePlanPosition", new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("SystemId", new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("OriginatingSystemId", new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("ArchivalAgencyArchiveUnitIdentifier",
                new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("OriginatingAgencyArchiveUnitIdentifier",
                new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("TransferringAgencyArchiveUnitIdentifier",
                new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("Description", new ComplexListType.MetadataKind(TextType.class, true));
        metadataMap.put("CustodialHistory",
                new ComplexListType.MetadataKind(CustodialHistory.class, false));
        metadataMap.put("Type", new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("DocumentType", new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("Language", new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("DescriptionLanguage", new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("Status", new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("Version", new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("Tag", new ComplexListType.MetadataKind(StringType.class, true));
        metadataMap.put("Keyword", new ComplexListType.MetadataKind(Keyword.class, true));
        metadataMap.put("Coverage", new ComplexListType.MetadataKind(Coverage.class, false));
        metadataMap.put("OriginatingAgency",
                new ComplexListType.MetadataKind(AgencyType.class, false));
        metadataMap.put("SubmissionAgency",
                new ComplexListType.MetadataKind(AgencyType.class, false));
        // can't implement AgentAbstract (abstract in middle of th list...)
        metadataMap.put("AuthorizedAgent", new ComplexListType.MetadataKind(AgentType.class, true));
        metadataMap.put("Writer", new ComplexListType.MetadataKind(AgentType.class, true));
        metadataMap.put("Addressee", new ComplexListType.MetadataKind(AgentType.class, true));
        metadataMap.put("Recipient", new ComplexListType.MetadataKind(AgentType.class, true));
        metadataMap.put("Transmitter", new ComplexListType.MetadataKind(AgentType.class, true));
        metadataMap.put("Sender", new ComplexListType.MetadataKind(AgentType.class, true));
        metadataMap.put("Source", new ComplexListType.MetadataKind(StringType.class, false));
        // RelatedObjectReference not implemented in Vitam
        metadataMap.put("CreatedDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("TransactedDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("AcquiredDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("SentDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("ReceivedDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("RegisteredDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("StartDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("EndDate", new ComplexListType.MetadataKind(DateTimeType.class, false));
        metadataMap.put("Event", new ComplexListType.MetadataKind(Event.class, true));
        metadataMap.put("Signature", new ComplexListType.MetadataKind(Signature.class, true));
        metadataMap.put("Gps", new ComplexListType.MetadataKind(Gps.class, false));
    }

    // Getters and setters

    @Override
    public List<String> getMetadataOrderedList() {
        if (metadataOrderedList == null)
            initMetadataOrderedList();
        return metadataOrderedList;
    }

    @Override
    public HashMap<String, MetadataKind> getMetadataMap() {
        if (metadataMap == null)
            initMetadataMap();
        return metadataMap;
    }
}
