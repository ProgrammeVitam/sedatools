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
package fr.gouv.vitam.tools.sedalib.metadata.management;

import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.BooleanType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataMap;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DateType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The type Hold rule.
 */
public class HoldRule extends RuleType {

    static final String HOLDRULE_TAG = "HoldRule";
    static final String HOLDENDDATE_TAG = "HoldEndDate";
    static final String HOLDOWNER_TAG = "HoldOwner";
    static final String HOLDREASSESSINGDATE_TAG = "HoldReassessingDate";
    static final String HOLDREASON_TAG = "HoldReason";
    static final String HOLDPREVENTREARRANGEMENT_TAG = "PreventRearrangement";

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true)
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put(RULE_TAG, new ComplexListMetadataKind(Rule.class, true));
        metadataMap.put(HOLDENDDATE_TAG, new RuleMetadataKind(DateType.class, true));
        metadataMap.put(HOLDOWNER_TAG, new RuleMetadataKind(StringType.class, true));
        metadataMap.put(HOLDREASSESSINGDATE_TAG, new RuleMetadataKind(DateType.class, true));
        metadataMap.put(HOLDREASON_TAG, new RuleMetadataKind(StringType.class, true));
        metadataMap.put(HOLDPREVENTREARRANGEMENT_TAG, new RuleMetadataKind(BooleanType.class, true));

        metadataMap.put(PREVENTINHERITANCE_TAG, new ComplexListMetadataKind(BooleanType.class, false));
        metadataMap.put(REFNONRULEID_TAG, new ComplexListMetadataKind(StringType.class, true));
    }

    /**
     * Instantiates a new hold rule.
     */
    public HoldRule() {
        super(HOLDRULE_TAG);
    }

    /**
     * Instantiates a new hold rule, with one rule and a date.
     *
     * @param rule      the rule
     * @param startDate the start date
     * @throws SEDALibException the seda lib exception
     */
    public HoldRule(String rule, LocalDate startDate) throws SEDALibException {
        super(HOLDRULE_TAG, rule, startDate);
    }

    /**
     * Instantiates a new hold rule, with one rule, a date and other metadata.
     *
     * @param rule          the rule
     * @param startDate     the start date
     * @param otherMetadata the other metadata
     * @throws SEDALibException the seda lib exception
     */
    public HoldRule(String rule, LocalDate startDate, Map<String, Object> otherMetadata) throws SEDALibException {
        super(HOLDRULE_TAG, rule, startDate);
        for (Map.Entry<String, Object> entry : otherMetadata.entrySet()) {
            addNewMetadata(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add hold end date.
     *
     * @param holdEndDate the hold end date
     * @throws SEDALibException the seda lib exception
     */
    public void addHoldEndDate(LocalDate holdEndDate) throws SEDALibException {
        addNewMetadata(HOLDENDDATE_TAG, holdEndDate);
    }

    /**
     * Add hold owner.
     *
     * @param holdOwner the hold owner
     * @throws SEDALibException the seda lib exception
     */
    public void addHoldOwner(String holdOwner) throws SEDALibException {
        addNewMetadata(HOLDOWNER_TAG, holdOwner);
    }

    /**
     * Add hold reassessing date.
     *
     * @param holdReassessingDate the hold reassessing date
     * @throws SEDALibException the seda lib exception
     */
    public void addHoldReassessingDate(LocalDate holdReassessingDate) throws SEDALibException {
        addNewMetadata(HOLDREASSESSINGDATE_TAG, holdReassessingDate);
    }

    /**
     * Add hold reason.
     *
     * @param holdReason the hold reason
     * @throws SEDALibException the seda lib exception
     */
    public void addHoldReason(String holdReason) throws SEDALibException {
        addNewMetadata(HOLDREASON_TAG, holdReason);
    }

    /**
     * Add prevent rearrangement.
     *
     * @param preventRearrangement the prevent rearrangement
     * @throws SEDALibException the seda lib exception
     */
    public void addPreventRearrangement(boolean preventRearrangement) throws SEDALibException {
        addNewMetadata(HOLDPREVENTREARRANGEMENT_TAG, preventRearrangement);
    }
}
