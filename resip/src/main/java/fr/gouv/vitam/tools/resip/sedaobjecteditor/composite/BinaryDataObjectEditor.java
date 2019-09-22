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
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
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
    private JButton openButton, defineButton;

    /**
     * Ordered SEDAObjectEditor collection
     */
    static final int MESSAGE_DIGEST = 2;
    static final int SIZE = 3;
    static final int FORMAT_IDENTIFICATION = 5;
    static final int FILE_INFO = 6;
    private SEDAObjectEditor[] objectEditorArray;

    /**
     * Instantiates a new BinaryDataObject editor.
     *
     * @param editedObject the BinaryDataObject editedObject
     * @param father   the father
     */
    public BinaryDataObjectEditor(BinaryDataObject editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
        this.editedOnDiskPath = editedObject.getOnDiskPath();
        this.objectEditorArray = new SEDAObjectEditor[8];
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
        return translateTag("BinaryDataObject") + " - " +
                (editedObject == null ? translateTag("Unknown") :
                        (getBinaryDataObjectMetadata().getInDataObjectPackageId() == null ? "Tbd" :
                                getBinaryDataObjectMetadata().getInDataObjectPackageId()));
    }

    @Override
    public BinaryDataObject extractEditedObject() throws SEDALibException {
        BinaryDataObject tmpBdo = new BinaryDataObject();
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            SEDAMetadata subMetadata = (SEDAMetadata) objectEditor.extractEditedObject();
            switch (subMetadata.getXmlElementName()) {
                case "DataObjectVersion":
                    tmpBdo.dataObjectVersion = (StringType) subMetadata;
                    break;
                case "Uri":
                    tmpBdo.uri = (StringType) subMetadata;
                    break;
                case "MessageDigest":
                    tmpBdo.messageDigest = (DigestType) subMetadata;
                    break;
                case "Size":
                    tmpBdo.size = (IntegerType) subMetadata;
                    break;
                case "Compressed":
                    tmpBdo.compressed = (StringType) subMetadata;
                    break;
                case "FormatIdentification":
                    tmpBdo.formatIdentification = (FormatIdentification) subMetadata;
                    break;
                case "FileInfo":
                    tmpBdo.fileInfo = (FileInfo) subMetadata;
                    break;
                case "Metadata":
                    tmpBdo.metadata = (Metadata) subMetadata;
                    break;
            }
        }
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

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<String>(objectEditorList.size());
        String tmp;

        if (getBinaryDataObjectMetadata().dataObjectVersion != null)
            summaryList.add(getBinaryDataObjectMetadata().dataObjectVersion.getValue());
        else
            summaryList.add(translateTag("Unknown"));

        if (getBinaryDataObjectMetadata().fileInfo != null) {
            tmp = getBinaryDataObjectMetadata().fileInfo.getSimpleMetadata("Filename");
            if (tmp == null)
                tmp = translateTag("Unknown");
        } else
            tmp = translateTag("Unknown");
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().formatIdentification != null) {
            tmp = getBinaryDataObjectMetadata().formatIdentification.getSimpleMetadata("MimeType");
            if (tmp == null)
                tmp = translateTag("Unknown");
        } else
            tmp = translateTag("Unknown");
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().formatIdentification != null) {
            tmp = getBinaryDataObjectMetadata().formatIdentification.getSimpleMetadata("FormatId");
            if (tmp == null)
                tmp = translateTag("Unknown");
        } else
            tmp = translateTag("Unknown");
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().fileInfo != null) {
            tmp = getBinaryDataObjectMetadata().fileInfo.getSimpleMetadata("LastModified");
            if (tmp == null)
                tmp = translateTag("Unknown");
        } else
            tmp = translateTag("Unknown");
        summaryList.add(tmp);

        return String.join(", ", summaryList);
    }

    private void updateObjectEditorList() throws SEDALibException {
        List<SEDAObjectEditor> result = new ArrayList<SEDAObjectEditor>();
        for (int i = 0; i < 8; i++)
            if (objectEditorArray[i] != null)
                result.add(objectEditorArray[i]);
        objectEditorList = result;
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<SEDAObjectEditor>();

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

        defineButton = new JButton();
        defineButton.setIcon(new ImageIcon(getClass().getResource("/icon/document-save-as.png")));
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
        if (getBinaryDataObjectMetadata().dataObjectVersion != null)
            objectEditorArray[0] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().dataObjectVersion, this);
        if (getBinaryDataObjectMetadata().uri != null)
            objectEditorArray[1] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().uri, this);
        if (getBinaryDataObjectMetadata().messageDigest != null)
            objectEditorArray[2] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().messageDigest, this);
        if (getBinaryDataObjectMetadata().size != null)
            objectEditorArray[3] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().size, this);
        if (getBinaryDataObjectMetadata().compressed != null)
            objectEditorArray[4] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().compressed, this);
        if (getBinaryDataObjectMetadata().formatIdentification != null)
            objectEditorArray[5] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().formatIdentification, this);
        if (getBinaryDataObjectMetadata().fileInfo != null)
            objectEditorArray[6] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().fileInfo, this);
        if (getBinaryDataObjectMetadata().metadata != null)
            objectEditorArray[7] = SEDAObjectEditor.createSEDAObjectEditor(getBinaryDataObjectMetadata().metadata, this);
        updateObjectEditorList();
    }

    private void openButton() {
        try {
            if (editedOnDiskPath != null)
                Desktop.getDesktop().open(editedOnDiskPath.toFile());
        } catch (IOException ignored) {
        }
    }

    private void defineButton() {
        JFileChooser fileChooser = new JFileChooser((editedOnDiskPath == null ? Prefs.getInstance().getPrefsLoadDir() :
                editedOnDiskPath.toAbsolutePath().getParent().toString()));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showOpenDialog(ResipGraphicApp.getTheWindow()) == JFileChooser.APPROVE_OPTION) {
            editedOnDiskPath = fileChooser.getSelectedFile().toPath();

            if (editedOnDiskPath != null) {
                openButton.setVisible(true);
                openButton.setToolTipText(editedOnDiskPath.toAbsolutePath().toString());
            }

            try {
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
                    objectEditorArray[MESSAGE_DIGEST] = createSEDAObjectEditor(new DigestType("MessageDigest",
                            getDigestSha512(editedOnDiskPath), "SHA-512"), this);
                    objectEditorArray[SIZE] = createSEDAObjectEditor(new IntegerType("Size",
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
                        formatIdentification = new FormatIdentification(translateTag("Unknown"), null,
                                translateTag("Unknown"), null);
                    objectEditorArray[FORMAT_IDENTIFICATION] = createSEDAObjectEditor(formatIdentification, this);
                    ((SEDAObjectEditorCompositePanel) objectEditorArray[FORMAT_IDENTIFICATION].getSEDAObjectEditorPanel()).setExpanded(expanded);

                    expanded = false;
                    if (objectEditorArray[FILE_INFO] != null)
                        expanded = ((SEDAObjectEditorCompositePanel) objectEditorArray[FILE_INFO].getSEDAObjectEditorPanel()).isExpanded();
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.addNewMetadata("Filename", editedOnDiskPath.getFileName().toString());
                    fileInfo.addNewMetadata("LastModified", Files.getLastModifiedTime(editedOnDiskPath).toString());
                    objectEditorArray[FILE_INFO] = createSEDAObjectEditor(fileInfo, this);
                    ((SEDAObjectEditorCompositePanel) objectEditorArray[FILE_INFO].getSEDAObjectEditorPanel()).setExpanded(expanded);

                    updateObjectEditorList();

                    sedaObjectEditorPanel.revalidate();
                    sedaObjectEditorPanel.repaint();
                }
            } catch (SEDALibException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(Arrays.asList(
                Pair.of("DataObjectVersion", translateTag("DataObjectVersion")),
                Pair.of("Uri", translateTag("Uri")),
                Pair.of("MessageDigest", translateTag("MessageDigest")),
                Pair.of("Size", translateTag("Size")),
                Pair.of("FormatIdentification", translateTag("FormatIdentification")),
                Pair.of("FileInfo", translateTag("FileInfo")),
                Pair.of("Metadata", translateTag("Metadata"))));

        for (SEDAObjectEditor me : objectEditorList) {
            String name = me.getTag();
            extensionList.remove(Pair.of(name, translateTag(name)));
        }
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata = null;
        switch (metadataName) {
            case "DataObjectVersion":
                sedaMetadata = createSEDAMetadataSample("StringType", "DataObjectVersion", true);
                objectEditorArray[0] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "Uri":
                sedaMetadata = createSEDAMetadataSample("StringType", "Uri", true);
                objectEditorArray[1] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "MessageDigest":
                sedaMetadata = createSEDAMetadataSample("DigestType", "MessageDigest", true);
                objectEditorArray[2] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "Size":
                sedaMetadata = createSEDAMetadataSample("IntegerType", "Size", true);
                objectEditorArray[3] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "Compressed":
                sedaMetadata = createSEDAMetadataSample("StringType", "Compressed", true);
                objectEditorArray[4] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "FormatIdentification":
                sedaMetadata = createSEDAMetadataSample("FormatIdentification", "FormatIdentification", true);
                objectEditorArray[5] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "FileInfo":
                sedaMetadata = createSEDAMetadataSample("FileInfo", "FileInfo", true);
                objectEditorArray[6] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "Metadata":
                sedaMetadata = createSEDAMetadataSample("Metadata", "Metadata", true);
                objectEditorArray[7] = createSEDAObjectEditor(sedaMetadata, this);
                break;
        }
        if (sedaMetadata == null)
            throw new SEDALibException("La métadonnée [" + metadataName + "] n'existe pas dans un BinaryDataObject");
        updateObjectEditorList();
    }

    public void removeChild(SEDAObjectEditor objectEditor) throws SEDALibException {
        for (int i = 0; i < 8; i++)
            if (objectEditorArray[i] == objectEditor)
                objectEditorArray[i] = null;
        updateObjectEditorList();
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        return false;
    }

    /**
     * Create binary data object sample binary data object.
     *
     * @param minimal the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the binary data object
     * @throws SEDALibException the seda lib exception
     */
    static public BinaryDataObject createBinaryDataObjectSample(boolean minimal) throws SEDALibException {
        BinaryDataObject result = new BinaryDataObject();
        result.dataObjectVersion = (StringType) createSEDAMetadataSample("StringType", "DataObjectVersion", minimal);
        result.messageDigest = (DigestType) createSEDAMetadataSample("DigestType", "MessageDigest", minimal);
        result.size = (IntegerType) createSEDAMetadataSample("IntegerType", "Size", minimal);
        result.formatIdentification = (FormatIdentification) createSEDAMetadataSample("FormatIdentification", "FormatIdentification", minimal);
        result.fileInfo = (FileInfo) createSEDAMetadataSample("FileInfo", "FileInfo", minimal);
        if (!minimal) {
            result.uri = (StringType) createSEDAMetadataSample("StringType", "Uri", minimal);
            result.metadata = (Metadata) createSEDAMetadataSample("Metadata", "Metadata", minimal);
        }
        return result;
    }
}
