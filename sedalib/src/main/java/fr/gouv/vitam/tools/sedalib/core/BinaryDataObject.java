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
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The Class BinaryDataObject.
 * <p>
 * Class for SEDA element BinaryDataObject. It contains metadata and links to
 * the binary file.
 */
public class BinaryDataObject extends DataObjectPackageIdElement implements DataObject {

    // SEDA elements

    /**
     * The data object system id.
     */
    public StringType dataObjectSystemId;

    /**
     * The data object group system id.
     */
    public StringType dataObjectGroupSystemId;

    /**
     * The relationships xml element in String form.
     */
    public List<AnyXMLType> relationshipsXmlData;

    /**
     * The data object group reference id.
     */
    public StringType dataObjectGroupReferenceId;

    /**
     * The data object group id.
     */
    public StringType dataObjectGroupId;

    /**
     * The data object version.
     */
    public StringType dataObjectVersion;

    // Attachment
    // - binary content in xml not supported by SEDALib

    /**
     * The uri.
     */
    public StringType uri;

    /**
     * The message digest.
     */
    public DigestType messageDigest;

    /**
     * The size.
     */
    public IntegerType size;

    /**
     * The compressed.
     */
    public StringType compressed;

    /**
     * The format identification.
     */
    public FormatIdentification formatIdentification;

    /**
     * The file info.
     */
    public FileInfo fileInfo;

    /**
     * The metadata.
     */
    public Metadata metadata;

    /**
     * The otherMetadata xml element not supported.
     */

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
     * Instantiates a new BinaryDataObject.
     * <p>
     * If DataObjectPackage is defined the new ArchiveUnit is added with a generated
     * uniqID in the structure.
     *
     * @param dataObjectPackage the DataObjectPackage containing the
     *                          BinaryDataObject
     * @param path              the path defining the BinaryDataObject onDisk
     *                          representation or null
     * @param filename          the filename metadata
     * @param dataObjectVersion the DataObjectVersion
     */
    public BinaryDataObject(DataObjectPackage dataObjectPackage, Path path, String filename, String dataObjectVersion) {
        super(dataObjectPackage);
        this.dataObjectSystemId = null;
        this.dataObjectGroupSystemId = null;
        this.relationshipsXmlData = new ArrayList<AnyXMLType>();
        this.dataObjectGroupReferenceId = null;
        this.dataObjectGroupId = null;
        this.dataObjectVersion = new StringType("DataObjectVersion", dataObjectVersion);
        this.uri = null;
        this.messageDigest = null;
        this.size = null;
        this.compressed = null;
        this.formatIdentification = null;
        if (filename == null) {
            if (path != null) {
                this.fileInfo = new FileInfo();
                try {
                    this.fileInfo.addNewMetadata("Filename", path.getFileName().toString());
                } catch (SEDALibException ignored) {
                }
            } else
                this.fileInfo = null;
        } else {
            this.fileInfo = new FileInfo();
            try {
                this.fileInfo.addNewMetadata("Filename", filename);
            } catch (SEDALibException ignored) {
            }
        }
        this.metadata = null;

        if (path == null)
            this.onDiskPath = null;
        else
            this.onDiskPath = path.toAbsolutePath().normalize();
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
     * &lt;DataObjectVersion&gt;BinaryMaster_1_DataObjectVersion&gt;
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

    /**
     * Gets the digest sha 512.
     *
     * @return the digest sha 512
     * @throws SEDALibException if unable to get digest
     */
    static public String getDigestSha512(Path path) throws SEDALibException {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e1) {
            throw new SEDALibException("Impossible de mobiliser l'algorithme de hashage SHA-512", e1);
        }

        try (InputStream is = new BufferedInputStream(Files.newInputStream(path))) {
            final byte[] buffer = new byte[4096];
            for (int read; (read = is.read(buffer)) != -1; ) {
                messageDigest.update(buffer, 0, read);
            }
        } catch (Exception e) {
            throw new SEDALibException(
                    "Impossible de calculer le hash du fichier [" + path.toString() + "]", e);
        }

        // Convert the byte to hex format
        try (Formatter formatter = new Formatter()) {
            for (final byte b : messageDigest.digest()) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (Exception e) {
            throw new SEDALibException(
                    "Impossible d'encoder le hash du fichier [" + path.toString() + "]", e);
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
        IdentificationResult ir = null;
        String lfilename = null;
        long lsize;
        FileTime llastModified;

        if (fileInfo != null)
            lfilename = fileInfo.getSimpleMetadata("Filename");
        try {
            lsize = Files.size(onDiskPath);
            if (lfilename == null)
                lfilename = onDiskPath.getFileName().toString();
            llastModified = Files.getLastModifiedTime(onDiskPath);
        } catch (IOException e) {
            throw new SEDALibException("Impossible de générer les infos techniques pour le fichier ["
                    + onDiskPath.toString() + "]", e);
        }

        messageDigest = new DigestType("MessageDigest", getDigestSha512(onDiskPath), "SHA-512");
        size = new IntegerType("Size", lsize);

        try {
            ir = DroidIdentifier.getInstance().getIdentificationResult(onDiskPath);
        } catch (SEDALibException e) {
            doProgressLogWithoutInterruption(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_WARNINGS, "sedalib: impossible de faire l'identification Droid pour le fichier ["
                    + onDiskPath.toString() + "]", e);
        }
        if (ir != null)
            formatIdentification = new FormatIdentification(ir.getName(), ir.getMimeType(), ir.getPuid(), null);
        else
            formatIdentification = new FormatIdentification("Unknown", null, "UNKNOWN", null);

        if (fileInfo == null)
            fileInfo = new FileInfo();
        if (fileInfo.getSimpleMetadata("Filename") == null)
            fileInfo.addNewMetadata("Filename", lfilename);
        fileInfo.addNewMetadata("LastModified", llastModified.toString());
    }

    /**
     * Change null values of String to "non défini".
     *
     * @param a a String
     * @return the transformed string
     */
    private String undefined(String a) {
        if (a == null)
            return ("non défini");
        else
            return a;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result;
        try {
            if (dataObjectVersion == null)
                result = "DataObjectVersion: non défini";
            else
                result = "DataObjectVersion: " + undefined(dataObjectVersion.getValue());
            if (fileInfo == null)
                result += "\nPas d'éléments FileInfo";
            else {
                result += "\nNom: " + undefined(fileInfo.getSimpleMetadata("Filename"));
                if (fileInfo.getSimpleMetadata("LastModified") != null)
                    result += "\nModifié le:" + fileInfo.getSimpleMetadata("LastModified");
            }
            if (size == null)
                result += "\nTaille inconnue";
            else
                result += "\nTaille: " + size.getValue() + " octets";
            result += "\nSIP Id: " + undefined(inDataPackageObjectId);
            if (uri == null)
                result += "\nURI: non défini";
            else
                result += "\nURI: " + undefined(uri.getValue());
            if (formatIdentification == null)
                result += "\nPas d'éléments FormatIdentification";
            else {
                result += "\nMimeType: " + undefined(formatIdentification.getSimpleMetadata("MimeType")) + "\nPUID: "
                        + undefined(formatIdentification.getSimpleMetadata("FormatId")) + "\nFormat: "
                        + undefined(formatIdentification.getSimpleMetadata("FormatLitteral"));
            }
            if (messageDigest == null)
                result += "\nDigest: non défini";
            else
                result += "\nDigest: " + undefined(messageDigest.getAlgorithm()) + " - " + undefined(messageDigest.getValue());
            if (onDiskPath == null)
                result += "\nPath: non défini";
            else
                result += "\nPath: " + undefined(onDiskPath.toString());
            if (metadata != null)
                result += "\nMetadata:\n" + metadata.toString();
        } catch (Exception e) {
            return "Can't give elements-" + super.toString();
        }
        return result;
    }

    // SEDA XML exporter

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.core.DataObject#toSedaXml(fr.gouv.vitam.tools.
     * sedalib.xml.SEDAXMLStreamWriter)
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter, SEDALibProgressLogger sedaLibProgressLogger)
            throws InterruptedException, SEDALibException {
        try {
            xmlWriter.writeStartElement("BinaryDataObject");
            xmlWriter.writeAttributeIfNotEmpty("id", inDataPackageObjectId);
            if (dataObjectSystemId != null)
                dataObjectSystemId.toSedaXml(xmlWriter);
            if (dataObjectGroupSystemId != null)
                dataObjectGroupSystemId.toSedaXml(xmlWriter);
            for (AnyXMLType rXmlData : relationshipsXmlData)
                rXmlData.toSedaXml(xmlWriter);
//		dataObjectGroupReferenceId not used in 2.1 DataObjectGroup mode
//		dataObjectGroupId not used in 2.1 DataObjectGroup mode
            if (dataObjectVersion != null)
                dataObjectVersion.toSedaXml(xmlWriter);
            String tmpUri = "content/" + inDataPackageObjectId;
            if (fileInfo != null) {
                String tmp = fileInfo.getSimpleMetadata("Filename");
                tmpUri += (tmp != null ? "." + getExtension(tmp) : "");
            }
            uri = new StringType("Uri", tmpUri);
            uri.toSedaXml(xmlWriter);
            if (messageDigest != null)
                messageDigest.toSedaXml(xmlWriter);
            if (size != null)
                size.toSedaXml(xmlWriter);
            if (compressed != null)
                compressed.toSedaXml(xmlWriter);
            if (formatIdentification != null)
                formatIdentification.toSedaXml(xmlWriter);
            if (fileInfo != null)
                fileInfo.toSedaXml(xmlWriter);
            if (metadata != null)
                metadata.toSedaXml(xmlWriter);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException(
                    "Erreur d'écriture XML du BinaryDataObject [" + inDataPackageObjectId + "]", e);
        }

        int counter = getDataObjectPackage().getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
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
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
            toSedaXml(xmlWriter, null);
            xmlWriter.close();
            result = baos.toString("UTF-8");
        } catch (SEDALibException | XMLStreamException | InterruptedException | IOException e) {
            throw new SEDALibException("Erreur interne", e);
        }
        if (result != null) {
            if (result.isEmpty())
                result = null;
            else {
                result = result.replaceFirst("\\<BinaryDataObject .*\\>", "");
                result = result.substring(0, result.lastIndexOf("</BinaryDataObject>") - 1);
                result = result.trim();
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
        String nextElementName;
        try {
            nextElementName = xmlReader.peekName();
            if ((nextElementName != null) && (nextElementName.equals("DataObjectSystemId"))) {
                dataObjectSystemId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectGroupSystemId"))) {
                dataObjectGroupSystemId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            relationshipsXmlData = new ArrayList<>();
            while ((nextElementName != null) && (nextElementName.equals("Relationship"))) {
                relationshipsXmlData.add((AnyXMLType) SEDAMetadata.fromSedaXml(xmlReader, AnyXMLType.class));
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectGroupReferenceId"))) {
                dataObjectGroupReferenceId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectGroupId"))) {
                dataObjectGroupId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectVersion"))) {
                dataObjectVersion = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("Attachment")))
                throw new SEDALibException("Elément Attachment non supporté");
            if ((nextElementName != null) && (nextElementName.equals("Uri"))) {
                uri = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("MessageDigest"))) {
                messageDigest = (DigestType) SEDAMetadata.fromSedaXml(xmlReader, DigestType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("Size"))) {
                size = (IntegerType) SEDAMetadata.fromSedaXml(xmlReader, IntegerType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("Compressed"))) {
                compressed = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("FormatIdentification"))) {
                formatIdentification = (FormatIdentification) SEDAMetadata.fromSedaXml(xmlReader, FormatIdentification.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("FileInfo"))) {
                fileInfo = (FileInfo) SEDAMetadata.fromSedaXml(xmlReader, FileInfo.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("Metadata"))) {
                metadata = (Metadata) SEDAMetadata.fromSedaXml(xmlReader, Metadata.class);
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML", e);
        }
    }

    /**
     * Import the BinaryDataObject in XML expected form from the SEDA Manifest in
     * the DataObjectPackage.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage     the DataObjectPackage to be completed
     * @param rootDir               the directory where the BinaryDataObject files are
     *                              exported
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read BinaryDataObject, or null if not a BinaryDataObject
     * @throws SEDALibException     if the XML can't be read or the SEDA scheme is
     *                              not respected
     * @throws InterruptedException if export process is interrupted
     */
    public static BinaryDataObject fromSedaXml(SEDAXMLEventReader xmlReader, DataObjectPackage dataObjectPackage,
                                               String rootDir, SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
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

        if ((bdo.dataObjectGroupReferenceId != null) && (bdo.dataObjectGroupId != null))
            throw new SEDALibException("Eléments DataObjectGroupReferenceId et DataObjectGroupId incompatibles");
        if (bdo.dataObjectGroupId != null) {
            if (dataObjectPackage.getDataObjectGroupById(bdo.dataObjectGroupId.getValue()) != null)
                throw new SEDALibException("Elément DataObjectGroup [" + bdo.dataObjectGroupId.getValue() + "] déjà créé");
            if (bdo.dataObjectGroupId.getValue() == null)
                throw new SEDALibException("Elément DataObjectGroup vide");
            dog = new DataObjectGroup();
            dog.setInDataObjectPackageId(bdo.dataObjectGroupId.getValue());
            dataObjectPackage.addDataObjectGroup(dog);
            dog.addDataObject(bdo);
            doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_WARNINGS, "sedalib: dataObjectGroup [" + dog.inDataPackageObjectId
                    + "] créé depuis BinaryDataObject [" + bdo.inDataPackageObjectId + "]", null);
        } else if (bdo.dataObjectGroupReferenceId != null) {
            if (bdo.dataObjectGroupReferenceId.getValue() == null)
                throw new SEDALibException("Elément DataObjectGroupReferenceId vide");
            dog = dataObjectPackage.getDataObjectGroupById(bdo.dataObjectGroupReferenceId.getValue());
            if (dog == null)
                throw new SEDALibException("Erreur de référence au DataObjectGroup [" + bdo.dataObjectGroupId.getValue() + "]");
            dog.addDataObject(bdo);
        }
        bdo.dataObjectGroupReferenceId = null;
        bdo.dataObjectGroupId = null;

        int counter = dataObjectPackage.getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
                "sedalib: " + counter + " métadonnées DataObject importées");
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

        try (ByteArrayInputStream bais = new ByteArrayInputStream(fragments.getBytes("UTF-8"));
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

        if ((bdo.dataObjectGroupId != null) || (bdo.dataObjectGroupReferenceId != null))
            throw new SEDALibException(
                    "La référence à un DataObjectGroup n'est pas modifiable par édition, il ne doit pas être défini");
        this.dataObjectSystemId = bdo.dataObjectSystemId;
        this.dataObjectGroupSystemId = bdo.dataObjectGroupSystemId;
        this.relationshipsXmlData = bdo.relationshipsXmlData;
        this.dataObjectVersion = bdo.dataObjectVersion;
        this.messageDigest = bdo.messageDigest;
        this.size = bdo.size;
        this.compressed = bdo.compressed;
        this.fileInfo = bdo.fileInfo;
        this.formatIdentification = bdo.formatIdentification;
        this.metadata = bdo.metadata;
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
