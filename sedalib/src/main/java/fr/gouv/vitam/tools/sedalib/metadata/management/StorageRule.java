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
package fr.gouv.vitam.tools.sedalib.metadata.management;

import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class StorageRule.
 * <p>
 * Class for SEDA element StorageRule
 * <p>
 * A management ArchiveUnit metadata.
 * <p>
 * Standard quote: "Gestion de la durée d’utilité courante"
 */
public class StorageRule extends RuleType {

    static final String STORAGERULE_TAG="StorageRule";

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put(RULE_TAG, new ComplexListMetadataKind(Rule.class, true));
        metadataMap.put(PREVENTINHERITANCE_TAG, new ComplexListMetadataKind(BooleanType.class, false));
        metadataMap.put(REFNONRULEID_TAG, new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put(FINALACTION_TAG, new ComplexListMetadataKind(StringType.class, false));
    }

    /**
     * The final action list.
     */
    protected static 
List<String> finalActionList;

    static {
        finalActionList = new ArrayList<>();
        finalActionList.add("RestrictAccess");
        finalActionList.add("Transfer");
        finalActionList.add("Copy");
    }

    /**
     * Instantiates a new access rule.
     */
    public StorageRule() {
        super(STORAGERULE_TAG);
    }

    /**
     * Instantiates a new storage rule, with one rule and a date.
     *
     * @param rule      the rule
     * @param startDate the start date
     * @throws SEDALibException the seda lib exception
     */
    public StorageRule(String rule, LocalDate startDate) throws SEDALibException {
        super(STORAGERULE_TAG, rule , startDate);
    }

    /**
     * Instantiates a new storage rule, with one rule, a date and final action.
     *
     * @param rule        the rule
     * @param startDate   the start date
     * @param finalAction the final action
     * @throws SEDALibException if the FinalAction field or value is not expected in                          this kind of rule
     */
    public StorageRule(String rule, LocalDate startDate, String finalAction) throws SEDALibException {
        super(STORAGERULE_TAG, rule, startDate);
        setFinalAction(finalAction);
    }

    @Override
    public List<String> getFinalActionList() {
        return finalActionList;
    }
}
