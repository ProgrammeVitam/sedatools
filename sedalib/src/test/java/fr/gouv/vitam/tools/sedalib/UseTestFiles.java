package fr.gouv.vitam.tools.sedalib;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public interface UseTestFiles {

    @BeforeAll
    static void initializeTestFiles() throws IOException, SEDALibException {
        TestUtilities.ContructTestFiles();
    }
}
