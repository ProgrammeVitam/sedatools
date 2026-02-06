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
package fr.gouv.vitam.tools.javalibpst;

import java.io.IOException;
import java.util.HashMap;

/**
 * Object that represents a RSS item
 *
 * @author Richard Johnson
 */
public class PSTRss extends PSTMessage {

    /**
     * Instantiates a new Pst rss.
     *
     * @param theFile             the the file
     * @param descriptorIndexNode the descriptor index node
     * @throws PSTException the pst exception
     * @throws IOException  the io exception
     */
    public PSTRss(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * Instantiates a new Pst rss.
     *
     * @param theFile              the the file
     * @param folderIndexNode      the folder index node
     * @param table                the table
     * @param localDescriptorItems the local descriptor items
     */
    public PSTRss(
        final PSTFile theFile,
        final DescriptorIndexNode folderIndexNode,
        final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems
    ) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * Channel
     *
     * @return the post rss channel link
     */
    public String getPostRssChannelLink() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008900, PSTFile.PSETID_PostRss));
    }

    /**
     * Item link
     *
     * @return the post rss item link
     */
    public String getPostRssItemLink() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008901, PSTFile.PSETID_PostRss));
    }

    /**
     * Item hash Integer 32-bit signed
     *
     * @return the post rss item hash
     */
    public int getPostRssItemHash() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008902, PSTFile.PSETID_PostRss));
    }

    /**
     * Item GUID
     *
     * @return the post rss item guid
     */
    public String getPostRssItemGuid() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008903, PSTFile.PSETID_PostRss));
    }

    /**
     * Channel GUID
     *
     * @return the post rss channel
     */
    public String getPostRssChannel() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008904, PSTFile.PSETID_PostRss));
    }

    /**
     * Item XML
     *
     * @return the post rss item xml
     */
    public String getPostRssItemXml() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008905, PSTFile.PSETID_PostRss));
    }

    /**
     * Subscription
     *
     * @return the post rss subscription
     */
    public String getPostRssSubscription() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008906, PSTFile.PSETID_PostRss));
    }

    @Override
    public String toString() {
        return (
            "Channel ASCII or Unicode string values: " +
            this.getPostRssChannelLink() +
            "\n" +
            "Item link ASCII or Unicode string values: " +
            this.getPostRssItemLink() +
            "\n" +
            "Item hash Integer 32-bit signed: " +
            this.getPostRssItemHash() +
            "\n" +
            "Item GUID ASCII or Unicode string values: " +
            this.getPostRssItemGuid() +
            "\n" +
            "Channel GUID ASCII or Unicode string values: " +
            this.getPostRssChannel() +
            "\n" +
            "Item XML ASCII or Unicode string values: " +
            this.getPostRssItemXml() +
            "\n" +
            "Subscription ASCII or Unicode string values: " +
            this.getPostRssSubscription()
        );
    }
}
