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
import java.util.Map;

/**
 * The Class FileInfo.
 * <p>
 * Class for SEDA element FileInfo.
 * <p>
 * A BinaryDataObject metadata.
 * <p>
 * Standard quote: "Propriétés techniques génériques du fichier (nom d’origine,
 * logiciel de création, système d’exploitation de création)"
 */
public class FileInfo extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put("Filename", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("CreatingApplicationName", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("CreatingApplicationVersion", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("DateCreatedByApplication", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap.put("CreatingOs", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("CreatingOsVersion", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("LastModified", new ComplexListMetadataKind(DateTimeType.class, false));
    }

    // Constructors

    /**
     * Instantiates a new file info.
     */
    public FileInfo() {
        super("FileInfo");
    }

    /**
     * Instantiates a new file info.
     *
     * @param filename                   the filename
     * @param creatingApplicationName    the creating application name
     * @param creatingApplicationVersion the creating application version
     * @param dateCreatedByApplication   the creation date by application
     * @param creatingOs                 the creating os
     * @param creatingOsVersion          the creating os version
     * @param lastModified               the last modified
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public FileInfo(String filename, String creatingApplicationName, String creatingApplicationVersion,
                     LocalDateTime dateCreatedByApplication, String creatingOs, String creatingOsVersion, FileTime lastModified) throws SEDALibException {
        super("FileInfo");
        if (filename!=null) addNewMetadata("Filename", filename);
        if (creatingApplicationName!=null) addNewMetadata("CreatingApplicationName", creatingApplicationName);
        if (creatingApplicationVersion!=null) addNewMetadata("CreatingApplicationVersion", creatingApplicationVersion);
        if (dateCreatedByApplication!=null) addNewMetadata("DateCreatedByApplication", dateCreatedByApplication);
        if (creatingOs!=null) addNewMetadata("CreatingOs", creatingOs);
        if (creatingOsVersion!=null)  addNewMetadata("CreatingOsVersion", creatingOsVersion);
        if (lastModified!=null) addNewMetadata("LastModified", lastModified.toString());
    }
}
