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
package fr.gouv.vitam.tools.sedalib.droid;

import uk.gov.nationalarchives.droid.container.*;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ContainerDroidIdentifier.
 * <p>
 * Class for the DROID identifiers of one type of container (ole2, zip...)
 */
public class ContainerDroidIdentifier {

    /** The container identifier init. */
    private ContainerIdentifierInit containerIdentifierInit;

    /** The binary signature file. */
    private FFSignatureFile binarySignatureFile;

    /** The identifier engine. */
    private IdentifierEngine identifierEngine;

    /** The formats. */
    private Map<Integer, List<FileFormatMapping>> formats = new HashMap<>();

    // Contructors

    /**
     * Instantiates a new DROID container identifier.
     *
     * @param defs the container signature definitions from a "container-signature-xxxx.xml" file
     * @param binarySignatureFile the binary signature file from a " DROID_SignatureFile_Vxxx.xml"
     * @param containerType the container type ("OLE2", "ZIP"...)
     */
    public ContainerDroidIdentifier(final ContainerSignatureDefinitions defs, final FFSignatureFile binarySignatureFile,
                                    final String containerType) {
        containerIdentifierInit = new ContainerIdentifierInit();
        containerIdentifierInit.init(defs, containerType, formats, null);
        this.binarySignatureFile = binarySignatureFile;
    }

    // Methods

    /**
     * Gets the container format identification if any.
     *
     * @param inputStream the inputStream to identify
     * @param identificationResults the identification results list to be completed
     * @return the identification results list completed, if a new format have been identified, or the original list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public IdentificationResultCollection getContainerIdentification(final InputStream inputStream,
                                                                     final IdentificationResultCollection identificationResults) throws IOException {

        try (IdentificationRequest<InputStream> request = new ContainerFileIdentificationRequest(null)){
            request.open(inputStream);

            int maxBytesToScan = -1;
            ContainerSignatureMatchCollection matches = new ContainerSignatureMatchCollection(
                    getContainerIdentifierInit().getContainerSignatures(),
                    getContainerIdentifierInit().getUniqueFileEntries(), maxBytesToScan);

            identifierEngine.process(request, matches);

            final Map<String, String> puidMap = new HashMap<>();
            for (ContainerSignatureMatch match : matches.getContainerSignatureMatches()) {
                if (match.isMatch()) {
                    List<FileFormatMapping> mappings = getFormats().get(match.getSignature().getId());
                    for (FileFormatMapping mapping : mappings) {
                        IdentificationResultImpl result = new IdentificationResultImpl();
                        result.setMethod(IdentificationMethod.CONTAINER);
                        result.setRequestMetaData(request.getRequestMetaData());
                        String puid = mapping.getPuid();
                        result.setPuid(mapping.getPuid());
                        FileFormat ff = binarySignatureFile.getFileFormat(puid);
                        result.setName(ff.getName());
                        result.setMimeType(ff.getMimeType());
                        result.setVersion(ff.getVersion());
                        if (!puidMap.containsKey(puid)) {
                            puidMap.put(puid, "");
                            identificationResults.addResult(result);
                        }
                    }
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return identificationResults;
    }

    // Getters and setters

    /**
     * Gets the container identifier init.
     *
     * @return The Container Identifier Initializer
     */
    public ContainerIdentifierInit getContainerIdentifierInit() {
        return containerIdentifierInit;
    }

    /**
     * Gets the formats.
     *
     * @return The File Format mappings
     */
    public Map<Integer, List<FileFormatMapping>> getFormats() {
        return formats;
    }

    /**
     * Sets the identifier engine.
     *
     * @param identifierEngine The identifier engine
     */
    public void setIdentifierEngine(final IdentifierEngine identifierEngine) {
        this.identifierEngine = identifierEngine;
    }
}
