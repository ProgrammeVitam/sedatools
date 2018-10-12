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
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.SchemeType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

/**
 * The Class ArchiveUnitProfile.
 * <p>
 * Class for SEDA element ArchiveUnitProfile.
 * <p>
 * A ArchiveUnitProfile metadata.
 * <p>
 * Standard quote: "Référence à une partie d'un profil d’archivage applicable à
 * un ArchiveUnit en particulier. Permet par exemple de faire référence à une
 * typologie documentaire dans un profil d'archivage."
 */
public class ArchiveUnitProfile extends SchemeType {

    /**
     * Instantiates a new ArchiveUnitProfile.
     */
    public ArchiveUnitProfile() {
        super("ArchiveUnitProfile");
    }

    /**
     * Instantiates a new ArchiveUnitProfile.
     *
     * @param value the value
     */
    public ArchiveUnitProfile(String value) {
        super("ArchiveUnitProfile", value);
    }

    /**
     * Instantiates a new ArchiveUnitProfile.
     *
     * @param value            the value
     * @param schemeAgencyID   the scheme agency ID
     * @param schemeAgencyName the scheme agency name
     * @param schemeDataURI    the scheme data URI
     * @param schemeID         the scheme ID
     * @param schemeName       the scheme name
     * @param schemeURI        the scheme URI
     * @param schemeVersionID  the scheme version ID
     */
    public ArchiveUnitProfile(String value, String schemeAgencyID, String schemeAgencyName,
                              String schemeDataURI, String schemeID, String schemeName, String schemeURI, String schemeVersionID) {
        super("ArchiveUnitProfile", value, schemeAgencyID, schemeAgencyName,
                schemeDataURI, schemeID, schemeName, schemeURI, schemeVersionID);
    }

    /**
     * Import the ArchiveUnitProfile in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read Content
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static ArchiveUnitProfile fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        ArchiveUnitProfile result = new ArchiveUnitProfile();
        result = (ArchiveUnitProfile) fromSedaXmlInObject(xmlReader, result);
        return result;
    }
}
