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
package fr.gouv.vitam.tools.sedalib.metadata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

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

    static SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");

    /** The rules. */
    private List<String> rules;

    /** The start dates. */
    private List<Date> startDates;

    /** The prevent inheritance. */
    private Boolean preventInheritance;

    /** The ref non rule ids. */
    private List<String> refNonRuleIds;

    /** The classification level. */
    private String classificationLevel;

    /** The classification owner. */
    private String classificationOwner;

    /** The classification reassessing date. */
    private Date classificationReassessingDate;

    /** The need reassessing authorization flag. */
    private Boolean needReassessingAuthorization;

    /**
     * Instantiates a new classification rule.
     */
    public ClassificationRule() {
        this.rules = new ArrayList<String>();
        this.startDates = new ArrayList<Date>();
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

    /**
     * Instantiates a new classification rule form args.
     *
     * @param elementName the XML element name (here "ClassificationRule")
     * @param args        the generic args for metadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public ClassificationRule(String elementName, Object[] args) throws SEDALibException {
        this();
        if ((args.length == 2) && (args[0] instanceof String) && (args[1] instanceof String)) {
            this.classificationLevel = (String) args[0];
            this.classificationOwner = (String) args[1];
        } else
            throw new SEDALibException("Mauvais constructeur de l'élément [" + elementName + "]");
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
    public void addRule(String rule, Date startDate) {
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
    public void setClassificationReassessingDate(Date classificationReassessingDate) {
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
                    xmlWriter.writeElementValue("StartDate", daySdf.format(startDates.get(i)));
            }
            if (preventInheritance != null)
                xmlWriter.writeElementValue("PreventInheritance", preventInheritance.toString());
            for (String refNonRuleId : refNonRuleIds)
                xmlWriter.writeElementValue("RefNonRuleId", refNonRuleId);
            xmlWriter.writeElementValueIfNotEmpty("ClassificationLevel", classificationLevel);
            xmlWriter.writeElementValueIfNotEmpty("ClassificationOwner", classificationOwner);
            if (classificationReassessingDate != null)
                xmlWriter.writeElementValue("ClassificationReassessingDate",
                        daySdf.format(classificationReassessingDate));
            if (needReassessingAuthorization != null)
                xmlWriter.writeElementValueIfNotEmpty("NeedReassessingAuthorization",
                        needReassessingAuthorization.toString());
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément FileInfo\n->" + e.getMessage());
        }
    }

    /**
     * Import the ClassificationRule in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read ClassificationRule
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not                          respected
     */
    public static ClassificationRule fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        ClassificationRule cr = null;
        String tmp, tmpDate;
        Date startDate;
        try {
            if (xmlReader.nextBlockIfNamed("ClassificationRule")) {
                cr = new ClassificationRule();
                tmp = xmlReader.nextValueIfNamed("Rule");
                while (tmp != null) {
                    tmpDate = xmlReader.nextValueIfNamed("StartDate");
                    if (tmpDate == null)
                        startDate = null;
                    else try {
                        startDate = daySdf.parse(tmpDate);
                    } catch (ParseException e) {
                        throw new SEDALibException("La date d'une règle est mal formatée");
                    }
                    cr.addRule(tmp, startDate);
                    tmp = xmlReader.nextValueIfNamed("Rule");
                }
                cr.preventInheritance = xmlReader.nextBooleanValueIfNamed("PreventInheritance");
                tmp = xmlReader.nextValueIfNamed("RefNonRuleId");
                while (tmp != null) {
                    cr.addRefNonRuleId(tmp);
                    tmp = xmlReader.nextValueIfNamed("RefNonRuleId");
                }
                cr.classificationLevel = xmlReader.nextValueIfNamed("ClassificationLevel");
                cr.classificationOwner = xmlReader.nextValueIfNamed("ClassificationOwner");
                tmpDate = xmlReader.nextValueIfNamed("ClassificationReassessingDate");
                if (tmpDate != null)
                    try {
                        cr.classificationReassessingDate = daySdf.parse(tmpDate);
                    } catch (ParseException e) {
                        throw new SEDALibException("La date ClassificationReassessingDate est mal formatée");
                    }
                cr.needReassessingAuthorization = xmlReader.nextBooleanValueIfNamed("NeedReassessingAuthorization");
                xmlReader.endBlockNamed("ClassificationRule");
            }
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément StorageRule\n->" + e.getMessage());
        }
        return cr;
    }
}
