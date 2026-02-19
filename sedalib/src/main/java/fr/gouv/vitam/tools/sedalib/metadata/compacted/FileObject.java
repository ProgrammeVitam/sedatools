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
package fr.gouv.vitam.tools.sedalib.metadata.compacted;

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.Relationship;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class FileObject.
 * <p>
 * Class for element FileObject in compactor SEDA extension.
 * <p>
 * A FileObject metadata.
 * <p>
 * Standard quote: "Informations décrivant un fichier dans un ensemble compacté de documents.
 * Ce contenu est équivalent à celui de BinaryDataObject."
 */
public class FileObject extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = false)
    public static final Map<String, ComplexListMetadataKind> METADATA_MAP_V1;

    static {
        METADATA_MAP_V1 = new LinkedHashMap<>();
        METADATA_MAP_V1.put("Relationship", new ComplexListMetadataKind(Relationship.class, true));
        METADATA_MAP_V1.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        METADATA_MAP_V1.put("Uri", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V1.put("MessageDigest", new ComplexListMetadataKind(DigestType.class, false));
        METADATA_MAP_V1.put("Size", new ComplexListMetadataKind(IntegerType.class, false));
        METADATA_MAP_V1.put("Compressed", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V1.put("FormatIdentification", new ComplexListMetadataKind(FormatIdentification.class, false));
        METADATA_MAP_V1.put("FileInfo", new ComplexListMetadataKind(FileInfo.class, false));
        METADATA_MAP_V1.put("Metadata", new ComplexListMetadataKind(Metadata.class, false));
        METADATA_MAP_V1.put("OtherMetadata", new ComplexListMetadataKind(AnyXMLListType.class, false));
    }

    @ComplexListMetadataMap(sedaVersion = { SedaVersion.V2_2 })
    public static final Map<String, ComplexListMetadataKind> METADATA_MAP_V2;

    static {
        METADATA_MAP_V2 = new LinkedHashMap<>();
        METADATA_MAP_V2.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V2.put("Relationship", new ComplexListMetadataKind(Relationship.class, true));
        METADATA_MAP_V2.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        METADATA_MAP_V2.put("Uri", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V2.put("MessageDigest", new ComplexListMetadataKind(DigestType.class, false));
        METADATA_MAP_V2.put("Size", new ComplexListMetadataKind(IntegerType.class, false));
        METADATA_MAP_V2.put("Compressed", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V2.put("FormatIdentification", new ComplexListMetadataKind(FormatIdentification.class, false));
        METADATA_MAP_V2.put("FileInfo", new ComplexListMetadataKind(FileInfo.class, false));
        METADATA_MAP_V2.put("Metadata", new ComplexListMetadataKind(Metadata.class, false));
        METADATA_MAP_V2.put("OtherMetadata", new ComplexListMetadataKind(AnyXMLListType.class, false));
    }

    @ComplexListMetadataMap(sedaVersion = { SedaVersion.V2_3 })
    public static final Map<String, ComplexListMetadataKind> METADATA_MAP_V3;

    static {
        METADATA_MAP_V3 = new LinkedHashMap<>();
        METADATA_MAP_V3.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("Relationship", new ComplexListMetadataKind(Relationship.class, true));
        METADATA_MAP_V3.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("PersistentIdentifier", new ComplexListMetadataKind(PersistentIdentifier.class, true));
        METADATA_MAP_V3.put("DataObjectUse", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("DataObjectNumber", new ComplexListMetadataKind(IntegerType.class, false));

        METADATA_MAP_V3.put("Uri", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("MessageDigest", new ComplexListMetadataKind(DigestType.class, false));
        METADATA_MAP_V3.put("Size", new ComplexListMetadataKind(IntegerType.class, false));
        METADATA_MAP_V3.put("Compressed", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("FormatIdentification", new ComplexListMetadataKind(FormatIdentification.class, false));
        METADATA_MAP_V3.put("FileInfo", new ComplexListMetadataKind(FileInfo.class, false));
        METADATA_MAP_V3.put("Metadata", new ComplexListMetadataKind(Metadata.class, false));
        METADATA_MAP_V3.put("OtherMetadata", new ComplexListMetadataKind(AnyXMLListType.class, false));
    }

    /**
     * Instantiates a new FileObject.
     */
    public FileObject() {
        super("FileObject");
    }

    /**
     * Instantiates a new FileObject, from BinaryDataObject and URI.
     *
     * @param binaryDataObject the binary data object
     * @param uri              the URI in container
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public FileObject(BinaryDataObject binaryDataObject, String uri) throws SEDALibException {
        super("FileObject");
        for (SEDAMetadata sm : binaryDataObject.getMetadataList()) addMetadata(sm);
        if (uri != null) addMetadata(new StringType("Uri", uri));
    }
}
