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
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class AgencyType.
 * <p>
 * For abstract agency type SEDA metadata
 */
public class AgencyType extends ComplexListType {

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
     * Instantiates a new agency type.
     *
     * @param elementName the element name
     */
    public AgencyType(String elementName) {
        super(elementName);
    }

    /**
     * Instantiates a new agency type with identifier.
     *
     * @param elementName the element name
     * @param identifier  the identifier
     */
    public AgencyType(String elementName, String identifier) {
        super(elementName);
        try {
            addNewMetadata("Identifier", identifier);
        } catch (SEDALibException ignored) {
        }
    }

    /**
     * Instantiates a new agency type from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public AgencyType(String elementName, Object[] args) throws SEDALibException {
        super(elementName);
        if ((args.length == 1) && (args[0] instanceof String)) {
            addNewMetadata("Identifier", (String) args[0]);
        } else
            throw new SEDALibException("Mauvais arguments pour le constructeur de l'élément [" + elementName + "]");
    }

    /**
     * Import the AgencyType in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read AgencyType
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not                          respected
     */
    public static AgencyType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        XMLEvent event;
        try {
            event = xmlReader.peekUsefullEvent();
            AgencyType agencyType = new AgencyType(event.asStartElement().getName().getLocalPart());
            fromSedaXmlInObject(xmlReader, agencyType);
            return agencyType;
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type AgencyType\n->" + e.getMessage());
        }
    }

    // Init

    /**
     * Init metadata ordered list.
     */
    protected void initMetadataOrderedList() {
        metadataOrderedList = new ArrayList<String>();
        metadataOrderedList.add("Identifier");
    }

    /**
     * Init metadata map.
     */
    protected void initMetadataMap() {
        metadataMap = new HashMap<String, MetadataKind>();
        metadataMap.put("Identifier", new MetadataKind(StringType.class, false));
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

    @Override
    public boolean isNotExpendable() {
        return true;
    }
}
