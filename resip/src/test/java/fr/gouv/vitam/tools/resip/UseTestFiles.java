package fr.gouv.vitam.tools.resip;

import fr.gouv.vitam.tools.resip.utils.ResipException;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The interface Use test files.
 */
public interface UseTestFiles {

    /**
     * Initialize test files.
     *
     * @throws IOException    the io exception
     * @throws ResipException the resip exception
     */
    @BeforeAll
    static void initializeTestFiles() throws IOException, ResipException {
        TestUtilities.ContructTestFiles();
        Path dir = Paths.get("./target/tmpJunit");
        Files.createDirectories(dir);
    }
}
