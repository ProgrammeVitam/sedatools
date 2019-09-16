package fr.gouv.vitam.tools.resip.metadataeditor;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataEditorConstants {
    /**
     * The Type extra information map.
     */
    static HashMap<String, String> typeExtraInformationMap;
    static List<String> minimalTagList;
    static List<String> largeAreaTagList;
    static HashMap<String, String> translateMap;


    static {
        typeExtraInformationMap = new HashMap<String, String>();
        typeExtraInformationMap.put("DateTimeType", "Date ou Date/Temps au format ISO8601\n    YYYY-MM-DD[Timezone ou Z]" +
                " ou YYYY-MM-DD'T'HH:MM:SS[Timezone ou Z]");
        typeExtraInformationMap.put("DateType", "Date\n    YYYY-MM-DD");
        typeExtraInformationMap.put("AnyXMLType", "Bloc XML de structure non connue par ReSIP");
        typeExtraInformationMap.put("AgentType", "Metadonnée de type agent");
        typeExtraInformationMap.put("AgencyType", "Metadonnée de type agence");
        typeExtraInformationMap.put("PlaceType", "Metadonnée de type localisation");
        typeExtraInformationMap.put("StringType", "Metadonnée de type chaîne de caractères");
        typeExtraInformationMap.put("DescriptionLevel", "Metadonnée de type code de niveau de description, fait partie des valeurs: " +
                        "Fonds, Subfonds, Class, Collection, Series, Subseries, RecordGrp, SubGrp, File, Item, OtherLevel");
        typeExtraInformationMap.put("KeywordType", "Metadonnée de type code de mot-clef, fait partie des valeurs: " +
                "corpname, famname, geogname, name, occupation, persname, subject, genreform, function");
        typeExtraInformationMap.put("IntegerType", "Metadonnée de type entier long");
        typeExtraInformationMap.put("SIPInternalIDType", "Metadonnée de type ID de référence interne au SIP");
        typeExtraInformationMap.put("DataObjectOrArchiveUnitReferenceType", "Metadonnée de type ID de référence ArchiveUnit ou DataObject interne au SIP ou externe ");
        typeExtraInformationMap.put("DataObjectReference", "Metadonnée de type ID de référence DataObject interne au SIP");
        typeExtraInformationMap.put("ReferencedObject", "Metadonnée de type référence à un objet signé");
        typeExtraInformationMap.put("TextType", "Métadonnée de type chaîne de caractères. Il peut y avoir plusieurs occurrences " +
                "de ce champ en plusieurs langues et il faut dans ce cas spécifier la langue dans un attribut xml. " +
                "Par exemple pour définir la langue du texte comme anglaise on mettra l'attribut xml:lang=\"en\"");
        typeExtraInformationMap.put("RelatedObjectReference", "Metadonnée de type relation ArchiveUnit ou DataObject externe ou interne au SIP");
        typeExtraInformationMap.put("DigestType", "Metadonnée de type hachage avec un attribut d'algorithme");
        typeExtraInformationMap.put("ArchiveUnitProfile", "Identifiant du profil d'unité archivistique");
        typeExtraInformationMap.put("Coverage", "Métadonnées de couverture spatiale, temporelle ou juridictionnelle");
        typeExtraInformationMap.put("CustodialHistory", "Métadonnées indiquant les changements successifs de propriété, " +
                "de responsabilité et de conservation avant leur entrée dans le lieu de conservation");
        typeExtraInformationMap.put("Gps", "Métadonnées de coordonnées gps complétées ou vérifiées par un utilisateur");
        typeExtraInformationMap.put("Keyword", "Métadonnées de mots-clef avec contexte inspiré du SEDA 1.0. KeywordType " +
                "doit faire partie des valeurs: corpname, famname, geogname, name, occupation, persname, subject, genreform, function");
        typeExtraInformationMap.put("Signer", "Métadonnées d'un signataire de la transaction ou de l'objet");
        typeExtraInformationMap.put("Validator", "Métadonnées du validateur de la signature");
        typeExtraInformationMap.put("Signature", "Ensemble des métadonnées relatives à la signature.");
        typeExtraInformationMap.put("AccessRule", "Règle de communicabilité");
        typeExtraInformationMap.put("AppraisalRule", "Règle de durée d’utilité administrative (FinalAction possible Keep ou Destroy)");
        typeExtraInformationMap.put("ClassificationRule", "Règle de classification\n    ClassificationLevel et ClassificationOwner obligatoires");
        typeExtraInformationMap.put("DisseminationRule", "Règle de diffusion");
        typeExtraInformationMap.put("ReuseRule", "Règle de réutilisation");
        typeExtraInformationMap.put("StorageRule", "Règle de durée d’utilité courante (FinalAction possible RestrictAccess,Transfer ou Copy)");
        typeExtraInformationMap.put("Event", "Evènement (EventDateTime obligatoire)");
        typeExtraInformationMap.put("UpdateOperation", "Définition d'attachement à une AU existante\n    Accepte aussi la forme <UpdateOperation><SystemId>guid</SystemId><UpdateOperation>)");

        minimalTagList=new ArrayList<>();
        minimalTagList.add("DescriptionLevel");
        minimalTagList.add("Title");
        minimalTagList.add("Description");
        minimalTagList.add("CustodialHistoryItem");
        minimalTagList.add("KeywordContent");
        minimalTagList.add("Identifier");
        minimalTagList.add("FirstName");
        minimalTagList.add("BirthName");
        minimalTagList.add("Address");
        minimalTagList.add("PostalCode");
        minimalTagList.add("City");
        minimalTagList.add("Region");
        minimalTagList.add("Country");
        minimalTagList.add("EventType");
        minimalTagList.add("EventDateTime");
        minimalTagList.add("EventDetail");
        minimalTagList.add("Signer");
        minimalTagList.add("SigningTime");
        minimalTagList.add("Validator");
        minimalTagList.add("ValidationTime");
        minimalTagList.add("ReferencedObject");
        minimalTagList.add("SignedObjectId");
        minimalTagList.add("SignedObjectDigest");
        minimalTagList.add("GpsVersionID");
        minimalTagList.add("GpsAltitude");
        minimalTagList.add("GpsAltitudeRef");
        minimalTagList.add("GpsLatitude");
        minimalTagList.add("GpsLatitudeRef");
        minimalTagList.add("GpsLongitude");
        minimalTagList.add("GpsLongitudeRef");
        minimalTagList.add("AppraisalRule");
        minimalTagList.add("AccessRule");
        minimalTagList.add("Rule");
        minimalTagList.add("ClassificationLevel");
        minimalTagList.add("FinalAction");

        largeAreaTagList=new ArrayList<>();
        largeAreaTagList.add("Address");
        largeAreaTagList.add("CustodialHistoryItem");
        largeAreaTagList.add("Description");
        largeAreaTagList.add("TextContent");

        translateMap = new HashMap<String, String>();
        translateMap.put("ArchiveUnit", "Unité d'archive");
        translateMap.put("Content", "Descriptif");
        translateMap.put("AcquiredDate", "Date de numérisation");
        translateMap.put("ArchivalAgencyArchiveUnitIdentifier", "ID-archiveur");
        translateMap.put("AuthorizedAgent", "Titulaire des droits");
        translateMap.put("Addressee", "Destinataire");
        translateMap.put("Coverage", "Couverture");
        translateMap.put("CreatedDate", "Date de création");
        translateMap.put("CustodialHistory", "Historique conservation");
        translateMap.put("Description", "Description");
        translateMap.put("DescriptionLanguage", "Langue de description");
        translateMap.put("DescriptionLevel", "Niveau de description");
        translateMap.put("DocumentType", "Type de document");
        translateMap.put("EndDate", "Date de fin");
        translateMap.put("Event", "Evènement");
        translateMap.put("FilePlanPosition", "ID-classement-prod");
        translateMap.put("Gps", "GPS");
        translateMap.put("Keyword", "Mot-clé");
        translateMap.put("Language", "Langue");
        translateMap.put("OriginatingAgency", "Service producteur");
        translateMap.put("OriginatingAgencyArchiveUnitIdentifier", "ID-producteur");
        translateMap.put("OriginatingSystemId", "ID-SI-prod");
        translateMap.put("OriginatingSystemIdReplyTo", "ID-Re-SI-prod");
        translateMap.put("ReceivedDate", "Date de réception");
        translateMap.put("Recipient", "Destinataire-copie");
        translateMap.put("RegisteredDate", "Date d'enregistrement");
        translateMap.put("RelatedObjectReference", "Objet associé");
        translateMap.put("Sender", "Expéditeur");
        translateMap.put("SentDate", "Date d'envoi");
        translateMap.put("Signature", "Signature");
        translateMap.put("Source", "ID-papier");
        translateMap.put("StartDate", "Date de début");
        translateMap.put("Status", "Etat");
        translateMap.put("SubmissionAgency", "Service versant");
        translateMap.put("SystemId", "ID-interne");
        translateMap.put("Tag", "Tag");
        translateMap.put("TextContent", "Contenu textuel");
        translateMap.put("Title", "Titre");
        translateMap.put("TransactedDate", "Date de transaction");
        translateMap.put("TransferringAgencyArchiveUnitIdentifier", "ID-versant");
        translateMap.put("Transmitter", "Emetteur");
        translateMap.put("Type", "Type OAIS");
        translateMap.put("Version", "Version");
        translateMap.put("Writer", "Rédacteur");
        translateMap.put("AnyXMLType", "XML Brut");

        // CustodialHistory subfields
        translateMap.put("CustodialHistoryItem", "Détail");

        // Keyword subfields
        translateMap.put("KeywordContent", "Valeur");
        translateMap.put("KeywordReference", "Identifiant");
        translateMap.put("KeywordType", "Type");

        // Coverage subfields
        translateMap.put("Spatial", "Spatiale");
        translateMap.put("Temporal", "Temporelle");
        translateMap.put("Juridictional", "juridictionnelle ");

        // AgentType subfields
        translateMap.put("FirstName", "Prénom");
        translateMap.put("BirthName", "Nom de naissance");
        translateMap.put("FullName", "Nom complet");
        translateMap.put("GivenName", "Nom d'usage");
        translateMap.put("Gender", "Sexe");
        translateMap.put("BirthDate", "Date de naissance");
        translateMap.put("BirthPlace", "Lieu de naissance");
        translateMap.put("DeathDate", "Date de décès");
        translateMap.put("DeathPlace", "Lieu de décès");
        translateMap.put("Nationality", "Nationalité");
        translateMap.put("CorpName", "Nom d'entité");
        translateMap.put("Identifier", "Identifiant");
        translateMap.put("Function", "Fonction");
        translateMap.put("Activity", "Activité");
        translateMap.put("Position", "Poste");
        translateMap.put("Role", "Rôle");
        translateMap.put("Mandate", "Droits");

        // PlaceType subfields
        translateMap.put("Geogname", "Nom de lieu");
        translateMap.put("Address", "Adresse");
        translateMap.put("PostalCode", "Code postal");
        translateMap.put("City", "Ville");
        translateMap.put("Region", "Province");
        translateMap.put("Country", "Pays");

        // RelatedObjectReference subfields
        translateMap.put("IsVersionOf", "Est une version de");
        translateMap.put("Replaces", "Remplace");
        translateMap.put("Requires", "Requiert");
        translateMap.put("IsPartOf", "Est une partie de");
        translateMap.put("References", "Référence");

        // DataObjectOrArchiveUnitReferenceType subfields
        translateMap.put("ArchiveUnitRefID", "ID-AU-interne");
        translateMap.put("DataObjectReference", "ID-Objet-interne");
        translateMap.put("RepositoryArchiveUnitPID", "ID-AU-SAE");
        translateMap.put("RepositoryObjectPID", "ID-Objet-SAE");
        translateMap.put("ExternalReference", "ID-externe");

        // Event subfields
        translateMap.put("EventIdentifier", "Identifiant");
        translateMap.put("EventTypeCode", "Code de type");
        translateMap.put("EventType", "Type");
        translateMap.put("EventDateTime", "Date & Heure");
        translateMap.put("EventDetail", "Détail");
        translateMap.put("Outcome", "Résultat");
        translateMap.put("OutcomeDetail", "Détail du résultat");
        translateMap.put("OutcomeDetailMessage", "Message de résultat");
        translateMap.put("EventDetailData", "Détail technique");

        // Signature all subfields
        translateMap.put("SigningTime", "Date de signature");
        translateMap.put("ValidationTime", "Date de validation");
        translateMap.put("Masterdata", "Référentiel technique");
        translateMap.put("ReferencedObject", "Objet signé");
        translateMap.put("SignedObjectId", "Identifiant");
        translateMap.put("SignedObjectDigest", "Hachage");

        // GPS subfields
        translateMap.put("GpsVersionID", "Identifiant version");
        translateMap.put("GpsAltitude", "Altitude");
        translateMap.put("GpsAltitudeRef", "Référence alt.");
        translateMap.put("GpsLatitude", "Latitude");
        translateMap.put("GpsLatitudeRef", "Référence lat..");
        translateMap.put("GpsLongitude", "Longitude");
        translateMap.put("GpsLongitudeRef", "Référence long.");
        translateMap.put("GpsDateStamp", "Date & Heure GPS");

        //Management all subfields
        translateMap.put("Management", "Gestion");
        translateMap.put("AccessRule", "Règles communicabilité");
        translateMap.put("AppraisalRule", "Règles DUA");
        translateMap.put("ClassificationRule", "Règles classification");
        translateMap.put("DisseminationRule", "Règles diffusion");
        translateMap.put("ReuseRule", "Règles réutilisation");
        translateMap.put("StorageRule", "Règles DUC");
        translateMap.put("Rule", "ID-règle");
        translateMap.put("StartDate", "Date de début");
        translateMap.put("PreventInheritance", "Héritage bloqué");
        translateMap.put("RefNonRuleId", "ID-règle bloquée");
        translateMap.put("FinalAction", "Action Finale");
        translateMap.put("ClassificationAudience", "Mention spéciale");
        translateMap.put("ClassificationLevel", "Niveau");
        translateMap.put("ClassificationOwner", "Propriétaire");
        translateMap.put("ClassificationReassessingDate", "Date de révision");
        translateMap.put("NeedReassessingAuthorization", "Révision humaine");
        translateMap.put("LogBook", "Journaux");
        translateMap.put("NeedAuthorization", "Révision humaine");
        translateMap.put("UpdateOperation", "Opération de MàJ");
        translateMap.put("ArchiveUnitIdentifierKey", "Recherche");
        translateMap.put("MetadataName", "Nom de métadonnée");
        translateMap.put("MetadataValue", "Valeur de métadonnée");



        //ArchiveUnitProfile
        translateMap.put("ArchiveUnitProfile", "Profil d'unité");
    }

    static int labelWidth=0;

    static public int computeLabelWidth(){
        if (labelWidth!=0)
            return (int)labelWidth;

        double result=0;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
        for (Map.Entry<String,String>e: MetadataEditorConstants.translateMap.entrySet()){
            double width=MetadataEditor.LABEL_FONT.getStringBounds(e.getValue(),frc).getWidth();
            result=Math.max(result,width);
        }
        labelWidth=(int)(result+31.99);
        return labelWidth;
    }
}
