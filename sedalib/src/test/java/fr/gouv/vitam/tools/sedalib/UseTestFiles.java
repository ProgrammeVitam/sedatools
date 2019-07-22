package fr.gouv.vitam.tools.sedalib;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import mslinks.ShellLinkException;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public interface UseTestFiles {

	    @BeforeAll
	    static void initializeTestFiles() throws IOException, ShellLinkException, SEDALibException {
	    	TestUtilities.ContructTestFiles();
	    }
}
