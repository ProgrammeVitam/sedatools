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

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;

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

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    static final public LinkedHashMap<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<String, ComplexListMetadataKind>();
        metadataMap.put("GpsVersionID", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("GpsAltitude", new ComplexListMetadataKind(IntegerType.class, false));
        metadataMap.put("GpsAltitudeRef", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("GpsLatitude", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("GpsLatitudeRef", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("GpsLongitude", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("GpsLongitudeRef", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("GpsDateStamp", new ComplexListMetadataKind(StringType.class, false));
    }

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
     * @param gpsVersionID the gps version id
     * @param gpsAltitude  the gps altitude
     * @param gpsLatitude  the gps latitude
     * @param gpsLongitude the gps longitude
     * @param gpsDateStamp the gps date stamp
     * @throws SEDALibException if sub element construction is not possible (not supposed to occur)
     */
    public Gps(String gpsVersionID, int gpsAltitude, String gpsLatitude, String gpsLongitude,
               String gpsDateStamp) throws SEDALibException {
        this();

        if (gpsVersionID != null) addNewMetadata("GpsVersionID", gpsVersionID);
        if (gpsAltitude != -100000) {
            if (gpsAltitude < 0)
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
    }
}
