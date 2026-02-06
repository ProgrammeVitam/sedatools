/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * The Class DataObjectPackageSerializer.
 * <p>
 * Class for a Jackson serializer for DataObjectPackage
 */
public class DataObjectPackageSerializer extends StdSerializer<DataObjectPackage> {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -2569548330768460458L;

    /**
     * Instantiates a new DataObjectPackage serializer.
     */
    public DataObjectPackageSerializer() {
        this(null);
    }

    /**
     * Instantiates a new DataObjectPackage serializer.
     *
     * @param t the t
     */
    public DataObjectPackageSerializer(Class<DataObjectPackage> t) {
        super(t);
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(DataObjectPackage dataObjectPackage, JsonGenerator jGen, SerializerProvider serializerProvider)
            throws IOException {
        {
            String[] tempArray;

            jGen.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

            jGen.writeStartObject();

            // DataObjectGroup list sorted by ID
            jGen.writeArrayFieldStart("dataObjectGroupList");
            Set<String> sog = dataObjectPackage.getDogInDataObjectPackageIdMap().keySet();
            tempArray = sog.toArray(new String[0]);
            Arrays.sort(tempArray);
            for (String s : tempArray) {
                jGen.writeObject(dataObjectPackage.getDogInDataObjectPackageIdMap().get(s));
            }
            jGen.writeEndArray();

            // ArchiveUnit list sorted by ID
            jGen.writeArrayFieldStart("ArchiveUnitList");
            Set<String> sau = dataObjectPackage.getAuInDataObjectPackageIdMap().keySet();
            tempArray = sau.toArray(new String[0]);
            Arrays.sort(tempArray);
            for (String s : tempArray) {
                jGen.writeObject(dataObjectPackage.getAuInDataObjectPackageIdMap().get(s));
            }
            jGen.writeEndArray();

            jGen.writeObjectField("ghostRootAu", dataObjectPackage.getGhostRootAu());

            jGen.writeObjectField("vitamNormalizationStatus", dataObjectPackage.getVitamNormalizationStatus());

            jGen.writeEndObject();
        }
    }
}
