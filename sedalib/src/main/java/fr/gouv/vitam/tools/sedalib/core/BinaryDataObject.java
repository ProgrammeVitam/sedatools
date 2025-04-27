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
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The Class BinaryDataObject.
 * <p>
 * Class for SEDA element BinaryDataObject. It contains metadata and links to
 * the binary file.
 */
public class BinaryDataObject extends DataObjectPackageIdElement implements DataObject, ComplexListInterface {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(seda2Version = {1})
    public static final Map<String, ComplexListMetadataKind> metadataMap_v1;

    static {
        metadataMap_v1 = new LinkedHashMap<>();
        metadataMap_v1.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        metadataMap_v1.put("DataObjectGroupReferenceId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("DataObjectGroupId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        metadataMap_v1.put("Uri", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("MessageDigest", new ComplexListMetadataKind(DigestType.class, false));
        metadataMap_v1.put("Size", new ComplexListMetadataKind(IntegerType.class, false));
        metadataMap_v1.put("Compressed", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("FormatIdentification", new ComplexListMetadataKind(FormatIdentification.class, false));
        metadataMap_v1.put("FileInfo", new ComplexListMetadataKind(FileInfo.class, false));
        metadataMap_v1.put("Metadata", new ComplexListMetadataKind(Metadata.class, false));
        metadataMap_v1.put("OtherMetadata", new ComplexListMetadataKind(AnyXMLListType.class, false));
    }

    @ComplexListMetadataMap(seda2Version = {2})
    public static final Map<String, ComplexListMetadataKind> metadataMap_v2;

    static {
        metadataMap_v2 = new LinkedHashMap<>();
        metadataMap_v2.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        metadataMap_v2.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        metadataMap_v2.put("Uri", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("MessageDigest", new ComplexListMetadataKind(DigestType.class, false));
        metadataMap_v2.put("Size", new ComplexListMetadataKind(IntegerType.class, false));
        metadataMap_v2.put("Compressed", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("FormatIdentification", new ComplexListMetadataKind(FormatIdentification.class, false));
        metadataMap_v2.put("FileInfo", new ComplexListMetadataKind(FileInfo.class, false));
        metadataMap_v2.put("Metadata", new ComplexListMetadataKind(Metadata.class, false));
        metadataMap_v2.put("OtherMetadata", new ComplexListMetadataKind(AnyXMLListType.class, false));
    }

    @ComplexListMetadataMap(seda2Version = {3})
    public static final Map<String, ComplexListMetadataKind> metadataMap_v3;

    static {
        metadataMap_v3 = new LinkedHashMap<>();
        metadataMap_v3.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        metadataMap_v3.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("PersistentIdentifier", new ComplexListMetadataKind(PersistentIdentifier.class, true));
        metadataMap_v3.put("DataObjectUse", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("DataObjectNumber", new ComplexListMetadataKind(IntegerType.class, false));

        metadataMap_v3.put("Uri", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("MessageDigest", new ComplexListMetadataKind(DigestType.class, false));
        metadataMap_v3.put("Size", new ComplexListMetadataKind(IntegerType.class, false));
        metadataMap_v3.put("Compressed", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("FormatIdentification", new ComplexListMetadataKind(FormatIdentification.class, false));
        metadataMap_v3.put("FileInfo", new ComplexListMetadataKind(FileInfo.class, false));
        metadataMap_v3.put("Metadata", new ComplexListMetadataKind(Metadata.class, false));
        metadataMap_v3.put("OtherMetadata", new ComplexListMetadataKind(AnyXMLListType.class, false));
    }

    public final static LinkedHashMap<String, ComplexListMetadataKind>[] metadataMaps = new LinkedHashMap[SEDA2Version.MAX_SUPPORTED_VERSION + 1];
    public final static Boolean[] notExpandables = new Boolean[SEDA2Version.MAX_SUPPORTED_VERSION + 1];

    static {
        ComplexListInterface.initMetadataMaps(BinaryDataObject.class, metadataMaps, notExpandables);
    }

    /**
     * The metadata list.
     */
    private List<SEDAMetadata> metadataList;

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public LinkedHashMap<String, ComplexListMetadataKind> getMetadataMap() throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind> result = metadataMaps[SEDA2Version.getSeda2Version()];
        if (result == null)
            result = metadataMaps[0];
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public boolean isNotExpandable() throws SEDALibException {
        Boolean result = notExpandables[SEDA2Version.getSeda2Version()];
        if (result == null)
            result = notExpandables[0];
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SEDAMetadata> getMetadataList() {
        return metadataList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMetadataList(List<SEDAMetadata> metadataList) {
        this.metadataList = metadataList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String getXmlElementName() {
        return "BinaryDataObject";
    }

    // Inner element
    /**
     * The DataObjectGroup in which is the BinaryDataObject, or null.
     */
    @JsonIgnore
    private DataObjectGroup dataObjectGroup;

    // Constructors

    /**
     * Instantiates a new BinaryDataObject.
     */
    public BinaryDataObject() {
        this(null, null, null, null);
    }

    /**
     * Instantiates a new BinaryDataObject.
     *
     * @param dataObjectPackage the DataObjectPackage
     */
    public BinaryDataObject(DataObjectPackage dataObjectPackage) {
        this(dataObjectPackage, null, null, null);
    }

    /**
     * Adds filename metadata to the metadata list.
     * If an explicit filename is provided, it will be used,
     * otherwise the filename will be extracted from the path if available.
     *
     * @param path             The file path to extract filename from if no explicit name provided
     * @param explicitFilename The explicit filename to use, or null to use path filename
     */
    private void addFilenameMetadata(Path path, String explicitFilename) {
        String nameValue = explicitFilename != null
                ? explicitFilename
                : (path != null ? path.getFileName().toString() : null);
        if (nameValue == null) {
            return;
        }
        FileInfo fileInfo = new FileInfo();
        try {
            fileInfo.addNewMetadata("Filename", nameValue);
        } catch (SEDALibException ignored) {
        }
        metadataList.add(fileInfo);
    }

    /**
     * Instantiates a new BinaryDataObject.
     * <p>
     * If DataObjectPackage is defined the new ArchiveUnit is added with a generated
     * uniqID in the structure.
     *
     * @param dataObjectPackage the DataObjectPackage containing the
     *                          BinaryDataObject
     * @param path              the path defining the BinaryDataObject onDisk
     *                          representation or null
     * @param explicitFilename  the filename metadata
     * @param dataObjectVersion the DataObjectVersion
     */
    public BinaryDataObject(DataObjectPackage dataObjectPackage, Path path, String explicitFilename, String dataObjectVersion) {
        super(dataObjectPackage);
        this.metadataList = new ArrayList<>(17);

        if (dataObjectVersion != null)
            metadataList.add(new StringType("DataObjectVersion", dataObjectVersion));
        addFilenameMetadata(path, explicitFilename);
        this.onDiskPath = (path != null ? path.toAbsolutePath().normalize() : null);
        this.dataObjectGroup = null;
        if (dataObjectPackage != null)
            try {
                dataObjectPackage.addBinaryDataObject(this);
            } catch (SEDALibException ignored) {
                // impossible as the uniqID is generated by the called function.
            }
    }

    /**
     * Instantiates a new BinaryDataObject, giving the metadata in XML fragments
     * format.
     * <p>
     * If DataObjectPackage is defined the new ArchiveUnit is added with a generated
     * uniqID in the structure.
     * <p>
     * Fragment sample: <code>
     * &lt;DataObjectProfile&gt;DataObject1&lt;DataObjectProfile&gt; // only in SEDA 2.2
     * &lt;DataObjectVersion&gt;BinaryMaster_1&lt;DataObjectVersion&gt;
     * &lt;Uri&gt;content/ID37.zip&lt;/Uri&gt;
     * &lt;MessageDigest algorithm="SHA-512"&gt;
     * 4723e0f6f8d54cda8989ffa5809318b2369b4b0c7957deda8399c311c397c026cc1511a0494d6f8e7b474e20171c40a5d40435c95841820a08a92e844b960947
     * &lt;/MessageDigest&gt;
     * &lt;Size&gt;1466&lt;/Size&gt;
     * &lt;FormatIdentification&gt;
     * &lt;FormatLitteral&gt;ZIP Format&lt;/FormatLitteral&gt;
     * &lt;MimeType&gt;application/zip&lt;/MimeType&gt;
     * &lt;FormatId&gt;x-fmt/263&lt;/FormatId&gt;
     * &lt;/FormatIdentification&gt;
     * &lt;FileInfo&gt;
     * &lt;Filename&gt;OK-RULES-MDRULES.zip&lt;/Filename&gt;
     * &lt;LastModified&gt;2018-08-28T19:22:19Z&lt;/LastModified&gt;
     * &lt;/FileInfo&gt;
     * </code>
     *
     * @param dataObjectPackage the DataObjectPackage
     * @param xmlData           the raw XML content describing this BinaryDataObject
     *                          in manifest but without DataObjectGroup ID or RefID
     *                          information
     * @throws SEDALibException if any xmlData reading exception
     */
    public BinaryDataObject(DataObjectPackage dataObjectPackage, String xmlData) throws SEDALibException {
        this(dataObjectPackage);
        fromSedaXmlFragments(xmlData);
    }

    // Methods

    /**
     * Gets the extension of a file name.
     *
     * @param fileName the file name
     * @return the extension
     */
    private static String getExtension(String fileName) {
        if (fileName == null)
            return "";
        int i = fileName.lastIndexOf('.');
        return i < 0 ? "seda" : fileName.substring(i + 1);
    }

    private static final String SHA512_ALGORITHM = "SHA-512";
    private static final long SMALL_FILE_THRESHOLD = 2 * 1024 * 1024; // 10Mo

    /**
     * Computes the message digest (hash) for a file.
     *
     * @param digest The MessageDigest instance to use for computing the hash
     * @param path   The path to the file to hash
     * @return The computed digest bytes
     * @throws SEDALibException if an error occurs reading the file
     */
    private static byte[] computeDigest(MessageDigest digest, Path path) throws SEDALibException {
        try {
            long size = Files.size(path);

            if (size <= SMALL_FILE_THRESHOLD) {
                byte[] all = Files.readAllBytes(path);
                digest.update(all);
                return digest.digest();
            }

            // Quicker on big files
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
                digest.update(buffer);
                return digest.digest();
            }

        } catch (IOException e) {
            throw new SEDALibException(
                    String.format("Impossible de calculer le hash du fichier [%s]", path), e);
        }
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     *
     * @param bytes The byte array to convert
     * @return The hexadecimal string representation of the bytes
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Gets the digest sha 512.
     *
     * @param path the path of the file to hash
     * @return the digest sha 512
     * @throws SEDALibException if unable to get digest
     */
    public static String getDigestSha512(Path path) throws SEDALibException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(SHA512_ALGORITHM);
            byte[] hash = computeDigest(digest, path);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new SEDALibException("Impossible de mobiliser l'algorithme de hashage " + SHA512_ALGORITHM, e);
        }
    }

    /**
     * Updates the FileInfo metadata for a binary data object.
     * If no FileInfo exists, creates a new one. Sets the filename from the onDiskPath if not already set.
     * Updates the last modified timestamp.
     *
     * @param lastModifiedTime The last modified timestamp to set
     * @throws SEDALibException if unable to add metadata
     */
    private void updateFileInfo(FileTime lastModifiedTime) throws SEDALibException {
        String filename;

        FileInfo fileInfo = getMetadataFileInfo();
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            filename = null;
        } else
            filename = fileInfo.getSimpleMetadata("Filename");

        if (filename == null) {
            filename = onDiskPath.getFileName().toString();
            fileInfo.addNewMetadata("Filename", filename);
        }

        fileInfo.addNewMetadata("LastModified", lastModifiedTime.toString());
        addMetadata(fileInfo);
    }

    /**
     * Identifies the format of a file using DROID.
     *
     * @param logger The progress logger for warnings
     * @param path   The path to the file to identify
     * @return The DROID identification result, or null if identification failed
     */
    private IdentificationResult identifyFormat(SEDALibProgressLogger logger, Path path) {
        try {
            return DroidIdentifier.getInstance().getIdentificationResult(path);
        } catch (SEDALibException e) {
            doProgressLogWithoutInterruption(
                    logger,
                    OBJECTS_WARNINGS,
                    "sedalib: impossible de faire l'identification Droid pour le fichier [" + path + "]",
                    e
            );
            return null;
        }
    }

    /**
     * Extract technical elements (lastmodified date, size, format, digest...) from
     * file and complete the BinaryDataObject metadata.
     *
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException if unable to get size or lastmodified date (probably
     *                          can't access file)
     */
    public void extractTechnicalElements(SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException {
        long size;
        FileTime lastModifiedTime;
        try {
            size = Files.size(onDiskPath);
            lastModifiedTime = Files.getLastModifiedTime(onDiskPath);
        } catch (IOException e) {
            throw new SEDALibException("Impossible d'obtenir les infos techniques pour le fichier ["
                    + onDiskPath.toString() + "]", e);
        }

        updateFileInfo(lastModifiedTime);
        addMetadata(new DigestType("MessageDigest", getDigestSha512(onDiskPath), "SHA-512"));
        addMetadata(new IntegerType("Size", size));

        IdentificationResult idResult = identifyFormat(sedaLibProgressLogger, onDiskPath);
        if (idResult != null)
            addMetadata(new FormatIdentification(idResult.getName(), idResult.getMimeType(), idResult.getPuid(), null));
        else
            addMetadata(new FormatIdentification("Unknown", null, "UNKNOWN", null));
    }

    /**
     * Change null values of String to "non défini".
     *
     * @param a a String
     * @return the transformed string
     */
    private String undefined(String a) {
        return a == null ? "non défini" : a;
    }

    // SEDA XML exporter

    private void finalizeUri() throws SEDALibException {
        FileInfo fileInfo = getMetadataFileInfo();
        String tmpUri = "content/" + inDataPackageObjectId;
        if (fileInfo != null) {
            String tmp = fileInfo.getSimpleMetadata("Filename");
            tmpUri += (tmp != null ? "." + getExtension(tmp) : "");
        }
        addMetadata(new StringType("Uri", tmpUri));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.core.DataObject#toSedaXml(fr.gouv.vitam.tools.
     * sedalib.xml.SEDAXMLStreamWriter)
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter, SEDALibProgressLogger sedaLibProgressLogger)
            throws InterruptedException, SEDALibException {
        finalizeUri();
        // XML write
        try {
            xmlWriter.writeStartElement("BinaryDataObject");
            xmlWriter.writeAttributeIfNotEmpty("id", inDataPackageObjectId);
            toSedaXmlMetadataList(xmlWriter);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException(
                    "Erreur d'écriture XML dans un élément [" + getXmlElementName() + "]", e);
        }

        int counter = getDataObjectPackage().getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, OBJECTS_GROUP, counter,
                "sedalib: " + counter + " métadonnées DataObject exportées");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.sedalib.core.DataObject#toSedaXmlFragments()
     */
    @Override
    public String toSedaXmlFragments() throws SEDALibException {
        String result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
                toSedaXml(xmlWriter, null);
            }
            result = baos.toString(StandardCharsets.UTF_8);
        } catch (SEDALibException | XMLStreamException | IOException e) {
            throw new SEDALibException("Erreur interne d'écriture du BinaryDataObject", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SEDALibException("Interruption du process", e);
        }
        if (result != null) {
            if (result.isEmpty())
                result = null;
            else {
                result = result.replaceFirst("<BinaryDataObject .*>", "");
                result = result.substring(1, result.lastIndexOf("</BinaryDataObject>") - 1);
            }
        }
        return result;
    }

    // SEDA XML importer

    /**
     * Read the BinaryDataObject element content in XML expected form from the SEDA
     * Manifest in the ArchiveTransfer. Utility methods for fromSedaXml and
     * fromSedaXmlFragments
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    private void setFromXmlContent(SEDAXMLEventReader xmlReader)
            throws SEDALibException {
        try {
            metadataList = new ArrayList<>(18);
            fillFromSedaXmlMetadataList(xmlReader);
        } catch (SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML du BinaryDataObject", e);
        }
    }

    /**
     * Import the BinaryDataObject in XML expected form from the SEDA Manifest in
     * the DataObjectPackage.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage     the DataObjectPackage to be completed
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read BinaryDataObject, or null if not a BinaryDataObject
     * @throws SEDALibException     if the XML can't be read or the SEDA scheme is
     *                              not respected
     * @throws InterruptedException if export process is interrupted
     */
    public static BinaryDataObject fromSedaXml(SEDAXMLEventReader xmlReader, DataObjectPackage dataObjectPackage,
                                               SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        BinaryDataObject bdo = null;
        DataObjectGroup dog;
        String tmp;
        try {
            tmp = xmlReader.peekAttributeBlockIfNamed("BinaryDataObject", "id");
            if (tmp != null) {
                bdo = new BinaryDataObject();
                bdo.inDataPackageObjectId = tmp;
                dataObjectPackage.addBinaryDataObject(bdo);
                xmlReader.nextUsefullEvent();
                bdo.setFromXmlContent(xmlReader);
                xmlReader.endBlockNamed("BinaryDataObject");
            }
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture dans le BinaryDataObject"
                    + (bdo != null ? " [" + bdo.inDataPackageObjectId + "]" : ""), e);
        }
        //case not a BinaryDataObject
        if (bdo == null)
            return null;

        StringType dataObjectGroupId = (StringType) bdo.getFirstNamedMetadata("DataObjectGroupId");
        StringType dataObjectGroupReferenceId = (StringType) bdo.getFirstNamedMetadata("DataObjectGroupReferenceId");

        if ((dataObjectGroupId != null) && (dataObjectGroupReferenceId != null))
            throw new SEDALibException("Eléments DataObjectGroupReferenceId et DataObjectGroupId incompatibles");
        if (dataObjectGroupId != null) {
            if (dataObjectPackage.getDataObjectGroupById(dataObjectGroupId.getValue()) != null)
                throw new SEDALibException("Elément DataObjectGroup [" + dataObjectGroupId.getValue() + "] déjà créé");
            if (dataObjectGroupId.getValue() == null)
                throw new SEDALibException("Elément DataObjectGroup vide");
            dog = new DataObjectGroup();
            dog.setInDataObjectPackageId(dataObjectGroupId.getValue());
            dataObjectPackage.addDataObjectGroup(dog);
            dog.addDataObject(bdo);
            doProgressLog(sedaLibProgressLogger, OBJECTS_WARNINGS, "sedalib: dataObjectGroup [" + dog.inDataPackageObjectId
                    + "] créé depuis BinaryDataObject [" + bdo.inDataPackageObjectId + "]", null);
        } else if (dataObjectGroupReferenceId != null) {
            if (dataObjectGroupReferenceId.getValue() == null)
                throw new SEDALibException("Elément DataObjectGroupReferenceId vide");
            dog = dataObjectPackage.getDataObjectGroupById(dataObjectGroupReferenceId.getValue());
            if (dog == null)
                throw new SEDALibException("Erreur de référence au DataObjectGroup [" + dataObjectGroupReferenceId.getValue() + "]");
            dog.addDataObject(bdo);
        }
        bdo.removeFirstNamedMetadata("DataObjectGroupReferenceId");
        bdo.removeFirstNamedMetadata("DataObjectGroupId");

        int counter = dataObjectPackage.getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, OBJECTS_GROUP, counter, "sedalib: " + counter + " métadonnées DataObject importées");
        return bdo;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.core.DataObject#fromSedaXmlFragments(java.lang.
     * String)
     */
    public void fromSedaXmlFragments(String fragments) throws SEDALibException {
        BinaryDataObject bdo = new BinaryDataObject();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(fragments.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            // jump StartDocument
            xmlReader.nextUsefullEvent();
            bdo.setFromXmlContent(xmlReader);
            XMLEvent event = xmlReader.xmlReader.peek();
            if (!event.isEndDocument())
                throw new SEDALibException("Il y a des champs illégaux");
        } catch (XMLStreamException | SEDALibException | IOException e) {
            throw new SEDALibException("Erreur de lecture du BinaryDataObject", e);
        }

        metadataList = bdo.metadataList;
    }

    /**
     * Gets the FileInfo metadata from the metadata list.
     *
     * @return the FileInfo metadata, or null if not found
     */
    @JsonIgnore
    public FileInfo getMetadataFileInfo() {
        return (FileInfo) getFirstNamedMetadata("FileInfo");
    }

    /**
     * Gets the DataObjectVersion metadata from the metadata list.
     *
     * @return the DataObjectVersion metadata, or null if not found
     */
    @JsonIgnore
    public StringType getMetadataDataObjectVersion() {
        return (StringType) getFirstNamedMetadata("DataObjectVersion");
    }

    /**
     * Gets the Uri metadata from the metadata list.
     *
     * @return the Uri metadata, or null if not found
     */
    @JsonIgnore
    public StringType getMetadataUri() {
        return (StringType) getFirstNamedMetadata("Uri");
    }

    /**
     * Gets the Size metadata from the metadata list.
     *
     * @return the Size metadata, or null if not found
     */
    @JsonIgnore
    public IntegerType getMetadataSize() {
        return (IntegerType) getFirstNamedMetadata("Size");
    }

    /**
     * Gets the MessageDigest metadata from the metadata list.
     *
     * @return the MessageDigest metadata, or null if not found
     */
    @JsonIgnore
    public DigestType getMetadataMessageDigest() {
        return (DigestType) getFirstNamedMetadata("MessageDigest");
    }

    /**
     * Gets the FormatIdentification metadata from the metadata list.
     *
     * @return the FormatIdentification metadata, or null if not found
     */
    @JsonIgnore
    public FormatIdentification getMetadataFormatIdentification() {
        return (FormatIdentification) getFirstNamedMetadata("FormatIdentification");
    }

// Getters and setters

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.sedalib.core.DataObject#getOg()
     */
    public DataObjectGroup getDataObjectGroup() {
        return dataObjectGroup;
    }

    /**
     * Sets the dataObjectGroup.
     *
     * @param dataObjectGroup the new DataObjectGroup
     */
    public void setDataObjectGroup(DataObjectGroup dataObjectGroup) {
        this.dataObjectGroup = dataObjectGroup;
    }

}
