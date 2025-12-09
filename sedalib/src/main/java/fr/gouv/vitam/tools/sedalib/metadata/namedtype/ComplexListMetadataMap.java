package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComplexListMetadataMap {
    boolean isExpandable() default false;
    SedaVersion[] sedaVersion() default {};
}
