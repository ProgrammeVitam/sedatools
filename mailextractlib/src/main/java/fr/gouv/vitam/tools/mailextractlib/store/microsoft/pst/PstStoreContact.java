/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst;

import com.pff.PSTContact;

import java.io.PrintStream;
import java.util.LinkedHashSet;

/**
 * StoreMessage sub-class for mail boxes extracted through libpst library.
 */
public class PstStoreContact {

    /**
     * The containing PST folder
     **/
    private PstStoreFolder pstStoreFolder;

    /**
     * The contact.
     */
    private PSTContact contact;

    /**
     * Instantiates a new pst store message.
     *
     * @param pstStoreFolder the store folder containing the contact
     * @param contact        the contact
     */
    public PstStoreContact(PstStoreFolder pstStoreFolder, PSTContact contact) {
        this.pstStoreFolder = pstStoreFolder;
        this.contact = contact;
    }

    private String getFullName() {
        String tmp1, tmp2;
        tmp1 = contact.getAccount();
        tmp2 = contact.getDisplayName();
        if ((tmp1 == null) || tmp1.isEmpty())
            return tmp2;
        if ((tmp2 == null) || tmp2.isEmpty())
            return tmp1;
        if (tmp1.toLowerCase().equals(tmp2.toLowerCase()))
            return tmp1;
        return tmp1 + "/" + tmp2;
    }

    private String getStringFromSet(LinkedHashSet<String> set) {
        String result = "";
        for (String s : set) {
            if ((s != null) && !s.isEmpty())
                result += s + ";";
        }
        if (!result.isEmpty())
            result = result.substring(0, result.length() - 1);
        return result;
    }

    private String getSMTPAddresses() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add(contact.getSMTPAddress());
        if (contact.getEmail1AddressType().toLowerCase().equals("smtp"))
            set.add(contact.getEmail1EmailAddress());
        if (contact.getEmail2AddressType().toLowerCase().equals("smtp"))
            set.add(contact.getEmail2EmailAddress());
        if (contact.getEmail3AddressType().toLowerCase().equals("smtp"))
            set.add(contact.getEmail3EmailAddress());
        return getStringFromSet(set);
    }

    private String getOtherMailAddresses() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        if (!contact.getEmail1AddressType().toLowerCase().equals("smtp"))
            set.add(contact.getEmail1EmailAddress());
        if (!contact.getEmail2AddressType().toLowerCase().equals("smtp"))
            set.add(contact.getEmail2EmailAddress());
        if (!contact.getEmail3AddressType().toLowerCase().equals("smtp"))
            set.add(contact.getEmail3EmailAddress());
        return getStringFromSet(set);
    }

    private String getBusinessTelephoneNumbers() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add(contact.getBusinessTelephoneNumber());
        set.add(contact.getBusiness2TelephoneNumber());
        set.add(contact.getCompanyMainPhoneNumber());
        return getStringFromSet(set);
    }

    private String getHomeTelephoneNumbers() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add(contact.getHomeTelephoneNumber());
        set.add(contact.getHome2TelephoneNumber());
        return getStringFromSet(set);
    }

    private String getMobileTelephoneNumbers() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add(contact.getMobileTelephoneNumber());
        set.add(contact.getRadioTelephoneNumber());
        return getStringFromSet(set);
    }

    private String getOtherTelephoneNumbers() {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add(contact.getCarTelephoneNumber());
        set.add(contact.getOtherTelephoneNumber());
        set.add(contact.getPagerTelephoneNumber());
        set.add(contact.getPrimaryFaxNumber());
        set.add(contact.getBusinessFaxNumber());
        set.add(contact.getHomeFaxNumber());
        set.add(contact.getIsdnNumber());
        return getStringFromSet(set);
    }

    private String getTitle() {
        String tmp1, tmp2;
        tmp1 = contact.getTitle();
        tmp2 = contact.getProfession();
        if ((tmp1 == null) || tmp1.isEmpty())
            return tmp2;
        if ((tmp2 == null) || tmp2.isEmpty())
            return tmp1;
        if (tmp1.toLowerCase().equals(tmp2.toLowerCase()))
            return tmp1;
        return tmp1 + "/" + tmp2;
    }

    private String getBusinessAddress() {
        String tmp, line = "", result = "";
        tmp = contact.getBusinessAddressStreet();
        if (!tmp.isEmpty())
            result = "L1:" + tmp + "\n";
        tmp = contact.getBusinessPostalCode();
        if (!tmp.isEmpty())
            line = tmp + " ";
        tmp = contact.getBusinessAddressCity();
        if (!tmp.isEmpty())
            line += tmp + " ";
        tmp = contact.getBusinessPoBox();
        if (!tmp.isEmpty())
            line += "POB:" + tmp + " ";
        if (!line.isEmpty()) {
            result += "L2:" + line.substring(0, line.length() - 1) + "\n";
            line = "";
        }
        tmp = contact.getBusinessAddressCountry();
        if (!tmp.isEmpty())
            line += tmp;
        tmp = contact.getBusinessAddressStateOrProvince();
        if (!tmp.isEmpty() && !tmp.toLowerCase().equals(line.toLowerCase())) {
            if (line.isEmpty())
                line = tmp;
            else line += "/" + tmp;
        }
        if (!line.isEmpty()) {
            result += "L3:" + line + "\n";
        }
        return result;
    }

    private String getHomeAddress() {
        String tmp, line = "", result = "";
        tmp = contact.getHomeAddressStreet();
        if (!tmp.isEmpty())
            result = "L1:" + tmp + "\n";
        tmp = contact.getHomeAddressPostalCode();
        if (!tmp.isEmpty())
            line = tmp + " ";
        tmp = contact.getHomeAddressCity();
        if (!tmp.isEmpty())
            line += tmp + " ";
        if (!line.isEmpty()) {
            result += "L2:" + line.substring(0, line.length() - 1) + "\n";
            line = "";
        }
        tmp = contact.getHomeAddressPostOfficeBox();
        if (!tmp.isEmpty())
            line += "POB:" + tmp + " ";
        tmp = contact.getHomeAddressCountry();
        if (!tmp.isEmpty())
            line += tmp;
        tmp = contact.getHomeAddressStateOrProvince();
        if (!tmp.isEmpty() && !tmp.toLowerCase().equals(line.toLowerCase())) {
            if (line.isEmpty())
                line = tmp;
            else line += "/" + tmp;
        }
        if (!line.isEmpty()) {
            result += "L3:" + line + "\n";
        }
        return result;
    }

    // the global contacts list identifier
    static public String EXTRACTED_CONTACTS_LIST = "contactsList";

    /**
     * Print the header for contacts list csv file
     *
     * @param ps the dedicated print stream
     */
    static public void printGlobalListCSVHeader(PrintStream ps) {
        ps.println("Nom complet|Prénom|Nom|Notes|Organisation|Service|Titre|Adresse défaut|" +
                "Adresse smtp|Tel défaut|Tel mobile|Site pro|Localisation pro|Tel pro|Adresse pro|RefID|" +
                "Autres adresse messagerie|Autres tel|Nom secrétaire|Tel secrétaire|Site perso|" +
                "Localisation|Tel perso|Adresse perso|Surnom");
    }

    /**
     * Gets contacts global list name used for the csv file name construction.
     *
     * @return the global list name
     */
    static public String getGlobalListName() {
        return "contacts";
    }

    /**
     * Write to the contacts list, after initialising it if first contact
     *
     * @param writeFlag the write flag
     */
    public void writeToContactsList(boolean writeFlag) {
        if (writeFlag && pstStoreFolder.getStoreExtractor().getOptions().extractObjectsLists) {
            PrintStream ps = pstStoreFolder.getStoreExtractor().getInitializedGlobalListPS(this.getClass());

            ps.format("\"%s\"|", filterHyphen(getFullName()));
            ps.format("\"%s\"|", filterHyphen(contact.getGivenName()));
            ps.format("\"%s\"|", filterHyphen(contact.getSurname()));
            ps.format("\"%s\"|", filterHyphen(contact.getBody()));
            ps.format("\"%s\"|", filterHyphen(contact.getCompanyName()));
            ps.format("\"%s\"|", filterHyphen(contact.getDepartmentName()));
            ps.format("\"%s\"|", filterHyphen(getTitle()));
            ps.format("\"%s\"|", filterHyphen(contact.getPostalAddress()));
            ps.format("\"%s\"|", filterHyphen(getSMTPAddresses()));
            ps.format("\"%s\"|", filterHyphen(contact.getPrimaryTelephoneNumber()));
            ps.format("\"%s\"|", filterHyphen(getMobileTelephoneNumbers()));
            ps.format("\"%s\"|", filterHyphen(contact.getBusinessHomePage()));
            ps.format("\"%s\"|", filterHyphen(contact.getOfficeLocation()));
            ps.format("\"%s\"|", filterHyphen(getBusinessTelephoneNumbers()));
            ps.format("\"%s\"|", filterHyphen(getBusinessAddress()));
            ps.format("\"%s\"|", filterHyphen(contact.getCustomerId()));
            ps.format("\"%s\"|", filterHyphen(getOtherMailAddresses()));
            ps.format("\"%s\"|", filterHyphen(getOtherTelephoneNumbers()));
            ps.format("\"%s\"|", filterHyphen(contact.getAssistant()));
            ps.format("\"%s\"|", filterHyphen(contact.getAssistantTelephoneNumber()));
            ps.format("\"%s\"|", filterHyphen(contact.getPersonalHomePage()));
            ps.format("\"%s\"|", filterHyphen(contact.getLocation()));
            ps.format("\"%s\"|", filterHyphen(getHomeTelephoneNumbers()));
            ps.format("\"%s\"|", filterHyphen(getHomeAddress()));
            ps.format("\"%s\"|", filterHyphen(contact.getNickname()));

            // are not extracted the following values from java-libpst
            // getCallbackTelephoneNumber, getInitials, getKeyword, getLanguage, getMhsCommonName,
            // getOrganizationalIdNumber, getMiddleName, getDisplayNamePrefix, getPreferredByName,
            // getSpouseName, getComputerNetworkName, getTtytddPhoneNumber, getChildrensNames,
            // getTransmittableDisplayName, getTelexNumber, getHobbies, getOriginalDisplayName,
            // getOtherAddressCity, getOtherAddressCountry, getOtherAddressStateOrProvince,
            // getOtherAddressStreet, getOtherAddressPostOfficeBox, getOriginalDisplayName,
            // getFtpSite, getManagerName

            ps.println();
            ps.flush();
        }
    }

    private String filterHyphen(String s) {
        return s.replace("\"", " ");
    }

}
