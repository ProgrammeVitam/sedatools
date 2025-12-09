package fr.gouv.vitam.tools.sedalib.core.seda;

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
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

public class SedaVersionConverter {

    private final SEDALibProgressLogger progressLogger;

    public SedaVersionConverter(SEDALibProgressLogger progressLogger) {
        this.progressLogger = progressLogger;
    }

    /**
     * Converts a DataObjectPackage from one SEDA version to another by serializing and re-parsing the XML.
     *
     * @param originalPackage the original DataObjectPackage
     * @param sourceSedaVersion the source SEDA version
     * @param targetSedaVersion the target SEDA version
     * @return a new DataObjectPackage in the target SEDA version
     * @throws SEDALibException if conversion fails
     * @throws InterruptedException if the operation is interrupted
     */
    public DataObjectPackage convert(
        DataObjectPackage originalPackage,
        SedaVersion sourceSedaVersion,
        SedaVersion targetSedaVersion
    ) throws SEDALibException, InterruptedException {
        if (sourceSedaVersion.equals(targetSedaVersion)) {
            throw new SEDALibException("Source and target SEDA versions are identical: " + sourceSedaVersion);
        }
        if (!sourceSedaVersion.equals(SedaContext.getVersion())) {
            throw new SEDALibException("Source and context SEDA versions are different: " +
                "(source: " + sourceSedaVersion + ", context: " + SedaContext.getVersion());
        }

        logGlobal("Conversion from " + sourceSedaVersion + " to " + targetSedaVersion);

        String sedaXml = serializeToSedaXml(originalPackage, sourceSedaVersion);
        SedaContext.setVersion(targetSedaVersion);

        try {
            DataObjectPackage convertedPackage = deserializeFromSedaXml(sedaXml);
            restoreBinaryObjectPaths(originalPackage, convertedPackage);
            return convertedPackage;
        } catch (Exception e) {
            SedaContext.setVersion(sourceSedaVersion);
            throw new SEDALibException("Failed to convert DataObjectPackage to " + targetSedaVersion, e);
        }
    }

    private String serializeToSedaXml(DataObjectPackage dataObjectPackage, SedaVersion sedaVersion)
        throws SEDALibException, InterruptedException {

        logStep("-> Serializing DataObjectPackage to " + sedaVersion.displayString() + " XML");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, IndentXMLTool.STANDARD_INDENT)) {

            dataObjectPackage.toSedaXml(xmlWriter, true, progressLogger);
            return baos.toString(StandardCharsets.UTF_8);

        } catch (IOException | XMLStreamException e) {
            throw new SEDALibException("Failed to serialize DataObjectPackage to " + sedaVersion.displayString() + " XML", e);
        }
    }

    private DataObjectPackage deserializeFromSedaXml(String xml)
        throws SEDALibException, InterruptedException {

        logStep("-> Deserializing XML to DataObjectPackage");

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {

            xmlReader.xmlReader.nextEvent(); // skip StartDocument
            return DataObjectPackage.fromSedaXml(xmlReader, "unknown", progressLogger);

        } catch (IOException | XMLStreamException e) {
            throw new SEDALibException("Failed to deserialize XML to DataObjectPackage", e);
        }
    }

    private void restoreBinaryObjectPaths(DataObjectPackage source, DataObjectPackage target) {
        for (Map.Entry<String, BinaryDataObject> entry : source.getBdoInDataObjectPackageIdMap().entrySet()) {
            String id = entry.getKey();
            BinaryDataObject sourceBdo = entry.getValue();
            BinaryDataObject targetBdo = target.getBdoInDataObjectPackageIdMap().get(id);
            if (targetBdo != null) {
                targetBdo.setOnDiskPath(sourceBdo.getOnDiskPath());
            }
        }
    }

    private void logStep(String message) throws InterruptedException {
        SEDALibProgressLogger.doProgressLog(progressLogger, SEDALibProgressLogger.STEP, message, null);
    }

    private void logGlobal(String message) throws InterruptedException {
        SEDALibProgressLogger.doProgressLog(progressLogger, SEDALibProgressLogger.GLOBAL, message, null);
    }
}
