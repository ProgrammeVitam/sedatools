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
package fr.gouv.vitam.tools.sedalib.metadata.compacted;

import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataMap;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class Document.
 * <p>
 * Class for element Document in compactor SEDA extension.
 * <p>
 * A Document metadata.
 * <p>
 * Standard quote: "Informations décrivant un document dans un ensemble compacté de documents.
 * Ce contenu est extrait d'un ArchiveUnit de type Item à la racine ou rattaché à un RecordGroup."
 */
public class RecordGrp extends ComplexListType {

    /**
     * The element name.
     */
    private static final String ELEMENT_NAME = "RecordGrp";

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true)
    public static final Map<String, ComplexListMetadataKind> metadataMap_default;

    static {
        metadataMap_default = new LinkedHashMap<>();//NOSONAR public mandatory for ComplexlistType mechanism
        metadataMap_default.put("RecordGrpID",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap_default.put("Management", new ComplexListMetadataKind(Management.class, false));
        metadataMap_default.put("Content", new ComplexListMetadataKind(Content.class, false));
        metadataMap_default.put(ELEMENT_NAME, new ComplexListMetadataKind(RecordGrp.class, true));
    }

    /**
     * Instantiates a new RecordGrp.
     */
    public RecordGrp() {
        super(ELEMENT_NAME);
    }

    /**
     * Instantiates a new RecordGrp, from RecordGrpID and content.
     *
     * @param recordGrpID the record group id
     * @param content     the content
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public RecordGrp(String recordGrpID, Content content) throws SEDALibException {
        super(ELEMENT_NAME);

        addNewMetadata("RecordGrpID", recordGrpID);
        addMetadata(content);
    }
}
