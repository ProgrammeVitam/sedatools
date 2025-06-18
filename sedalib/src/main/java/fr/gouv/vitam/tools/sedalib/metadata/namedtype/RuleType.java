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
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.xml.stream.XMLStreamException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * The Class RuleType.
 * <p>
 * For abstract Rule type SEDA metadata
 */
public abstract class RuleType extends ComplexListType {

    public static final String RULE_TAG = "Rule";
    public static final String PREVENTINHERITANCE_TAG = "PreventInheritance";
    public static final String REFNONRULEID_TAG = "RefNonRuleId";
    public static final String FINALACTION_TAG = "FinalAction";

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
        addNewMetadata(RULE_TAG,rule,startDate);
    }

    /**
     * Adds a rule element with only the rule identifier.
     *
     * @param rule the rule
     * @throws SEDALibException the seda lib exception
     */
    public void addRule(String rule) throws SEDALibException {
        addNewMetadata(RULE_TAG,rule,null);
    }

    /**
     * Adds a rule element with only the rule identifier and start date.
     *
     * @param rule      the rule
     * @param startDate the start date
     * @throws SEDALibException the seda lib exception
     */
    public void addRule(String rule, LocalDate startDate) throws SEDALibException {
        addNewMetadata(RULE_TAG,rule,startDate);
    }

    /**
     * Sets the prevent inheritance.
     *
     * @param preventInheritance the new prevent inheritance
     * @throws SEDALibException the seda lib exception
     */
    public void setPreventInheritance(boolean preventInheritance) throws SEDALibException {
        addNewMetadata(PREVENTINHERITANCE_TAG,preventInheritance);
    }

    /**
     * Adds the ref non rule id.
     *
     * @param rule the rule
     * @throws SEDALibException the seda lib exception
     */
    public void addRefNonRuleId(String rule) throws SEDALibException {
        addNewMetadata(REFNONRULEID_TAG,rule);
    }

    /**
     * Set final action.
     *
     * @param finalAction the final action
     * @throws SEDALibException if the FinalAction field or value is not expected in this kind of rule
     */
    public void setFinalAction(String finalAction) throws SEDALibException {
        List<String> finalValues = getFinalActionList();
        if (finalValues == null)
            throw new SEDALibException("Le type de règle [" + elementName + "] n'a pas de FinalAction");
        if (!finalValues.contains(finalAction))
            throw new SEDALibException(
                    "Le type de règle [" + elementName + "] n'accepte pas la FinalAction [" + finalAction + "]");
        addNewMetadata(FINALACTION_TAG,finalAction);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toCsvList()
     */
    @Override
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        LinkedHashMap<String, String> smCsvList;
        String previousXMLElementName = null;
        int count = 0;
        int ruleElementCount = -1;
        for (SEDAMetadata sm : metadataList) {
            if (sm instanceof Rule) {
                smCsvList = ((Rule) sm).toCsvList(++ruleElementCount);
                result.putAll(smCsvList);
            } else if (getRuleMetadataKindList().contains(sm.getXmlElementName())) {
                smCsvList = sm.toCsvList();
                String addedName = sm.getXmlElementName() + "." + ruleElementCount;
                smCsvList.forEach((key, value) -> result.put(addedName + (key.isEmpty() ? "" : "." + key), value));
            } else {
                if (!sm.getXmlElementName().equals(previousXMLElementName)) {
                    previousXMLElementName = sm.getXmlElementName();
                    count = 0;
                } else
                    count++;
                final String addedName;
                if (isAMultiValuedMetadata(sm.getXmlElementName()))
                    addedName = sm.getXmlElementName() + "." + count;
                else
                    addedName = sm.getXmlElementName();
                smCsvList = sm.toCsvList();
                smCsvList.forEach((key, value) -> result.put(addedName + (key.isEmpty() ? "" : "." + key), value));
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


    @Override
    public void addMetadata(SEDAMetadata sedaMetadata) throws SEDALibException {
        int addOrderIndex, curOrderIndex, i;
        boolean manyFlag, setFlag;
        addOrderIndex = indexOfMetadata(sedaMetadata.getXmlElementName());
        i = 0;
        setFlag = false;
        if (addOrderIndex == -1) {
            if (isNotExpandable())
                throw new SEDALibException(
                    "Impossible d'étendre le schéma avec des métadonnées non prévues ["
                        + elementName + "]");
            manyFlag = true;
            boolean noBeforeEqual = true;
            for (SEDAMetadata sm : metadataList) {
                if ((sm.getXmlElementName().equals(sedaMetadata.getXmlElementName())) && noBeforeEqual)
                    noBeforeEqual = false;
                if (!(sm.getXmlElementName().equals(sedaMetadata.getXmlElementName())) && !noBeforeEqual)
                    break;
                i++;
            }
        } else {
            manyFlag = getMetadataMap().get(sedaMetadata.getXmlElementName()).isMany();
            int lastRuleIndex = findLastRuleIndex();
            for (SEDAMetadata sm : metadataList.subList(lastRuleIndex, metadataList.size())) {
                if (getMetadataMap().get(sm.getXmlElementName()) instanceof RuleMetadataKind) {
                    i++;
                    continue;
                }
                curOrderIndex = indexOfMetadata(sm.getXmlElementName());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
                    setFlag = true;
                    break;
                }
                if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex))
                    break;
                i++;
            }
            i += lastRuleIndex;
        }
        if (manyFlag)
            metadataList.add(i, sedaMetadata);
        else {
            if (setFlag)
                metadataList.set(i, sedaMetadata);
            else
                metadataList.add(i, sedaMetadata);
        }
    }

    private int findLastRuleIndex() {
        for (int i = metadataList.size(); i > 0; --i) {
            SEDAMetadata item = metadataList.get(i - 1);
            if (item.getXmlElementName().equals(RULE_TAG)) {
                return i;
            }
        }
        return 0;
    }

    public List<String> getRuleMetadataKindList() throws SEDALibException {
        return this.getMetadataMap().entrySet().stream().filter(e -> e.getValue() instanceof RuleMetadataKind)
            .map(Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public boolean fillFromSedaXml(SEDAXMLEventReader xmlReader)
        throws SEDALibException {
        Class<?> metadataClass;
        try {
            if (xmlReader.nextBlockIfNamed(elementName)) {
                String tmp = xmlReader.peekName();
                while (tmp != null) {
                    ComplexListMetadataKind mi = getMetadataMap().get(tmp);
                    if (mi == null) {
                        if (isNotExpandable())
                            throw new SEDALibException(
                                "Impossible d'étendre le schéma avec des métadonnées non prévues ["
                                    + tmp + "]");
                        else
                            metadataClass = AnyXMLType.class;
                    } else
                        metadataClass = mi.getMetadataClass();
                    SEDAMetadata sm = SEDAMetadata.fromSedaXml(xmlReader, metadataClass);
                    addMetadata(sm);
                    tmp = xmlReader.peekName();
                }
                xmlReader.endBlockNamed(elementName);
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément [" + elementName + "]", e);
        }
        return true;
    }
}
