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
package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.metadata.management.AccessRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.ClassificationRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.DisseminationRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.HoldRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.ReuseRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.StorageRule;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataMap;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * The Class ManagementMetadata.
 * <p>
 * Class for SEDA element ManagementMetadata.
 * <p>
 * In DataObjectPackage metadata.
 * <p>
 * Standard quote: "Bloc des métadonnées de gestion par défaut des
 * objets-données."
 */
public class ManagementMetadata extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true)
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put("ArchivalProfile",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("ServiceLevel", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("AcquisitionInformation",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("LegalStatus", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("OriginatingAgencyIdentifier",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("SubmissionAgencyIdentifier",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("StorageRule", new ComplexListMetadataKind(StorageRule.class, false));
        metadataMap.put("AppraisalRule", new ComplexListMetadataKind(AppraisalRule.class, false));
        metadataMap.put("AccessRule", new ComplexListMetadataKind(AccessRule.class, false));
        metadataMap.put("DisseminationRule",
                new ComplexListMetadataKind(DisseminationRule.class, false));
        metadataMap.put("ReuseRule", new ComplexListMetadataKind(ReuseRule.class, false));
        metadataMap.put("ClassificationRule",
                new ComplexListMetadataKind(ClassificationRule.class, false));
        metadataMap.put("HoldRule",
                new ComplexListMetadataKind(HoldRule.class, false));
        metadataMap.put("LogBook", new ComplexListMetadataKind(AnyXMLType.class, false));
        metadataMap.put("NeedAuthorization",
                new ComplexListMetadataKind(StringType.class, false));
    }

    /**
     * Instantiates a new Management.
     */
    public ManagementMetadata() {
        super("ManagementMetadata");
    }

    // FIXME: est-ce vraiment pertinent de tordre la logique systématique des constructeurs pour ce cas?

    /**
     * Adds a new metadata, or replace it if it exists and the metadata can't have
     * many values. For RuleType metadata, if an instance already exists, adds the args
     * as new rules to the existing RuleType instance rather than creating a new one.
     * This is a flexible constructor used to simplify metadata management.
     *
     * @param elementName The name of the element to add
     * @param args        The arguments to construct the metadata
     * @throws SEDALibException if metadata construction fails
     */
    @Override
    public void addNewMetadata(String elementName, Object... args) throws SEDALibException {
        ComplexListMetadataKind metadataKind = getMetadataMap().get(elementName);

        if (RuleType.class.isAssignableFrom(metadataKind.getMetadataClass())) {
            int ruleIndex = 0;
            for (int i = 0; i < metadataList.size(); i++) {
                if (metadataList.get(i).getXmlElementName().equals(elementName)) {
                    ruleIndex = i;
                }
            }
            if (ruleIndex != 0) {
                ((RuleType) metadataList.get(ruleIndex)).addNewMetadata("Rule", args);
                return;
            }
        }
        super.addNewMetadata(elementName, args);
    }

    // FIXME: est-ce vraiment pertinent de tordre la logique systématique des constructeurs pour ce cas?

    /**
     * Adds a metadata object that matches the element name in the metadata map.
     * If the metadata is a RuleType and one already exists with same element name, merges the metadata lists.
     * Otherwise delegates to parent class implementation.
     *
     * @param sedaMetadata the metadata object to add
     * @throws SEDALibException if metadata does not match element name in metadata map
     */
    @Override
    public void addMetadata(SEDAMetadata sedaMetadata) throws SEDALibException {
        ComplexListMetadataKind metadataKind = getMetadataMap().get(sedaMetadata.getXmlElementName());

        if (RuleType.class.isAssignableFrom(metadataKind.getMetadataClass())) {
            int ruleIndex = 0;
            for (int i = 0; i < metadataList.size(); i++) {
                if (metadataList.get(i).getXmlElementName().equals(sedaMetadata.getXmlElementName())) {
                    ruleIndex = i;
                }
            }
            if (ruleIndex != 0) {
                RuleType ruleType = ((RuleType) metadataList.get(ruleIndex));
                for (SEDAMetadata metadata : ((RuleType) sedaMetadata).getMetadataList()) {
                    ruleType.addMetadata(metadata);
                }
                super.addMetadata(ruleType);
                return;
            }
        }
        super.addMetadata(sedaMetadata);
    }
}