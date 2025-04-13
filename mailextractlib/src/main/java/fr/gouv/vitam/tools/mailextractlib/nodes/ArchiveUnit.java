/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitam.tools.mailextractlib.nodes;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;

/**
 * Class for SEDA Archive Unit managing metadata, objects, if any, and on disk
 * representation.
 *
 * <p>
 * Other classes create Archive Units on the fly with extracted information and
 * content. This class write on disk representation with convenient
 * directory/file structure and ArchiveUnitContent.xml files for metadata.
 * <p>
 * All the files, if not pure binary, are UTF-8 encoded, as the file names.
 */
public class ArchiveUnit {
    private StoreExtractor storeExtractor;
    private String rootPath;
    private String name;
    private boolean forceMessageUnit;
    private MetadataXMLList contentmetadatalist = new MetadataXMLList();
    private List<ArchiveObject> objects = new ArrayList<ArchiveObject>();

    // Utility class containing one Object of the ObjectGroup
    private class ArchiveObject {
        /**
         * The Filename.
         */
        String filename;
        /**
         * The Usage.
         */
        String usage;
        /**
         * The Version.
         */
        int version;
        /**
         * The Raw content.
         */
        byte[] rawContent;

        /**
         * Instantiates a new Archive object.
         *
         * @param rawContent the raw content
         * @param filename   the filename
         * @param usage      the usage
         * @param version    the version
         */
        ArchiveObject(byte[] rawContent, String filename, String usage, int version) {
            this.rawContent = rawContent;
            this.filename = filename;
            this.usage = usage;
            this.version = version;
        }
    }

    /**
     * Instantiates a new archive unit.
     * <p>
     * This constructor is used for root Archive Unit (no father).
     *
     * @param storeExtractor Operation store extractor
     * @param rootPath       Directory in which the directory/file structure is created
     * @param name           Name of the root Archive Unit
     */
    public ArchiveUnit(StoreExtractor storeExtractor, String rootPath, String name) {
        this.storeExtractor = storeExtractor;
        this.rootPath = rootPath;
        this.name = name;
        this.forceMessageUnit = false;
    }

    /**
     * Instantiates a new archive unit.
     * <p>
     * Name is reduced as defined by options, and Archive Unit directory name is generated as
     * "'unitType'#'UniqID':'name'
     *
     * @param storeExtractor Operation store extractor
     * @param father         Father Archive Unit
     * @param unitType       Unit type (Folder| Message| Attachment...)
     * @param name           Name of the Archive Unit
     */
    public ArchiveUnit(StoreExtractor storeExtractor, ArchiveUnit father, String unitType, String name) {
        this.storeExtractor = storeExtractor;
        if (unitType == null)
            this.name = name;
        else {
            this.name = this.normalizeUniqUnitname(unitType, name);
            if (unitType.equals("Message"))
                forceMessageUnit = true;
        }
        this.rootPath = father.getFullName();
    }

    /**
     * Gets the logger created during the store extractor construction, and used
     * in all mailextract classes.
     *
     * <p>
     * For convenience each class which may have some log actions has it's own
     * getProgressLogger method always returning the store extractor logger.
     *
     * @return logger progress logger
     */
    public MailExtractProgressLogger getProgressLogger() {
        return storeExtractor.getProgressLogger();
    }

    /**
     * Gets the full name.
     *
     * @return Full path Archive Unit directory name
     */
    public String getFullName() {
        return rootPath + File.separator + getName();
    }

    /**
     * Gets the root path.
     *
     * @return Root path Archive Unit directory name
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Force the ArchiveUnit as an ArchiveUnit with objects.
     *
     * @param b the new object unit
     */
    public void setObjectUnit(boolean b) {
        forceMessageUnit = true;
    }

    /**
     * Gets the name.
     *
     * @return Name of Archive Unit in directory name
     */
    public String getName() {
        if (!forceMessageUnit && (objects.isEmpty()))
            return name;
        else {
            if (storeExtractor.getOptions().model == StoreExtractorOptions.MODEL_V1)
                return "__" + name + "__";
            else
                return name;
        }
    }

    /**
     * Adds a simple (key, value) metadata.
     * <p>
     * If value is null or empty, no metadata is added.
     * <p>
     * If mandatory flag is true and value is null or empty, the fact is logged
     *
     * @param key       Metadata key
     * @param value     Metadata String value
     * @param mandatory Mandatory flag
     * @throws InterruptedException the interrupted exception
     */
    public void addMetadata(String key, String value, boolean mandatory) throws InterruptedException {
        if (value != null && !value.isEmpty())
            contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, value));
        else if (mandatory)
            doProgressLog(getProgressLogger(), MailExtractProgressLogger.MESSAGE_DETAILS, "mailextractlib: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
                    + rootPath + "'", null);
    }

    /**
     * Adds a simple (key, value) metadata which can be splitted if necessary
     * <p>
     * If value is null or empty, no metadata is added.
     * If value is longer than 32k0, the value will be splitted in 32000.
     * @see fr.gouv.vitam.tools.mailextractlib.nodes.MetadataXMLSplittedNode
     * <p>
     * If mandatory flag is true and value is null or empty, the fact is logged
     *
     * @param key       Metadata key
     * @param value     Metadata String value
     * @param mandatory Mandatory flag
     * @throws InterruptedException the interrupted exception
     */
    public void addLongMetadata(String key, String value, boolean mandatory) throws InterruptedException {
        if (value != null && !value.isEmpty()) {
            contentmetadatalist.addMetadataXMLNode(new MetadataXMLSplittedNode(key, value));
        } else if (mandatory)
            doProgressLog(getProgressLogger(),MailExtractProgressLogger.MESSAGE_DETAILS, "mailextractlib: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
                    + rootPath + "'", null);
    }

    /**
     * Adds a simple (key, value) metadata with an attribute.
     * <p>
     * If value is null or empty, no metadata is added.
     * <p>
     * If mandatory flag is true and value is null or empty, the fact is logged
     *
     * @param key            Metadata key
     * @param attributename  Metadata attribute name
     * @param attributevalue Metadata attribute value
     * @param value          Metadata String value
     * @param mandatory      Mandatory flag
     * @throws InterruptedException the interrupted exception
     */
    public void addMetadata(String key, String attributename, String attributevalue, String value, boolean mandatory) throws InterruptedException {
        if (value != null && !value.isEmpty())
            contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, attributename, attributevalue, value));
        else if (mandatory) {
            doProgressLog(getProgressLogger(),MailExtractProgressLogger.MESSAGE_DETAILS, "mailextractlib: mandatory metadata '" + key + "' is not defined in unit '" + name
                    + "' in folder '" + rootPath + "'", null);
        }
    }

    /**
     * Adds a simple (key, value) metadata with a default value if value is null
     * or empty.
     *
     * @param key          Metadata key
     * @param value        Metadata value
     * @param defaultValue Default metadata value
     */
    public void addDefaultValuedMetadata(String key, String value, String defaultValue) {
        if ((value != null) && !value.isEmpty())
            contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, value));
        else
            contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, defaultValue));
    }

    /**
     * Adds a list of (key, value) metadata for the same key .
     *
     * <p>
     * If list of values is null or empty, no metadata is added.
     * <p>
     * If mandatory flag is true and list of values is null or empty, the
     * metadata lack is logged
     *
     * @param key        Metadata key
     * @param valuesList Metadata list of values
     * @param mandatory  Mandatory flag
     * @throws InterruptedException the interrupted exception
     */
    public void addSameMetadataList(String key, List<String> valuesList, boolean mandatory) throws InterruptedException {
        if ((valuesList != null) && (!valuesList.isEmpty())) {
            for (String s : valuesList) {
                contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, s));
            }
        } else if (mandatory)
            doProgressLog(getProgressLogger(),MailExtractProgressLogger.MESSAGE_DETAILS, "mailextractlib: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
                    + rootPath + "'", null);
    }

    /**
     * Adds for the key metadata a person value
     * <p>
     * This is a utility method, used first for the writer. For example the due
     * structure for "TOTO&lt;toto@sample.fr&gt;" Writer metadata in XML is:
     * <p>
     * &lt;Writer&gt;&lt;FullName&gt;TOTO&lt;/FullName&gt;
     * &lt;Identifier&gt;toto@sample.fr&lt;/Identifier&gt;&lt;/Writer&gt;
     * <p>
     * If mandatory flag is true and value is null or empty, the metadata lack
     * is logged
     *
     * @param key       Metadata key
     * @param value     Person value
     * @param mandatory Mandatory flag
     * @throws InterruptedException the interrupted exception
     */
    public void addPersonMetadata(String key, String value, boolean mandatory) throws InterruptedException {
        MetadataPerson p;
        MetadataXMLNode mvMetaData;
        MetadataXMLList mlMetaData;

        if ((value != null) && !value.isEmpty()) {
            p = new MetadataPerson(value);
            mlMetaData = new MetadataXMLList();
            mvMetaData = new MetadataXMLNode("FullName", p.fullName);
            mlMetaData.addMetadataXMLNode(mvMetaData);
            mvMetaData = new MetadataXMLNode("Identifier", p.identifier);
            mlMetaData.addMetadataXMLNode(mvMetaData);
            contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, mlMetaData));
        } else if (mandatory)
            doProgressLog(getProgressLogger(),MailExtractProgressLogger.MESSAGE_DETAILS, "mailextractlib: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
                    + rootPath + "'", null);
    }

    /**
     * Adds an event value.
     *
     * @param identifier the event identifier
     * @param type       the event type
     * @param dateTime   the event date time
     * @param detail     the event detail
     */
    public void addEventMetadata(String identifier, String type, String dateTime, String detail) {
        MetadataXMLNode mvMetaData;
        MetadataXMLList mlMetaData;

        mlMetaData = new MetadataXMLList();
        mvMetaData = new MetadataXMLNode("EventIdentifier", identifier);
        mlMetaData.addMetadataXMLNode(mvMetaData);
        mvMetaData = new MetadataXMLNode("EventType", type);
        mlMetaData.addMetadataXMLNode(mvMetaData);
        mvMetaData = new MetadataXMLNode("EventDateTime", dateTime);
        mlMetaData.addMetadataXMLNode(mvMetaData);
        mvMetaData = new MetadataXMLNode("EventDetail", detail);
        mlMetaData.addMetadataXMLNode(mvMetaData);
        contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode("Event", mlMetaData));
    }

    /**
     * Adds for the key metadata an array of person values, with values in
     * valuesList
     * <p>
     * This is a utility method, used first for the addresses list. For example
     * the due structure for "TOTO&lt;toto@sample.fr&gt;" Addressee metadata in XML
     * is:
     * <p>
     * &lt;Addressee&gt;&lt;FullName&gt;TOTO&lt;/FullName&gt;
     * &lt;Identifier&gt;toto@sample.fr&lt;/Identifier&gt;&lt;/Addressee&gt;
     * <p>
     * If valuesList is null or empty, no metadata is added.
     * <p>
     * If mandatory flag is true and value is null or empty, the metadata lack
     * is logged
     *
     * @param key        Metadata key
     * @param valuesList Values list
     * @param mandatory  Mandatory flag
     * @throws InterruptedException the interrupted exception
     */
    public void addPersonMetadataList(String key, List<String> valuesList, boolean mandatory) throws InterruptedException {
        MetadataPerson p;
        MetadataXMLNode mvMetaData;
        MetadataXMLList mlMetaData;

        if ((valuesList != null) && (valuesList.size() != 0)) {
            for (String s : valuesList) {
                p = new MetadataPerson(s);
                mlMetaData = new MetadataXMLList();
                mvMetaData = new MetadataXMLNode("FullName", p.fullName);
                mlMetaData.addMetadataXMLNode(mvMetaData);
                mvMetaData = new MetadataXMLNode("Identifier", p.identifier);
                mlMetaData.addMetadataXMLNode(mvMetaData);
                contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, mlMetaData));
            }
        } else if (mandatory)
            doProgressLog(getProgressLogger(),MailExtractProgressLogger.MESSAGE_DETAILS, "mailextractlib: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
                    + rootPath + "'", null);
    }

    /**
     * Adds an object with content from a String.
     * <p>
     * This object will be saved to disk UTF-8 encoded.
     *
     * @param stringContent Object content
     * @param filename      File name
     * @param usage         Usage type (BinaryMaster| TextContent...)
     * @param version       Object version (usually 1)
     */
    public void addObject(String stringContent, String filename, String usage, int version) {
        objects.add(new ArchiveObject(stringContent.getBytes(StandardCharsets.UTF_8), normalizeFilename(filename), usage, version));
    }

    /**
     * Adds an object with content from a byte array.
     * <p>
     * This object will be saved to disk in raw binary format.
     *
     * @param byteContent Object content
     * @param filename    File name
     * @param usage       Usage type (BinaryMaster| TextContent...)
     * @param version     Object version (usually 1)
     */
    public void addObject(byte[] byteContent, String filename, String usage, int version) {
        objects.add(new ArchiveObject(byteContent, normalizeFilename(filename), usage, version));
    }

    // create all the directories hierarchy
    // synchronized to prevent conflicts or errors caused by concurrent directory creation.
    private void createDirectory(String dirname) throws MailExtractLibException {
        synchronized (storeExtractor.getRootStoreExtractor()) {
            File dir = new File(dirname);
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new MailExtractLibException("mailextractlib: can't create destination directory[" + dirname + "] for writing unit \"" + name + "\"", null);
            }
        }
    }

    // create a file from byte array
    private void writeFile(String dirPath, String filename, byte[] byteContent) throws MailExtractLibException {
        try (FileOutputStream fos = new FileOutputStream(dirPath + File.separator + filename)) {
            if (byteContent != null)
                fos.write(byteContent);
        } catch (IOException ex) {
            if (dirPath.length() + filename.length() > 250) {
                throw new MailExtractLibException(
                        "mailextractlib: illegal destination file (may be too long pathname), writing unit \"" + name
                                + "\"" + " dir=" + dirPath + " filename=" + filename, ex);
            } else {
                throw new MailExtractLibException("mailextractlib: illegal destination file, writing unit \"" + name + "\""
                        + " dir=" + dirPath + " filename=" + filename, ex);
            }
        }
    }

    /**
     * Write the Archive Unit representation on disk.
     *
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     */
    public void write() throws MailExtractLibException {
        String dirPath;
        String filename;

        // different name if groupe unit or unit with objects
        dirPath = getFullName();

        // write unit directory
        createDirectory(dirPath);

        // add content surrounding metadata
        MetadataXMLNode contentmetadata = new MetadataXMLNode("Content", contentmetadatalist);

        // write unit metadata file
        if (storeExtractor.getOptions().model == StoreExtractorOptions.MODEL_V1)
            writeFile(dirPath, "ArchiveUnitContent.xml", contentmetadata.writeXML().getBytes(StandardCharsets.UTF_8));
        else
            writeFile(dirPath, "__ArchiveUnitMetadata.xml", contentmetadata.writeXML().getBytes(StandardCharsets.UTF_8));


        // write objects files
        if (!objects.isEmpty()) {
            for (ArchiveObject o : objects) {
                if (o.filename == null || o.filename.isEmpty())
                    filename = "undefined";
                else
                    filename = o.filename;
                if (storeExtractor.getOptions().model == StoreExtractorOptions.MODEL_V1)
                    writeFile(dirPath, "__" + o.usage + "_" + Integer.toString(o.version) + "_" + filename, o.rawContent);
                else
                    writeFile(dirPath, "__" + o.usage + "_" + Integer.toString(o.version) + "__" + filename, o.rawContent);
            }
        }
    }

    // reduce if needed a filename conserving the extension
    private String normalizeFilename(String filename) {
        String result;
        String extension = "";
        int len;

        // extract extension, short string after last point, if any
        int lastPoint = filename.lastIndexOf('.');
        if (lastPoint != -1) {
            extension = filename.substring(lastPoint);
            if (lastPoint >= 1)
                result = filename.substring(0, lastPoint);
            else
                result = "";
        } else
            result = filename;

        len = storeExtractor.getOptions().namesLength + 20;

        result = result.replaceAll("[^\\p{IsAlphabetic}\\p{Digit}\\.]", "-");

        if (result.length() > len)
            result = result.substring(0, len);

        return result + extension;
    }

    // create a unique name for an typed archive unit reduced as defined by options
    private String normalizeUniqUnitname(String type, String filename) {
        String result = "";
        int len;

        len = storeExtractor.getOptions().namesLength;
        if (len < 32)
            type = type.substring(0, 1);

        if (filename != null)
            result = filename.replaceAll("[^\\p{IsAlphabetic}\\p{Digit}]", "-");

        if (result.length() > len)
            result = result.substring(0, len);
        int uniqID = storeExtractor.getNewUniqID();
        result = type + "#" + Integer.toString(uniqID) + "-" + result;

        return result;
    }

}
