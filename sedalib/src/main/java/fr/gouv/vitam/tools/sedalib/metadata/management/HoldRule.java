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

public class HoldRule extends RuleType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true)
    static final public LinkedHashMap<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put(RULE_TAG, new ComplexListMetadataKind(Rule.class, true));
        metadataMap.put("HoldEndDate", new RuleMetadataKind(DateType.class, true));
        metadataMap.put("HoldOwner", new RuleMetadataKind(StringType.class, true));
        metadataMap.put("HoldReassessingDate", new RuleMetadataKind(DateType.class, true));
        metadataMap.put("HoldReason", new RuleMetadataKind(StringType.class, true));
        metadataMap.put("PreventRearrangement", new RuleMetadataKind(BooleanType.class, true));

        metadataMap.put("PreventInheritance", new ComplexListMetadataKind(BooleanType.class, false));
        metadataMap.put("RefNonRuleId", new ComplexListMetadataKind(StringType.class, true));
    }

    /**
     * Instantiates a new hold rule.
     */
    public HoldRule() {
        super("HoldRule");
    }

    /**
     * Instantiates a new hold rule, with one rule and a date.
     *
     * @param rule the rule
     * @param startDate the start date
     * @throws SEDALibException the seda lib exception
     */
    public HoldRule(String rule, LocalDate startDate) throws SEDALibException {
        super("HoldRule", rule, startDate);
    }

    /**
     * Instantiates a new hold rule, with one rule, a date and other metadata.
     *
     * @param rule the rule
     * @param startDate the start date
     * @param otherMetadata the other metadata
     * @throws SEDALibException the seda lib exception
     */
    public HoldRule(String rule, LocalDate startDate, Map<String, Object> otherMetadata) throws SEDALibException {
        super("HoldRule", rule, startDate);
        for (Map.Entry<String, Object> entry : otherMetadata.entrySet()) {
            addNewMetadata(entry.getKey(), entry.getValue());
        }
    }

    public void addHoldEndDate(LocalDate holdEndDate) throws SEDALibException {
        addNewMetadata("HoldEndDate", holdEndDate);
    }

    public void addHoldOwner(String holdOwner) throws SEDALibException {
        addNewMetadata("HoldOwner", holdOwner);
    }

    public void addHoldReassessingDate(LocalDate holdReassessingDate) throws SEDALibException {
        addNewMetadata("HoldReassessingDate", holdReassessingDate);
    }

    public void addHoldReason(String holdReason) throws SEDALibException {
        addNewMetadata("HoldReason", holdReason);
    }

    public void addPreventRearrangement(boolean preventRearrangement) throws SEDALibException {
        addNewMetadata("PreventRearrangement", preventRearrangement);
    }
}
