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
 * The Class Gps.
 * <p>
 * Class for Gps metadata.
 * <p>
 * An ArchiveUnit metadata.
 * <p>
 * Standard quote: "Coordonnées gps complétées ou vérifiées par un utilisateur.
 * Fait référence à des coordonnées traitées par un utilisateur et non à des coordonnées captées"
 */
public class Gps extends ComplexListType {

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
     * Instantiates a new gps type.
     */
    public Gps() {
        super("Gps");
    }

    /**
     * Instantiates a new gps type with GpsVersionID, GpsAltitude (signed int), GpsLatitude(if begin by '-' GpsLatitudeRef
     * is 'S', if not 'N'), GpsLongitude(if begin by '-' GpsLongitudeRef is 'O', if not 'E') and GpsDateStamp. If version
     * or date stamp is null, or if altitude=-100000 they are not added
     *
     * @param elementName  the element name
     * @param gpsVersionID the gps version id
     * @param gpsAltitude  the gps altitude
     * @param gpsLatitude  the gps latitude
     * @param gpsLongitude the gps longitude
     * @param gpsDateStamp the gps date stamp
     */
    public Gps(String elementName, String gpsVersionID, int gpsAltitude, String gpsLatitude, String gpsLongitude,
               String gpsDateStamp) {
        super(elementName);
        try {
            if (gpsVersionID != null) addNewMetadata("GpsVersionID", gpsVersionID);
            if (gpsAltitude != -100000) {
                if (gpsAltitude< 0)
                    addNewMetadata("GpsAltitudeRef", "1");
                else
                    addNewMetadata("GpsAltitudeRef", "0");
                addNewMetadata("GpsAltitude", Math.abs(gpsAltitude));
            }
            if (gpsLatitude.startsWith("-")) {
                gpsLatitude = gpsLatitude.substring(1);
                addNewMetadata("GpsLatitudeRef", "S");
            } else
                addNewMetadata("GpsLatitudeRef", "N");
            addNewMetadata("GpsLatitude", gpsLatitude);
            if (gpsLongitude.startsWith("-")) {
                gpsLongitude = gpsLongitude.substring(1);
                addNewMetadata("GpsLongitudeRef", "O");
            } else
                addNewMetadata("GpsLongitudeRef", "E");
            addNewMetadata("GpsLongitude", gpsLongitude);
            if (gpsDateStamp != null) addNewMetadata("GpsDateStamp", gpsDateStamp);
        } catch (SEDALibException ignored) {
        }
    }

    /**
     * Instantiates a new gps type from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public Gps(String elementName, Object[] args) throws SEDALibException {
        super(elementName);

        if ((args.length == 5) && ((args[0] == null) || (args[0] instanceof String))
                && (args[1] instanceof Integer)
                && (args[2] instanceof String)
                && (args[3] instanceof String)
                && ((args[4] == null) || (args[4] instanceof String))) {
            if (args[0] != null) addNewMetadata("GpsVersionID", (String) args[0]);
            if ((Integer) args[1] != -100000) {
                if ((Integer) args[1] < 0)
                    addNewMetadata("GpsAltitudeRef", "1");
                else
                    addNewMetadata("GpsAltitudeRef", "0");
                addNewMetadata("GpsAltitude", Math.abs((Integer) args[1]));
            }
            if (((String) args[2]).startsWith("-")) {
                args[2] = ((String) args[2]).substring(1);
                addNewMetadata("GpsLatitudeRef", "S");
            } else
                addNewMetadata("GpsLatitudeRef", "N");
            addNewMetadata("GpsLatitude", (String) args[2]);
            if (((String) args[3]).startsWith("-")) {
                args[3] = ((String) args[3]).substring(1);
                addNewMetadata("GpsLongitudeRef", "O");
            } else
                addNewMetadata("GpsLongitudeRef", "E");
            addNewMetadata("GpsLongitude", (String) args[3]);
            if (args[4] != null) addNewMetadata("GpsDateStamp", (String) args[4]);
        } else
            throw new SEDALibException("Mauvais arguments pour le constructeur de l'élément [" + elementName + "]");
    }

    /**
     * Import the Gps in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read Gps
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not                          respected
     */
    public static Gps fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        XMLEvent event;
        try {
            event = xmlReader.peekUsefullEvent();
            Gps gps = new Gps();
            fromSedaXmlInObject(xmlReader, gps);
            return gps;
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type Gps\n->" + e.getMessage());
        }
    }

    // Init

    /**
     * Init metadata ordered list.
     */
    protected void initMetadataOrderedList() {
        metadataOrderedList = new ArrayList<String>();
        metadataOrderedList.add("GpsVersionID");
        metadataOrderedList.add("GpsAltitude");
        metadataOrderedList.add("GpsAltitudeRef");
        metadataOrderedList.add("GpsLatitude");
        metadataOrderedList.add("GpsLatitudeRef");
        metadataOrderedList.add("GpsLongitude");
        metadataOrderedList.add("GpsLongitudeRef");
        metadataOrderedList.add("GpsDateStamp");
    }

    /**
     * Init metadata map.
     */
    protected void initMetadataMap() {
        metadataMap = new HashMap<String, MetadataKind>();
        metadataMap.put("GpsVersionID", new MetadataKind(StringType.class, false));
        metadataMap.put("GpsAltitude", new MetadataKind(IntegerType.class, false));
        metadataMap.put("GpsAltitudeRef", new MetadataKind(StringType.class, false));
        metadataMap.put("GpsLatitude", new MetadataKind(StringType.class, false));
        metadataMap.put("GpsLatitudeRef", new MetadataKind(StringType.class, false));
        metadataMap.put("GpsLongitude", new MetadataKind(StringType.class, false));
        metadataMap.put("GpsLongitudeRef", new MetadataKind(StringType.class, false));
        metadataMap.put("GpsDateStamp", new MetadataKind(StringType.class, false));
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
