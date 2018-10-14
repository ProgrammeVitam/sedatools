/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA dataObjectPackage the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.inout.importer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.ProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

/**
 * The Class DiskToDataObjectPackageImporter.
 * <p>
 * Class for on disk hierarchy import in DataObjectPackage object.
 * <p>
 * The general principles are:
 * <ul>
 * <li>the directory imported contains all the directories and files that
 * represent one root ArchiveUnit in the ArchiveTransfer</li>
 * <li>each sub-directory in the hierarchy represent an ArchiveUnit</li>
 * <li>each file represent an ArchiveUnit containing a BinaryDataObject for the
 * file itself, being the BinaryMaster_1 and with format identification
 * compliant to PRONOM register</li>
 * <li>the title of ArchiveUnit is the directory/file name, if no other metadata
 * is defined</li>
 * </ul>
 * For example, if you import this disk hierarchy:
 * <p>
 * RootDir
 * <p>
 * |---TestDir1
 * <p>
 * |---NodeFile1.1.jpg
 * <p>
 * |---NodeFile2.pdf
 * <p>
 * the ArchiveTransfer will contain:
 * <p>
 * ArchiveUnit (Title="TestDir1")
 * <p>
 * |---ArchiveUnit (Title="NodeFile1.1.jpg")
 * <p>
 * ---|---BinaryDataObject(DataObjectVersion="BinaryMaster_1"
 * FileName="NodeFile1.1.jpg"...)
 * <p>
 * |--- ArchiveUnit (Title="NodeFile2.pdf")
 * <p>
 * ---|---BinaryDataObject(DataObjectVersion="BinaryMaster_1"
 * FileName="NodeFile2.pdf"...)
 * <p>
 * but for adding metadata definitions, link to ArchiveUnits and
 * DataobjectGroups... there are two models of description that's better to use
 * exclusively.
 * <p>
 * Model V1-Historical GenerateurSEDA model
 * <ul>
 * <li>in a directory when there is a ArchiveUnitContent.xml file, it's used to
 * define the generated ArchiveUnit metadata described in the Content XML
 * element</li>
 * <li>in a directory when there is a ArchiveUnitManagement.xml file, it's used
 * to define the generated ArchiveUnit metadata described in the Management XML
 * element</li>
 * <li>in a directory when a file begins by __USAGE_VERSION_, USAGE being any
 * String and VERSION any integer, this file is directly considered to be a
 * BinaryDataObject with USAGE_VERSION as DataObjectVersion and then is not used
 * for generating an ArchiveUnit</li>
 * <li>a windows shortcut to a target existing directory or file, which is in
 * the imported hierarchy, is considered as a reference to the ArchiveUnit
 * represented by the target</li>
 * </ul>
 * <p>
 * Model V2-Extended model
 * <ul>
 * <li>in a directory when there is a ArchiveUnitMetadata.xml file, it's used to
 * define the generated ArchiveUnit metadata. At root level you can have one or
 * more XML elements that define metadata but not structure that is to say
 * &gt;ArchiveUnitProfile&lt;, &gt;Management&lt; or &gt;Content&lt;</li>
 * <li>in a directory when a file is named like
 * __USAGE_VERSION_PhysicalDataObjectMetadata.xml, USAGE being any String and
 * VERSION any integer, this file is directly considered to be a
 * PhysicalDataObject metadata with USAGE_VERSION as DataObjectVersion and then
 * is not used for generating an ArchiveUnit</li>
 * <li>in a directory when a file is named like
 * __USAGE_VERSION_BinaryDataObjectMetadata.xml, USAGE being any String and
 * VERSION any integer, this file is directly considered to define the
 * BinaryDataObject with USAGE_VERSION as DataObjectVersion metadata. It needs
 * to also have a content file named __USAGE_VERSION_XXXX. The filename in
 * metadata file is overriding the derivation from file name to define FileName
 * in BinaryDataObject</li>
 * <li>a directory named like "##xxxxx##" is used to explicitly define a
 * DataObjectGroup, so that a reference can be made</li>
 * <li>a windows shortcut or windows/linux symbolic link to a target existing
 * directory or file, which is in the imported hierarchy, is considered as a
 * reference to the ArchiveUnit or the DataObjectGroup represented by the
 * target</li>
 * </ul>
 */
public class DiskToDataObjectPackageImporter {

    /**
     * The on disk SIP directory.
     */
    private Path currentDiskImportDirectory;

    /**
     * The on disk root paths.
     */
    private List<Path> onDiskRootPaths;

    /**
     * The archive transfer.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * The ignore patterns.
     */
    private List<Pattern> ignorePatterns;

    /**
     * The is windows.
     */
    private boolean isWindows;

    /**
     * The ArchiveUnit path string map, used to manage symbolic and shortcut links.
     */
    private HashMap<String, ArchiveUnit> auPathStringMap;

    /**
     * The DataObjectGroup path string map, used to manage symbolic and shortcut
     * links.
     */
    private HashMap<String, DataObjectGroup> dogPathStringMap;

    /**
     * The last analyzed link target.
     */
    private Path lastAnalyzedLinkTarget;

    /**
     * The model version, V1 or V2.
     */
    private int modelVersion;

    /**
     * The inCounter, used for ArchiveUnit process count.
     */
    private int inCounter;

    /**
     * The start and end instants, for duration computation.
     */
    private Instant start, end;

    /**
     * The progress logger.
     */
    private ProgressLogger progressLogger;

    /**
     * Instantiates a new DataObjectPackage importer.
     *
     * @param progressLogger the progress logger or null if no progress log expected
     */
    private DiskToDataObjectPackageImporter(ProgressLogger progressLogger) {
        this.currentDiskImportDirectory = null;
        this.onDiskRootPaths = new ArrayList<Path>();
        this.dataObjectPackage = null;
        ignorePatterns = new ArrayList<Pattern>();
        isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        this.auPathStringMap = new HashMap<String, ArchiveUnit>();
        this.dogPathStringMap = new HashMap<String, DataObjectGroup>();
        this.modelVersion = 0;

        this.inCounter = 0;
        this.progressLogger = progressLogger;
    }

    /**
     * Instantiates a new DataObjectPackage importer from a single directory name.
     * <p>
     * It will consider each directory and each file in this directory as a root
     * ArchiveUnit or the management metadata file
     *
     * @param directory      the directory
     * @param progressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException if not a directory
     */
    public DiskToDataObjectPackageImporter(String directory, ProgressLogger progressLogger)
            throws SEDALibException {
        this(progressLogger);
        Path path;
        Iterator<Path> pi;

        path = Paths.get(directory).toAbsolutePath();
        if (!Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("[" + directory + "] n'est pas un répertoire");
        try (Stream<Path> sp = Files.list(path)) {
            pi = sp.iterator();
            while (pi.hasNext())
                this.onDiskRootPaths.add(pi.next());
        } catch (IOException e) {
            throw new SEDALibException(
                    "Impossible de lister les fichiers du répertoire [" + directory + "]\n->" + e.getMessage());
        }
        dataObjectPackage = new DataObjectPackage();
    }

    /**
     * Instantiates a new DataObjectPackage importer from a list of paths.
     * <p>
     * It will consider each directory and each file in this list as a root
     * ArchiveUnit or the management metadata file
     *
     * @param paths          the paths
     * @param progressLogger the progress logger or null if no progress log expected
     */
    public DiskToDataObjectPackageImporter(List<Path> paths, ProgressLogger progressLogger) {
        this(progressLogger);
        this.onDiskRootPaths = paths;
        dataObjectPackage = new DataObjectPackage();
    }

    /**
     * Adds the ignore pattern.
     *
     * @param patternString the pattern string
     */
    public void addIgnorePattern(String patternString) {
        ignorePatterns.add(Pattern.compile(patternString));
    }

    /**
     * Analyze windows shortcut and linux or windows symbolic links and get target.
     * <p>
     * NB: Use a specific Java class, slightly corrected, to analyse links. The
     * library used to generate links (mslinks) in tests has some bugs when reading,
     * and then is not used here...
     *
     * @param path the path
     * @return true, if successful
     */
    private boolean analyzeLink(Path path) {
        try {
            if (Files.isSymbolicLink(path)) {
                lastAnalyzedLinkTarget = path.toRealPath();
                return true;
            } else if (isWindows && Files.isRegularFile(path)
                    && path.getFileName().toString().toLowerCase().endsWith(".lnk")) {
                WindowsShortcut ws = new WindowsShortcut(path.toFile());
                lastAnalyzedLinkTarget = Paths.get(ws.getRealFilename());
                return true;
            }
        } catch (Exception ignored) {
        }
        lastAnalyzedLinkTarget = null;
        return false;
    }

    /**
     * Checks if is data object group directory.
     *
     * @param path the path
     * @return true, if is data object group directory
     */
    private boolean isDataObjectGroupDirectory(Path path) {
        return Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)
                && path.getFileName().toString().startsWith("##") && path.getFileName().toString().endsWith("##");
    }

    /**
     * Process the GlobalMetadata file.
     *
     * @param path the path
     * @return the ArchiveUnit generated from path
     * @throws SEDALibException any import exception
     */
    private String processManagementMetadata(Path path) throws SEDALibException {
        String xmlData;

        try (FileInputStream bais = new FileInputStream(path.toFile());
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            xmlData = xmlReader.nextBlockAsStringIfNamed("ManagementMetadata");
        } catch (XMLStreamException | SEDALibException | IOException e) {
            throw new SEDALibException("Lecture des métadonnées globales à partir du fichier [" + path
                    + "] impossible\n->" + e.getMessage());
        }
        return xmlData;
    }

    /**
     * Process the path by calling methods convenient for the path nature, and then
     * generate the represented ArchiveUnit.
     *
     * @param path the path
     * @return the ArchiveUnit generated from path
     * @throws SEDALibException     any import exception
     * @throws InterruptedException if import process is interrupted
     */
    private ArchiveUnit processPath(Path path) throws SEDALibException, InterruptedException {
        ArchiveUnit au;

        // test if already analyzed
        au = getArchiveUnit(path);
        if (au != null)
            return au;

        if (analyzeLink(path))
            au = processSymbolicLink(lastAnalyzedLinkTarget);
        else if (Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            au = processDirectory(path);
        else
            au = processFile(path);

        return au;
    }

    /**
     * Process the symbolic link to generate the represented ArchiveUnit.
     *
     * @param path the path
     * @return the ArchiveUnit generated from path
     * @throws SEDALibException     any import exception
     * @throws InterruptedException if import process is interrupted
     */
    private ArchiveUnit processSymbolicLink(Path path) throws SEDALibException, InterruptedException {
        ArchiveUnit au;

        analyzeLink(path);
        // verify it's in the original path
        if (!lastAnalyzedLinkTarget.toAbsolutePath().startsWith(currentDiskImportDirectory.toAbsolutePath()))
            throw new SEDALibException(
                    "La cible du lien [" + path.toString() + "] est hors de la hiérarchie à importer");
        au = processPath(lastAnalyzedLinkTarget);

        return au;
    }

    /**
     * Adds the PhysicalDataObject which is only metadata.
     *
     * @param path     the path
     * @param fileName the file name
     * @param au       the au
     * @param dog      the DataObjectGroup containing this DataObject, if null has to be created
     * @return the data object group containing this DataObject
     * @throws SEDALibException if there's a usage_version problem (coherence between file content and file name), or access problem to metadata file
     */
    private DataObjectGroup addPhysicalDataObjectMetadata(Path path, String fileName, ArchiveUnit au,
                                                          DataObjectGroup dog) throws SEDALibException {
        fileName = fileName.substring(2);
        String usage = fileName.substring(0, fileName.indexOf('_'));
        fileName = fileName.substring(fileName.indexOf('_') + 1);
        String version = fileName.substring(0, fileName.indexOf('_'));
        if (dog == null) {
            dog = new DataObjectGroup(dataObjectPackage, null);
            au.addDataObjectById(dog.getInDataObjectPackageId());
        }
        try {
            PhysicalDataObject pdo = new PhysicalDataObject(dataObjectPackage,
                    new String(Files.readAllBytes(path), "UTF-8"));
            if (pdo.dataObjectVersion == null)
                pdo.dataObjectVersion = usage + "_" + version;
            else if (!pdo.dataObjectVersion.equals(usage + "_" + version))
                throw new SEDALibException(
                        "usage_version incomptabible entre le contenu du fichier [" + path.toString() + "] et son nom");
            dog.addDataObject(pdo);
        } catch (IOException e) {
            throw new SEDALibException("Impossible d'accéder au fichier [" + path.toString() + "]");
        } catch (SEDALibException e) {
            throw new SEDALibException(
                    "Problème à la lecture du fichier [" + path.toString() + "]\n->" + e.getMessage());
        }
        return dog;
    }

    /**
     * Adds the BinaryDataObject metadata part.
     *
     * @param path     the path
     * @param fileName the file name
     * @param au       the au
     * @param dog      the DataObjectGroup containing this DataObject, if null has to be created
     * @return the data object group containing this DataObject
     * @throws SEDALibException if there's a usage_version problem (coherence between file content and file name), or access problem to metadata file
     */
    private DataObjectGroup addBinaryDataObjectMetadata(Path path, String fileName, ArchiveUnit au,
                                                        DataObjectGroup dog) throws SEDALibException {
        BinaryDataObject bdo;
        fileName = fileName.substring(2);
        String usage = fileName.substring(0, fileName.indexOf('_'));
        fileName = fileName.substring(fileName.indexOf('_') + 1);
        String version = fileName.substring(0, fileName.indexOf('_'));
        if (dog == null) {
            dog = new DataObjectGroup(dataObjectPackage, null);
            au.addDataObjectById(dog.getInDataObjectPackageId());
        }
        DataObject zdo = dog.findDataObjectByDataObjectVersion(usage + "_" + version);
        if (zdo == null) {
            try {
                bdo = new BinaryDataObject(dataObjectPackage, new String(Files.readAllBytes(path), "UTF-8"));
                if (bdo.dataObjectVersion == null)
                    bdo.dataObjectVersion = usage + "_" + version;
                else if (!bdo.dataObjectVersion.equals(usage + "_" + version))
                    throw new SEDALibException("usage_version incomptabible entre le contenu du fichier ["
                            + path.toString() + "] et son nom");
                dog.addDataObject(bdo);
            } catch (IOException e) {
                throw new SEDALibException("Impossible d'accéder au fichier [" + path.toString() + "]");
            } catch (SEDALibException e) {
                throw new SEDALibException(
                        "Problème à la lecture du fichier [" + path.toString() + "]\n->" + e.getMessage());
            }
        } else if (zdo instanceof BinaryDataObject) {
            bdo = (BinaryDataObject) zdo;
            try {
                bdo.fromSedaXmlFragments(new String(Files.readAllBytes(path), "UTF-8"));
            } catch (IOException e) {
                throw new SEDALibException("Impossible d'accéder au fichier [" + path.toString() + "]");
            } catch (SEDALibException e) {
                throw new SEDALibException(
                        "Problème à la lecture du fichier [" + path.toString() + "]\n->" + e.getMessage());
            }
        } else
            throw new SEDALibException("Les métadonnées du BinaryDataObject en [" + path.toString()
                    + "] s'applique un usage_version utilisé par un autre type de DataObject");
        return dog;
    }

    /**
     * Adds the BinaryDataObject.
     *
     * @param path     the path
     * @param fileName the file name
     * @param au       the au
     * @param dog      the DataObjectGroup containing this DataObject, if null has to be created
     * @return the data object group containing this DataObject
     * @throws SEDALibException if there's an access problem to binary file
     */
    private DataObjectGroup addBinaryDataObject(Path path, String fileName, ArchiveUnit au, DataObjectGroup dog)
            throws SEDALibException {
        BinaryDataObject bdo;
        fileName = fileName.substring(2);
        String usage = fileName.substring(0, fileName.indexOf('_'));
        fileName = fileName.substring(fileName.indexOf('_') + 1);
        String version = fileName.substring(0, fileName.indexOf('_'));
        fileName = fileName.substring(fileName.indexOf('_') + 1);
        if (dog == null) {
            dog = new DataObjectGroup(dataObjectPackage, null);
            au.addDataObjectById(dog.getInDataObjectPackageId());
        }
        DataObject zdo = dog.findDataObjectByDataObjectVersion(usage + "_" + version);
        if (zdo == null) {
            bdo = new BinaryDataObject(dataObjectPackage, path, fileName, usage + "_" + version);
            dog.addDataObject(bdo);
        } else if (zdo instanceof BinaryDataObject) {
            bdo = (BinaryDataObject) zdo;
            if (bdo.getOnDiskPath() != null)
                throw new SEDALibException("Un autre BinaryDataObject que [" + path.toString()
                        + "] a été créé avec le même usage_version");
            bdo.setOnDiskPath(path);
        } else
            throw new SEDALibException("Le BinaryDataObject en [" + path.toString()
                    + "] a un usage_version utilisé par un autre type de DataObject");
        return dog;
    }

    /**
     * Process the directory to generate the represented ArchiveUnit.
     *
     * @param path the path
     * @return the ArchiveUnit generated from path
     * @throws SEDALibException     any import exception
     * @throws InterruptedException if import process is interrupted
     */
    private ArchiveUnit processDirectory(Path path) throws SEDALibException, InterruptedException {
        Path curPath;
        Iterator<Path> pi;
        ArchiveUnit au;
        DataObjectGroup implicitDog = null;
        boolean auMetadataDefined = false;

        // test if already analyzed
        au = getArchiveUnit(path);
        if (au != null)
            return au;

        // verify this is not a DataObjectGroup directory
        if (isDataObjectGroupDirectory(path))
            throw new SEDALibException("Le chemin [" + path.toString()
                    + "] devrait décrire un ArchiveUnit, mais décrit un DataObjectGroup");

        inCounter++;
        if (progressLogger!=null)
            progressLogger.progressLogIfStep(ProgressLogger.OBJECTS_GROUP, inCounter, Integer.toString(inCounter) +
                " ArchiveUnits importées");

        String dirName = path.getFileName().toString();

        au = new ArchiveUnit(dataObjectPackage);
        au.setOnDiskPath(path);
        auPathStringMap.put(au.getOnDiskPath().toString(), au);

        try (Stream<Path> sp = Files.list(path)) {
            // get sorted list of sub paths
            pi = sp.iterator();
            ArrayList<String> subPathStringList = new ArrayList<String>(100);
            while (pi.hasNext())
                subPathStringList.add(pi.next().toString());
            Collections.sort(subPathStringList);

            for (String curPathString : subPathStringList) {
                curPath = Paths.get(curPathString);

                if (analyzeLink(curPath)) {
                    // verify it's in the currently imported path
                    if (!lastAnalyzedLinkTarget.toAbsolutePath()
                            .startsWith(currentDiskImportDirectory.toAbsolutePath()))
                        throw new SEDALibException(
                                "Le lien est hors du champ de l'import [" + curPath.toString() + "]");
                    // treat specific case of DataObjectGroup directory
                    if (isDataObjectGroupDirectory(lastAnalyzedLinkTarget)) {
                        modelVersion |= 2;
                        au.addDataObjectById(processObjectGroup(lastAnalyzedLinkTarget).getInDataObjectPackageId());
                    } else
                        au.addChildArchiveUnit(processPath(lastAnalyzedLinkTarget));

                } else if (isDataObjectGroupDirectory(curPath)) {
                    modelVersion |= 2;
                    au.addDataObjectById(processObjectGroup(curPath).getInDataObjectPackageId());
                } else if (Files.isDirectory(curPath))
                    au.addChildArchiveUnit(processDirectory(curPath));
                    // manage files either ArchiveUnit file, BinaryDataObject file, ArchiveUnit
                    // metadata file or DataObject metadata file
                else {
                    String fileName = curPath.getFileName().toString();
                    // Model V1 (GenerateurSeda) AU Content metadataXmlData file
                    if (fileName.equals("ArchiveUnitContent.xml")) {
                        modelVersion |= 1;
                        try {
                            au.setContentXmlData(new String(Files.readAllBytes(curPath), "UTF-8"));
                        } catch (IOException e) {
                            throw new SEDALibException("Impossible d'accéder au fichier [" + curPath.toString() + "]");
                        }
                        // Model V1 (GenerateurSeda) AU Management metadataXmlData file
                    } else if (fileName.equals("ArchiveUnitManagement.xml")) {
                        modelVersion |= 1;
                        try {
                            au.setManagementXmlData(new String(Files.readAllBytes(curPath), "UTF-8"));
                        } catch (IOException e) {
                            throw new SEDALibException("Impossible d'accéder au fichier [" + curPath.toString() + "]");
                        }
                        // Model V2 (SEDALib) AU Management metadataXmlData file
                    } else if (fileName.equals("__ArchiveUnitMetadata.xml")) {
                        modelVersion |= 2;
                        try {
                            au.fromSedaXmlFragments(new String(Files.readAllBytes(curPath), "UTF-8"));
                            auMetadataDefined = true;
                        } catch (IOException e) {
                            throw new SEDALibException(
                                    "Impossible de lire les métadonnées de l'ArchiveUnit depuis le fichier ["
                                            + curPath.toString() + "]");
                        }
                        // Model V2 (SEDALib) PhysicalDataObject metadataXmlData file
                    } else if (fileName.matches("__.+_[0-9]+_PhysicalDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        implicitDog = addPhysicalDataObjectMetadata(curPath, fileName, au, implicitDog);
                        // Model V2 (SEDALib) BinaryDataObject metadataXmlData file
                    } else if (fileName.matches("__.+_[0-9]+_BinaryDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        implicitDog = addBinaryDataObjectMetadata(curPath, fileName, au, implicitDog);
                        // Model V1&V2 BinaryDataObject file
                    } else if (fileName.matches("__.+_[0-9]+_.+")) {
                        implicitDog = addBinaryDataObject(curPath, fileName, au, implicitDog);
                    }
                    // archive file except if conform to ignore patterns
                    else {
                        boolean doMatch = false;
                        for (Pattern p : ignorePatterns) {
                            if (p.matcher(fileName).matches()) {
                                doMatch = true;
                                break;
                            }
                        }
                        if (!doMatch)
                            au.addChildArchiveUnit(processFile(curPath));
                    }
                }

            }
            if ((!auMetadataDefined) && (au.getContent() == null))
                au.setDefaultContentXmlData(dirName, "RecordGrp");
        } catch (IOException e) {
            throw new SEDALibException(
                    "Impossible de parcourir le répertoire [" + path.toString() + "]->" + e.getMessage());
        }
        return au;
    }

    /**
     * Process the file.
     * <p>
     * When a file is imported
     * <p>
     * - an ArchiveUnit is created with the name of the file as title and empty
     * description
     * <p>
     * - the file is defined as BinaryMaster_1 BinaryDataObject
     *
     * @param path the path
     * @return the archive unit
     * @throws SEDALibException     any import exception
     * @throws InterruptedException if import process is interrupted
     */
    // - an ObjectGroup is associated with the file as BinaryMaster object
    private ArchiveUnit processFile(Path path) throws SEDALibException, InterruptedException {
        String filename;
        ArchiveUnit au;
        DataObjectGroup dog;
        BinaryDataObject bdo;

        // test if already analyzed
        au = getArchiveUnit(path);
        if (au != null)
            return au;

        // verify that the file is not one with special meaning
        filename = path.getFileName().toString();
        if (filename.equals("ArchiveUnitContent.xml") || filename.equals("ArchiveUnitManagement.xml")
                || filename.equals("__ArchiveUnitMetadata.xml") || filename.matches("__.+_[0-9]+_.+"))
            throw new SEDALibException("Le chemin [" + path.toString() + "] a la racine de l'import n'a pas de sens");

        inCounter++;
        if (progressLogger!=null)
            progressLogger.progressLogIfStep(ProgressLogger.OBJECTS_GROUP, inCounter, Integer.toString(inCounter) +
                " ArchiveUnits importées");

        dog = new DataObjectGroup(dataObjectPackage, path);
        bdo = new BinaryDataObject(dataObjectPackage, path, null, "BinaryMaster_1");
        au = new ArchiveUnit(dataObjectPackage);
        au.setOnDiskPath(path);
        au.setDefaultContentXmlData(path.getFileName().toString(), "Item");

        auPathStringMap.put(au.getOnDiskPath().toString(), au);
        dogPathStringMap.put(dog.getOnDiskPath().toString(), dog);
        dog.addDataObject(bdo);
        au.addDataObjectById(dog.getInDataObjectPackageId());

        return au;
    }

    /**
     * Process the data object group directory.
     *
     * @param path the path
     * @return the data object group
     * @throws SEDALibException any import exception
     */
    private DataObjectGroup processObjectGroup(Path path) throws SEDALibException {
        Path curPath;
        Iterator<Path> pi;
        DataObjectGroup dog;

        dog = getDataObjectGroup(path);
        if (dog != null)
            return dog;

        dog = new DataObjectGroup(dataObjectPackage, path);
        dogPathStringMap.put(dog.getOnDiskPath().toString(), dog);

        try (Stream<Path> sp = Files.list(path)) {
            pi = sp.iterator();
            while (pi.hasNext()) {
                curPath = pi.next();
                if (Files.isSymbolicLink(curPath))
                    throw new SEDALibException(
                            "Le lien [" + path.toString() + "] ne peut pas pointer vers un BinaryDataObject");
                else if (Files.isDirectory(curPath))
                    throw new SEDALibException("Un répertoire de DataObjectGroup ne peut pas contenir un répertoire ["
                            + path.toString() + "]");
                else {
                    String fileName = curPath.getFileName().toString();
                    // Model V2 (SEDALib) PhysicalDataObject metadataXmlData file
                    if (fileName.matches("__.+_[0-9]+_PhysicalDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        addPhysicalDataObjectMetadata(curPath, fileName, null, dog);
                        // Model V2 (SEDALib) BinaryDataObject metadataXmlData file
                    } else if (fileName.matches("__.+_[0-9]+_BinaryDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        addBinaryDataObjectMetadata(curPath, fileName, null, dog);
                        // Model V1&V2 BinaryDataObject file
                    } else if (fileName.matches("__.+_[0-9]+_.+")) {
                        addBinaryDataObject(curPath, fileName, null, dog);
                    } else
                        throw new SEDALibException("Le chemin [" + path.toString() + "] ne décrit pas un DataObject");

                }
            }
        } catch (IOException e) {
            throw new SEDALibException("Impossible de parcourir le répertoire [" + path.toString() + "]");
        }
        return dog;
    }

    /**
     * Do import the disk structure to DataObjectPackage. It will import from a
     * directory content or from a list of files or directories:
     * <ul>
     * <li>ManagementMetadata XML element at the end of DataObjectPackage from the
     * __ManagementMetadata.xml file</li>
     * <li>each root ArchiveUnit from a sub directory, and recursively all the
     * DataObjectPackage structure (see {@link DiskToDataObjectPackageImporter} for
     * details)</li>
     * </ul>
     *
     * @throws SEDALibException     any import exception
     * @throws InterruptedException if export process is interrupted
     */
    public void doImport() throws SEDALibException, InterruptedException {
        Iterator<Path> pi;
        Path nextPath;
        ArchiveUnit au;
        start = Instant.now();

        try (Stream<Path> sp = onDiskRootPaths.stream()) {
            pi = sp.iterator();
            while (pi.hasNext()) {
                nextPath = pi.next();
                if (nextPath.getFileName().toString().equals("__ManagementMetadata.xml")) {
                    dataObjectPackage.setManagementMetadataXmlData(processManagementMetadata(nextPath));
                    continue;
                }
                currentDiskImportDirectory = nextPath;
                au = processPath(currentDiskImportDirectory);
                dataObjectPackage.addRootAu(au);
            }
            if (progressLogger!=null)
                progressLogger.progressLog(ProgressLogger.OBJECTS_GROUP, Integer.toString(inCounter) + " ArchiveUnits importées");
        } catch (SEDALibException e) {
            throw new SEDALibException("Impossible d'importer les ressources du répertoire ["
                    + currentDiskImportDirectory.toString() + "]\n->" + e.getMessage());
        }

        inCounter = 0;
        for (Map.Entry<String, BinaryDataObject> pair : dataObjectPackage.getBdoInDataObjectPackageIdMap().entrySet()) {
            if (pair.getValue().fileInfo.lastModified == null)
                pair.getValue().extractTechnicalElements(progressLogger);
            inCounter++;
            if (progressLogger!=null)
                progressLogger.progressLogIfStep(ProgressLogger.OBJECTS_GROUP, inCounter, Integer.toString(inCounter) +
                    " BinaryDataObject analysés");
        }
        if (progressLogger!=null)
            progressLogger.progressLog(ProgressLogger.OBJECTS_GROUP, Integer.toString(inCounter) + " BinaryDataObject analysés");
        end = Instant.now();
    }

    /**
     * Gets the DataObjectPackage.
     *
     * @return the DataObjectPackage
     */
    public DataObjectPackage getDataObjectPackage() {
        return dataObjectPackage;
    }

    /**
     * Gets the model version.
     *
     * @return the model version
     */
    public int getModelVersion() {
        return modelVersion;
    }

    /**
     * Gets the archive unit.
     *
     * @param path the path
     * @return the archive unit
     */
    private ArchiveUnit getArchiveUnit(Path path) {
        return auPathStringMap.get(path.toAbsolutePath().normalize().toString());
    }

    /**
     * Gets the data object group.
     *
     * @param path the path
     * @return the data object group
     */
    private DataObjectGroup getDataObjectGroup(Path path) {
        return dogPathStringMap.get(path.toAbsolutePath().normalize().toString());
    }

    /**
     * Gets the summary of the import process.
     *
     * @return the summary
     */
    public String getSummary() {
        String result = "Import d'une structure d'archives depuis une hiérarchie sur disque\n";
        result += "en [";
        boolean first = true;
        for (Path path : onDiskRootPaths) {
            if (first)
                first = false;
            else
                result += ",\n";
            result += path.toString();
        }
        result += "]\n";
        result += dataObjectPackage.getDescription() + "\n";
        switch (getModelVersion()) {
            case 0:
                result += "encodé selon un modèle neutre de la structure\n";
                break;
            case 1:
                result += "encodé selon un modèle V1 de la structure\n";
                break;
            case 2:
                result += "encodé selon un modèle V2 de la structure\n";
                break;
            case 3:
                result += "encodé selon un modèle hybride V1/V2 de la structure\n";
                break;
        }
        if ((start != null) && (end != null))
            result += "chargé en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}