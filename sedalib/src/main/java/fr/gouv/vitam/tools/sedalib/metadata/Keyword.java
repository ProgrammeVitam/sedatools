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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class Keyword.
 * <p>
 * Class for Keyword metadata.
 * <p>
 * an ArchiveUnit metadata.
 * <p>
 * Standard quote: "Mots-clef avec contexte inspiré du SEDA 1.0. En ce qui concerne l'indexation, on pourra
 * utiliser Tag ou Keyword en fonction de ce que l'on souhaite décrire."
 */
public class Keyword extends ComplexListType {

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
     * Instantiates a new keyword type.
     */
    public Keyword() {
        super("Keyword");
    }

    /**
     * Instantiates a new keyword type with KeywordContent, KeywordReference and KeywordType. If any is null, it's not added.
     *
     * @param elementName      the element name
     * @param keywordContent   the keyword content
     * @param keywordReference the keyword reference
     * @param keywordType      the keyword type
     */
    public Keyword(String elementName, String keywordContent, String keywordReference, String keywordType) {
        super(elementName);
        try {
            if (keywordContent != null) addNewMetadata("KeywordContent", keywordContent);
            if (keywordReference != null) addNewMetadata("KeywordReference", keywordReference);
            if (keywordType != null) addNewMetadata("KeywordType", keywordType);
        } catch (SEDALibException ignored) {
        }
    }

    /**
     * Instantiates a new keyword type from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public Keyword(String elementName, Object[] args) throws SEDALibException {
        super(elementName);
        if ((args.length == 3) && (args[0] instanceof String)
                && ((args[1] == null) || (args[1] instanceof String))
                && ((args[2] == null) || (args[2] instanceof String))) {
            addNewMetadata("KeywordContent", (String) args[0]);
            if (args[1] != null) addNewMetadata("KeywordReference", (String) args[1]);
            if (args[2] != null) addNewMetadata("KeywordType", (String) args[2]);
        } else
            throw new SEDALibException("Mauvais arguments pour le constructeur de l'élément [" + elementName + "]");
    }

    /**
     * Import the Keyword in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read Keyword
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not                          respected
     */
    public static Keyword fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        XMLEvent event;
        try {
            event = xmlReader.peekUsefullEvent();
            Keyword keyword = new Keyword();
            fromSedaXmlInObject(xmlReader, keyword);
            return keyword;
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type Keyword\n->" + e.getMessage());
        }
    }

    // Init

    /**
     * Init metadata ordered list.
     */
    protected void initMetadataOrderedList() {
        metadataOrderedList = new ArrayList<String>();
        metadataOrderedList.add("KeywordContent");
        metadataOrderedList.add("KeywordReference");
        metadataOrderedList.add("KeywordType");
    }

    /**
     * Init metadata map.
     */
    protected void initMetadataMap() {
        metadataMap = new HashMap<String, MetadataKind>();
        metadataMap.put("KeywordContent", new MetadataKind(StringType.class, false));
        metadataMap.put("KeywordReference", new MetadataKind(StringType.class, false));
        metadataMap.put("KeywordType", new MetadataKind(CodeKeywordType.class, false));
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
