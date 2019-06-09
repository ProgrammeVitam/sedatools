package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.WindowsShortcut;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.eraseAll;
import static fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter.ALL_DATAOBJECTS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CSVMetadataExporterTest {

    private static boolean isLink(Path path) {
        try {
            if (Files.isSymbolicLink(path)) {
                return true;
            } else if (TestUtilities.isWindows && Files.isRegularFile(path)
                    && path.getFileName().toString().toLowerCase().endsWith(".lnk")) {
                WindowsShortcut ws = new WindowsShortcut(path.toFile());
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean compareImportAndExportDirectories(Path first, Path second) throws IOException {
        Set<String> secondListNames = new HashSet<String>();
        for (Path inSecond : Files.list(second).collect(Collectors.toList())) {
            if ((!inSecond.getFileName().startsWith("__")) && (!inSecond.getFileName().equals("metadata.csv")))
                secondListNames.add(inSecond.getFileName().toString());
        }
        for (Path firstPath : Files.list(first).collect(Collectors.toList())) {
            String filename = firstPath.getFileName().toString();
            if (filename.startsWith("__BinaryMaster") || filename.startsWith("__TextContent")) {
                String[] usageVersion = filename.substring(2, filename.lastIndexOf("__")).split("_");
                String shortUsageVersion;
                if (usageVersion[0].isEmpty())
                    shortUsageVersion = "Z";
                else
                    shortUsageVersion = usageVersion[0].substring(0, 1);
                shortUsageVersion += usageVersion[1];
                filename = filename.substring(filename.lastIndexOf("__") + 2);
                filename = filename.substring(0, filename.lastIndexOf('.')) + "_" + shortUsageVersion
                        + filename.substring(filename.lastIndexOf('.'));
            } else if (filename.startsWith("__"))
                continue;
            if ((Files.isDirectory(firstPath)) && (second.getFileSystem()!= FileSystems.getDefault()))
                filename+="/";

            Path secondPath = second.resolve(filename);
            if (filename.endsWith(".link")) {
                filename = filename.substring(0, filename.lastIndexOf(".link"));
                if (!secondListNames.contains(filename)) {
                    System.err.println("Can't find " + firstPath + " in second");
                    return false;
                }
                if (!isLink(secondPath)) {
                    System.err.println(firstPath + " in second is not a link");
                    return false;
                }
                //TODO verify redirection content
                secondListNames.remove(filename);
                continue;
            }
            if (isLink(firstPath)) {
                //TODO verify redirection content
                if (Files.exists(second.resolve(filename + ".link"))) {
                    secondListNames.remove(filename + ".link");
                    continue;
                }
                if (!isLink(secondPath)) {
                    System.err.println(firstPath + " in second is not a link");
                    return false;
                }
                secondListNames.remove(filename);
                continue;
            }
            if (!secondListNames.contains(filename)) {
                System.err.println("Can't find " + firstPath + " in second");
                return false;
            }
            if (Files.isDirectory(firstPath)) {
                if (!Files.isDirectory(secondPath)) {
                    System.err.println(firstPath + " in second is not a directory");
                    return false;
                }
                if (!compareImportAndExportDirectories(firstPath, secondPath))
                    return false;
            } else {
                if (Files.size(firstPath) != Files.size(secondPath)) {
                    System.err.println(firstPath + " in second has not same size");
                    return false;
                }
                if (!Arrays.equals(Files.readAllBytes(firstPath), Files.readAllBytes(second.resolve(filename)))) {
                    System.err.println(filename + " in second has not same content");
                    return false;
                }
            }
            secondListNames.remove(filename);
        }

        if (secondListNames.size() > 0) {
            if ((secondListNames.size() == 1) && (secondListNames.toArray()[0].equals("metadata.csv"))) {
                System.out.println("metadata.csv left in " + second);
            } else {
                System.err.println(secondListNames.size() + " left in " + second);
                for (String name : secondListNames)
                    System.err.println("->" + name);
            }
        }
        return true;
    }

    private FileSystem getZipFileSystem(String zipFileName) throws SEDALibException {
        FileSystem result=null;
        if (zipFileName != null)
            try {
                final Path path = Paths.get(zipFileName);
                final URI uri = URI.create("jar:file:" + path.toUri().getPath());

                final Map<String, String> env = new HashMap<>();
                env.put("create", "true");
                result = FileSystems.newFileSystem(uri, env);
            } catch (IOException e) {
                throw new SEDALibException(
                        "Impossible de créer le fichier zip [" + zipFileName + "]\n->" + e.getMessage());
            }
        return result;
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
        cme = new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(), "UTF8", ';', ALL_DATAOBJECTS, 0, null);
        cme.doExportToCSVDiskHierarchy("target/tmpJunit/CSVMetadataExporterDisk/metadata.csv");


        assert (compareImportAndExportDirectories(Paths.get("src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2"),
                Paths.get("target/tmpJunit/CSVMetadataExporterDisk")));
    }

    @Test
    void exportCSVOK() throws SEDALibException, InterruptedException, IOException {
        // do import of test directory
        DiskToArchiveTransferImporter di;
        di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithLinksModelV2", null);

        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        DataObjectPackageToCSVMetadataExporter cme;

        // When loaded with the csv OK test file
        eraseAll("target/tmpJunit/CSVMetadataExporterCSV");
        cme = new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(), "UTF8", ';', ALL_DATAOBJECTS, 0, null);
        cme.doExportToCSVMetadataFile("target/tmpJunit/CSVMetadataExporterCSV/metadata.csv");

        assertThat(new File("target/tmpJunit/CSVMetadataExporterCSV/metadata.csv"))
                .hasSameContentAs(new File("src/test/resources/ExpectedResults/ExportedMetadata.csv"));
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
        cme = new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(), "UTF8", ';', ALL_DATAOBJECTS, 0, null);
        cme.doExportToCSVZip("target/tmpJunit/CSVMetadataExporterZIP/export.zip");

        FileSystem zipFS=getZipFileSystem("target/tmpJunit/CSVMetadataExporterZIP/export.zip");
        assertThat(zipFS).isNotNull();

        assert (compareImportAndExportDirectories(Paths.get("src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2"),
                zipFS.getPath("/")));
    }

}
