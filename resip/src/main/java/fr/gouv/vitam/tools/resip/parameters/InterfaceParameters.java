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
package fr.gouv.vitam.tools.resip.parameters;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InterfaceParameters.
 */
public class InterfaceParameters {

    // general elements
    /**
     * The structured metadata edition flag.
     */
    boolean structuredMetadataEditionFlag;

    /**
     * The debugFlag flag
     */
    private boolean debugFlag;

    /**
     * Instantiates a new creation context.
     */
    public InterfaceParameters() {
        structuredMetadataEditionFlag = true;
    }


    /**
     * Instantiates a new creation context.
     *
     * @param prefs the prefs
     */
    public InterfaceParameters(Prefs prefs) {
        structuredMetadataEditionFlag=Boolean.parseBoolean(prefs.getPrefProperties().getProperty("interfaceParameters.structuredEdtionFlag", "true"));
        debugFlag=Boolean.parseBoolean(prefs.getPrefProperties().getProperty("interfaceParameters.debugFlag", "false"));
    }

    /**
     * Put in preferences the values specific of this class.
     *
     * @param prefs the prefs
     */
    public void toPrefs(Prefs prefs) {
        prefs.getPrefProperties().setProperty("interfaceParameters.structuredEdtionFlag",Boolean.toString(structuredMetadataEditionFlag));
        prefs.getPrefProperties().setProperty("interfaceParameters.debugFlag", Boolean.toString(debugFlag));
    }

    /**
     * Sets the default prefs.
     */
    public void setDefaultPrefs() {
        structuredMetadataEditionFlag=true;
        debugFlag=false;
    }

    // Getters and setters

    /**
     * Is structured metadata edition flag boolean.
     *
     * @return the boolean
     */
    public boolean isStructuredMetadataEditionFlag() {
        return structuredMetadataEditionFlag;
    }

    /**
     * Set structured metadata edition flag.
     *
     * @param structuredMetadataEditionFlag the structured metadata edition flag
     */
    public void setStructuredMetadataEditionFlag(boolean structuredMetadataEditionFlag) {
        this.structuredMetadataEditionFlag = structuredMetadataEditionFlag;
    }

    /**
     * Is debug flag.
     *
     * @return the debug flag
     */
    public boolean isDebugFlag() {
        return debugFlag;
    }

    /**
     * Set debug flag.
     *
     * @param debugFlag the debug flag
     */
    public void setDebugFlag(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }
}