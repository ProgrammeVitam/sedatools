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
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class Coverage.
 * <p>
 * Class for Coverage metadata.
 * <p>
 * An ArchiveUnit metadata.
 * <p>
 * Standard quote: "Couverture spatiale, temporelle ou juridictionnelle"
 */
public class Coverage extends ComplexListType {

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
     * Instantiates a new coverage type.
     */
    public Coverage() {
        super("Coverage");
    }

    /**
     * Instantiates a new coverage type with Spatial, Temporal and Juridictional. If any is null, it's not added
     *
     * @param elementName   the element name
     * @param spatial       the spatial
     * @param temporal      the temporal
     * @param juridictional the juridictional
     */
    public Coverage(String elementName, String spatial, String temporal, String juridictional) {
        super(elementName);
        try {
            if (spatial != null) addNewMetadata("Spatial", spatial);
            if (temporal != null) addNewMetadata("Temporal", temporal);
            if (juridictional != null) addNewMetadata("Juridictional", juridictional);
        } catch (SEDALibException ignored) {
        }
    }

    /**
     * Instantiates a new coverage type from args. If any is null, it's not added
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public Coverage(String elementName, Object[] args) throws SEDALibException {
        super(elementName);

        if ((args.length == 3) && ((args[0] == null) || (args[0] instanceof String))
                && ((args[1] == null) || (args[1] instanceof String))
                && ((args[2] == null) || (args[2] instanceof String))) {
            if (args[0] != null) addNewMetadata("Spatial", (String) args[0]);
            if (args[1] != null) addNewMetadata("Temporal", (String) args[1]);
            if (args[2] != null) addNewMetadata("Juridictional", (String) args[2]);
        } else
            throw new SEDALibException("Mauvais arguments pour le constructeur de l'élément [" + elementName + "]");
    }

    /**
     * Import the Coverage in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read Coverage
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not                          respected
     */
    public static Coverage fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        XMLEvent event;
        try {
            event = xmlReader.peekUsefullEvent();
            Coverage coverage = new Coverage();
            fromSedaXmlInObject(xmlReader, coverage);
            return coverage;
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type Coverage\n->" + e.getMessage());
        }
    }

    // Init

    /**
     * Init metadata ordered list.
     */
    protected void initMetadataOrderedList() {
        metadataOrderedList = new ArrayList<String>();
        metadataOrderedList.add("Spatial");
        metadataOrderedList.add("Temporal");
        metadataOrderedList.add("Juridictional");
    }

    /**
     * Init metadata map.
     */
    protected void initMetadataMap() {
        metadataMap = new HashMap<String, MetadataKind>();
        metadataMap.put("Spatial", new MetadataKind(StringType.class, true));
        metadataMap.put("Temporal", new MetadataKind(StringType.class, true));
        metadataMap.put("Juridictional", new MetadataKind(StringType.class, true));
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
