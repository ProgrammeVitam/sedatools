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
package fr.gouv.vitam.tools.sedalib.core.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibJsonProcessingException;

import java.io.IOException;

/**
 * The Class DataObjectPackageDeserializer.
 * <p>
 * Class for a Jackson deserializer for DataObjectPackage
 */
public class DataObjectPackageDeserializer extends StdDeserializer<DataObjectPackage> {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -2569548330768460468L;

    /**
     * Instantiates a new DataObjectPackage deserializer.
     */
    public DataObjectPackageDeserializer() {
        this(null);
    }

    /**
     * Instantiates a new DataObjectPackage deserializer.
     *
     * @param t the t
     */
    public DataObjectPackageDeserializer(Class<DataObjectPackage> t) {
        super(t);
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
     */
    @Override
    public DataObjectPackage deserialize(JsonParser parser, DeserializationContext deserializer)
            throws IOException {
        DataObjectPackage dataObjectPackage = new DataObjectPackage();

        parser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        try {
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = parser.getCurrentName();
                parser.nextToken();
                switch (fieldname) {
                    case "dataObjectGroupList":
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            DataObjectGroup og = parser.readValueAs(DataObjectGroup.class);
                            dataObjectPackage.addDataObjectGroup(og);
                            for (BinaryDataObject bdo : og.getBinaryDataObjectList())
                                dataObjectPackage.addBinaryDataObject(bdo);
                            for (PhysicalDataObject pdo : og.getPhysicalDataObjectList())
                                dataObjectPackage.addPhysicalDataObject(pdo);
                        }
                        break;
                    case "ArchiveUnitList":
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            ArchiveUnit au = parser.readValueAs(ArchiveUnit.class);
                            dataObjectPackage.addArchiveUnit(au);
                        }
                        break;
                    case "ghostRootAu":
                        ArchiveUnit au = parser.readValueAs(ArchiveUnit.class);
                        dataObjectPackage.setGhostRootAu(au);
                        break;
                    case "vitamNormalizationStatus":
                        int status = parser.readValueAs(Integer.class);
                        dataObjectPackage.setVitamNormalizationStatus(status);
                        break;
                    default:
                        // ignore other fields
                }
            }
        } catch (SEDALibException e) {
            throw new SEDALibJsonProcessingException("Erreur dans la deserialisation de l'ArchiveTransfer\n->" + e.getMessage());
        }

        return dataObjectPackage;
    }
}
