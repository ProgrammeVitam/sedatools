package fr.gouv.vitam.tools.resip;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import mslinks.ShellLinkException;

public interface UseTestFiles {

    @BeforeAll
    static void initializeTestFiles() throws IOException, ShellLinkException, SEDALibException {
        TestUtilities.ContructTestFiles();
    }
}
