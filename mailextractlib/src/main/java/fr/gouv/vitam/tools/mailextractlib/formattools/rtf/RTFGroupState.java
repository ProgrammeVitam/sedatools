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

package fr.gouv.vitam.tools.mailextractlib.formattools.rtf;

import java.nio.charset.Charset;

/**
 * The Class RTFGroupState holds all state associated with current RTF group.
 * <p>
 * This is strongly inspired from RTFParser in Tika 1.17
 * under http://www.apache.org/licenses/LICENSE-2.0
 */

class RTFGroupState {

    /** The uc skip. */
    public int ucSkip;

    /** The font charset. */
    public Charset fontCharset;

    /** The ignore flag, true if skip the text in the current group */
    public boolean ignore;

    // inner flag determined in parsing
    // composed to determine ignore flag current group

    private boolean inIgnorable;
    private boolean inHtmltag;
    private boolean inHtmlrtf;

    // previous in rtf group stack
    private RTFGroupState previous;

    /**
     * Instantiates a new RTFGroupState, inheriting all properties from current
     * one or root if previous=null
     *
     * @param previous
     *            the other
     */
    public RTFGroupState(RTFGroupState previous) {
        if (previous != null) {
            this.ucSkip = previous.ucSkip;
            this.fontCharset = previous.fontCharset;

            this.inIgnorable = previous.inIgnorable;
            this.inHtmltag = false;
            this.inHtmlrtf = previous.inHtmlrtf;
            this.ignore = inIgnorable | inHtmlrtf;

            this.previous = previous;
        } else {
            // Default is 1 if no uc control has been seen yet:
            this.ucSkip = 1;
            this.fontCharset = null;

            this.inIgnorable = false;
            this.inHtmltag = false;
            this.inHtmlrtf = false;
            this.ignore = false;

            this.previous = null;
        }
    }

    /**
     * Sets the inIgnorable rtf group flag, and compute ignore flag.
     *
     * @param b
     *            the new inIgnorable
     */
    public void setInIgnorable(boolean b) {
        inIgnorable = b;
        ignore = (inIgnorable && !inHtmltag) || inHtmlrtf;
    }

    /**
     * Sets the inHtmltag rtf group flag, and compute ignore flag.
     *
     * @param b
     *            the new in htmltag
     */
    public void setInHtmltag(boolean b) {
        inHtmltag = b;
        ignore = (inIgnorable && !inHtmltag) || inHtmlrtf;
    }

    /**
     * Sets the inHtmlrtf rtf group flag, and compute ignore flag.
     *
     * @param b
     *            the new in htmlrtf
     */
    public void setInHtmlrtf(boolean b) {
        inHtmlrtf = b;
        ignore = (inIgnorable && !inHtmltag) || inHtmlrtf;
    }

    /**
     * Gets the previous group state.
     *
     * @return the previous group state
     */
    public RTFGroupState getPreviousGroupState() {
        return previous;
    }
}
