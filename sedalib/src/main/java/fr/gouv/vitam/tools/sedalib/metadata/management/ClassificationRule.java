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

import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class ClassificationRule.
 * <p>
 * Class for SEDA element ClassificationRule
 * <p>
 * A management ArchiveUnit metadata.
 * <p>
 * Standard quote: " Gestion de la classification"
 */
public class ClassificationRule extends SEDAMetadata {

    /**
     * The rules.
     */
    private List<String> rules;

    /**
     * The start dates.
     */
    private List<LocalDate> startDates;

    /**
     * The prevent inheritance.
     */
    private Boolean preventInheritance;

    /**
     * The ref non rule ids.
     */
    private List<String> refNonRuleIds;

    /**
     * The classification level.
     */
    private String classificationLevel;

    /**
     * The classification owner.
     */
    private String classificationOwner;

    /**
     * The classification reassessing date.
     */
    private LocalDate classificationReassessingDate;

    /**
     * The need reassessing authorization flag.
     */
    private Boolean needReassessingAuthorization;

    /**
     * Instantiates a new classification rule.
     */
    public ClassificationRule() {
        this.rules = new ArrayList<String>();
        this.startDates = new ArrayList<LocalDate>();
        this.preventInheritance = null;
        this.refNonRuleIds = new ArrayList<String>();
        this.classificationLevel = null;
        this.classificationOwner = null;
        this.classificationReassessingDate = null;
        this.needReassessingAuthorization = null;
    }

    /**
     * Instantiates a new classification rule.
     *
     * @param classificationLevel the classification level
     * @param classificationOwner the classification owner
     */
    public ClassificationRule(String classificationLevel, String classificationOwner) {
        this();
        this.classificationLevel = classificationLevel;
        this.classificationOwner = classificationOwner;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#getXmlElementName()
     */
    @Override
    public String getXmlElementName() {
        return "ClassificationRule";
    }

    /**
     * Adds the rule.
     *
     * @param rule the rule
     */
    public void addRule(String rule) {
        rules.add(rule);
        startDates.add(null);
    }

    /**
     * Adds the rule.
     *
     * @param rule      the rule
     * @param startDate the start date
     */
    public void addRule(String rule, LocalDate startDate) {
        rules.add(rule);
        startDates.add(startDate);
    }

    /**
     * Sets the prevent inheritance.
     *
     * @param preventInheritance the new prevent inheritance
     */
    public void setPreventInheritance(boolean preventInheritance) {
        this.preventInheritance = preventInheritance;
    }

    /**
     * Adds the ref non rule id.
     *
     * @param rule the rule
     */
    public void addRefNonRuleId(String rule) {
        refNonRuleIds.add(rule);
    }

    /**
     * Sets the classification level.
     *
     * @param classificationLevel the new classification level
     */
    public void setClassificationLevel(String classificationLevel) {
        this.classificationLevel = classificationLevel;
    }

    /**
     * Sets the classification owner.
     *
     * @param classificationOwner the new classification owner
     */
    public void setClassificationOwner(String classificationOwner) {
        this.classificationOwner = classificationOwner;
    }

    /**
     * Sets the classification reassessing date.
     *
     * @param classificationReassessingDate the new classification reassessing date
     */
    public void setClassificationReassessingDate(LocalDate classificationReassessingDate) {
        this.classificationReassessingDate = classificationReassessingDate;
    }

    /**
     * Sets the need reassessing authorization.
     *
     * @param needReassessingAuthorization the new need reassessing authorization
     */
    public void setNeedReassessingAuthorization(Boolean needReassessingAuthorization) {
        this.needReassessingAuthorization = needReassessingAuthorization;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toSedaXml(fr.gouv.vitam.
     * tools.sedalib.xml.SEDAXMLStreamWriter)
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            xmlWriter.writeStartElement("ClassificationRule");
            for (int i = 0; i < rules.size(); i++) {
                xmlWriter.writeElementValue("Rule", rules.get(i));
                if (startDates.get(i) != null)
                    xmlWriter.writeElementValue("StartDate", SEDAXMLStreamWriter.getStringFromDate(startDates.get(i)));
            }
            if (preventInheritance != null)
                xmlWriter.writeElementValue("PreventInheritance", preventInheritance.toString());
            for (String refNonRuleId : refNonRuleIds)
                xmlWriter.writeElementValue("RefNonRuleId", refNonRuleId);
            xmlWriter.writeElementValueIfNotEmpty("ClassificationLevel", classificationLevel);
            xmlWriter.writeElementValueIfNotEmpty("ClassificationOwner", classificationOwner);
            if (classificationReassessingDate != null)
                xmlWriter.writeElementValue("ClassificationReassessingDate",
                        SEDAXMLStreamWriter.getStringFromDate(classificationReassessingDate));
            if (needReassessingAuthorization != null)
                xmlWriter.writeElementValueIfNotEmpty("NeedReassessingAuthorization",
                        needReassessingAuthorization.toString());
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément FileInfo\n->" + e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toCsvList()
     */
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        for (int i = 0; i < rules.size(); i++) {
            result.put("Rule." + i, rules.get(i));
            if (startDates.get(i) != null)
                result.put("StartDate." + i, SEDAXMLStreamWriter.getStringFromDate(startDates.get(i)));
        }
        if (preventInheritance != null)
            result.put("PreventInheritance", preventInheritance.toString());
        int count = 0;
        for (String refNonRuleId : refNonRuleIds) {
            result.put("RefNonRuleId." + count, refNonRuleId);
            count++;
        }
        if ((classificationLevel != null) && !classificationLevel.isEmpty())
            result.put("ClassificationLevel", classificationLevel);
        if ((classificationOwner != null) && !classificationOwner.isEmpty())
            result.put("ClassificationOwner", classificationOwner);
        if (classificationReassessingDate != null)
            result.put("ClassificationReassessingDate",
                    SEDAXMLStreamWriter.getStringFromDate(classificationReassessingDate));
        if (needReassessingAuthorization != null)
            result.put("NeedReassessingAuthorization",
                    needReassessingAuthorization.toString());
        return result;
    }


    /**
     * Import the metadata content in XML expected form from the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return true, if it finds something convenient, false if not
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not respected, for example
     */
    public boolean fillFromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        String tmp, tmpDate;
        LocalDate startDate;
        try {
            if (xmlReader.nextBlockIfNamed("ClassificationRule")) {
                tmp = xmlReader.nextValueIfNamed("Rule");
                while (tmp != null) {
                    tmpDate = xmlReader.nextValueIfNamed("StartDate");
                    if (tmpDate == null)
                        startDate = null;
                    else try {
                        startDate = SEDAXMLEventReader.getDateFromString(tmpDate);
                    } catch (DateTimeParseException e) {
                        throw new SEDALibException("La date d'une règle est mal formatée");
                    }
                    addRule(tmp, startDate);
                    tmp = xmlReader.nextValueIfNamed("Rule");
                }
                preventInheritance = xmlReader.nextBooleanValueIfNamed("PreventInheritance");
                tmp = xmlReader.nextValueIfNamed("RefNonRuleId");
                while (tmp != null) {
                    addRefNonRuleId(tmp);
                    tmp = xmlReader.nextValueIfNamed("RefNonRuleId");
                }
                classificationLevel = xmlReader.nextValueIfNamed("ClassificationLevel");
                classificationOwner = xmlReader.nextValueIfNamed("ClassificationOwner");
                tmpDate = xmlReader.nextValueIfNamed("ClassificationReassessingDate");
                if (tmpDate != null)
                    try {
                        classificationReassessingDate = SEDAXMLEventReader.getDateFromString(tmpDate);
                    } catch (DateTimeParseException e) {
                        throw new SEDALibException("La date ClassificationReassessingDate est mal formatée");
                    }
                needReassessingAuthorization = xmlReader.nextBooleanValueIfNamed("NeedReassessingAuthorization");
                xmlReader.endBlockNamed("ClassificationRule");
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type ClassificationRule\n->" + e.getMessage());
        }
        return true;
    }
}
