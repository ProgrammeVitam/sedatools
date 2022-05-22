package fr.gouv.vitam.tools.sedalib.core;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * The Seda2 version management class, make it possible to use different Seda2 version if:
 * <ul><li>only the ontology and a the xsd declaration are different, not the messages structure</li>
 * <li>the Seda2 version has not to be changed when previous version objects exist</li></ul>
 * The global Seda2 version is used in all SEDAMetadata class to adapt to ontology used.
 * <P>Warning: there is a very special function ({@link #convertToSeda2Version(DataObjectPackage, int, SedaLibProgressLogger) convertToSeda2Version}) implemented to convert a DataObjectPackage to another Seda2 version.
 * This is mostly done to be used in interactive program like Resip.</P>
 */
public final class Seda2Version {
    /**
     * For Seda2.1 SEDA2_1=1
     */
    public final static int SEDA2_1 = 1;
    /**
     * For Seda2.2 SEDA2_2=2
     */
    public final static int SEDA2_2 = 2;
    /**
     * Max Seda2.x supported version MAX_SUPPORTED_VERSION=2
     */
    public final static int MAX_SUPPORTED_VERSION = 2;

    /**
     * SEDA2 convention used for all lib processing
     * Warning: usage for different SEDA2 versions is not possible in parallel process!
     */
    private static int seda2Version = SEDA2_1;

    /**
     * Gets Seda2 version.
     *
     * @return the Seda2 version
     */
    public static int getSeda2Version() {
        return seda2Version;
    }

    /**
     * Gets Seda2 version string.
     *
     * @return the Seda2 version string
     */
    public static String getSeda2VersionString() {
        return "Seda2." + seda2Version;
    }

    /**
     * Is supported Seda2 version boolean.
     *
     * @param newSeda2Version the new seda 2 version
     * @return the boolean
     */
    public static boolean isSupportedSeda2Version(int newSeda2Version) {
        switch (seda2Version) {
            case SEDA2_1:
            case SEDA2_2:
                return true;
            default:
                return false;
        }
    }

    /**
     * Sets Seda2 version.
     *
     * @param newSeda2Version the new Seda2 version
     * @throws SEDALibException if this is not a supported version
     */
    public static void setSeda2Version(int newSeda2Version) throws SEDALibException {
        if (isSupportedSeda2Version(newSeda2Version))
            seda2Version = newSeda2Version;
        else
            throw new SEDALibException("Unknown Seda2." + newSeda2Version + " version");
    }

    /**
     * Convert à DataObjectPackage to Seda2 version using XML as pivot format.
     * <P>Warning: this very special function is mostly done to be used in interactive program like Resip.</P>
     *
     * @param dataObjectPackage the DataObjectPackage
     * @param toSeda2Version    the destination Seda2 version
     * @return the converted DataObjectPackage
     * @throws SEDALibException if not supported version or if conversion through XML is not possible
     */
    public static DataObjectPackage convertToSeda2Version(DataObjectPackage dataObjectPackage, int toSeda2Version, SEDALibProgressLogger sedaLibProgressLogger)
            throws SEDALibException, InterruptedException {
        String xmlForm;
        DataObjectPackage result;
        int originSeda2Version = getSeda2Version();

        if (!isSupportedSeda2Version(toSeda2Version))
            throw new SEDALibException("Unknown Seda2." + toSeda2Version + " version");

        SEDALibProgressLogger.doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL,
                "Passage de la version Seda2."+originSeda2Version+" à la version Seda2."+toSeda2Version,null);
        SEDALibProgressLogger.doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.STEP,
                "-> Ecriture XML en version Seda2."+originSeda2Version,null);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter oxsw = new SEDAXMLStreamWriter(baos, IndentXMLTool.STANDARD_INDENT)) {
            dataObjectPackage.toSedaXml(oxsw, true, sedaLibProgressLogger);
            xmlForm = baos.toString("UTF-8");
        } catch (XMLStreamException | IOException | InterruptedException e) {
            throw new SEDALibException("Echec d'écriture XML du DataObjectPackage dans la version seda2." + originSeda2Version, e);
        }
        setSeda2Version(toSeda2Version);
        SEDALibProgressLogger.doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.STEP,
                "-> Prise en compte à partir de l'XML généré, vers la version Seda2."+toSeda2Version,null);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlForm.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader ixer = new SEDAXMLEventReader(bais, true)) {
            ixer.xmlReader.nextEvent(); // drop the StartDocument event
            result = DataObjectPackage.fromSedaXml(ixer, "unknown", sedaLibProgressLogger);
        } catch (XMLStreamException | IOException | InterruptedException e) {
            setSeda2Version(originSeda2Version);
            throw new SEDALibException("Echec de conversion à travers XML du DataObjectPackage dans la version seda2." + toSeda2Version, e);
        }
        for (Map.Entry<String, BinaryDataObject> identifiedDataObject : dataObjectPackage.getBdoInDataObjectPackageIdMap().entrySet())
            result.getBdoInDataObjectPackageIdMap().get(identifiedDataObject.getKey()).setOnDiskPath(identifiedDataObject.getValue().getOnDiskPath());
        setSeda2Version(originSeda2Version);

        return result;
    }
}
