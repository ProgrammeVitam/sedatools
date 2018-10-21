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
        SEDAMetadata create(String elementName);
    }

    static HashMap<String, Creator> typeCreatorMap;
    static HashMap<String, String> typeExtraInformationMap;

    static {
        typeCreatorMap = new HashMap<String, Creator>();
        typeExtraInformationMap = new HashMap<String, String>();

        typeCreatorMap.put("DateTimeType", AddMetadataItem::dateTimeTypeSample);
        typeExtraInformationMap.put("DateTimeType", "Date ou Date/Temps au format ISO8601\n    YYYY-MM-DD[Timezone ou Z] ou YYYY-MM-DD'T'HH:MM:SS[Timezone ou Z]");

        typeCreatorMap.put("GenericXMLBlockType", AddMetadataItem::genericXMLBlockTypeSample);
        typeExtraInformationMap.put("GenericXMLBlockType", "Bloc XML libre");

        typeCreatorMap.put("CodeType", AddMetadataItem::codeTypeSample);
        typeExtraInformationMap.put("CodeType", "Metadonnée de type code");

        typeCreatorMap.put("PersonOrEntityType", AddMetadataItem::personOrEntityTypeSample);
        typeExtraInformationMap.put("PersonOrEntityType", "Metadonnée de type personne ou entité");

        typeCreatorMap.put("SchemeType", AddMetadataItem::schemeTypeSample);
        typeExtraInformationMap.put("SchemeType", "Metadonnée de type scheme");

        typeCreatorMap.put("StringType", AddMetadataItem::stringTypeSample);
        typeExtraInformationMap.put("StringType", "Metadonnée de type chaîne de caractères");

        typeCreatorMap.put("TextType", AddMetadataItem::textTypeSample);
        typeExtraInformationMap.put("TextType", "Metadonnée de type chaîne de caractères avec un attribut de langue");

        typeCreatorMap.put("ArchiveUnitProfile", AddMetadataItem::archiveUnitProfileSample);
        typeExtraInformationMap.put("ArchiveUnitProfile", "Identifiant du profil d'unité archivistique");

        typeCreatorMap.put("Signer", AddMetadataItem::signerSample);
        typeExtraInformationMap.put("Signer", "Métadonnées d'un signataire de la transaction ou de l'objet");

        typeCreatorMap.put("Validator", AddMetadataItem::validatorSample);
        typeExtraInformationMap.put("Validator", "Métadonnées du validateur de la signature");

        typeCreatorMap.put("Signature", AddMetadataItem::signatureSample);
        typeExtraInformationMap.put("Signature", "Ensemble des métadonnées relatives à la signature.");

        typeCreatorMap.put("AccessRule", AddMetadataItem::accessRuleSample);
        typeExtraInformationMap.put("AccessRule", "Règle de communicabilité");

        typeCreatorMap.put("AppraisalRule", AddMetadataItem::appraisalRuleSample);
        typeExtraInformationMap.put("AppraisalRule", "Règle de durée d’utilité administrative");

        typeCreatorMap.put("ClassificationRule", AddMetadataItem::classificationRuleSample);
        typeExtraInformationMap.put("ClassificationRule", "Règle de classification\n    ClassificationLevel et ClassificationOwner obligatoires");

        typeCreatorMap.put("DisseminationRule", AddMetadataItem::disseminationRuleSample);
        typeExtraInformationMap.put("DisseminationRule", "Règle de diffusion");

        typeCreatorMap.put("ReuseRule", AddMetadataItem::reuseRuleSample);
        typeExtraInformationMap.put("ReuseRule", "Règle de réutilisation");

        typeCreatorMap.put("StorageRule", AddMetadataItem::storageRuleSample);
        typeExtraInformationMap.put("StorageRule", "Règle de durée d’utilité courante");

        typeCreatorMap.put("Event", AddMetadataItem::eventSample);
        typeExtraInformationMap.put("Event", "Evènement (EventDateTime obligatoire)");

        typeCreatorMap.put("UpdateOperation", AddMetadataItem::updateOperationSample);
        typeExtraInformationMap.put("UpdateOperation", "Définition d'attachement à une AU existante\n    Accepte aussi la forme <UpdateOperation><SystemId>guid</SystemId><UpdateOperation>)");
    }

    static String[] addContentMetadataArray = null;

    public String elementName;

    public SEDAMetadata skeleton;

    public String extraInformation;

    static DateTimeType dateTimeTypeSample(String elementName) {
        return new DateTimeType(elementName, LocalDateTime.of(1970, 1, 1, 1, 0, 0));
    }

    static GenericXMLBlockType genericXMLBlockTypeSample(String elementName) {
        return new GenericXMLBlockType(elementName, "<Tag><SubTag1>Text1</SubTag1><SubTag2 attr=\"any\">Text2</SubTag2></Tag>");
    }

    static CodeType codeTypeSample(String elementName) {
        return new CodeType(elementName, "Text", "listID", "listAgencyID", "listAgencyName",
                "listName", "listVersionID", "name", "languageID", "listURI", "listSchemeURI");
    }

   static void constructComplexListType(ComplexListType clt){
        for (String metadataName : clt.getMetadataOrderedList()) {
            ComplexListType.MetadataKind metadataKind = clt.getMetadataMap().get(metadataName);
            Creator c = typeCreatorMap.get(metadataKind.metadataClass.getSimpleName());
            SEDAMetadata metadataObject = c.create(metadataName);
            try {
                clt.addMetadata(metadataObject);
            } catch (SEDALibException ignored) {
            }
            if (metadataKind.many) {
                metadataObject = c.create(metadataName);
                try {
                    clt.addMetadata(metadataObject);
                } catch (SEDALibException ignored) {
                }
            }
        }
        if (!clt.isNotExpendable())
            try {
                clt.addNewMetadata("AnyOtherMetadata","<AnyOtherMetadata><SubTag1>Text1</SubTag1><SubTag2 attr=\"any\">Text2</SubTag2></AnyOtherMetadata>");
            } catch (SEDALibException ignored) {
            }
    }

    static PersonOrEntityType personOrEntityTypeSample(String elementName) {
        PersonOrEntityType result = new PersonOrEntityType(elementName);
        constructComplexListType(result);
        return result;
    }

    static Signer signerSample(String elementName) {
        Signer result = new Signer();
        constructComplexListType(result);
        return result;
    }

    static Validator validatorSample(String elementName) {
        Validator result = new Validator();
        constructComplexListType(result);
        return result;
    }

    static Signature signatureSample(String elementName) {
        Signature result = new Signature();
        constructComplexListType(result);
        return result;
    }

    static SchemeType schemeTypeSample(String elementName) {
        return new SchemeType(elementName, "Text", "AgencyID",
                "AgencyName", "DataURI", "ID",
                "Name", "URI", "VersionID");
    }

    static StringType stringTypeSample(String elementName) {
        return new StringType(elementName, "Text");
    }

    static TextType textTypeSample(String elementName) {
        return new TextType(elementName, "Text", "fr");
    }

    static ArchiveUnitProfile archiveUnitProfileSample(String elementName) {
        return new ArchiveUnitProfile("Text", "AgencyID",
                "AgencyName", "DataURI", "ID",
                "Name", "URI", "VersionID");
    }

    static void ruleTypeCompleteSample(RuleType ruleTypeSample) {
        ruleTypeSample.addRule("Rule1", LocalDate.of(1970, 1, 1));
        ruleTypeSample.addRule("Rule2");
        ruleTypeSample.setPreventInheritance(false);
        ruleTypeSample.addRefNonRuleId("Rule3");
        ruleTypeSample.addRefNonRuleId("Rule4");
        if (ruleTypeSample.getFinalActionList() != null) {
            try {
                ruleTypeSample.setFinalAction(String.join("|", ruleTypeSample.getFinalActionList()));
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

    static Event eventSample(String elementName) {
        Event result = new Event();
        for (String metadataName : result.getMetadataOrderedList()) {
            ComplexListType.MetadataKind metadataKind = result.getMetadataMap().get(metadataName);
            Creator c = typeCreatorMap.get(metadataKind.metadataClass.getSimpleName());
            SEDAMetadata metadataObject = c.create(metadataName);
            try {
                result.addMetadata(metadataObject);
            } catch (SEDALibException ignored) {
            }
            if (metadataKind.many) {
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

    public AddMetadataItem(Class metadataClass, String elementName) {
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
            List<String> tmp = new ArrayList<String>();
            tmp.add("[A]ArchiveUnitProfile ");
            Content c=new Content();
            List<String> contentMetadataList=new ArrayList<String>();
            contentMetadataList.add("[C]AnyOtherMetadata ");
            for (String metadataName : c.getMetadataOrderedList()) {
                ComplexListType.MetadataKind metadataKind = c.getMetadataMap().get(metadataName);
                contentMetadataList.add("[C]" + metadataName + (metadataKind.many ? " *" : " "));
            }
            Collections.sort(contentMetadataList);
            tmp.addAll(contentMetadataList);
            Management m=new Management();
            List<String> managementMetadataList=new ArrayList<String>();
            managementMetadataList.add("[C]AnyOtherMetadata ");
            for (String metadataName : m.getMetadataOrderedList()) {
                ComplexListType.MetadataKind metadataKind = m.getMetadataMap().get(metadataName);
                managementMetadataList.add("[M]" + metadataName + (metadataKind.many ? " *" : " "));
            }
            Collections.sort(managementMetadataList);
            tmp.addAll(managementMetadataList);
            addContentMetadataArray = tmp.toArray(new String[0]);
        }
        return addContentMetadataArray;
    }
}
