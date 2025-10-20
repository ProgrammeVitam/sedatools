package fr.gouv.vitam.tools.sedalib;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SedaContextExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SedaContext.setVersion(SedaVersion.V2_1);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SedaContext.setVersion(null);
    }
}

