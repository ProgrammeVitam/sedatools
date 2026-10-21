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

/**
 * A particle of a content model: a term (element, group or wildcard) with its occurrence.
 */
public final class Particle {

    private final Occurrence occurrence;
    private final ElementDef element;
    private final Group group;
    private final Wildcard wildcard;

    private Particle(Occurrence occurrence, ElementDef element, Group group, Wildcard wildcard) {
        this.occurrence = occurrence;
        this.element = element;
        this.group = group;
        this.wildcard = wildcard;
    }

    public static Particle of(ElementDef element, Occurrence occurrence) {
        return new Particle(occurrence, element, null, null);
    }

    public static Particle of(Group group, Occurrence occurrence) {
        return new Particle(occurrence, null, group, null);
    }

    public static Particle of(Wildcard wildcard, Occurrence occurrence) {
        return new Particle(occurrence, null, null, wildcard);
    }

    public Occurrence getOccurrence() {
        return occurrence;
    }

    public boolean isElement() {
        return element != null;
    }

    public boolean isGroup() {
        return group != null;
    }

    public boolean isWildcard() {
        return wildcard != null;
    }

    public ElementDef getElement() {
        return element;
    }

    public Group getGroup() {
        return group;
    }

    public Wildcard getWildcard() {
        return wildcard;
    }

    @Override
    public String toString() {
        return (isElement() ? element.getName() : isGroup() ? group.toString() : wildcard.toString()) + occurrence;
    }
}
