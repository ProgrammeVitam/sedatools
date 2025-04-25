package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComplexListMetadataMap {
    boolean isExpandable() default false;
    int[] seda2Version() default {-1}; // NON_SPECIFIC_SEDA2_VERSION
}
