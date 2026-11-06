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
package fr.gouv.vitam.tools.sedaschema.model;

import java.util.Collections;
import java.util.List;

/**
 * An xsd:any: which namespaces are accepted and how their content is processed.
 */
public final class Wildcard {

    public enum NamespaceConstraint {
        /**
         * ##any
         */
        ANY,
        /**
         * ##other, or a list of excluded namespaces
         */
        NOT,
        /**
         * an explicit list of namespaces
         */
        LIST,
    }

    public enum ProcessContents {
        STRICT,
        LAX,
        SKIP,
    }

    private final NamespaceConstraint constraint;
    private final List<String> namespaces;
    private final ProcessContents processContents;

    public Wildcard(NamespaceConstraint constraint, List<String> namespaces, ProcessContents processContents) {
        this.constraint = constraint;
        this.namespaces = namespaces == null ? Collections.emptyList() : namespaces;
        this.processContents = processContents;
    }

    public NamespaceConstraint getConstraint() {
        return constraint;
    }

    /**
     * @return the namespaces listed (LIST) or excluded (NOT); the null/absent namespace is the
     * empty string
     */
    public List<String> getNamespaces() {
        return namespaces;
    }

    public ProcessContents getProcessContents() {
        return processContents;
    }

    /**
     * @param namespace an element namespace (null or empty for none)
     * @return true if an element of this namespace is accepted by the wildcard
     */
    public boolean accepts(String namespace) {
        String ns = namespace == null ? "" : namespace;
        switch (constraint) {
            case ANY:
                return true;
            case NOT:
                return !namespaces.contains(ns);
            case LIST:
            default:
                return namespaces.contains(ns);
        }
    }

    @Override
    public String toString() {
        return "any(" + constraint + namespaces + ")";
    }
}
