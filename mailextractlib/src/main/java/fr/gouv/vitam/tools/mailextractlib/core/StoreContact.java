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

package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

import java.io.File;
import java.io.PrintStream;

/**
 * Abstract class for store element which is a contact.
 * <p>
 * It defines all informations to collect from a contact. Each subclass has to
 * be able to collect these generic informations from a native contact format.
 * <p>
 */
public abstract class StoreContact extends StoreElement {

    /**
     * All contact informations.
     */
    protected int listLineId;
    protected String fullName, givenName, lastName, nickName;
    protected String postalAddress, homeAddress, homeLocation, businessAddress, businessLocation;
    protected String companyName, departmentName, title;
    protected String primaryTelephoneNumber, businessTelephoneNumbers, homeTelephoneNumbers, mobileTelephoneNumbers, otherTelephoneNumbers;
    protected String smtpAddress, otherMailAddresses;
    protected String assistantName, assistantTelephoneNumber;
    protected String personalHomePage, businessHomePage;
    protected String customerId;
    protected String miscNotes;

    /**
     * Contact picture if any.
     */
    protected String pictureFileName;
    protected byte[] pictureData;

    /**
     * Instantiates a new contact.
     *
     * @param storeFolder Mail box folder containing this contact
     */
    protected StoreContact(StoreFolder storeFolder) {
        super(storeFolder);
    }

    @Override
    public String getLogDescription() {
        String result = "contact n°" + getStoreExtractor().getGlobalListCounter(this.getClass());
        if (fullName != null)
            result += " [" + fullName + "]";
        else if ((givenName != null) || (lastName != null))
            result += " ["+(givenName!=null?givenName:"NoGivenName")+" "+(lastName!=null?lastName:"NoLastName")+"]";
        else
            result += " [no name]";
        return result;
    }

    /**
     * Analyze contact to collect contact information (protocol
     * specific).
     * <p>
     * This is the method for sub classes, where all contact
     * information has to be extracted in standard representation out of the
     * inner representation of the contact.
     *
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    abstract public void analyzeAllContactInformations() throws MailExtractLibException, InterruptedException;

    /**
     * Analyze contact to collect contact picture if any.
     * <p>
     * This is the methode for sub classes, where a contact
     * picture has to be extracted in standard representation out of the
     * inner representation of the contact.
     *
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    abstract public void analyzeContactPicture() throws MailExtractLibException, InterruptedException;

    /**
     * Analyze contact to collect contact information and optionally picture.
     **/
    public void analyzeContact() throws MailExtractLibException, InterruptedException {
        analyzeAllContactInformations();
        try {
            analyzeContactPicture();
        } catch (MailExtractLibException ignored) {
        }
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
     * Print the header for contacts list csv file
     *
     * @param ps the dedicated print stream
     */
    static public void printGlobalListCSVHeader(PrintStream ps) {
        ps.println("ID;Full Name;Given Name;Last Name;Misc Notes;Company;Department;Title;Default Address;" +
                "SMTP Mail Address;Default Telephone Number;Mobile Telephone Number;Business HomePage;Business Location;" +
                "Business Telephone Number;Business Address;RefID;" +
                "Other Mail Addresses;Other Telephone Numbers;Assistant Name;Assistant Telephone Number;Personal HomePage;" +
                "Home Location;Home Telephone Number;Home Address;Nickname");
    }

    /**
     * Extract to the contacts list, after initialising it if first contact, and to the contact picture file.
     * <p>The picture file is saved in a directory called "Contacts Pictures" and is named using the line number
     * listLineId as "contact_id".ext with the extension of the original file.******************************
     *
     * @param writeFlag the write flag
     */
    public void extractContact(boolean writeFlag) throws InterruptedException, MailExtractLibException {
        if (writeFlag && storeFolder.getStoreExtractor().getOptions().extractObjectsLists) {
            writeToContactsList();
            if (pictureData != null)
                extractPicture();
        }
    }

    private void writeToContactsList() {
        PrintStream ps = storeFolder.getStoreExtractor().getGlobalListPS(this.getClass());
        ps.format("\"%d\";", listLineId);
        ps.format("\"%s\";", filterHyphen(fullName));
        ps.format("\"%s\";", filterHyphen(givenName));
        ps.format("\"%s\";", filterHyphen(lastName));
        ps.format("\"%s\";", filterHyphen(miscNotes));
        ps.format("\"%s\";", filterHyphen(companyName));
        ps.format("\"%s\";", filterHyphen(departmentName));
        ps.format("\"%s\";", filterHyphen(title));
        ps.format("\"%s\";", filterHyphen(postalAddress));
        ps.format("\"%s\";", filterHyphen(smtpAddress));
        ps.format("\"%s\";", filterHyphen(primaryTelephoneNumber));
        ps.format("\"%s\";", filterHyphen(mobileTelephoneNumbers));
        ps.format("\"%s\";", filterHyphen(businessHomePage));
        ps.format("\"%s\";", filterHyphen(businessLocation));
        ps.format("\"%s\";", filterHyphen(businessTelephoneNumbers));
        ps.format("\"%s\";", filterHyphen(businessAddress));
        ps.format("\"%s\";", filterHyphen(customerId));
        ps.format("\"%s\";", filterHyphen(otherMailAddresses));
        ps.format("\"%s\";", filterHyphen(otherTelephoneNumbers));
        ps.format("\"%s\";", filterHyphen(assistantName));
        ps.format("\"%s\";", filterHyphen(assistantTelephoneNumber));
        ps.format("\"%s\";", filterHyphen(personalHomePage));
        ps.format("\"%s\";", filterHyphen(homeLocation));
        ps.format("\"%s\";", filterHyphen(homeTelephoneNumbers));
        ps.format("\"%s\";", filterHyphen(homeAddress));
        ps.format("\"%s\"", filterHyphen(nickName));
        ps.println("");
        ps.flush();
    }

    private String filterHyphen(String s) {
        return s.replace("\"", " ");
    }

    private void extractPicture() throws InterruptedException, MailExtractLibException {
        ArchiveUnit attachmentNode = new ArchiveUnit(storeFolder.storeExtractor, storeFolder.storeExtractor.destRootPath +
                File.separator + storeFolder.storeExtractor.destName + File.separator + "contacts", "ContactPicture#" + listLineId);
        attachmentNode.addMetadata("DescriptionLevel", "Item", true);
        attachmentNode.addMetadata("Title", "Contact Picture #" + listLineId, true);
        attachmentNode.addMetadata("Description", "Contact picture extracted for " + fullName, true);
        attachmentNode.addPersonMetadata("Recipient", fullName + (smtpAddress.isEmpty() ? "" : "<" + smtpAddress + ">"), false);
        attachmentNode.addObject(pictureData, pictureFileName, "BinaryMaster", 1);
        attachmentNode.write();
    }

    @Override
    public void processElement(boolean writeFlag) throws InterruptedException, MailExtractLibException {
        listLineId = storeFolder.getStoreExtractor().incGlobalListCounter(this.getClass());
        analyzeContact();
        extractContact(writeFlag);
    }

    @Override
    public void listElement(boolean statsFlag) throws InterruptedException, MailExtractLibException {
        listLineId = storeFolder.getStoreExtractor().incGlobalListCounter(this.getClass());
    }
}
