package fr.gouv.vitam.tools.sedalib.inout;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.WindowsShortcut;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.eraseAll;
import static fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter.ALL_DATAOBJECTS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class CSVMetadataExporterTest {

    private static final String TEMPORARY_FILE = "target/tmpJunit/CSVMetadataExporterCSV/ExportedMetadata.csv";

    private static boolean isLink(Path path) {
        try {
            if (Files.isSymbolicLink(path)) {
                return true;
            } else if (TestUtilities.isWindowsOS() && Files.isRegularFile(path)
                    && path.getFileName().toString().toLowerCase().endsWith(".lnk")) {
                WindowsShortcut ws = new WindowsShortcut(path.toFile());
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean compareTwoPathString(Path pathA, Path pathB) {
        if (Files.isDirectory(pathA) && Files.isDirectory(pathB))
            return true;
        else if (Files.isSymbolicLink(pathA) && Files.isSymbolicLink(pathB)) {
            try {
                if (!Files.readSymbolicLink(pathA).equals(Files.readSymbolicLink(pathB))) {
                    System.err.println("Different symbolic links " + pathA + " and " + pathB);
                    return false;
                }
                return true;
            } catch (IOException e) {
                System.err.println("Wrong symbolic link " + pathA + " or " + pathB);
            }
        } else if (Files.isRegularFile(pathA) && Files.isRegularFile(pathB)) {
            try {
                if (!FileUtils.contentEquals(pathA.toFile(), pathB.toFile())) {
                    System.err.println("Different file content " + pathA + " and " + pathB);
                    return false;
                }
                return true;
            } catch (IOException e) {
                System.err.println("Wrong files " + pathA + " or " + pathB);
            }
        }
        return false;
    }

    public static boolean compareDirectories(Path first, Path second) throws IOException {
        HashMap<String, Path> firstPathMap, secondPathMap;

        firstPathMap = new HashMap<>();
        secondPathMap = new HashMap<>();

        try (Stream<Path> stream = Files.walk(first)) {
            stream.forEach(p -> firstPathMap.put(first.relativize(p).toString(), p));
        }
        try (Stream<Path> stream = Files.walk(second)) {
            stream.forEach(p -> secondPathMap.put(second.relativize(p).toString(), p));
        }

        for (String firstPathString : firstPathMap.keySet()) {
            Path secondPath = secondPathMap.get(firstPathString);
            Path firstPath = firstPathMap.get(firstPathString);
            if (secondPath == null) {
                System.err.println("Can't find " + firstPath + " in second");
                return false;
            }
            if (!compareTwoPathString(firstPathMap.get(firstPathString), secondPath)) {
                System.err.println(firstPath + " and " + secondPath + " are different");
            }
        }
        for (String secondPathString : secondPathMap.keySet()) {
            Path firstPath = firstPathMap.get(secondPathString);
            Path secondPath = secondPathMap.get(secondPathString);
            if (firstPath == null) {
                System.err.println("Can't find " + secondPath + " in first");
                return false;
            }
        }
        return true;
    }

    private void unzip(String zipFile, String extractFolder) {
        try {
            int BUFFER = 2048;
            File file = new File(zipFile);

            ZipFile zip = new ZipFile(file);

            new File(extractFolder).mkdir();
            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(extractFolder, currentEntry);
                //destFile = new File(newPath, destFile.getName());
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (entry.isDirectory())
                    destFile.mkdirs();
                else {
                    BufferedInputStream is = new BufferedInputStream(zip
                            .getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte[] data = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                            BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }


            }
        } catch (Exception e) {
            System.err.println("Can't unzip " + e.getMessage());
        }
    }

    @Test
    void exportDiskOK() throws SEDALibException, InterruptedException, IOException {
        // do import of test directory
        DiskToArchiveTransferImporter di;
        di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2", null);

        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        DataObjectPackageToCSVMetadataExporter cme;

        // When loaded with the csv OK test file
        eraseAll("target/tmpJunit/CSVMetadataExporterDisk");
        cme = new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(), "UTF8", ';', ALL_DATAOBJECTS, false, 0, null);
        cme.doExportToCSVDiskHierarchy("target/tmpJunit/CSVMetadataExporterDisk", "metadata.csv");

        unzip("src/test/resources/ExpectedResults/ExportedMetadata.zip", "target/tmpJunit/CSVMetadataExporterZIP/expectedUnzip");

        // Then exported directory is equivalent to imported one
        assertThat(compareDirectories(Paths.get("target/tmpJunit/CSVMetadataExporterZIP/expectedUnzip"),
                Paths.get("target/tmpJunit/CSVMetadataExporterDisk"))).isTrue();
    }

    @Test
    void exportCSVOK() throws SEDALibException, InterruptedException, IOException {
        // do import of test directory
        DiskToArchiveTransferImporter di;
        di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2", null);

        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        DataObjectPackageToCSVMetadataExporter csvMetadataExporter;

        // When loaded with the csv OK test file
        eraseAll("target/tmpJunit/CSVMetadataExporterCSV");
        csvMetadataExporter = new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(), "UTF8", ';', ALL_DATAOBJECTS, false, 0, null);
        csvMetadataExporter.doExportToCSVMetadataFile(TEMPORARY_FILE);

        // Then verify that csv content is the expected content, except for the system dependant file separator and new lines
        String generatedFileContent = TestUtilities.SlackNormalize(FileUtils.readFileToString(new File(TEMPORARY_FILE), "UTF8"));
        String expectedFileContent = ResourceUtils.getResourceAsString("ExpectedResults/ExportedMetadata.csv");

        assertThat(generatedFileContent).isEqualToNormalizingNewlines(expectedFileContent);
    }

    @Test
    void exportCSVWithExtendedFormat() throws SEDALibException, InterruptedException, IOException {
        // do import of test directory
        DiskToArchiveTransferImporter di;
        di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2", null);

        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        DataObjectPackageToCSVMetadataExporter cme;

        // When loaded with the csv OK test file
        eraseAll("target/tmpJunit/CSVMetadataExporterCSV");
        cme = new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(), "UTF8", ';', ALL_DATAOBJECTS, true, 0, null);
        cme.doExportToCSVMetadataFile(TEMPORARY_FILE);

        // Then verify that csv content is the expected content, except for the system dependant file separator and new lines
        String generatedFileContent = TestUtilities.SlackNormalize(FileUtils.readFileToString(new File(TEMPORARY_FILE), "UTF8"));
        String expectedFileContent = ResourceUtils.getResourceAsString("ExpectedResults/ExportedMetadataWithExtendedFormat.csv");
        assertThat(generatedFileContent).isEqualToNormalizingNewlines(expectedFileContent);
    }

    @Test
    void exportZipOK() throws SEDALibException, InterruptedException, IOException {
        // do import of test directory
        DiskToArchiveTransferImporter di;
        di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2", null);

        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        DataObjectPackageToCSVMetadataExporter cme;

        // When loaded with the csv OK test file
        eraseAll("target/tmpJunit/CSVMetadataExporterZIP");
        cme = new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(), "UTF8", ';', ALL_DATAOBJECTS, false, 0, null);
        cme.doExportToCSVZip("target/tmpJunit/CSVMetadataExporterZIP/ExportedMetadata.zip", "metadata.csv");

        unzip("target/tmpJunit/CSVMetadataExporterZIP/ExportedMetadata.zip", "target/tmpJunit/CSVMetadataExporterZIP/unzip");
        unzip("src/test/resources/ExpectedResults/ExportedMetadata.zip", "target/tmpJunit/CSVMetadataExporterZIP/expectedUnzip");

        // Then exported directory in the zip is equivalent to imported one
        assertThat(compareDirectories(Paths.get("target/tmpJunit/CSVMetadataExporterZIP/expectedUnzip"),
                Paths.get("target/tmpJunit/CSVMetadataExporterZIP/unzip"))).isTrue();
    }

}
