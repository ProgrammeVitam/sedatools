/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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
            throw new SEDALibException(
                "Source and context SEDA versions are different: " +
                "(source: " +
                sourceSedaVersion +
                ", context: " +
                SedaContext.getVersion()
            );
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

        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, IndentXMLTool.STANDARD_INDENT)
        ) {
            dataObjectPackage.toSedaXml(xmlWriter, true, progressLogger);
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException | XMLStreamException e) {
            throw new SEDALibException(
                "Failed to serialize DataObjectPackage to " + sedaVersion.displayString() + " XML",
                e
            );
        }
    }

    private DataObjectPackage deserializeFromSedaXml(String xml) throws SEDALibException, InterruptedException {
        logStep("-> Deserializing XML to DataObjectPackage");

        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)
        ) {
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
