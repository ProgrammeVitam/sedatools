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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.composite;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.FileMetadata;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static fr.gouv.vitam.tools.resip.app.ResipGraphicApp.OK_DIALOG;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The BinaryDataObject object editor class.
 */
public class BinaryDataObjectEditor extends AbstractUnitaryDataObjectEditor {

    /**
     * The edited on disk path.
     */
    private Path editedOnDiskPath;

    /**
     * The graphic elements.
     */
    private JButton openButton;

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
    public BinaryDataObject extractEditedObject() throws SEDALibException {
        getBinaryDataObject().setOnDiskPath(editedOnDiskPath);
        return (BinaryDataObject) super.extractEditedObject();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
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

        prepareSEDAObjectEditorPanel(moreButtons);
    }

    private boolean setReadOnly(Path path) {
        try {
            // Office bug workaround
            // This is a special patch to prevent Office to change a file when opening it to see the content...
            if (System.getProperty("os.name").toLowerCase().contains("win")) Files.setAttribute(
                path,
                "dos:readonly",
                true
            );
        } catch (IOException e) {
            return (
                UserInteractionDialog.getUserAnswer(
                    ResipGraphicApp.getTheWindow(),
                    "Impossible de passer le fichier à ouvrir [" +
                    path +
                    "] en lecture seule\nVoulez-vous tout de même l'ouvrir?",
                    "Confirmation",
                    UserInteractionDialog.WARNING_DIALOG,
                    null
                ) ==
                OK_DIALOG
            );
        }
        return true;
    }

    private void openButton() {
        if (editedOnDiskPath != null) {
            try {
                if (setReadOnly(editedOnDiskPath)) Desktop.getDesktop().open(editedOnDiskPath.toFile());
            } catch (IOException ignored) {} //NOSONAR
        }
    }

    private SEDAObjectEditor getSEDAObjectEditor(String metadataName) {
        for (SEDAObjectEditor soe : objectEditorList) {
            if (soe.getTag().equals(metadataName)) return soe;
        }
        return null;
    }

    private void extractFileMetadataInEditors() {
        try {
            for (SEDAMetadata sm : FileMetadata.of(editedOnDiskPath, getSedaVersion(), translateTag(UNKNOWN))) {
                SEDAObjectEditor previous = getSEDAObjectEditor(sm.getXmlElementName());
                SEDAObjectEditor editor = createSEDAObjectEditor(sm, this);
                // the composite ones keep the folding of the editor they replace
                if (sm instanceof ComplexListType) {
                    boolean expanded =
                        (previous != null) &&
                        ((SEDAObjectEditorCompositePanel) previous.getSEDAObjectEditorPanel()).isExpanded();
                    ((SEDAObjectEditorCompositePanel) editor.getSEDAObjectEditorPanel()).setExpanded(expanded);
                }
                replaceOrAddObjectEditor(editor);
            }
            updateObjectEditorList();
        } catch (SEDALibException | IOException e) {
            e.printStackTrace();
        }
    }

    private void defineButton() {
        JFileChooser fileChooser = new JFileChooser(
            (editedOnDiskPath == null
                    ? Preferences.getInstance().getPrefsLoadDir()
                    : editedOnDiskPath.toAbsolutePath().getParent().toString())
        );
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showOpenDialog(ResipGraphicApp.getTheWindow()) == JFileChooser.APPROVE_OPTION) {
            editedOnDiskPath = fileChooser.getSelectedFile().toPath();

            openButton.setVisible(true);
            openButton.setToolTipText(editedOnDiskPath.toAbsolutePath().toString());

            String filename = null;
            if (getSEDAObjectEditor("FileInfo") != null) filename = ((ComplexListTypeEditor) getSEDAObjectEditor(
                    "FileInfo"
                )).getComplexListTypeMetadata()
                .getSimpleMetadata("Filename");
            if (
                (getSEDAObjectEditor("FileInfo") == null) ||
                (filename == null) ||
                filename.isEmpty() ||
                UserInteractionDialog.getUserAnswer(
                        ResipGraphicApp.getTheWindow(),
                        "Voulez-vous extraire automatiquement les informations de ce fichier?",
                        "Question",
                        UserInteractionDialog.WARNING_DIALOG,
                        null
                    ) ==
                    OK_DIALOG
            ) {
                extractFileMetadataInEditors();

                sedaObjectEditorPanel.revalidate();
                sedaObjectEditorPanel.repaint();
            }
        }
    }
}
