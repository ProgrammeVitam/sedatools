package fr.gouv.vitam.tools.sedalib.inout.importer.model;


import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Line {

    public String guid;
    public String parentGUID;
    public String file;
    public List<String> objectFiles;

    /**
     * everything inside <content></content>
     */
    public String contentXMLMetadata;
    /**
     * everything inside <management></management>
     */
    public String managementXMLMetadata;
    public ArchiveUnit au;

    public Line(String guid, String parentGUID, String file, String objectFiles, String contentXMLMetadata, String managementXMLMetadata) {
        this.guid = guid;
        this.parentGUID = parentGUID;
        this.file = file;
        if (objectFiles.trim().isEmpty())
            this.objectFiles = Collections.emptyList();
        else
            this.objectFiles = Arrays.asList(objectFiles.split("\\|"));
        this.contentXMLMetadata = contentXMLMetadata;
        this.managementXMLMetadata = managementXMLMetadata;
        this.au = null;
    }
}
