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
package fr.gouv.vitam.tools.sedalib.metadata.data;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/**
 * The Class Metadata.
 * <p>
 * Class for SEDA element Metadata.
 * <p>
 * A BinaryDataObject metadata.
 * <p>
 * Standard quote: "Propriétés techniques spécifiques du fichier en
 * fonction de sa nature technique (texte, document, image, audio, vidéo, etc.)."
 */
public class Metadata extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true)
    static final public LinkedHashMap<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<String, ComplexListMetadataKind>();
        metadataMap.put("Text", new ComplexListMetadataKind(AnyXMLListType.class, false));
        metadataMap.put("Document", new ComplexListMetadataKind(AnyXMLListType.class, false));
        metadataMap.put("Image", new ComplexListMetadataKind(AnyXMLListType.class, false));
        metadataMap.put("Audio", new ComplexListMetadataKind(AnyXMLListType.class, false));
        metadataMap.put("Video", new ComplexListMetadataKind(AnyXMLListType.class, false));
    }

    // Constructors

    /**
     * Instantiates a new file info.
     */
    public Metadata() {
        super("Metadata");
    }
}
