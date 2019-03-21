package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComplexListMetadataMap {
    boolean isExpandable() default false;
}
