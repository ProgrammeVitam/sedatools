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
package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

import java.io.File;
import java.io.PrintStream;

/**
 * Abstract class for a store element representing a contact.
 * <p>
 * This class defines all the metadata to be collected from a contact and provides a base
 * for format-specific implementations. A contact typically contains personal or professional
 * information, such as names, addresses, email addresses, phone numbers, and optional details
 * like profile pictures or notes.
 * <p>
 * Subclasses of this class are responsible for extracting contact information from specific
 * native formats and mapping it to the standardized representation defined here.
 * <p>
 * Metadata to collect:
 * <ul>
 *     <li>Full name, first name, last name, and nickname,</li>
 *     <li>Home and business addresses with location details,</li>
 *     <li>Email addresses and phone numbers (personal, business, mobile, etc.),</li>
 *     <li>Professional details (company name, department, title),</li>
 *     <li>Optional profile picture and personal or business notes.</li>
 * </ul>
 * <p>
 * Subclasses must implement methods for:
 * <ul>
 *     <li>Analyzing and extracting contact information specific to the native format,</li>
 *     <li>Processing and exporting metadata for archival or reporting purposes.</li>
 * </ul>
 */
public abstract class StoreContact extends StoreElement {

    /**
     * All contact informations.
     */
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
        String result = "contact " + listLineId;
        if (fullName != null) result += " [" + fullName + "]";
        else if ((givenName != null) || (lastName != null)) result +=
        " [" +
        (givenName != null ? givenName : "NoGivenName") +
        " " +
        (lastName != null ? lastName : "NoLastName") +
        "]";
        else result += " [no name]";
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
    public abstract void analyzeAllContactInformations() throws MailExtractLibException, InterruptedException;

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
    public abstract void analyzeContactPicture() throws MailExtractLibException, InterruptedException;

    /**
     * Analyze contact to collect contact information and optionally picture.
     *
     * @throws MailExtractLibException the mail extract lib exception
     * @throws InterruptedException    the interrupted exception
     */
    public void analyzeContact() throws MailExtractLibException, InterruptedException {
        analyzeAllContactInformations();
        try {
            analyzeContactPicture();
        } catch (MailExtractLibException ignored) {}
    }

    /**
     * Gets element name used for the csv file name construction.
     *
     * @return the element name
     */
    public static String getElementName() {
        return "contacts";
    }

    /**
     * Print the header for contacts list csv file
     *
     * @param ps the dedicated print stream
     */
    public static void printGlobalListCSVHeader(PrintStream ps) {
        synchronized (ps) {
            ps.println(
                "ID;Full Name;Given Name;Last Name;Misc Notes;Company;Department;Title;Default Address;" +
                "SMTP Mail Address;Default Telephone Number;Mobile Telephone Number;Business HomePage;Business Location;" +
                "Business Telephone Number;Business Address;RefID;" +
                "Other Mail Addresses;Other Telephone Numbers;Assistant Name;Assistant Telephone Number;Personal HomePage;" +
                "Home Location;Home Telephone Number;Home Address;Nickname"
            );
        }
    }

    /**
     * Extract to the contacts list, after initialising it if first contact, and to the contact picture file.
     * <p>The picture file is saved in a directory called "Contacts Pictures" and is named using the line number
     * listLineId as "contact_id".ext with the extension of the original file.******************************
     *
     * @param writeFlag the write flag
     * @throws InterruptedException    the interrupted exception
     * @throws MailExtractLibException the mail extract lib exception
     */
    public void extractContact(boolean writeFlag) throws InterruptedException, MailExtractLibException {
        if (writeFlag) {
            if (storeFolder.getStoreExtractor().getOptions().extractElementsList) writeToContactsList();
            if (
                (pictureData != null) && storeFolder.getStoreExtractor().getOptions().extractElementsContent
            ) extractPicture();
        }
    }

    private void writeToContactsList() {
        PrintStream ps = storeFolder.getStoreExtractor().getGlobalListPS(this.getClass());
        synchronized (ps) {
            ps.format("\"%d\";", listLineId);
            ps.format("\"%s\";", filterHyphenForCsv(fullName));
            ps.format("\"%s\";", filterHyphenForCsv(givenName));
            ps.format("\"%s\";", filterHyphenForCsv(lastName));
            ps.format("\"%s\";", filterHyphenForCsv(miscNotes));
            ps.format("\"%s\";", filterHyphenForCsv(companyName));
            ps.format("\"%s\";", filterHyphenForCsv(departmentName));
            ps.format("\"%s\";", filterHyphenForCsv(title));
            ps.format("\"%s\";", filterHyphenForCsv(postalAddress));
            ps.format("\"%s\";", filterHyphenForCsv(smtpAddress));
            ps.format("\"%s\";", filterHyphenForCsv(primaryTelephoneNumber));
            ps.format("\"%s\";", filterHyphenForCsv(mobileTelephoneNumbers));
            ps.format("\"%s\";", filterHyphenForCsv(businessHomePage));
            ps.format("\"%s\";", filterHyphenForCsv(businessLocation));
            ps.format("\"%s\";", filterHyphenForCsv(businessTelephoneNumbers));
            ps.format("\"%s\";", filterHyphenForCsv(businessAddress));
            ps.format("\"%s\";", filterHyphenForCsv(customerId));
            ps.format("\"%s\";", filterHyphenForCsv(otherMailAddresses));
            ps.format("\"%s\";", filterHyphenForCsv(otherTelephoneNumbers));
            ps.format("\"%s\";", filterHyphenForCsv(assistantName));
            ps.format("\"%s\";", filterHyphenForCsv(assistantTelephoneNumber));
            ps.format("\"%s\";", filterHyphenForCsv(personalHomePage));
            ps.format("\"%s\";", filterHyphenForCsv(homeLocation));
            ps.format("\"%s\";", filterHyphenForCsv(homeTelephoneNumbers));
            ps.format("\"%s\";", filterHyphenForCsv(homeAddress));
            ps.format("\"%s\"", filterHyphenForCsv(nickName));
            ps.println("");
            ps.flush();
        }
    }

    private void extractPicture() throws InterruptedException, MailExtractLibException {
        ArchiveUnit attachmentNode = new ArchiveUnit(
            storeFolder.storeExtractor,
            storeFolder.storeExtractor.destRootPath +
            File.separator +
            storeFolder.storeExtractor.destName +
            File.separator +
            "contacts",
            "ContactPicture#" + listLineId
        );
        attachmentNode.addMetadata("DescriptionLevel", "Item", true);
        attachmentNode.addMetadata("Title", "Contact Picture #" + listLineId, true);
        attachmentNode.addMetadata("Description", "Contact picture extracted for " + fullName, true);
        attachmentNode.addPersonMetadata(
            "Recipient",
            fullName + (smtpAddress.isEmpty() ? "" : "<" + smtpAddress + ">"),
            false
        );
        attachmentNode.addObject(pictureData, pictureFileName, "BinaryMaster", 1);
        attachmentNode.write();
    }

    @Override
    public void processElement(boolean writeFlag) throws InterruptedException, MailExtractLibException {
        if (storeFolder.getStoreExtractor().getOptions().extractContacts) {
            listLineId = storeFolder.getStoreExtractor().incElementCounter(this.getClass());
            analyzeContact();
            extractContact(writeFlag);
        }
    }

    @Override
    public void listElement(boolean statsFlag) throws InterruptedException, MailExtractLibException {
        if (storeFolder.getStoreExtractor().getOptions().extractContacts) listLineId = storeFolder
            .getStoreExtractor()
            .incElementCounter(this.getClass());
    }
}
