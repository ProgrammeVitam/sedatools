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

    static final String HOLDRULE_TAG="HoldRule";
    static final String HOLDENDDATE_TAG="HoldEndDate";
    static final String HOLDOWNER_TAG="HoldOwner";
    static final String HOLDREASSESSINGDATE_TAG="HoldReassessingDate";
    static final String HOLDREASON_TAG="HoldReason";
    static final String HOLDPREVENTREARRANGEMENT_TAG="PreventRearrangement";

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
