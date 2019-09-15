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
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class RuleType.
 * <p>
 * For abstract Rule type SEDA metadata
 */
abstract public class RuleType extends ComplexListType {

    /**
     * Instantiates a new rule Type.
     *
     * @param elementName the element name
     */
    public RuleType(String elementName) {
        super(elementName);
    }

    /**
     * Instantiates a new rule type, with one rule and a date.
     *
     * @param elementName the element name
     * @param rule        the rule
     * @param startDate   the start date
     * @throws SEDALibException the seda lib exception
     */
    public RuleType(String elementName, String rule, LocalDate startDate) throws SEDALibException {
        super(elementName);
        addNewMetadata("Rule",rule,startDate);
    }

    /**
     * Adds a rule element with only the rule identifier.
     *
     * @param rule the rule
     */
    public void addRule(String rule) throws SEDALibException {
        addNewMetadata("Rule",rule,null);
    }

    /**
     * Adds a rule element with only the rule identifier and start date.
     *
     * @param rule      the rule
     * @param startDate the start date
     */
    public void addRule(String rule, LocalDate startDate) throws SEDALibException {
        addNewMetadata("Rule",rule,startDate);
    }

    /**
     * Sets the prevent inheritance.
     *
     * @param preventInheritance the new prevent inheritance
     */
    public void setPreventInheritance(boolean preventInheritance) throws SEDALibException {
        addNewMetadata("PreventInheritance",preventInheritance);
    }

    /**
     * Adds the ref non rule id.
     *
     * @param rule the rule
     */
    public void addRefNonRuleId(String rule) throws SEDALibException {
        addNewMetadata("RefNonRuleId",rule);
    }

    /**
     * Set final action.
     *
     * @param finalAction the final action
     * @throws SEDALibException if the FinalAction field or value is not expected in
     *                          this kind of rule
     */
    public void setFinalAction(String finalAction) throws SEDALibException {
        List<String> finalValues = getFinalActionList();
        if (finalValues == null)
            throw new SEDALibException("Le type de règle [" + elementName + "] n'a pas de FinalAction");
        if (!finalValues.contains(finalAction))
            throw new SEDALibException(
                    "Le type de règle [" + elementName + "] n'accepte pas la FinalAction [" + finalAction + "]");
        addNewMetadata("FinalAction",finalAction);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toCsvList()
     */
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> smCsvList;
        String previousXMLElementName = null;
        int count = 0;
        int ruleElementCount=0;
        for (SEDAMetadata sm : metadataList) {
            if (sm instanceof Rule) {
                smCsvList = ((Rule)sm).toCsvList(ruleElementCount++);
                result.putAll(smCsvList);
            }
            else{
                if (!sm.getXmlElementName().equals(previousXMLElementName)) {
                    previousXMLElementName = sm.getXmlElementName();
                    count = 0;
                } else count++;
                final String addedName;
                if (isAMultiValuedMetadata(sm.getXmlElementName()))
                    addedName = sm.getXmlElementName() + "." + count;
                else
                    addedName = sm.getXmlElementName();
                smCsvList = sm.toCsvList();
                smCsvList.entrySet().stream().forEach(e -> {
                    result.put(addedName + (e.getKey().isEmpty() ? "" : "." + e.getKey()), e.getValue());
                });
            }
        }
        return result;
    }

    /**
     * Gets the final action value list or null if final action is not authorized.
     *
     * @return the final action list
     */
    public List<String> getFinalActionList(){return null;}
}
