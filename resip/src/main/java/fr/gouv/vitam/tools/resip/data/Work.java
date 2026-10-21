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
package fr.gouv.vitam.tools.resip.data;

import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import java.time.Duration;
import java.time.Instant;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * The Class Work.
 */
public class Work {

    // Inner Object

    /**
     * The Constant CURRENT_SERIALIZATION_VERSION.
     */
    static final String CURRENT_SERIALIZATION_VERSION = "1.0";

    /**
     * The version of this object used for to distinct serialization in prefs or on
     * disk.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private String serializationVersion;

    /**
     * The SIP export context.
     */
    private ExportContext exportContext;

    /**
     * The creation context.
     */
    private CreationContext creationContext;

    /**
     * The DataObjectPackage.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * SEDA version
     */
    private SedaVersion version;

    /**
     * Instantiates a new work for json serialization.
     */
    public Work() {
        this(null, null, null);
    }

    /**
     * Instantiates a new work.
     *
     * @param dataObjectPackage the archive transfer
     * @param creationContext   the creation context
     * @param exportContext     the export context
     */
    public Work(DataObjectPackage dataObjectPackage, CreationContext creationContext, ExportContext exportContext) {
        this.serializationVersion = CURRENT_SERIALIZATION_VERSION;
        this.dataObjectPackage = dataObjectPackage;
        this.creationContext = creationContext;
        this.exportContext = exportContext;
    }

    /**
     * Do vitam normalize.
     *
     * @param spl the spl
     * @return the string
     * @throws SEDALibException     the SEDA lib exception
     * @throws InterruptedException the interrupted exception
     */
    public String doVitamNormalize(SEDALibProgressLogger spl) throws SEDALibException, InterruptedException {
        Instant start = Instant.now();
        String log =
            "resip: début de la vérification de l'absence de cycle et de la normalisation Vitam de la structure";
        doProgressLog(spl, SEDALibProgressLogger.GLOBAL, log, null);

        dataObjectPackage.vitamNormalize(spl);

        doProgressLog(
            spl,
            SEDALibProgressLogger.STEP,
            "resip: " + dataObjectPackage.getArchiveUnitCount() + " ArchiveUnits normalisées",
            null
        );
        Instant end = Instant.now();
        doProgressLog(
            spl,
            SEDALibProgressLogger.GLOBAL,
            "resip: opération terminée en " + Duration.between(start, end).toString().substring(2),
            null
        );
        return "Vérification de l'absence de cycle et normalisation Vitam effectuées";
    }

    /**
     * Gets the archive transfer.
     *
     * @return the archive transfer
     */
    public DataObjectPackage getDataObjectPackage() {
        return dataObjectPackage;
    }

    /**
     * Sets the archive transfer.
     *
     * @param dataObjectPackage the new archive transfer
     */
    public void setDataObjectPackage(DataObjectPackage dataObjectPackage) {
        this.dataObjectPackage = dataObjectPackage;
    }

    /**
     * Gets the creation context.
     *
     * @return the creation context
     */
    public CreationContext getCreationContext() {
        return creationContext;
    }

    /**
     * Sets the creation context.
     *
     * @param creationContext the new creation context
     */
    public void setCreationContext(CreationContext creationContext) {
        this.creationContext = creationContext;
    }

    /**
     * Gets the SEDA version of this work, the one of its package. It is also saved apart, so as to
     * be read without the package.
     *
     * @return the SEDA version, null for a work without package and saved without version
     */
    public SedaVersion getVersion() {
        return dataObjectPackage != null ? dataObjectPackage.getSedaVersion() : version;
    }

    /**
     * Sets the SEDA version read from a save, the one of the work when it has no package.
     *
     * @param version the SEDA version
     */
    public void setVersion(SedaVersion version) {
        this.version = version;
    }

    /**
     * Gets the global metadata context.
     *
     * @return the global metadata context
     */
    public ExportContext getExportContext() {
        return exportContext;
    }

    /**
     * Sets the global metadata context.
     *
     * @param exportContext the new global metadata context
     */
    public void setExportContext(ExportContext exportContext) {
        this.exportContext = exportContext;
    }
}
