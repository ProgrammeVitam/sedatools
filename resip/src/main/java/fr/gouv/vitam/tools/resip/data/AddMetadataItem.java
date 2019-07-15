/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 *
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.resip.data;

import fr.gouv.vitam.tools.sedalib.metadata.*;
import fr.gouv.vitam.tools.sedalib.metadata.content.*;
import fr.gouv.vitam.tools.sedalib.metadata.management.*;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AddMetadataItem {

    @FunctionalInterface
    interface Creator {
        SEDAMetadata create(String elementName) throws SEDALibException;
    }

    static HashMap<String, Creator> typeCreatorMap;
    static HashMap<String, String> typeExtraInformationMap;

    static {
        typeCreatorMap = new HashMap<String, Creator>();
        typeExtraInformationMap = new HashMap<String, String>();

        typeCreatorMap.put("DateTimeType", AddMetadataItem::dateTimeTypeSample);
        typeExtraInformationMap.put("DateTimeType", "Date ou Date/Temps au format ISO8601\n    YYYY-MM-DD[Timezone ou Z]" +
                " ou YYYY-MM-DD'T'HH:MM:SS[Timezone ou Z]");

        typeCreatorMap.put("DateType", AddMetadataItem::dateTypeSample);
        typeExtraInformationMap.put("DateType", "Date\n    YYYY-MM-DD");

        typeCreatorMap.put("AnyXMLType", AddMetadataItem::genericXMLBlockTypeSample);
        typeExtraInformationMap.put("AnyXMLType", "Bloc XML de structure non connue par ReSIP");

        typeCreatorMap.put("AgentType", AddMetadataItem::agentTypeSample);
        typeExtraInformationMap.put("AgentType", "Metadonnée de type agent");

        typeCreatorMap.put("AgencyType", AddMetadataItem::agencyTypeSample);
        typeExtraInformationMap.put("AgencyType", "Metadonnée de type agence");

        typeCreatorMap.put("PlaceType", AddMetadataItem::placeTypeSample);
        typeExtraInformationMap.put("PlaceType", "Metadonnée de type localisation");

        typeCreatorMap.put("StringType", AddMetadataItem::stringTypeSample);
        typeExtraInformationMap.put("StringType", "Metadonnée de type chaîne de caractères");

        typeCreatorMap.put("KeywordType", AddMetadataItem::keywordTypeSample);
        typeExtraInformationMap.put("KeywordType", "Metadonnée de type code de mot-clef, fait partie des valeurs: " +
                "corpname, famname, geogname, name, occupation, persname, subject, genreform, function");

        typeCreatorMap.put("IntegerType", AddMetadataItem::integerTypeSample);
        typeExtraInformationMap.put("IntegerType", "Metadonnée de type entier long");

        typeCreatorMap.put("SIPInternalIDType", AddMetadataItem::sipInternalIDTypeSample);
        typeExtraInformationMap.put("SIPInternalIDType", "Metadonnée de type ID de référence interne au SIP");

        typeCreatorMap.put("DataObjectOrArchiveUnitReferenceType", AddMetadataItem::dataObjectOrArchiveUnitReferenceTypeSample);
        typeExtraInformationMap.put("DataObjectOrArchiveUnitReferenceType", "Metadonnée de type ID de référence ArchiveUnit ou DataObject interne au SIP ou externe ");

        typeCreatorMap.put("DataObjectReference", AddMetadataItem::dataObjectReferenceSample);
        typeExtraInformationMap.put("DataObjectReference", "Metadonnée de type ID de référence DataObject interne au SIP");

        typeCreatorMap.put("ReferencedObject", AddMetadataItem::referencedObjectSample);
        typeExtraInformationMap.put("ReferencedObject", "Metadonnée de type référence à un objet signé");

        typeCreatorMap.put("TextType", AddMetadataItem::textTypeSample);
        typeExtraInformationMap.put("TextType", "Métadonnée de type chaîne de caractères. Il peut y avoir plusieurs occurrences " +
                "de ce champ en plusieurs langues et il faut dans ce cas spécifier la langue dans un attribut xml. " +
                "Par exemple pour définir la langue du texte comme anglaise on mettra l'attribut xml:lang=\"en\"");

        typeCreatorMap.put("RelatedObjectReference", AddMetadataItem::relatedObjectReferenceSample);
        typeExtraInformationMap.put("RelatedObjectReference", "Metadonnée de type relation ArchiveUnit ou DataObject externe ou interne au SIP");

        typeCreatorMap.put("DigestType", AddMetadataItem::digestTypeSample);
        typeExtraInformationMap.put("DigestType", "Metadonnée de type hachage avec un attribut d'algorithme");

        typeCreatorMap.put("ArchiveUnitProfile", AddMetadataItem::archiveUnitProfileSample);
        typeExtraInformationMap.put("ArchiveUnitProfile", "Identifiant du profil d'unité archivistique");

        typeCreatorMap.put("Coverage", AddMetadataItem::coverageSample);
        typeExtraInformationMap.put("Coverage", "Métadonnées de couverture spatiale, temporelle ou juridictionnelle");

        typeCreatorMap.put("CustodialHistory", AddMetadataItem::custodialHistorySample);
        typeExtraInformationMap.put("CustodialHistory", "Métadonnées indiquant les changements successifs de propriété, " +
                "de responsabilité et de conservation avant leur entrée dans le lieu de conservation");

        typeCreatorMap.put("Gps", AddMetadataItem::gpsSample);
        typeExtraInformationMap.put("Gps", "Métadonnées de coordonnées gps complétées ou vérifiées par un utilisateur");

        typeCreatorMap.put("Keyword", AddMetadataItem::keywordSample);
        typeExtraInformationMap.put("Keyword", "Métadonnées de mots-clef avec contexte inspiré du SEDA 1.0. KeywordType " +
                "doit faire partie des valeurs: corpname, famname, geogname, name, occupation, persname, subject, genreform, function");

        typeCreatorMap.put("Signer", AddMetadataItem::signerSample);
        typeExtraInformationMap.put("Signer", "Métadonnées d'un signataire de la transaction ou de l'objet");

        typeCreatorMap.put("Validator", AddMetadataItem::validatorSample);
        typeExtraInformationMap.put("Validator", "Métadonnées du validateur de la signature");

        typeCreatorMap.put("Signature", AddMetadataItem::signatureSample);
        typeExtraInformationMap.put("Signature", "Ensemble des métadonnées relatives à la signature.");

        typeCreatorMap.put("AccessRule", AddMetadataItem::accessRuleSample);
        typeExtraInformationMap.put("AccessRule", "Règle de communicabilité");

        typeCreatorMap.put("AppraisalRule", AddMetadataItem::appraisalRuleSample);
        typeExtraInformationMap.put("AppraisalRule", "Règle de durée d’utilité administrative (FinalAction possible Keep ou Destroy)");

        typeCreatorMap.put("ClassificationRule", AddMetadataItem::classificationRuleSample);
        typeExtraInformationMap.put("ClassificationRule", "Règle de classification\n    ClassificationLevel et ClassificationOwner obligatoires");

        typeCreatorMap.put("DisseminationRule", AddMetadataItem::disseminationRuleSample);
        typeExtraInformationMap.put("DisseminationRule", "Règle de diffusion");

        typeCreatorMap.put("ReuseRule", AddMetadataItem::reuseRuleSample);
        typeExtraInformationMap.put("ReuseRule", "Règle de réutilisation");

        typeCreatorMap.put("StorageRule", AddMetadataItem::storageRuleSample);
        typeExtraInformationMap.put("StorageRule", "Règle de durée d’utilité courante (FinalAction possible RestrictAccess,Transfer ou Copy)");

        typeCreatorMap.put("Event", AddMetadataItem::eventSample);
        typeExtraInformationMap.put("Event", "Evènement (EventDateTime obligatoire)");

        typeCreatorMap.put("UpdateOperation", AddMetadataItem::updateOperationSample);
        typeExtraInformationMap.put("UpdateOperation", "Définition d'attachement à une AU existante\n    Accepte aussi la forme <UpdateOperation><SystemId>guid</SystemId><UpdateOperation>)");
    }

    static String[] addContentMetadataArray = null;

    public String elementName;

    public SEDAMetadata skeleton;

    public String extraInformation;

    static DateType dateTypeSample(String elementName) {
        return new DateType(elementName, LocalDate.of(1970, 1, 1));
    }

    static DateTimeType dateTimeTypeSample(String elementName) {
        return new DateTimeType(elementName, LocalDateTime.of(1970, 1, 1, 1, 0, 0));
    }

    static AnyXMLType genericXMLBlockTypeSample(String elementName) {
        return new AnyXMLType(elementName, "<"+elementName+"><BlockTag1>Text1</BlockTag1><BlockTag2>Text2</BlockTag2></"+elementName+">");
    }

   static void constructComplexListType(ComplexListType clt) throws SEDALibException {
        for (String metadataName : clt.getMetadataOrderedList()) {
            ComplexListMetadataKind complexListMetadataKind = clt.getMetadataMap().get(metadataName);
            Creator c = typeCreatorMap.get(complexListMetadataKind.metadataClass.getSimpleName());
            SEDAMetadata metadataObject = c.create(metadataName);
            try {
                clt.addMetadata(metadataObject);
            } catch (SEDALibException ignored) {
            }
            if (complexListMetadataKind.many) {
                metadataObject = c.create(metadataName);
                try {
                    clt.addMetadata(metadataObject);
                } catch (SEDALibException ignored) {
                }
            }
        }
        if (!clt.isNotExpendable())
            try {
                clt.addNewMetadata("AnyOtherMetadata","<AnyOtherMetadata><SubTag1>Text1</SubTag1><SubTag2>Text2</SubTag2></AnyOtherMetadata>");
            } catch (SEDALibException ignored) {
            }
    }

    static AgentType agentTypeSample(String elementName) throws SEDALibException {
        AgentType result = new AgentType(elementName);
        constructComplexListType(result);
        return result;
    }

    static PlaceType placeTypeSample(String elementName) throws SEDALibException {
        PlaceType result = new PlaceType(elementName);
        constructComplexListType(result);
        return result;
    }

    static ReferencedObject referencedObjectSample(String elementName) throws SEDALibException {
        ReferencedObject result = new ReferencedObject();
        constructComplexListType(result);
        return result;
    }

    static DataObjectOrArchiveUnitReferenceType dataObjectOrArchiveUnitReferenceTypeSample(String elementName) throws SEDALibException {
        DataObjectOrArchiveUnitReferenceType result = new DataObjectOrArchiveUnitReferenceType(elementName);
        constructComplexListType(result);
        return result;
    }

    static Coverage coverageSample(String elementName) throws SEDALibException {
        Coverage result = new Coverage();
        constructComplexListType(result);
        return result;
    }

    static DataObjectReference dataObjectReferenceSample(String elementName) throws SEDALibException {
        DataObjectReference result = new DataObjectReference();
        constructComplexListType(result);
        return result;
    }

    static RelatedObjectReference relatedObjectReferenceSample(String elementName) throws SEDALibException {
        RelatedObjectReference result = new RelatedObjectReference();
        constructComplexListType(result);
        return result;
    }

    static CustodialHistory custodialHistorySample(String elementName) throws SEDALibException {
        CustodialHistory result = new CustodialHistory();
        constructComplexListType(result);
        return result;
    }

    static Gps gpsSample(String elementName) throws SEDALibException {
        Gps result = new Gps();
        constructComplexListType(result);
        return result;
    }

    static Keyword keywordSample(String elementName) throws SEDALibException {
        Keyword result = new Keyword();
        constructComplexListType(result);
        return result;
    }

    static Signer signerSample(String elementName) throws SEDALibException {
        Signer result = new Signer();
        constructComplexListType(result);
        return result;
    }

    static Validator validatorSample(String elementName) throws SEDALibException {
        Validator result = new Validator();
        constructComplexListType(result);
        return result;
    }

    static Signature signatureSample(String elementName) throws SEDALibException {
        Signature result = new Signature();
        constructComplexListType(result);
        return result;
    }

    static AgencyType agencyTypeSample(String elementName) throws SEDALibException {
        AgencyType result = new AgencyType(elementName);
        constructComplexListType(result);
        return result;
    }

    static KeywordType keywordTypeSample(String elementName) {
        KeywordType ckt=null;
        try {
            ckt=new KeywordType("subject");
        }
        catch(SEDALibException ignored){
        }
        return ckt;
    }

    static StringType stringTypeSample(String elementName) {
        return new StringType(elementName, "Text");
    }

    static SIPInternalIDType sipInternalIDTypeSample(String elementName) {
        return new SIPInternalIDType(elementName, "ID");
    }

    static IntegerType integerTypeSample(String elementName) {
        return new IntegerType(elementName, 123);
    }

    static TextType textTypeSample(String elementName) {
        return new TextType(elementName, "Text");
    }

    static DigestType digestTypeSample(String elementName) {
        return new DigestType(elementName, "HashSHA-512", "SHA-512");
    }

    static ArchiveUnitProfile archiveUnitProfileSample(String elementName) {
        return new ArchiveUnitProfile("Text");
    }

    static void ruleTypeCompleteSample(RuleType ruleTypeSample) {
        ruleTypeSample.addRule("Rule1", LocalDate.of(1970, 1, 1));
        ruleTypeSample.addRule("Rule2");
        ruleTypeSample.setPreventInheritance(false);
        ruleTypeSample.addRefNonRuleId("Rule3");
        ruleTypeSample.addRefNonRuleId("Rule4");
        if (ruleTypeSample.getFinalActionList() != null) {
            try {
                ruleTypeSample.setFinalAction(ruleTypeSample.getFinalActionList().get(0));
            } catch (SEDALibException ignored) {
            }
        }
    }

    static AccessRule accessRuleSample(String elementName) {
        AccessRule result = new AccessRule();
        ruleTypeCompleteSample(result);
        return result;
    }

    static AppraisalRule appraisalRuleSample(String elementName) {
        AppraisalRule result = new AppraisalRule();
        ruleTypeCompleteSample(result);
        return result;
    }

    static ClassificationRule classificationRuleSample(String elementName) {
        ClassificationRule result = new ClassificationRule();
        result.addRule("Rule1", LocalDate.of(1970, 1, 1));
        result.addRule("Rule2");
        result.setPreventInheritance(false);
        result.addRefNonRuleId("Rule3");
        result.addRefNonRuleId("Rule4");
        result.setClassificationLevel("Level1");
        result.setClassificationOwner("Owner1");
        result.setClassificationReassessingDate(LocalDate.of(1970, 1, 1));
        result.setNeedReassessingAuthorization(true);
        return result;
    }

    static DisseminationRule disseminationRuleSample(String elementName) {
        DisseminationRule result = new DisseminationRule();
        ruleTypeCompleteSample(result);
        return result;
    }

    static ReuseRule reuseRuleSample(String elementName) {
        ReuseRule result = new ReuseRule();
        ruleTypeCompleteSample(result);
        return result;
    }

    static StorageRule storageRuleSample(String elementName) {
        StorageRule result = new StorageRule();
        ruleTypeCompleteSample(result);
        return result;
    }

    static Event eventSample(String elementName) throws SEDALibException {
        Event result = new Event();
        for (String metadataName : result.getMetadataOrderedList()) {
            ComplexListMetadataKind complexListMetadataKind = result.getMetadataMap().get(metadataName);
            Creator c = typeCreatorMap.get(complexListMetadataKind.metadataClass.getSimpleName());
            SEDAMetadata metadataObject = c.create(metadataName);
            try {
                result.addMetadata(metadataObject);
            } catch (SEDALibException ignored) {
            }
            if (complexListMetadataKind.many) {
                metadataObject = c.create(metadataName);
                try {
                    result.addMetadata(metadataObject);
                } catch (SEDALibException ignored) {
                }
            }
        }
        try {
            result.addNewMetadata("AnyOtherMetadata","<AnyOtherMetadata><SubTag1>Text1</SubTag1><SubTag2 attr=\"any\">Text2</SubTag2></AnyOtherMetadata>");
        } catch (SEDALibException ignored) {
        }
        return result;
    }

    static UpdateOperation updateOperationSample(String elementName) {
        return new UpdateOperation("Name", "Value");
    }

    public AddMetadataItem(Class metadataClass, String elementName) throws SEDALibException {
        SEDAMetadata result;

        Creator c = typeCreatorMap.get(metadataClass.getSimpleName());
        if (c == null) {
            this.elementName=null;
            this.skeleton = null;
            this.extraInformation = null;
        } else {
            this.elementName=elementName;
            this.skeleton = c.create(elementName);
            this.extraInformation = typeExtraInformationMap.get(metadataClass.getSimpleName());
        }
    }

    public static String[] getAddContentMetadataArray() {
        if (addContentMetadataArray == null) {
           try {
               List<String> tmp = new ArrayList<String>();
               tmp.add("[A]ArchiveUnitProfile ");
               Content c = new Content();
               List<String> contentMetadataList = new ArrayList<String>();
               contentMetadataList.add("[C]AnyOtherMetadata ");
               for (String metadataName : c.getMetadataOrderedList()) {
                   ComplexListMetadataKind complexListMetadataKind = c.getMetadataMap().get(metadataName);
                   contentMetadataList.add("[C]" + metadataName + (complexListMetadataKind.many ? " *" : " "));
               }
               Collections.sort(contentMetadataList);
               tmp.addAll(contentMetadataList);
               Management m = new Management();
               List<String> managementMetadataList = new ArrayList<String>();
               managementMetadataList.add("[C]AnyOtherMetadata ");
               for (String metadataName : m.getMetadataOrderedList()) {
                   ComplexListMetadataKind complexListMetadataKind = m.getMetadataMap().get(metadataName);
                   managementMetadataList.add("[M]" + metadataName + (complexListMetadataKind.many ? " *" : " "));
               }
               Collections.sort(managementMetadataList);
               tmp.addAll(managementMetadataList);
               addContentMetadataArray = tmp.toArray(new String[0]);
           } catch (SEDALibException e) {
               addContentMetadataArray = new String[0];
           }
        }
        return addContentMetadataArray;
    }
}
