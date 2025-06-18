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

import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class DiskToDataObjectPackageImporter.
 * <p>
 * Class for on disk hierarchy import in DataObjectPackage object.
 * <p>
 * The general principles are:
 * <ul>
 * <li>each directory and file in the directory imported represents one root
 * ArchiveUnit in the ArchiveTransfer</li>
 * <li>each sub-directory in the hierarchy represent an ArchiveUnit</li>
 * <li>each file represent an ArchiveUnit containing a BinaryDataObject for the
 * file itself, being the BinaryMaster_1 and with format identification
 * compliant to PRONOM register</li>
 * <li>the title of ArchiveUnit is the directory/file name, if no other metadata
 * is defined. A lambda function can also be defined to derive the title from directory/file name.</li>
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
 * <li>in a directory when there is a __ArchiveUnitMetadata.xml file, it's used to
 * define the generated ArchiveUnit metadata. At root level you can have one or
 * more XML elements that define metadata but not structure that is to say
 * &gt;ArchiveUnitProfile&lt;, &gt;Management&lt; or &gt;Content&lt;</li>
 * <li>in a directory when a file is named like
 * __USAGE_VERSION__PhysicalDataObjectMetadata.xml, USAGE being any String and
 * VERSION any integer, this file is directly considered to be a
 * PhysicalDataObject metadata with USAGE_VERSION as DataObjectVersion and then
 * is not used for generating an ArchiveUnit</li>
 * <li>in a directory when a file is named like
 * __USAGE_VERSION__BinaryDataObjectMetadata.xml, USAGE being any String and
 * VERSION any integer, this file is directly considered to define the
 * BinaryDataObject with USAGE_VERSION as DataObjectVersion metadata. It needs
 * to also have a content file named __USAGE_VERSION__XXXX. The filename in
 * metadata file is overriding the derivation from file name to define FileName
 * in BinaryDataObject</li>
 * <li>a directory named like "##xxxxx##" is used to explicitly define a
 * DataObjectGroup, so that a reference can be made</li>
 * <li>a windows shortcut or windows/linux symbolic link to a target existing
 * directory or file, which is in the imported hierarchy, is considered as a
 * reference to the ArchiveUnit or the DataObjectGroup represented by the
 * target</li>
 * </ul>
 * There are also two options in any model, that can be added to the constructors:
 * <ul>
 * <li>noLinkFlag: determine if the windows shortcut or windows/linux symbolic link are ignored (default false)</li>
 * <li>extractTitleFromFileNameFunction: define the function used to extract Title from file name (default simple copy)</li>
 * </ul>
 */
public class DiskToDataObjectPackageImporter {

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
     * The no link flag.
     */
    private boolean noLinkFlag;

    /**
     * The function used to extract Title from filename.
     */
    private Function<String, String> extractTitleFromFileNameFunction;

    /**
     * The simpleCopy function used by default to extract Title from file name.
     */
    public static final Function<String, String> simpleCopy = s -> s;

    /**
     * The progress logger.
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    private DiskToDataObjectPackageImporter(boolean noLinkFlag, Function<String, String> extractTitleFromFileNameFunction,
                                            SEDALibProgressLogger sedaLibProgressLogger) {
        this.onDiskRootPaths = new ArrayList<>();
        this.dataObjectPackage = null;
        ignorePatterns = new ArrayList<>();
        isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        this.auPathStringMap = new HashMap<>();
        this.dogPathStringMap = new HashMap<>();
        this.modelVersion = 0;
        this.noLinkFlag = noLinkFlag;
        if (extractTitleFromFileNameFunction != null)
            this.extractTitleFromFileNameFunction = extractTitleFromFileNameFunction;
        else
            this.extractTitleFromFileNameFunction = simpleCopy;

        this.inCounter = 0;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
    }

    /**
     * Instantiates a new DataObjectPackage importer from a single directory name.
     * <p>
     * It will consider each directory and each file in this directory as a root
     * ArchiveUnit or the management metadata file
     * <p>
     * It will take into account two options:
     * <ul>
     * <li>noLinkFlag: determine if the windows shortcut or windows/linux symbolic link are ignored</li>
     * <li>extractTitleFromFileNameFunction: define the function used to extract Title from file name (if null simpleCopy is used)</li>
     * </ul>
     *
     * @param directory                        the directory
     * @param noLinkFlag                       the no link flag
     * @param extractTitleFromFileNameFunction the extract title from file name function
     * @param sedaLibProgressLogger            the progress logger or null if no progress log expected
     * @throws SEDALibException if not a directory
     */
    public DiskToDataObjectPackageImporter(String directory, boolean noLinkFlag,
                                           Function<String, String> extractTitleFromFileNameFunction,
                                           SEDALibProgressLogger sedaLibProgressLogger)
            throws SEDALibException {
        this(noLinkFlag, extractTitleFromFileNameFunction, sedaLibProgressLogger);
        Path path;
        Iterator<Path> pi;

        path = Paths.get(directory).toAbsolutePath();
        if (!Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("[" + directory + "] n'est pas un répertoire");
        try (Stream<Path> sp = Files.list(path).sorted(Comparator.comparing(Path::getFileName))) {
            pi = sp.iterator();
            while (pi.hasNext())
                this.onDiskRootPaths.add(pi.next());
        } catch (IOException e) {
            throw new SEDALibException(
                    "Impossible de lister les fichiers du répertoire [" + directory + "]", e);
        }
        dataObjectPackage = new DataObjectPackage();
    }

    /**
     * Instantiates a new DataObjectPackage importer from a list of paths.
     * <p>
     * It will consider each directory and each file in this list as a root
     * ArchiveUnit or the management metadata file
     *
     * @param paths                 the paths
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     */
    public DiskToDataObjectPackageImporter(List<Path> paths, SEDALibProgressLogger sedaLibProgressLogger) {
        this(paths, false, simpleCopy, sedaLibProgressLogger);
    }

    /**
     * Instantiates a new DataObjectPackage importer from a list of paths.
     * <p>
     * It will consider each directory and each file in this list as a root
     * ArchiveUnit or the management metadata file
     * <p>
     * It will take into account two options:
     * <ul>
     * <li>noLinkFlag: determine if the windows shortcut or windows/linux symbolic link are ignored</li>
     * <li>extractTitleFromFileNameFunction: define the function used to extract Title from file name (if null simpleCopy is used)</li>
     * </ul>
     *
     * @param paths                            the paths
     * @param noLinkFlag                       the no link flag
     * @param extractTitleFromFileNameFunction the extract title from file name function
     * @param sedaLibProgressLogger            the progress logger or null if no progress log expected
     */
    public DiskToDataObjectPackageImporter(List<Path> paths, boolean noLinkFlag,
                                           Function<String, String> extractTitleFromFileNameFunction,
                                           SEDALibProgressLogger sedaLibProgressLogger) {
        this(noLinkFlag, extractTitleFromFileNameFunction, sedaLibProgressLogger);
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
     * Test if a file name is compliant to one of ignore patterns.
     *
     * @param fileName the file name string to test
     */
    private boolean mustBeIgnored(String fileName) {
        boolean doMatch = false;
        for (Pattern p : ignorePatterns) {
            if (p.matcher(fileName).matches()) {
                doMatch = true;
                break;
            }
        }
        return doMatch;
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
            } else if (Files.isRegularFile(path)
                    && path.getFileName().toString().toLowerCase().endsWith(".lnk")) {
                WindowsShortcut ws = new WindowsShortcut(path.toFile());
                lastAnalyzedLinkTarget = Paths.get(ws.getRealFilename());
                return true;
            }
        } catch (IOException | ParseException ignored) {
            //ignored
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
    public static boolean isDataObjectGroupDirectory(Path path) {
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
            xmlReader.nextUsefullEvent();
            xmlData = xmlReader.nextBlockAsStringIfNamed("ManagementMetadata");
        } catch (XMLStreamException | SEDALibException | IOException e) {
            throw new SEDALibException("Lecture des métadonnées globales à partir du fichier [" + path
                    + "] impossible", e);
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

        if (analyzeLink(path)) {
            if (noLinkFlag)
                au = null;
            else
                au = processSymbolicLink(lastAnalyzedLinkTarget);
        } else if (Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            au = processDirectory(path);
        else
            au = processFile(path, true);

        return au;
    }

    /**
     * Test if a path is in the import perimeter, to validate a link target.
     *
     * @param path the path to test
     * @return true if is in Root Paths
     */
    private boolean isInRootPaths(Path path) {
        boolean isInRootPath = false;
        path = path.toAbsolutePath();
        for (Path rootPath : onDiskRootPaths) {
            if (path.startsWith(rootPath.toAbsolutePath())) {
                isInRootPath = true;
                break;
            }
        }
        return isInRootPath;
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

        // verify it's in the original path
        if (!isInRootPaths(lastAnalyzedLinkTarget))
            throw new SEDALibException(
                    "La cible du lien [" + path.toString() + "] est hors de la hiérarchie à importer");
        au = processPath(lastAnalyzedLinkTarget);

        return au;
    }

    /**
     * Extract data object version string either usage or usage_version from file name.
     *
     * @param filename the filename
     * @return the data object version string
     */
    public static String extractDataObjectVersion(String filename) {
        String result;
        filename = filename.substring(2);
        result = filename.substring(0, filename.indexOf('_'));
        filename = filename.substring(filename.indexOf('_') + 1);
        if (filename.matches("[0-9]+__.*"))
            result += "_" + filename.substring(0, filename.indexOf("__"));
        else if (filename.matches("[0-9]+_.*"))
            result += "_" + filename.substring(0, filename.indexOf("_"));
        return result;
    }

    /**
     * Adds the PhysicalDataObject which is only metadata.
     *
     * @param path     the path
     * @param filename the file name
     * @param au       the au
     * @param dog      the DataObjectGroup containing this DataObject, if null has to be created
     * @return the data object group containing this DataObject
     * @throws SEDALibException if there's a usage_version problem (coherence between file content and file name), or access problem to metadata file
     */
    private DataObjectGroup addPhysicalDataObjectMetadata(Path path, String filename, ArchiveUnit au,
                                                          DataObjectGroup dog) throws SEDALibException {
        String dataObjectVersion = extractDataObjectVersion(filename);
        if (dog == null) {
            dog = new DataObjectGroup(dataObjectPackage, null);
            au.addDataObjectById(dog.getInDataObjectPackageId());
        }
        try {
            PhysicalDataObject pdo = new PhysicalDataObject(dataObjectPackage,
                    new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
            StringType pdoDataObjectVersion=pdo.getMetadataDataObjectVersion();
            if (pdoDataObjectVersion == null)
                pdoDataObjectVersion = new StringType("DataObjectVersion",dataObjectVersion);
            else if (!pdoDataObjectVersion.getValue().equals(dataObjectVersion))
                throw new SEDALibException(
                        "usage_version incomptabible entre le contenu du fichier [" + path.toString() + "] et son nom");
            dog.addDataObject(pdo);
        } catch (IOException e) {
            throw new SEDALibException("Impossible d'accéder au fichier [" + path.toString() + "]");
        } catch (SEDALibException e) {
            throw new SEDALibException(
                    "Problème à la lecture du fichier [" + path.toString() + "]", e);
        }
        return dog;
    }

    /**
     * Adds the BinaryDataObject metadata part.
     *
     * @param path     the path
     * @param filename the file name
     * @param au       the au
     * @param dog      the DataObjectGroup containing this DataObject, if null has to be created
     * @return the data object group containing this DataObject
     * @throws SEDALibException if there's a usage_version problem (coherence between file content and file name), or access problem to metadata file
     */
    private DataObjectGroup addBinaryDataObjectMetadata(Path path, String filename, ArchiveUnit au,
                                                        DataObjectGroup dog) throws SEDALibException {
        BinaryDataObject bdo;
        String dataObjectVersion = extractDataObjectVersion(filename);
        if (dog == null) {
            dog = new DataObjectGroup(dataObjectPackage, null);
            au.addDataObjectById(dog.getInDataObjectPackageId());
        }
        DataObject zdo = dog.findDataObjectByDataObjectVersion(dataObjectVersion);
        if (zdo == null) {
            try {
                bdo = new BinaryDataObject(dataObjectPackage, new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                StringType bdoDataObjectVersion= bdo.getMetadataDataObjectVersion();
                if (bdoDataObjectVersion == null)
                    bdo.addMetadata(new StringType("DataObjectVersion",dataObjectVersion));
                else if (!bdoDataObjectVersion.getValue().equals(dataObjectVersion))
                    throw new SEDALibException("usage_version incomptabible entre le contenu du fichier ["
                            + path.toString() + "] et son nom");
                dog.addDataObject(bdo);
            } catch (IOException e) {
                throw new SEDALibException("Impossible d'accéder au fichier [" + path.toString() + "]");
            } catch (SEDALibException e) {
                throw new SEDALibException(
                        "Problème à la lecture du fichier [" + path.toString() + "]", e);
            }
        } else if (zdo instanceof BinaryDataObject) {
            bdo = (BinaryDataObject) zdo;
            try {
                bdo.fromSedaXmlFragments(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new SEDALibException("Impossible d'accéder au fichier [" + path.toString() + "]");
            } catch (SEDALibException e) {
                throw new SEDALibException(
                        "Problème à la lecture du fichier [" + path.toString() + "]", e);
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
     * @param filename the file name
     * @param au       the au
     * @param dog      the DataObjectGroup containing this DataObject, if null has to be created
     * @return the data object group containing this DataObject
     * @throws SEDALibException if there's an access problem to binary file
     */
    public static DataObjectGroup addBinaryDataObject(DataObjectPackage dataObjectPackage, Path path, String filename, ArchiveUnit au, DataObjectGroup dog)
            throws SEDALibException {
        BinaryDataObject bdo;
        String dataObjectVersion = extractDataObjectVersion(filename);
        filename = filename.substring(dataObjectVersion.length() + 2);
        if (filename.startsWith("__"))
            filename = filename.substring(2);
        else
            filename = filename.substring(1);
        if (dog == null) {
            dog = new DataObjectGroup(dataObjectPackage, null);
            au.addDataObjectById(dog.getInDataObjectPackageId());
        }
        DataObject zdo = dog.findDataObjectByDataObjectVersion(dataObjectVersion);
        if (zdo == null) {
            bdo = new BinaryDataObject(dataObjectPackage, path, filename, dataObjectVersion);
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
        ArchiveUnit childAu;
        DataObjectGroup implicitDog = null;
        boolean auMetadataDefined = false;
        String fileName;

        // test if already analyzed
        au = getArchiveUnit(path);
        if (au != null)
            return au;

        // verify this is not a DataObjectGroup directory
        if (isDataObjectGroupDirectory(path))
            throw new SEDALibException("Le chemin [" + path.toString()
                    + "] devrait décrire un ArchiveUnit, mais décrit un DataObjectGroup");

        inCounter++;
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, inCounter, "sedalib: "+ inCounter +
                    " ArchiveUnits importées");

        String dirName = path.getFileName().toString();

        au = new ArchiveUnit(dataObjectPackage);
        au.setOnDiskPath(path);
        auPathStringMap.put(au.getOnDiskPath().toString(), au);

        try (Stream<Path> sp = Files.list(path).sorted(Comparator.comparing(Path::getFileName))) {
            // get sorted list of sub paths
            pi = sp.iterator();
            ArrayList<String> subPathStringList = new ArrayList<>(100);
            while (pi.hasNext())
                subPathStringList.add(pi.next().toString());
            Collections.sort(subPathStringList);

            for (String curPathString : subPathStringList) {
                curPath = Paths.get(curPathString);
                fileName = curPath.getFileName().toString();

                if (!Files.isDirectory(curPath) && mustBeIgnored(fileName)) {
                    continue;
                } else if (analyzeLink(curPath)) {
                    if (noLinkFlag)
                        continue;
                    // verify it's in the currently imported path
                    if (!isInRootPaths(lastAnalyzedLinkTarget))
                        throw new SEDALibException(
                                "Le lien [" + curPath.toString() + "] est hors du champ de l'import");
                    // treat specific case of DataObjectGroup directory
                    if (isDataObjectGroupDirectory(lastAnalyzedLinkTarget)) {
                        modelVersion |= 2;
                        au.addDataObjectById(processObjectGroup(lastAnalyzedLinkTarget).getInDataObjectPackageId());
                    } else {
                        childAu = processPath(lastAnalyzedLinkTarget);
                        if (childAu == null)
                            continue;
                        au.addChildArchiveUnit(childAu);
                    }
                } else if (isDataObjectGroupDirectory(curPath)) {
                    modelVersion |= 2;
                    au.addDataObjectById(processObjectGroup(curPath).getInDataObjectPackageId());
                } else if (Files.isDirectory(curPath))
                    au.addChildArchiveUnit(processDirectory(curPath));
                    // manage files either ArchiveUnit file, BinaryDataObject file, ArchiveUnit
                    // metadata file or DataObject metadata file
                else {
                    // Model V1 (GenerateurSeda) AU Content metadataXmlData file
                    if (fileName.equals("ArchiveUnitContent.xml")) {
                        modelVersion |= 1;
                        try {
                            au.setContentXmlData(new String(Files.readAllBytes(curPath), StandardCharsets.UTF_8));
                            if (!au.getContentXmlData().isEmpty())
                                auMetadataDefined = true;
                        } catch (IOException e) {
                            throw new SEDALibException("Impossible d'accéder au fichier [" + curPath.toString() + "]", e);
                        }
                        // Model V1 (GenerateurSeda) AU Management metadataXmlData file
                    } else if (fileName.equals("ArchiveUnitManagement.xml")) {
                        modelVersion |= 1;
                        try {
                            au.setManagementXmlData(new String(Files.readAllBytes(curPath), StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            throw new SEDALibException("Impossible d'accéder au fichier [" + curPath.toString() + "]", e);
                        }
                        // Model V2 (SEDALib) AU Management metadataXmlData file
                    } else if (fileName.equals("__ArchiveUnitMetadata.xml")) {
                        modelVersion |= 2;
                        try {
                            au.fromSedaXmlFragments(new String(Files.readAllBytes(curPath), StandardCharsets.UTF_8));
                            if ((au.getContentXmlData() != null) && !au.getContentXmlData().isEmpty())
                                auMetadataDefined = true;
                        } catch (IOException e) {
                            throw new SEDALibException(
                                    "Impossible de lire les métadonnées de l'ArchiveUnit depuis le fichier ["
                                            + curPath.toString() + "]", e);
                        }
                        // Model V2 (SEDALib) PhysicalDataObject metadataXmlData file
                    } else if (fileName.matches("__\\w+(_[0-9]+)?__PhysicalDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        implicitDog = addPhysicalDataObjectMetadata(curPath, fileName, au, implicitDog);
                        // Model V2 (SEDALib) BinaryDataObject metadataXmlData file
                    } else if (fileName.matches("__\\w+(_[0-9]+)?__BinaryDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        implicitDog = addBinaryDataObjectMetadata(curPath, fileName, au, implicitDog);
                        // Model V1&V2 BinaryDataObject file
                    } else if (fileName.matches("__\\w+__.+")) {
                        modelVersion |= 2;
                        implicitDog = addBinaryDataObject(dataObjectPackage, curPath, fileName, au, implicitDog);
                    } else if (fileName.matches("__\\w+_.+")) {
                        implicitDog = addBinaryDataObject(dataObjectPackage, curPath, fileName, au, implicitDog);
                    }
                    // archive file except if conform to ignore patterns
                    else {
                        au.addChildArchiveUnit(processFile(curPath, false));
                    }
                }

            }
            if (!auMetadataDefined)
                au.setDefaultContent(extractTitleFromFileNameFunction.apply(dirName), "RecordGrp");
        } catch (IOException e) {
            throw new SEDALibException(
                    "Impossible de parcourir le répertoire [" + path.toString() + "]", e);
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
    private ArchiveUnit processFile(Path path, boolean testIgnorePatterns) throws SEDALibException, InterruptedException {
        String filename;
        ArchiveUnit au;
        DataObjectGroup dog;
        BinaryDataObject bdo;

        // test if already analyzed
        au = getArchiveUnit(path);
        if (au != null)
            return au;

        filename = path.getFileName().toString();
        // verify not to be ignored
        if (testIgnorePatterns && mustBeIgnored(filename))
            return null;

        // verify that the file is not one with special meaning
        if (filename.equals("ArchiveUnitContent.xml") || filename.equals("ArchiveUnitManagement.xml")
                || filename.equals("__ArchiveUnitMetadata.xml") || filename.matches("__\\w+__.+"))
            throw new SEDALibException("Le chemin [" + path.toString() + "] a la racine de l'import n'a pas de sens");

        inCounter++;
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, inCounter, "sedalib: " + inCounter +
                    " ArchiveUnits importées");

        dog = new DataObjectGroup(dataObjectPackage, path);
        bdo = new BinaryDataObject(dataObjectPackage, path, null, "BinaryMaster_1");
        au = new ArchiveUnit(dataObjectPackage);
        au.setOnDiskPath(path);
        au.setDefaultContent(extractTitleFromFileNameFunction.apply(path.getFileName().toString()), "Item");

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

        try (Stream<Path> sp = Files.list(path).sorted(Comparator.comparing(Path::getFileName))) {
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
                    if (fileName.matches("__\\w+(_[0-9]+)?__PhysicalDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        addPhysicalDataObjectMetadata(curPath, fileName, null, dog);
                        // Model V2 (SEDALib) BinaryDataObject metadataXmlData file
                    } else if (fileName.matches("__\\w+(_[0-9]+)?__BinaryDataObjectMetadata.xml")) {
                        modelVersion |= 2;
                        addBinaryDataObjectMetadata(curPath, fileName, null, dog);
                        // Model V1&V2 BinaryDataObject file
                    } else if (fileName.matches("__\\w+__.+")) {
                        addBinaryDataObject(dataObjectPackage, curPath, fileName, null, dog);
                    } else
                        throw new SEDALibException("Le chemin [" + path.toString() + "] ne décrit pas un DataObject");

                }
            }
        } catch (IOException e) {
            throw new SEDALibException("Impossible de parcourir le répertoire [" + path.toString() + "]", e);
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
        Path nextPath = null;
        ArchiveUnit au;
        start = Instant.now();

        try (Stream<Path> sp = onDiskRootPaths.stream()) {
            inCounter=0;
            pi = sp.iterator();
            while (pi.hasNext()) {
                nextPath = pi.next();
                if (nextPath.getFileName().toString().equals("__ManagementMetadata.xml")) {
                    dataObjectPackage.setManagementMetadataXmlData(processManagementMetadata(nextPath));
                    continue;
                }
                au = processPath(nextPath);
                if (au != null)
                    dataObjectPackage.addRootAu(au);
            }
            doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP, "sedalib: " + inCounter + " métadonnées ArchiveUnits importées dans le DataObjectPackage", null);
        } catch (SEDALibException e) {
            throw new SEDALibException("Impossible d'importer les ressources du répertoire ["
                    + nextPath.toString() + "]",e);
        }

        inCounter = 0;
        for (Map.Entry<String, BinaryDataObject> pair : dataObjectPackage.getBdoInDataObjectPackageIdMap().entrySet()) {
            FileInfo fileInfo= pair.getValue().getMetadataFileInfo();
            if (fileInfo.getSimpleMetadata("LastModified") == null)
                pair.getValue().extractTechnicalElements(sedaLibProgressLogger);
            inCounter++;
            doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, inCounter, "sedalib: " + inCounter +
                        " fichiers BinaryDataObject analysés");
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP, "sedalib: "+ inCounter + " fichiers BinaryDataObject analysés et importés dans le DataObjectPackage", null);
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
        String result = dataObjectPackage.getDescription() + "\n";
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
