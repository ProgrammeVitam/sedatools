package fr.gouv.vitam.tools.sedalib.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceUtils {

    private static final String FILE_NOT_FOUND_IN_RESOURCES = "File not found in Resources: ";
    private static final String SECURITY_IN_RESOURCES = "Security violation in Resources: ";

    /**
     * Get the InputStream representation from the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated File
     * @throws FileNotFoundException if the resource file not found
     */
    public static InputStream getResourceAsStream(String resourcesFile) throws FileNotFoundException {
        if (resourcesFile == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES);
        }
        InputStream stream = null;
        try {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcesFile);
        } catch (final SecurityException e) {
            throw new FileNotFoundException( SECURITY_IN_RESOURCES + resourcesFile);
        }
        if (stream == null) {
            try {
                stream = ResourceUtils.class.getClassLoader().getResourceAsStream(resourcesFile);
            } catch (final SecurityException e) {
                throw new FileNotFoundException( SECURITY_IN_RESOURCES + resourcesFile);
            }
        }
        if (stream == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        return stream;
    }


    /**
     * Get the File representation from the local path to the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated File
     * @throws FileNotFoundException if the resource file not found
     */
    public static File getResourceFile(String resourcesFile) throws FileNotFoundException {
        if (resourcesFile == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        URL url;
        try {
            url = ResourceUtils.class.getClassLoader().getResource(resourcesFile);
        } catch (final SecurityException e) {
            throw new FileNotFoundException(SECURITY_IN_RESOURCES + resourcesFile);
        }
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(resourcesFile);
        }
        if (url == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        File file;
        try {
            file = new File(url.toURI());
        } catch (final URISyntaxException e) {
            file = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
        }
        if (file.exists()) {
            return file;
        }
        throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
    }

    /**
     * Get the Path representation from the local path to the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated Path
     * @throws FileNotFoundException if resource file not found
     */
    public static Path getResourcePath(String resourcesFile) throws FileNotFoundException {
        return getResourceFile(resourcesFile).toPath();
    }

    /**
     * Get the String content from the Resources directory
     *
     * @param resourcesFile properties file from resources directory
     * @return the associated File content as a String
     * @throws FileNotFoundException if the resource file not found
     */
    public static String getResourceAsString(String resourcesFile) throws FileNotFoundException {
        if (resourcesFile == null) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES);
        }
        String fileAsString = null;
        try {
            fileAsString = new String(Files.readAllBytes(getResourcePath(resourcesFile)));
        } catch (final SecurityException e) {
            throw new FileNotFoundException( SECURITY_IN_RESOURCES + resourcesFile);
        } catch (final IOException e) {
            throw new FileNotFoundException(FILE_NOT_FOUND_IN_RESOURCES + resourcesFile);
        }
        return fileAsString;
    }

}
