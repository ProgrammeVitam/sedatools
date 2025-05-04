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
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.LocalDateTimeUtil;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
    }

    private BinaryDataObject getBinaryDataObject() {
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
                (getBinaryDataObject().getInDataObjectPackageId() == null ? TO_BE_DEFINED :
                        getBinaryDataObject().getInDataObjectPackageId());
    }

    @Override
    public BinaryDataObject extractEditedObject() throws SEDALibException {
        BinaryDataObject tmpBdo = new BinaryDataObject();
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            SEDAMetadata subMetadata = (SEDAMetadata) objectEditor.extractEditedObject();
            tmpBdo.addMetadata(subMetadata);
        }
        getBinaryDataObject().setMetadataList(tmpBdo.getMetadataList());
        getBinaryDataObject().setOnDiskPath(editedOnDiskPath);

        return getBinaryDataObject();
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
        for (SEDAMetadata sm : getBinaryDataObject().getMetadataList()) {
            if (sm instanceof StringType)
                summaryList.add(((StringType) sm).getValue());
            else if (sm instanceof PersistentIdentifier)
                summaryList.add(((PersistentIdentifier) sm).getSummary());
            else if (sm instanceof FileInfo) {
                summaryList.add(getItOrUnknown(((FileInfo) sm).getSimpleMetadata("Filename")));
                summaryList.add(getItOrUnknown(((FileInfo) sm).getSimpleMetadata("LastModified")));
            } else if (sm instanceof FormatIdentification) {
                summaryList.add(getItOrUnknown(((FormatIdentification) sm).getSimpleMetadata("MimeType")));
                summaryList.add(getItOrUnknown(((FormatIdentification) sm).getSimpleMetadata("FormatId")));
            }

        }
        return String.join(", ", summaryList);
    }

    private void updateObjectEditorList() throws SEDALibException {
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<>(17);

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
        openButton.setVisible(getBinaryDataObject().getOnDiskPath() != null);
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
        for (SEDAMetadata sm : getBinaryDataObject().getMetadataList()) {
            objectEditorList.add(SEDAObjectEditor.createSEDAObjectEditor(sm, this));
        }
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

    protected int getInsertionSEDAObjectEditorIndex(String metadataName) throws SEDALibException {
        int addOrderIndex;
        int curOrderIndex;
        int i;
        boolean manyFlag;
        addOrderIndex = getBinaryDataObject().indexOfMetadata(metadataName);
        i = 0;
        if (addOrderIndex == -1)
            return Integer.MAX_VALUE;
        else {
            manyFlag = getBinaryDataObject().getMetadataMap().get(metadataName).isMany();
            for (SEDAObjectEditor soe : objectEditorList) {
                curOrderIndex = getBinaryDataObject().indexOfMetadata(soe.getTag());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex) ||
                        (curOrderIndex == -1) || (curOrderIndex > addOrderIndex)) {
                    break;
                }
                i++;
            }
        }
        return i;
    }

    private void replaceOrAddObjectEditor(SEDAObjectEditor newObjectEditor) throws SEDALibException {
        // replace if it exists
        for (int i = 0; i < objectEditorList.size(); i++) {
            if (objectEditorList.get(i).getTag().equals(newObjectEditor.getTag())) {
                objectEditorList.set(i, newObjectEditor);
                return;
            }
        }

        // add in BinaryDataObject metadata order
        objectEditorList.add(getInsertionSEDAObjectEditorIndex(newObjectEditor.getTag()), newObjectEditor);
    }

    private SEDAObjectEditor getSEDAObjectEditor(String metadataName) {
        for (SEDAObjectEditor soe : objectEditorList) {
            if (soe.getTag().equals(metadataName))
                return soe;
        }
        return null;
    }

    private void extractFileMetadataInEditors() {
        try {
            replaceOrAddObjectEditor( createSEDAObjectEditor(new DigestType("MessageDigest",
                    getDigestSha512(editedOnDiskPath), "SHA-512"), this));
            replaceOrAddObjectEditor( createSEDAObjectEditor(new IntegerType("Size" ,
                    Files.size(editedOnDiskPath)), this));

            boolean expanded = false;
            SEDAObjectEditor formatInformationObjectEditor= getSEDAObjectEditor("FormatIdentification");
            if (formatInformationObjectEditor != null)
                expanded = ((SEDAObjectEditorCompositePanel) formatInformationObjectEditor.getSEDAObjectEditorPanel()).isExpanded();
            IdentificationResult ir = DroidIdentifier.getInstance().getIdentificationResult(editedOnDiskPath);
            FormatIdentification formatIdentification;
            if (ir != null)
                formatIdentification = new FormatIdentification(ir.getName(), ir.getMimeType(),
                        ir.getPuid(), null);
            else
                formatIdentification = new FormatIdentification(translateTag(UNKNOWN), null,
                        translateTag(UNKNOWN), null);
            formatInformationObjectEditor=createSEDAObjectEditor(formatIdentification, this);
            ((SEDAObjectEditorCompositePanel) formatInformationObjectEditor.getSEDAObjectEditorPanel()).setExpanded(expanded);
            replaceOrAddObjectEditor(formatInformationObjectEditor);

            expanded = false;
            SEDAObjectEditor fileInfoObjectEditor= getSEDAObjectEditor("FileInfo");
            if (fileInfoObjectEditor != null)
                expanded = ((SEDAObjectEditorCompositePanel) fileInfoObjectEditor.getSEDAObjectEditorPanel()).isExpanded();
            FileInfo fileInfo = new FileInfo();
            fileInfo.addNewMetadata("Filename", editedOnDiskPath.getFileName().toString());
            fileInfo.addNewMetadata("LastModified", LocalDateTimeUtil.getFormattedDateTime(Files.getLastModifiedTime(editedOnDiskPath)));
            fileInfoObjectEditor = createSEDAObjectEditor(fileInfo, this);
            ((SEDAObjectEditorCompositePanel) fileInfoObjectEditor.getSEDAObjectEditorPanel()).setExpanded(expanded);
            replaceOrAddObjectEditor(fileInfoObjectEditor);

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
            if (getSEDAObjectEditor("FileInfo") != null)
                filename = ((ComplexListTypeEditor) getSEDAObjectEditor("FileInfo")).getComplexListTypeMetadata().getSimpleMetadata("Filename");
            if ((getSEDAObjectEditor("FileInfo") == null) ||
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
        List<String> used = new ArrayList<>();
        List<Pair<String, String>> result = new ArrayList<>();
        for (SEDAObjectEditor soe : objectEditorList) {
            used.add(soe.getTag());
        }
        for (String metadataName : getBinaryDataObject().getMetadataMap().keySet()) {
            if (metadataName.endsWith("SystemId"))
                continue;
            ComplexListMetadataKind complexListMetadataKind = getBinaryDataObject().getMetadataMap().get(metadataName);
            if ((complexListMetadataKind.isMany()) || (!used.contains(metadataName)))
                result.add(Pair.of(metadataName, translateTag(metadataName)));
        }
        if (!getBinaryDataObject().isNotExpandable())
            result.add(Pair.of("AnyXMLType", translateTag("AnyXMLType")));

        result.sort((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
        return result;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sm=createSEDAMetadataSample(getBinaryDataObject().getMetadataMap().get(metadataName).getMetadataClass().getSimpleName(), metadataName, true);
        replaceOrAddObjectEditor(createSEDAObjectEditor(sm, this));
        updateObjectEditorList();
    }

    @Override
    public void removeChild(SEDAObjectEditor objectEditor) throws SEDALibException {
        objectEditorList.remove(objectEditor);
        updateObjectEditorList();
    }

    @Override
    public boolean canContainsMultiple(String metadataName) {
        try {
            return getBinaryDataObject().getMetadataMap().get(metadataName).isMany();
        } catch (SEDALibException e) {
            return false;
        }
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

        for (Map.Entry<String, ComplexListMetadataKind> e: result.getMetadataMap().entrySet()) {
            if (SEDAObjectEditorConstants.minimalTagList.contains(e.getKey()))
                result.addMetadata(createSEDAMetadataSample(e.getValue().getMetadataClass().getName(),e.getKey(),minimal));
        }

        return result;
    }
}
