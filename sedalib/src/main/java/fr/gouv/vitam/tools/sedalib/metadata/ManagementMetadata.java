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
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
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

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.GenericXMLBlockType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class ManagementMetadata.
 * <p>
 * Class for SEDA element ManagementMetadata.
 * <p>
 * In DataObjectPackage metadata.
 * <p>
 * Standard quote: "Bloc des métadonnées de gestion par défaut des
 * objets-données."
 */
public class ManagementMetadata extends ComplexListType {

    /** Init the metadata possibilities. */ {
        initMetadataOrderedList();
        initMetadataMap();
    }

    /** The metadata ordered list. */
    protected static List<String> metadataOrderedList;

    /** The metadata map. */
    protected static HashMap<String, MetadataKind> metadataMap;

    /**
     * Instantiates a new Management.
     */
    public ManagementMetadata() {
        super("ManagementMetadata");
    }

    /**
     * Import the ManagementMetadata in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read Content
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static ManagementMetadata fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        ManagementMetadata managementMetadata = new ManagementMetadata();
        managementMetadata = (ManagementMetadata) fromSedaXmlInObject(xmlReader, managementMetadata);
        return managementMetadata;
    }

    // Init

    /**
     * Init metadata ordered list.
     */
    protected void initMetadataOrderedList() {
        metadataOrderedList = new ArrayList<String>();
        metadataOrderedList.add("ArchivalProfile");
        metadataOrderedList.add("ServiceLevel");
        metadataOrderedList.add("AcquisitionInformation");
        metadataOrderedList.add("LegalStatus");
        metadataOrderedList.add("OriginatingAgencyIdentifier");
        metadataOrderedList.add("SubmissionAgencyIdentifier");
        metadataOrderedList.add("StorageRule");
        metadataOrderedList.add("AppraisalRule");
        metadataOrderedList.add("AccessRule");
        metadataOrderedList.add("DisseminationRule");
        metadataOrderedList.add("ReuseRule");
        metadataOrderedList.add("ClassificationRule");
        metadataOrderedList.add("LogBook");
        metadataOrderedList.add("NeedAuthorization");
    }

    /**
     * Init metadata map.
     */
    protected void initMetadataMap() {
        metadataMap = new HashMap<String, ComplexListType.MetadataKind>();
        metadataMap.put("ArchivalProfile",
                new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("ServiceLevel", new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("AcquisitionInformation",
                new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("LegalStatus", new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("OriginatingAgencyIdentifier",
                new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("SubmissionAgencyIdentifier",
                new ComplexListType.MetadataKind(StringType.class, false));
        metadataMap.put("StorageRule", new ComplexListType.MetadataKind(StorageRule.class, false));
        metadataMap.put("AppraisalRule", new ComplexListType.MetadataKind(AppraisalRule.class, false));
        metadataMap.put("AccessRule", new ComplexListType.MetadataKind(AccessRule.class, false));
        metadataMap.put("DisseminationRule",
                new ComplexListType.MetadataKind(DisseminationRule.class, false));
        metadataMap.put("ReuseRule", new ComplexListType.MetadataKind(ReuseRule.class, false));
        metadataMap.put("ClassificationRule",
                new ComplexListType.MetadataKind(ClassificationRule.class, false));
        metadataMap.put("LogBook", new ComplexListType.MetadataKind(GenericXMLBlockType.class, false));
        metadataMap.put("NeedAuthorization",
                new ComplexListType.MetadataKind(StringType.class, false));
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
