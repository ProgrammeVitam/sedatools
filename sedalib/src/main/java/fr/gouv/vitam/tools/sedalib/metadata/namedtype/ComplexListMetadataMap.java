package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComplexListMetadataMap {
    public static int NON_SPECIFIC_SEDA2_VERSION=-1;

    boolean isExpandable() default false;
    int seda2Version() default NON_SPECIFIC_SEDA2_VERSION;
}
