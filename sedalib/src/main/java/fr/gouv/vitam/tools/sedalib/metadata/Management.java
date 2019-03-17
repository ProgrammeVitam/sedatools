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

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.GenericXMLBlockType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class Management.
 * <p>
 * Class for SEDA element Management.
 * <p>
 * A ArchiveUnit metadata.
 * <p>
 * Standard quote: "Métadonnées de gestion applicables à l’ArchiveUnit concernée
 * et à ses héritiers"
 */
public class Management extends ComplexListType {

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
     * Instantiates a new Management.
     */
    public Management() {
        super("Management");
    }

    /**
     * Import the Content in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read Content
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static Management fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        Management management = new Management();
        management = (Management) fromSedaXmlInObject(xmlReader, management);
        return management;
    }

    // Getters and setters

    /**
     * Init metadata ordered list.
     */
    protected void initMetadataOrderedList() {
        metadataOrderedList = new ArrayList<String>();
        metadataOrderedList.add("StorageRule");
        metadataOrderedList.add("AppraisalRule");
        metadataOrderedList.add("AccessRule");
        metadataOrderedList.add("DisseminationRule");
        metadataOrderedList.add("ReuseRule");
        metadataOrderedList.add("ClassificationRule");
        metadataOrderedList.add("LogBook");
        metadataOrderedList.add("NeedAuthorization");
        // Vitam extension
        metadataOrderedList.add("UpdateOperation");
    }

    /**
     * Init metadata map.
     */
    protected void initMetadataMap() {
        metadataMap = new HashMap<String, ComplexListType.MetadataKind>();
        metadataMap.put("StorageRule", new ComplexListType.MetadataKind(StorageRule.class, false));
        metadataMap.put("AppraisalRule", new ComplexListType.MetadataKind(AppraisalRule.class, false));
        metadataMap.put("AccessRule", new ComplexListType.MetadataKind(AccessRule.class, false));
        metadataMap.put("DisseminationRule", new ComplexListType.MetadataKind(DisseminationRule.class, false));
        metadataMap.put("ReuseRule", new ComplexListType.MetadataKind(ReuseRule.class, false));
        metadataMap.put("ClassificationRule",
                new ComplexListType.MetadataKind(ClassificationRule.class, false));
        metadataMap.put("LogBook",
                new ComplexListType.MetadataKind(GenericXMLBlockType.class, false));
        metadataMap.put("NeedAuthorization",
                new ComplexListType.MetadataKind(StringType.class, false));
        // Vitam extension
        metadataMap.put("UpdateOperation",
                new ComplexListType.MetadataKind(UpdateOperation.class, false));
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
