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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.composite;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.sedalib.utils.LocalDateTimeUtil;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.tools.resip.app.ResipGraphicApp.OK_DIALOG;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;
import static fr.gouv.vitam.tools.sedalib.core.BinaryDataObject.getDigestSha512;

/**
 * The BinaryDataObject object editor class.
 */
public class BinaryDataObjectEditor extends CompositeEditor {

    /**
     * The edited on disk path.
     */
    private Path editedOnDiskPath;

    /**
     * The graphic elements.
     */
    private JButton openButton;

    /**
     * Ordered SEDAObjectEditor collection
     */
    private SEDAObjectEditor[] objectEditorArray;

    /**
     * Ordered SEDAObjectEditor collection
     */
    static final int DATA_OBJECT_PROFILE = 0;
    static final String DATA_OBJECT_PROFILE_ELEMENT_NAME = "DataObjectProfile";
    static final int DATA_OBJECT_VERSION = 1;
    static final String DATA_OBJECT_VERSION_ELEMENT_NAME = "DataObjectVersion";
    static final int URI = 2;
    static final int MESSAGE_DIGEST = 3;
    static final String MESSAGE_DIGEST_ELEMENT_NAME = "MessageDigest";
    static final int SIZE = 4;
    static final String SIZE_ELEMENT_NAME = "Size";
    static final int COMPRESSED = 5;
    static final String COMPRESSED_ELEMENT_NAME = "Compressed";
    static final int FORMAT_IDENTIFICATION = 6;
    static final String FORMAT_IDENTIFICATION_ELEMENT_NAME = "FormatIdentification";
    static final int FILE_INFO = 7;
    static final String FILE_INFO_ELEMENT_NAME = "FileInfo";
    static final int METADATA = 8;
    static final String METADATA_ELEMENT_NAME = "Metadata";

    static final String STRING_TYPE_CLASS = "StringType";
    static final String DIGEST_TYPE_CLASS = "DigestType";
    static final String INTEGER_TYPE_CLASS = "IntegerType";
    static final String FORMAT_IDENTIFICATION_CLASS = "FormatIdentification";
    static final String FILE_INFO_CLASS = "FileInfo";
    static final String METADATA_CLASS = "Metadata";

    /**
     * Explicative texts
     */
    static final String UNKNOWN = "Unknown";
    static final String TO_BE_DEFINED = "Tbd";

    /**
     * Instantiates a new BinaryDataObject editor.
     *
     * @param editedObject the BinaryDataObject editedObject
     * @param father       the father
     */
    public BinaryDataObjectEditor(BinaryDataObject editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
        this.editedOnDiskPath = editedObject.getOnDiskPath();
        this.objectEditorArray = new SEDAObjectEditor[9];
    }

    private BinaryDataObject getBinaryDataObjectMetadata() {
        return (BinaryDataObject) editedObject;
    }

    @Override
    public String getTag() {
        return "BinaryDataObject";
    }

    @Override
    public String getName() {
        if (editedObject == null)
            return translateTag(getTag()) + " - " + translateTag(UNKNOWN);
        return translateTag(getTag()) + " - " +
                (getBinaryDataObjectMetadata().getInDataObjectPackageId() == null ? TO_BE_DEFINED :
                        getBinaryDataObjectMetadata().getInDataObjectPackageId());
    }

    @Override
    public BinaryDataObject extractEditedObject() throws SEDALibException {
        BinaryDataObject tmpBdo = new BinaryDataObject();
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            SEDAMetadata subMetadata = (SEDAMetadata) objectEditor.extractEditedObject();
            switch (subMetadata.getXmlElementName()) {
                case DATA_OBJECT_PROFILE_ELEMENT_NAME:
                    tmpBdo.dataObjectProfile = (StringType) subMetadata;
                    break;
                case DATA_OBJECT_VERSION_ELEMENT_NAME:
                    tmpBdo.dataObjectVersion = (StringType) subMetadata;
                    break;
                case "Uri":
                    tmpBdo.uri = (StringType) subMetadata;
                    break;
                case MESSAGE_DIGEST_ELEMENT_NAME:
                    tmpBdo.messageDigest = (DigestType) subMetadata;
                    break;
                case SIZE_ELEMENT_NAME:
                    tmpBdo.size = (IntegerType) subMetadata;
                    break;
                case COMPRESSED_ELEMENT_NAME:
                    tmpBdo.compressed = (StringType) subMetadata;
                    break;
                case FORMAT_IDENTIFICATION_ELEMENT_NAME:
                    tmpBdo.formatIdentification = (FormatIdentification) subMetadata;
                    break;
                case FILE_INFO_ELEMENT_NAME:
                    tmpBdo.fileInfo = (FileInfo) subMetadata;
                    break;
                case METADATA_ELEMENT_NAME:
                    tmpBdo.metadata = (Metadata) subMetadata;
                    break;
                default:
                    // no other sub-editors metadata expected
                    break;
            }
        }
        getBinaryDataObjectMetadata().dataObjectProfile = tmpBdo.dataObjectProfile;
        getBinaryDataObjectMetadata().dataObjectVersion = tmpBdo.dataObjectVersion;
        getBinaryDataObjectMetadata().uri = tmpBdo.uri;
        getBinaryDataObjectMetadata().messageDigest = tmpBdo.messageDigest;
        getBinaryDataObjectMetadata().size = tmpBdo.size;
        getBinaryDataObjectMetadata().compressed = tmpBdo.compressed;
        getBinaryDataObjectMetadata().formatIdentification = tmpBdo.formatIdentification;
        getBinaryDataObjectMetadata().fileInfo = tmpBdo.fileInfo;
        getBinaryDataObjectMetadata().metadata = tmpBdo.metadata;
        getBinaryDataObjectMetadata().setOnDiskPath(editedOnDiskPath);

        return getBinaryDataObjectMetadata();
    }

    private String getItOrUnknown(String str) {
        if ((str == null) || (str.isEmpty()))
            return translateTag(UNKNOWN);
        return str;
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<>(objectEditorList.size());
        String tmp;

        if (getBinaryDataObjectMetadata().dataObjectProfile != null)
            summaryList.add(getBinaryDataObjectMetadata().dataObjectProfile.getValue());

        if (getBinaryDataObjectMetadata().dataObjectVersion != null)
            summaryList.add(getBinaryDataObjectMetadata().dataObjectVersion.getValue());
        else
            summaryList.add(translateTag(UNKNOWN));

        if (getBinaryDataObjectMetadata().fileInfo != null) {
            tmp = getItOrUnknown(getBinaryDataObjectMetadata().fileInfo.getSimpleMetadata("Filename"));
        } else
            tmp = translateTag(UNKNOWN);
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().formatIdentification != null) {
            tmp = getItOrUnknown(getBinaryDataObjectMetadata().formatIdentification.getSimpleMetadata("MimeType"));
        } else
            tmp = translateTag(UNKNOWN);
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().formatIdentification != null) {
            tmp = getItOrUnknown(getBinaryDataObjectMetadata().formatIdentification.getSimpleMetadata("FormatId"));
        } else
            tmp = translateTag(UNKNOWN);
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().fileInfo != null) {
            tmp = getItOrUnknown(getBinaryDataObjectMetadata().fileInfo.getSimpleMetadata("LastModified"));
        } else
            tmp = translateTag(UNKNOWN);
        summaryList.add(tmp);

        return String.join(", ", summaryList);
    }

    private void updateObjectEditorList() throws SEDALibException {
        List<SEDAObjectEditor> result = new ArrayList<>();
        for (SEDAObjectEditor sedaObjectEditor : objectEditorArray)
            if (sedaObjectEditor != null)
                result.add(sedaObjectEditor);
        objectEditorList = result;
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<>();

        JPanel moreButtons = new JPanel();

        openButton = new JButton();
        openButton.setIcon(new ImageIcon(getClass().getResource("/icon/folder-open.png")));
        openButton.setToolTipText((editedOnDiskPath == null ? "" : editedOnDiskPath.toAbsolutePath().toString()));
        openButton.setText("");
        openButton.setMaximumSize(new Dimension(16, 16));
        openButton.setMinimumSize(new Dimension(16, 16));
        openButton.setPreferredSize(new Dimension(16, 16));
        openButton.setBorderPainted(false);
        openButton.setContentAreaFilled(false);
        openButton.setFocusPainted(false);
        openButton.setFocusable(false);
        openButton.setVisible(getBinaryDataObjectMetadata().getOnDiskPath() != null);
        openButton.addActionListener(arg -> this.openButton());
        moreButtons.add(openButton);

        JButton defineButton = new JButton();
        defineButton.setIcon(new ImageIcon(getClass().getResource("/icon/document-save-as.png")));
        defineButton.setToolTipText("Changer le fichier associé...");
        defineButton.setText("");
        defineButton.setMaximumSize(new Dimension(16, 16));
        defineButton.setMinimumSize(new Dimension(16, 16));
        defineButton.setPreferredSize(new Dimension(16, 16));
        defineButton.setBorderPainted(false);
        defineButton.setContentAreaFilled(false);
        defineButton.setFocusPainted(false);
        defineButton.setFocusable(false);
        defineButton.addActionListener(arg -> this.defineButton());
        moreButtons.add(defineButton);

        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this, moreButtons, false);
        if (getBinaryDataObjectMetadata().dataObjectProfile != null)
            objectEditorArray[DATA_OBJECT_PROFILE] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().dataObjectProfile, this);
        if (getBinaryDataObjectMetadata().dataObjectVersion != null)
            objectEditorArray[DATA_OBJECT_VERSION] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().dataObjectVersion, this);
        if (getBinaryDataObjectMetadata().uri != null)
            objectEditorArray[URI] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().uri, this);
        if (getBinaryDataObjectMetadata().messageDigest != null)
            objectEditorArray[MESSAGE_DIGEST] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().messageDigest, this);
        if (getBinaryDataObjectMetadata().size != null)
            objectEditorArray[SIZE] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().size, this);
        if (getBinaryDataObjectMetadata().compressed != null)
            objectEditorArray[COMPRESSED] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().compressed, this);
        if (getBinaryDataObjectMetadata().formatIdentification != null)
            objectEditorArray[FORMAT_IDENTIFICATION] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().formatIdentification, this);
        if (getBinaryDataObjectMetadata().fileInfo != null)
            objectEditorArray[FILE_INFO] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().fileInfo, this);
        if (getBinaryDataObjectMetadata().metadata != null)
            objectEditorArray[METADATA] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().metadata, this);
        updateObjectEditorList();
    }

    private boolean setReadOnly(Path path) {
        try {
            // Office bug workaround
            // This is a special patch to prevent Office to change a file when opening it to see the content...
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                Files.setAttribute(path, "dos:readonly", true);
        } catch (IOException e) {
            return UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                    "Impossible de passer le fichier à ouvrir [" + path +
                            "] en lecture seule\nVoulez-vous tout de même l'ouvrir?",
                    "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                    null) == OK_DIALOG;
        }
        return true;
    }

    private void openButton() {
        if (editedOnDiskPath != null) {
            try {
                if (setReadOnly(editedOnDiskPath))
                    Desktop.getDesktop().open(editedOnDiskPath.toFile());
            } catch (IOException ignored) {//NOSONAR
            }
        }
    }

    private void extractFileMetadataInEditors() {
        try {
            objectEditorArray[MESSAGE_DIGEST] = createSEDAObjectEditor(new DigestType(MESSAGE_DIGEST_ELEMENT_NAME,
                    getDigestSha512(editedOnDiskPath), "SHA-512"), this);
            objectEditorArray[SIZE] = createSEDAObjectEditor(new IntegerType(SIZE_ELEMENT_NAME,
                    Files.size(editedOnDiskPath)), this);

            boolean expanded = false;
            if (objectEditorArray[FORMAT_IDENTIFICATION] != null)
                expanded = ((SEDAObjectEditorCompositePanel) objectEditorArray[FORMAT_IDENTIFICATION].getSEDAObjectEditorPanel()).isExpanded();
            IdentificationResult ir = DroidIdentifier.getInstance().getIdentificationResult(editedOnDiskPath);
            FormatIdentification formatIdentification;
            if (ir != null)
                formatIdentification = new FormatIdentification(ir.getName(), ir.getMimeType(),
                        ir.getPuid(), null);
            else
                formatIdentification = new FormatIdentification(translateTag(UNKNOWN), null,
                        translateTag(UNKNOWN), null);
            objectEditorArray[FORMAT_IDENTIFICATION] = createSEDAObjectEditor(formatIdentification, this);
            ((SEDAObjectEditorCompositePanel) objectEditorArray[FORMAT_IDENTIFICATION].getSEDAObjectEditorPanel()).setExpanded(expanded);

            expanded = false;
            if (objectEditorArray[FILE_INFO] != null)
                expanded = ((SEDAObjectEditorCompositePanel) objectEditorArray[FILE_INFO].getSEDAObjectEditorPanel()).isExpanded();
            FileInfo fileInfo = new FileInfo();
            fileInfo.addNewMetadata("Filename", editedOnDiskPath.getFileName().toString());
            fileInfo.addNewMetadata("LastModified", LocalDateTimeUtil.getFormattedDateTime(Files.getLastModifiedTime(editedOnDiskPath)));
            objectEditorArray[FILE_INFO] = createSEDAObjectEditor(fileInfo, this);
            ((SEDAObjectEditorCompositePanel) objectEditorArray[FILE_INFO].getSEDAObjectEditorPanel()).setExpanded(expanded);
            updateObjectEditorList();
        } catch (SEDALibException | IOException e) {
            e.printStackTrace();
        }
    }

    private void defineButton() {
        JFileChooser fileChooser = new JFileChooser((editedOnDiskPath == null ? Prefs.getInstance().getPrefsLoadDir() :
                editedOnDiskPath.toAbsolutePath().getParent().toString()));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showOpenDialog(ResipGraphicApp.getTheWindow()) == JFileChooser.APPROVE_OPTION) {
            editedOnDiskPath = fileChooser.getSelectedFile().toPath();

            openButton.setVisible(true);
            openButton.setToolTipText(editedOnDiskPath.toAbsolutePath().toString());

            String filename = null;
            if (objectEditorArray[FILE_INFO] != null)
                filename = ((ComplexListTypeEditor) objectEditorArray[FILE_INFO]).getComplexListTypeMetadata().getSimpleMetadata("Filename");
            if ((objectEditorArray[FILE_INFO] == null) ||
                    (filename == null) ||
                    filename.isEmpty() ||
                    UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                            "Voulez-vous extraire automatiquement les informations de ce fichier?",
                            "Question", UserInteractionDialog.WARNING_DIALOG,
                            null) == OK_DIALOG) {
                extractFileMetadataInEditors();

                sedaObjectEditorPanel.revalidate();
                sedaObjectEditorPanel.repaint();
            }
        }
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        List<Pair<String, String>> extensionList;

        switch (SEDA2Version.getSeda2Version()) {
            case 1:
                extensionList = new ArrayList<>(Arrays.asList(
                        Pair.of(DATA_OBJECT_VERSION_ELEMENT_NAME, translateTag(DATA_OBJECT_VERSION_ELEMENT_NAME)),
                        Pair.of("Uri", translateTag("Uri")),
                        Pair.of(MESSAGE_DIGEST_ELEMENT_NAME, translateTag(MESSAGE_DIGEST_ELEMENT_NAME)),
                        Pair.of(SIZE_ELEMENT_NAME, translateTag(SIZE_ELEMENT_NAME)),
                        Pair.of(FORMAT_IDENTIFICATION_ELEMENT_NAME, translateTag(FORMAT_IDENTIFICATION_ELEMENT_NAME)),
                        Pair.of(FILE_INFO_ELEMENT_NAME, translateTag(FILE_INFO_ELEMENT_NAME)),
                        Pair.of(METADATA_ELEMENT_NAME, translateTag(METADATA_ELEMENT_NAME))));
                break;
            case 2:
                extensionList = new ArrayList<>(Arrays.asList(
                        Pair.of(DATA_OBJECT_PROFILE_ELEMENT_NAME, translateTag(DATA_OBJECT_PROFILE_ELEMENT_NAME)),
                        Pair.of(DATA_OBJECT_VERSION_ELEMENT_NAME, translateTag(DATA_OBJECT_VERSION_ELEMENT_NAME)),
                        Pair.of("Uri", translateTag("Uri")),
                        Pair.of(MESSAGE_DIGEST_ELEMENT_NAME, translateTag(MESSAGE_DIGEST_ELEMENT_NAME)),
                        Pair.of(SIZE_ELEMENT_NAME, translateTag(SIZE_ELEMENT_NAME)),
                        Pair.of(FORMAT_IDENTIFICATION_ELEMENT_NAME, translateTag(FORMAT_IDENTIFICATION_ELEMENT_NAME)),
                        Pair.of(FILE_INFO_ELEMENT_NAME, translateTag(FILE_INFO_ELEMENT_NAME)),
                        Pair.of(METADATA_ELEMENT_NAME, translateTag(METADATA_ELEMENT_NAME))));
                break;
            default:
                throw new SEDALibException("Version SEDA [" + SEDA2Version.getSeda2VersionString() + "] inconnue", null);
        }

        for (SEDAObjectEditor me : objectEditorList) {
            String name = me.getTag();
            extensionList.remove(Pair.of(name, translateTag(name)));
        }
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata;
        switch (metadataName) {
            case DATA_OBJECT_PROFILE_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_PROFILE_ELEMENT_NAME, true);
                objectEditorArray[DATA_OBJECT_PROFILE] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case DATA_OBJECT_VERSION_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_VERSION_ELEMENT_NAME, true);
                objectEditorArray[DATA_OBJECT_VERSION] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "Uri":
                sedaMetadata = createSEDAMetadataSample(STRING_TYPE_CLASS, "Uri", true);
                objectEditorArray[URI] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case MESSAGE_DIGEST_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(DIGEST_TYPE_CLASS, MESSAGE_DIGEST_ELEMENT_NAME, true);
                objectEditorArray[MESSAGE_DIGEST] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case SIZE_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(INTEGER_TYPE_CLASS, SIZE_ELEMENT_NAME, true);
                objectEditorArray[SIZE] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case COMPRESSED_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(STRING_TYPE_CLASS, COMPRESSED_ELEMENT_NAME, true);
                objectEditorArray[COMPRESSED] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case FORMAT_IDENTIFICATION_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(FORMAT_IDENTIFICATION_CLASS, FORMAT_IDENTIFICATION_ELEMENT_NAME, true);
                objectEditorArray[FORMAT_IDENTIFICATION] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case FILE_INFO_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(FILE_INFO_CLASS, FILE_INFO_ELEMENT_NAME, true);
                objectEditorArray[FILE_INFO] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case METADATA_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(METADATA_CLASS, METADATA_ELEMENT_NAME, true);
                objectEditorArray[METADATA] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            default:
                throw new SEDALibException("La métadonnée [" + metadataName + "] n'existe pas dans un BinaryDataObject");
        }
        updateObjectEditorList();
    }

    @Override
    public void removeChild(SEDAObjectEditor objectEditor) throws SEDALibException {
        for (int i = 0; i < objectEditorArray.length; i++)
            if (objectEditorArray[i] == objectEditor)
                objectEditorArray[i] = null;
        updateObjectEditorList();
    }

    @Override
    public boolean canContainsMultiple(String metadataName) {
        return false;
    }

    /**
     * Create binary data object sample binary data object.
     *
     * @param minimal the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the binary data object
     * @throws SEDALibException the seda lib exception
     */
    public static BinaryDataObject createBinaryDataObjectSample(boolean minimal) throws SEDALibException {
        BinaryDataObject result = new BinaryDataObject();
        if (SEDA2Version.getSeda2Version() > 1)
            result.dataObjectProfile = (StringType) createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_PROFILE_ELEMENT_NAME, minimal);
        result.dataObjectVersion = (StringType) createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_VERSION_ELEMENT_NAME, minimal);
        result.messageDigest = (DigestType) createSEDAMetadataSample(DIGEST_TYPE_CLASS, MESSAGE_DIGEST_ELEMENT_NAME, minimal);
        result.size = (IntegerType) createSEDAMetadataSample(INTEGER_TYPE_CLASS, SIZE_ELEMENT_NAME, minimal);
        result.formatIdentification = (FormatIdentification) createSEDAMetadataSample(FORMAT_IDENTIFICATION_CLASS, FORMAT_IDENTIFICATION_ELEMENT_NAME, minimal);
        result.fileInfo = (FileInfo) createSEDAMetadataSample(FILE_INFO_CLASS, FILE_INFO_ELEMENT_NAME, minimal);
        if (!minimal) {
            result.uri = (StringType) createSEDAMetadataSample(STRING_TYPE_CLASS, "Uri", minimal);
            result.metadata = (Metadata) createSEDAMetadataSample(METADATA_CLASS, METADATA_ELEMENT_NAME, minimal);
        }
        return result;
    }
}
