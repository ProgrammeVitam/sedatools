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
package fr.gouv.vitam.tools.sedalib.metadata.content;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataMap;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;
import java.util.Map;

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

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put("Spatial", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("Temporal", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("Juridictional", new ComplexListMetadataKind(StringType.class, true));
    }

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
     * @throws SEDALibException if sub element construction is not possible (not supposed to occur)
     */
    public Coverage(String elementName, String spatial, String temporal, String juridictional) throws SEDALibException {
        super(elementName);

        if (spatial != null) addNewMetadata("Spatial", spatial);
        if (temporal != null) addNewMetadata("Temporal", temporal);
        if (juridictional != null) addNewMetadata("Juridictional", juridictional);
    }
}
