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
package fr.gouv.vitam.tools.resip.service;

import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;

/**
 * What an import gives: the package, the global metadata of the imported SIP or DIP if any, and
 * the summary of the import.
 */
public class ImportResult {

    private final DataObjectPackage dataObjectPackage;
    private final GlobalMetadata globalMetadata;
    private final String summary;

    /**
     * Instantiates a new import result.
     *
     * @param dataObjectPackage the imported package
     * @param globalMetadata    the global metadata of the imported SIP or DIP, or null
     * @param summary           the summary of the import
     */
    public ImportResult(DataObjectPackage dataObjectPackage, GlobalMetadata globalMetadata, String summary) {
        this.dataObjectPackage = dataObjectPackage;
        this.globalMetadata = globalMetadata;
        this.summary = summary;
    }

    public DataObjectPackage getDataObjectPackage() {
        return dataObjectPackage;
    }

    public GlobalMetadata getGlobalMetadata() {
        return globalMetadata;
    }

    public String getSummary() {
        return summary;
    }

    /**
     * Builds the work of the import, whose export context takes the global and management metadata
     * of the imported SIP or DIP over the default ones.
     *
     * @param creationContext the creation context of the import
     * @param exportContext   the default export context
     * @return the work
     */
    public Work toWork(CreationContext creationContext, ExportContext exportContext) {
        if (globalMetadata != null) exportContext.setArchiveTransferGlobalMetadata(globalMetadata);
        if (dataObjectPackage.getManagementMetadataXmlData() != null) exportContext.setManagementMetadataXmlData(
            dataObjectPackage.getManagementMetadataXmlData()
        );
        return new Work(dataObjectPackage, creationContext, exportContext);
    }
}
