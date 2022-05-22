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
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SEDAObjectEditorConstants {
    /**
     * The SEDAMetadata filling information map.
     */
    static public HashMap<String, String> sedaMetadataInformationMap;

    /**
     * The list of tags selected in minimal SEDAMetadata generation.
     */
    static public List<String> minimalTagList;

    /**
     * The list of tag for which the editor is a multilines text.
     */
    static public List<String> largeAreaTagList;

    /**
     * The object tags translation map.
     */
    static public HashMap<String, String> translateMap;


    static {
        sedaMetadataInformationMap = new HashMap<String, String>();
        sedaMetadataInformationMap.put("DateTimeType", "Date ou Date/Temps au format ISO8601\n    YYYY-MM-DD[Timezone ou Z]" +
                " ou YYYY-MM-DD'T'HH:MM:SS[Timezone ou Z]");
        sedaMetadataInformationMap.put("DateType", "Date\n    YYYY-MM-DD");
        sedaMetadataInformationMap.put("AnyXMLType", "Bloc XML de structure non connue par ReSIP");
        sedaMetadataInformationMap.put("AgentType", "Metadonnée de type agent");
        sedaMetadataInformationMap.put("AgencyType", "Metadonnée de type agence");
        sedaMetadataInformationMap.put("PlaceType", "Metadonnée de type localisation");
        sedaMetadataInformationMap.put("StringType", "Metadonnée de type chaîne de caractères");
        sedaMetadataInformationMap.put("DescriptionLevel", "Metadonnée de type code de niveau de description, fait partie des valeurs: " +
                        "Fonds, Subfonds, Class, Collection, Series, Subseries, RecordGrp, SubGrp, File, Item, OtherLevel");
        sedaMetadataInformationMap.put("KeywordType", "Metadonnée de type code de mot-clef, fait partie des valeurs: " +
                "corpname, famname, geogname, name, occupation, persname, subject, genreform, function");
        sedaMetadataInformationMap.put("IntegerType", "Metadonnée de type entier long");
        sedaMetadataInformationMap.put("SIPInternalIDType", "Metadonnée de type ID de référence interne au SIP");
        sedaMetadataInformationMap.put("DataObjectOrArchiveUnitReferenceType", "Metadonnée de type ID de référence ArchiveUnit ou DataObject interne au SIP ou externe ");
        sedaMetadataInformationMap.put("DataObjectReference", "Metadonnée de type ID de référence DataObject interne au SIP");
        sedaMetadataInformationMap.put("ReferencedObject", "Metadonnée de type référence à un objet signé");
        sedaMetadataInformationMap.put("TextType", "Métadonnée de type chaîne de caractères. Il peut y avoir plusieurs occurrences " +
                "de ce champ en plusieurs langues et il faut dans ce cas spécifier la langue dans un attribut xml. " +
                "Par exemple pour définir la langue du texte comme anglaise on mettra l'attribut xml:lang=\"en\"");
        sedaMetadataInformationMap.put("RelatedObjectReference", "Metadonnée de type relation ArchiveUnit ou DataObject externe ou interne au SIP");
        sedaMetadataInformationMap.put("DigestType", "Metadonnée de type hachage avec un attribut d'algorithme");
        sedaMetadataInformationMap.put("ArchiveUnitProfile", "Identifiant du profil d'unité archivistique");
        sedaMetadataInformationMap.put("Coverage", "Métadonnées de couverture spatiale, temporelle ou juridictionnelle");
        sedaMetadataInformationMap.put("CustodialHistory", "Métadonnées indiquant les changements successifs de propriété, " +
                "de responsabilité et de conservation avant leur entrée dans le lieu de conservation");
        sedaMetadataInformationMap.put("Gps", "Métadonnées de coordonnées gps complétées ou vérifiées par un utilisateur");
        sedaMetadataInformationMap.put("Keyword", "Métadonnées de mots-clef avec contexte inspiré du SEDA 1.0. KeywordType " +
                "doit faire partie des valeurs: corpname, famname, geogname, name, occupation, persname, subject, genreform, function");
        sedaMetadataInformationMap.put("Signer", "Métadonnées d'un signataire de la transaction ou de l'objet");
        sedaMetadataInformationMap.put("Validator", "Métadonnées du validateur de la signature");
        sedaMetadataInformationMap.put("Signature", "Ensemble des métadonnées relatives à la signature.");
        sedaMetadataInformationMap.put("AccessRule", "Règle de communicabilité");
        sedaMetadataInformationMap.put("AppraisalRule", "Règle de durée d’utilité administrative (FinalAction possible Keep ou Destroy)");
        sedaMetadataInformationMap.put("ClassificationRule", "Règle de classification\n    ClassificationLevel et ClassificationOwner obligatoires");
        sedaMetadataInformationMap.put("DisseminationRule", "Règle de diffusion");
        sedaMetadataInformationMap.put("ReuseRule", "Règle de réutilisation");
        sedaMetadataInformationMap.put("StorageRule", "Règle de durée d’utilité courante (FinalAction possible RestrictAccess,Transfer ou Copy)");
        sedaMetadataInformationMap.put("Event", "Evènement (EventDateTime obligatoire)");
        sedaMetadataInformationMap.put("UpdateOperation", "Définition d'attachement à une AU existante\n    Accepte aussi la forme <UpdateOperation><SystemId>guid</SystemId><UpdateOperation>)");

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
        minimalTagList.add("HoldRule");
        minimalTagList.add("Rule");
        minimalTagList.add("ClassificationLevel");
        minimalTagList.add("FinalAction");
        minimalTagList.add("FormatLitteral");
        minimalTagList.add("MimeType");
        minimalTagList.add("FormatId");
        minimalTagList.add("Filename");
        minimalTagList.add("LastModified");

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
        translateMap.put("Corpname", "Nom d'entité");
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
        translateMap.put("ArchiveUnitRefId", "ID-AU-interne");
        translateMap.put("DataObjectReference", "ID-Objet-interne");
        translateMap.put("DataObjectReferenceId", "Objet");
        translateMap.put("DataObjectGroupReferenceId", "Groupe d'objets");
        translateMap.put("RepositoryArchiveUnitPID", "ID-AU-SAE");
        translateMap.put("RepositoryObjectPID", "ID-Objet-SAE");
        translateMap.put("ExternalReference", "ID-externe");

        // LinkingAgentIdentifier subfields
        translateMap.put("LinkingAgentIdentifierType", "Type d'indentifiant");
        translateMap.put("LinkingAgentIdentifierValue", "Valeur d'identifiant");
        translateMap.put("LinkingAgentRole", "Rôle");

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
        translateMap.put("LinkingAgentIdentifier", "Agent répertorié");

        // Signature all subfields
        translateMap.put("SigningTime", "Date de signature");
        translateMap.put("ValidationTime", "Date de validation");
        translateMap.put("Masterdata", "Référentiel technique");
        translateMap.put("ReferencedObject", "Objet signé");
        translateMap.put("SignedObjectId", "Identifiant");
        translateMap.put("SignedObjectDigest", "Hachage");
        translateMap.put("Signer", "Signataire");
        translateMap.put("Validator", "Valideur");

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
        translateMap.put("AccessRule", "Règles comm.");
        translateMap.put("AppraisalRule", "Règles DUA");
        translateMap.put("ClassificationRule", "Règles classification");
        translateMap.put("DisseminationRule", "Règles diffusion");
        translateMap.put("ReuseRule", "Règles réutilisation");
        translateMap.put("StorageRule", "Règles DUC");
        translateMap.put("HoldRule", "Règles de Gel");
        translateMap.put("HoldOwner", "Propriétaire");
        translateMap.put("PreventRearrangement", "Blocage du reclassement");
        translateMap.put("HoldReason", "Raison");
        translateMap.put("HoldReassessingDate", "Date de révision");
        translateMap.put("HoldEndDate", "Date de dégel");
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

        //BinaryDataObject
        translateMap.put("BinaryDataObject", "Numérique");
        translateMap.put("DataObjectVersion", "Version");
        translateMap.put("MessageDigest", "Hachage");
        translateMap.put("Size", "Taille");
        translateMap.put("Uri", "Chemin dans le SIP");
        translateMap.put("FormatIdentification", "Format");
        translateMap.put("FormatLitteral", "Description");
        translateMap.put("MimeType", "Type Mime");
        translateMap.put("FormatId", "ID-Pronom");
        translateMap.put("Encoding", "Encodage");
        translateMap.put("FileInfo", "Fichier");
        translateMap.put("Filename", "Nom de fichier");
        translateMap.put("CreatingApplicationName", "Application-Nom");
        translateMap.put("CreatingApplicationVersion", "Application-Version");
        translateMap.put("DateCreatedByApplication", "Date de création");
        translateMap.put("CreatingOs", "OS-Nom");
        translateMap.put("CreatingOsVersion", "OS-Version");
        translateMap.put("LastModified", "Date de modification");
        translateMap.put("Metadata", "Métadonnées");
        translateMap.put("Audio", "Audio");
        translateMap.put("Document", "Document");
        translateMap.put("Image", "Image");
        translateMap.put("Text", "Texte");
        translateMap.put("Video", "Vidéo");

        //PhysicalDataObject
        translateMap.put("PhysicalDataObject", "Physique");
        translateMap.put("PhysicalId", "ID-Physique");
        translateMap.put("PhysicalDimensions", "Dimensions");
        translateMap.put("Width", "Largeur");
        translateMap.put("Height", "Hauteur");
        translateMap.put("Depth", "Profondeur");
        translateMap.put("Shape", "Forme");
        translateMap.put("Diameter", "Diamètre");
        translateMap.put("Length", "Longueur");
        translateMap.put("Thickness", "Epaisseur");
        translateMap.put("Weight", "Poids");
        translateMap.put("NumberOfPage", "Nombre de pages");

        //DataObjectGroup
        translateMap.put("DataObjectGroup", "Groupe d'objets");
        translateMap.put("DataObject", "Objet numérique ou physique");


        //Others
        translateMap.put("Unknown","Non défini");
    }

    /**
     * Translate object name.
     *
     * @param tag the tag
     * @return the translated string, if known, if not the same
     */
    public static String translateTag(String tag) {
        String result = SEDAObjectEditorConstants.translateMap.get(tag);
        if (result == null)
            return tag;
        return result;
    }

    static private int labelWidth=0;

    /**
     * Compute max label width taking into account all object translations.
     *
     * @return the int
     */
    static public int computeLabelWidth(){
        if (labelWidth!=0)
            return labelWidth;

        double result=0;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
        for (Map.Entry<String,String>e: SEDAObjectEditorConstants.translateMap.entrySet()){
            double width= SEDAObjectEditor.LABEL_FONT.getStringBounds(e.getValue(),frc).getWidth();
            result=Math.max(result,width);
        }
        labelWidth=(int)(result+31.99);
        return labelWidth;
    }
}
