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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.ProgressLogFunc;

import javax.swing.JTextArea;

/**
 * Factory building the SEDALibProgressLogger used by the resip threads, with
 * log level and step depending on the debug mode.
 */
public class ThreadLoggerFactory {

    private final JTextArea loggingComponent;
    private final boolean isDebugMode;

    /**
     * Instantiates a new Thread logger factory.
     *
     * @param loggingComponent the text area receiving progress logs, or null if
     *                         no progress log expected
     * @param isDebugMode      the debug mode flag
     */
    public ThreadLoggerFactory(JTextArea loggingComponent, boolean isDebugMode) {
        this.loggingComponent = loggingComponent;
        this.isDebugMode = isDebugMode;
    }

    /**
     * Builds a logger for the current application, using the interface debug
     * flag.
     *
     * @param loggingComponent the text area receiving progress logs, or null if
     *                         no progress log expected
     * @return the logger
     */
    public static SEDALibProgressLogger createLogger(JTextArea loggingComponent) {
        return new ThreadLoggerFactory(
            loggingComponent,
            ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()
        ).getLogger();
    }

    /**
     * Gets logger.
     *
     * @return the logger
     */
    public SEDALibProgressLogger getLogger() {
        SEDALibProgressLogger spl = new SEDALibProgressLogger(
            ResipLogger.getGlobalLogger().getLogger(),
            getLogLevel(),
            getProgressLogFunc(),
            getLogStep(),
            2,
            SEDALibProgressLogger.OBJECTS_GROUP,
            1000
        );
        spl.setDebugFlag(isDebugMode);
        return spl;
    }

    /**
     * Gets the function appending logs to the logging component, or null if no
     * logging component.
     *
     * @return the progress log function
     */
    public ProgressLogFunc getProgressLogFunc() {
        if (loggingComponent == null) return null;
        return (count, log) -> {
            String newLog = loggingComponent.getText() + "\n" + log;
            loggingComponent.setText(newLog);
            loggingComponent.setCaretPosition(newLog.length());
        };
    }

    /**
     * Gets log level.
     *
     * @return the log level
     */
    public int getLogLevel() {
        return isDebugMode ? SEDALibProgressLogger.OBJECTS_WARNINGS : SEDALibProgressLogger.OBJECTS_GROUP;
    }

    /**
     * Gets log step.
     *
     * @return the log step
     */
    public int getLogStep() {
        return isDebugMode ? 1 : 1000;
    }
}
