/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.resip.sedaobjecteditor.model;

import fr.gouv.vitam.tools.sedalib.core.AbstractUnitaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.management.LogBook;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.NamedTypeMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Puts back together an edited object from the objects its editor holds, and summarises it: what
 * the composite editors do when an edition ends or is folded.
 */
public final class EditedObjects {

    // the summary of a complex metadata is cut beyond this length
    private static final int SUMMARY_CUT = 160;

    private EditedObjects() {}

    /**
     * Builds an empty metadata of the same class, element name and SEDA version.
     *
     * @param sedaMetadata the metadata
     * @return the empty metadata
     * @throws SEDALibException if the class has no convenient constructor
     */
    public static SEDAMetadata emptyCopyOf(SEDAMetadata sedaMetadata) throws SEDALibException {
        Class<?> metadataClass = sedaMetadata.getClass();
        boolean named = metadataClass.getName().contains("namedtype");
        try {
            if (sedaMetadata instanceof ComplexListType) {
                SedaVersion sedaVersion = ((ComplexListType) sedaMetadata).getSedaVersion();
                if (named) return (SEDAMetadata) metadataClass
                    .getConstructor(SedaVersion.class, String.class)
                    .newInstance(sedaVersion, sedaMetadata.getXmlElementName());
                return (SEDAMetadata) metadataClass.getConstructor(SedaVersion.class).newInstance(sedaVersion);
            }
            if (named) {
                Constructor<?> constructor = metadataClass.getConstructor(String.class);
                return (SEDAMetadata) constructor.newInstance(sedaMetadata.getXmlElementName());
            }
            return (SEDAMetadata) metadataClass.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new SEDALibException(
                "Pas de constructeur vide pour la métadonnée de type [" + metadataClass + "]",
                e
            );
        } catch (InvocationTargetException e) {
            throw new SEDALibException(
                "Erreur durant la création de la métadonnée vide de type [" + metadataClass + "]",
                e.getTargetException()
            );
        }
    }

    /**
     * Builds a complex metadata of the same kind as the edited one with the edited fields, in the
     * schema order.
     *
     * @param edited the edited complex metadata
     * @param fields the edited fields
     * @return the new complex metadata
     * @throws SEDALibException if a field can't be added
     */
    public static ComplexListType rebuild(ComplexListType edited, List<SEDAMetadata> fields) throws SEDALibException {
        ComplexListType result = (ComplexListType) emptyCopyOf(edited);
        for (SEDAMetadata field : fields) result.addMetadata(field);
        return result;
    }

    /**
     * Puts the edited fields in a unitary DataObject, in the schema order.
     *
     * @param edited the edited unitary DataObject
     * @param fields the edited fields
     * @throws SEDALibException if a field can't be added
     */
    public static void rebuild(AbstractUnitaryDataObject edited, List<SEDAMetadata> fields) throws SEDALibException {
        AbstractUnitaryDataObject ordered = edited.getEmptySameAbstractUnitaryDataObjet();
        for (SEDAMetadata field : fields) ordered.addMetadata(field);
        edited.setMetadataList(ordered.getMetadataList());
    }

    /**
     * Puts the edited parts in an ArchiveUnit, the absent ones being removed.
     *
     * @param edited the edited ArchiveUnit
     * @param parts  the edited ArchiveUnitProfile, Content and Management, the present ones
     */
    public static void rebuild(ArchiveUnit edited, List<SEDAMetadata> parts) {
        ArchiveUnitProfile archiveUnitProfile = null;
        Content content = null;
        Management management = null;
        for (SEDAMetadata part : parts) {
            if (part instanceof ArchiveUnitProfile) archiveUnitProfile = (ArchiveUnitProfile) part;
            else if (part instanceof Content) content = (Content) part;
            else if (part instanceof Management) management = (Management) part;
        }
        edited.setContent(content);
        edited.setManagement(management);
        edited.setArchiveUnitProfile(archiveUnitProfile);
    }

    /**
     * Puts the edited objects in a DataObjectGroup: the removed DataObjects leave it, the new ones
     * join it and its package, and the LogBook is replaced.
     *
     * @param edited the edited DataObjectGroup
     * @param parts  the edited DataObjects and LogBook
     * @throws SEDALibException if a new DataObject can't join the package
     */
    public static void rebuild(DataObjectGroup edited, List<Object> parts) throws SEDALibException {
        List<BinaryDataObject> bdoList = new ArrayList<>();
        List<PhysicalDataObject> pdoList = new ArrayList<>();
        LogBook logBook = null;
        for (Object part : parts) {
            if (part instanceof BinaryDataObject) bdoList.add((BinaryDataObject) part);
            else if (part instanceof PhysicalDataObject) pdoList.add((PhysicalDataObject) part);
            else if (part instanceof LogBook) logBook = (LogBook) part;
        }
        edited.setLogBook(logBook);

        List<BinaryDataObject> previousBdoList = new ArrayList<>(edited.getBinaryDataObjectList());
        for (BinaryDataObject bdo : previousBdoList) if (!bdoList.contains(bdo)) edited.removeDataObject(bdo);
        for (BinaryDataObject bdo : bdoList) if (!previousBdoList.contains(bdo)) {
            edited.getDataObjectPackage().addDataObjectPackageIdElement(bdo);
            edited.addDataObject(bdo);
        }

        List<PhysicalDataObject> previousPdoList = new ArrayList<>(edited.getPhysicalDataObjectList());
        for (PhysicalDataObject pdo : previousPdoList) if (!pdoList.contains(pdo)) edited.removeDataObject(pdo);
        for (PhysicalDataObject pdo : pdoList) if (!previousPdoList.contains(pdo)) {
            edited.getDataObjectPackage().addDataObjectPackageIdElement(pdo);
            edited.addDataObject(pdo);
        }
    }

    /**
     * Summarises a metadata by its values, the ones of a complex metadata between braces, cut
     * beyond {@value #SUMMARY_CUT} characters.
     *
     * @param sedaMetadata the metadata
     * @return the summary
     */
    public static String summary(SEDAMetadata sedaMetadata) {
        return summary(sedaMetadata, 0, 0);
    }

    private static String summary(SEDAMetadata sedaMetadata, int depth, int length) {
        if (sedaMetadata instanceof ComplexListType) {
            List<String> summaryList = new ArrayList<>();
            for (SEDAMetadata field : ((ComplexListType) sedaMetadata).getMetadataList()) {
                String fieldSummary = summary(field, depth + 1, length);
                length += fieldSummary.length() + 2;
                summaryList.add(fieldSummary);
                if (length > SUMMARY_CUT) break;
            }
            String result = String.join(", ", summaryList) + (length > SUMMARY_CUT ? "..." : "");
            return depth > 0 ? "{" + result + "}" : result;
        }
        if (sedaMetadata instanceof NamedTypeMetadata) return ((NamedTypeMetadata) sedaMetadata).getValue().toString();
        return "?";
    }

    /**
     * Summarises a unitary DataObject by its texts and identifiers, and for a BinaryDataObject by
     * its file name, date, MIME type and format.
     *
     * @param dataObject the unitary DataObject
     * @param unknown    the text standing for an unknown file or format information
     * @return the summary
     * @throws SEDALibException if a file or format information can't be read
     */
    public static String summary(AbstractUnitaryDataObject dataObject, String unknown) throws SEDALibException {
        List<String> summaryList = new ArrayList<>();
        for (SEDAMetadata sm : dataObject.getMetadataList()) {
            if (sm instanceof StringType) summaryList.add(((StringType) sm).getValue());
            else if (sm instanceof PersistentIdentifier) summaryList.add(((PersistentIdentifier) sm).getSummary());
            else if (sm instanceof FileInfo) {
                summaryList.add(orUnknown(((FileInfo) sm).getSimpleMetadata("Filename"), unknown));
                summaryList.add(orUnknown(((FileInfo) sm).getSimpleMetadata("LastModified"), unknown));
            } else if (sm instanceof FormatIdentification) {
                summaryList.add(orUnknown(((FormatIdentification) sm).getSimpleMetadata("MimeType"), unknown));
                summaryList.add(orUnknown(((FormatIdentification) sm).getSimpleMetadata("FormatId"), unknown));
            }
        }
        return String.join(", ", summaryList);
    }

    private static String orUnknown(String value, String unknown) {
        return ((value == null) || value.isEmpty()) ? unknown : value;
    }
}
