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
package fr.gouv.vitam.tools.mailextractlib.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for a list of metadata XML representation
 * <p>
 * This very simple class is aimed to construct and write metadata XML
 * structure.
 */
public class MetadataXMLList extends MetadataXML {

    /** The XML node list. */
    private List<MetadataXMLNode> nodelist;

    /**
     * Instantiates a new metadata XML list.
     */
    public MetadataXMLList() {
        this.nodelist = new ArrayList<MetadataXMLNode>();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MetaData#isEmpty()
     */
    public boolean isEmpty() {
        return nodelist.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MetaData#writeJSON(int)
     */
    protected String writeXML(int depth) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (MetadataXMLNode node : nodelist) {
            if (!first) {
                sb.append('\n');
            } else {
                first = false;
            }
            sb.append(node.writeXML(depth + 1));
        }

        return sb.toString();
    }

    /**
     * Adds an MetadataXMLNode to the list.
     *
     * @param metadataxmlnode
     *            the metadata xml node
     */
    public void addMetadataXMLNode(MetadataXMLNode metadataxmlnode) {
        if ((metadataxmlnode != null) && !metadataxmlnode.isEmpty()) nodelist.add(metadataxmlnode);
    }
}
