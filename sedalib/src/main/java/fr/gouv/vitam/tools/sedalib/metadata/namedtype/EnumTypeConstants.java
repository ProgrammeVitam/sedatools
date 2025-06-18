package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnumTypeConstants {
    /**
     * The EnumType different enum list by element name map.
     */
    public static final Map<String, List<String>> enumListMap;

    static {
        enumListMap = new HashMap<>();
        enumListMap.put("KeywordType", Arrays.asList("corpname", "famname", "geogname", "name",
                "occupation", "persname", "subject", "genreform", "function"));

        enumListMap.put("SigningRole", Arrays.asList("SignedDocument", "Timestamp", "Signature", "AdditionalProof"));
        enumListMap.put("DetachedSigningRole", Arrays.asList("Timestamp", "Signature", "AdditionalProof"));
        enumListMap.put("DescriptionLevel", Arrays.asList("Fonds", "Subfonds", "Class", "Collection",
                "Series", "Subseries", "RecordGrp", "SubGrp", "File", "Item", "OtherLevel"));
    }
}
