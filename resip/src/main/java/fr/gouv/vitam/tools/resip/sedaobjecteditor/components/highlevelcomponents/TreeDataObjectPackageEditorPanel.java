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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeViewer;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static fr.gouv.vitam.tools.resip.app.ResipGraphicApp.OK_DIALOG;
import static fr.gouv.vitam.tools.resip.frame.MainWindow.CLICK_FONT;
import static fr.gouv.vitam.tools.resip.frame.MainWindow.TREE_FONT;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor.*;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

public class TreeDataObjectPackageEditorPanel extends JPanel {
    /**
     * The editedObject.
     */
    private DataObjectPackage editedDataObjectPackage;
    private DataObjectPackageTreeNode displayedTreeNode;

    /**
     * The graphic elements
     */
    private JLabel dataObjectPackageTreeLabel;
    private DataObjectPackageTreeViewer dataObjectPackageTreeViewer;
    private JButton openSipItemButton;

    /**
     * Instantiates a new tree DataObjectPackage editor panel.
     */
    public TreeDataObjectPackageEditorPanel() {
        this.editedDataObjectPackage = null;
        GridBagLayout gbl;
        GridBagConstraints gbc;

        gbl = new GridBagLayout();
        gbl.rowHeights = new int[]{0, 200, 0};
        gbl.rowWeights = new double[]{0.0, 1.0, 0.0};
        gbl.columnWidths = new int[]{75,75};
        gbl.columnWeights = new double[]{1.0,1.0};
        setLayout(gbl);

        dataObjectPackageTreeLabel = new JLabel("Arbre du SIP");
        dataObjectPackageTreeLabel.setFont(BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(dataObjectPackageTreeLabel, gbc);

        DataObjectPackageTreeModel dataObjectPackageTreeModel = new DataObjectPackageTreeModel(null);
        dataObjectPackageTreeViewer = new DataObjectPackageTreeViewer(this, dataObjectPackageTreeModel);
        dataObjectPackageTreeViewer.setFont(TREE_FONT);
        JScrollPane dataObjectPackageTreeViewerScrollPane = new JScrollPane(dataObjectPackageTreeViewer);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(dataObjectPackageTreeViewerScrollPane, gbc);

        JCheckBox longDataObjectPackageTreeItemNameCheckBox = new JCheckBox("+ (direct subAU/total subAU) - xmlID");
        longDataObjectPackageTreeItemNameCheckBox.setFont(CLICK_FONT);
        longDataObjectPackageTreeItemNameCheckBox.setVerticalAlignment(SwingConstants.TOP);
        longDataObjectPackageTreeItemNameCheckBox.addActionListener(e -> checkBoxLongDataObjectPackageTreeItemName(e));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 5, 0);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(longDataObjectPackageTreeItemNameCheckBox,gbc);

        JPanel expandReducePanel = new JPanel();
        expandReducePanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(expandReducePanel, gbc);

        JButton expandButton = new JButton("");
        expandButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-add-small.png")));
        expandButton.setToolTipText("Développer tout l'arbre depuis le noeud sélectionné...");
        expandButton.setMargin(new Insets(0, 0, 0, 0));
        expandButton.addActionListener(e -> buttonExpandReduceTree(true));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        expandReducePanel.add(expandButton, gbc);

        JButton reduceButton = new JButton("");
        reduceButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-remove-small.png")));
        reduceButton.setToolTipText("Réduire tout l'arbre depuis le noeud sélectionné...");
        reduceButton.setMargin(new Insets(0, 0, 0, 0));
        reduceButton.addActionListener(e -> buttonExpandReduceTree(false));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        expandReducePanel.add(reduceButton, gbc);

        openSipItemButton = new JButton("Ouvrir dossier AU/OG");
        openSipItemButton.setFont(CLICK_FONT);
        openSipItemButton.setEnabled(false);
        openSipItemButton.addActionListener(e -> buttonOpenDataObjectPackageItemDirectory());
        gbc = new GridBagConstraints();
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(openSipItemButton, gbc);
    }

    // action methods

    private void checkBoxLongDataObjectPackageTreeItemName(ActionEvent e) {
        JCheckBox cb = (JCheckBox) e.getSource();
        dataObjectPackageTreeViewer.activateLongDataObjectPackageTreeItemName(cb.isSelected());
        this.repaint();
        allTreeChanged();
    }

    private void buttonExpandReduceTree(boolean state) {
        TreePath[] selection;
        if (editedDataObjectPackage != null) {
            if (dataObjectPackageTreeViewer.getSelectionPaths() != null)
                selection = dataObjectPackageTreeViewer.getSelectionPaths();
            else {
                DataObjectPackageTreeNode ghostRootNode = (DataObjectPackageTreeNode) (dataObjectPackageTreeViewer.getModel().getRoot());
                if (ghostRootNode != null) {
                    selection = new TreePath[1];
                    selection[0] = new TreePath(ghostRootNode);
                } else
                    return;
            }

            int nb = 0;
            for (TreePath path : selection)
                nb += ((DataObjectPackageTreeNode) (path.getLastPathComponent())).getAuRecursivCount()
                        + ((DataObjectPackageTreeNode) (path.getLastPathComponent())).getOgRecursivCount();

            if ((nb > 10000) &&
                    (UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                            "Attention, quand il y a plus de 10000 ArchiveUnit et DataObjectGroup sélectionnés (en l'occurrence " + nb + "), " +
                                    "l'ouverture ou la fermeture de tous ces noeuds de l'arbre peut prendre beaucoup de temps.\n"
                                    + "Voulez-vous continuer? ",
                            "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                            "Si vous préférez éviter, utilisez l'expansion/réduction sur chaque noeud dans l'arbre en " +
                                    "cliquant sur le petit signe + ou - au début des lignes, ou en double cliquant sur l'ArchiveUnit.") != OK_DIALOG))
                return;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (TreePath path : selection)
                dataObjectPackageTreeViewer.setPathExpansionState(path, state);
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void buttonOpenDataObjectPackageItemDirectory() {
        Path path = null;
        try {
            DataObjectPackageTreeNode stn = displayedTreeNode;
            if (stn != null) {
                if (stn.getArchiveUnit() != null) {
                    // is ArchiveUnit
                    path = stn.getArchiveUnit().getOnDiskPath();
                } else if (stn.getDataObject() != null) {
                    // is DataObject
                    path = ((DataObjectPackageIdElement) stn.getDataObject()).getOnDiskPath();
                }
            }
            if (path != null)
                Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            // too bad
        }
    }

    // tree methods


   private void allChildNodesChanged(DataObjectPackageTreeNode attn) {
        int[] allChilds = new int[attn.getChildCount()];
        for (int i = 0; i < attn.getChildCount(); i++) {
            allChilds[i] = i;
            allChildNodesChanged((DataObjectPackageTreeNode) attn.getChildAt(i));
        }
       ((DataObjectPackageTreeModel)dataObjectPackageTreeViewer.getModel()).nodesChanged(attn, allChilds);
    }

    /**
     * All tree changed.
     */
    public void allTreeChanged() {
        DataObjectPackageTreeNode root = (DataObjectPackageTreeNode) ((DataObjectPackageTreeModel)dataObjectPackageTreeViewer.getModel()).getRoot();
        if (root != null)
            allChildNodesChanged(root);
    }

    /**
     * The displayed tree node changed.
     */
    public void displayedTreeNodeChanged() {
        ((DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel()).nodeChanged(displayedTreeNode);
    }

    /**
     * Add DataObjectGroup to displayed tree node.
     */
    public void addDataObjectGroupToDisplayedTreeNode(DataObjectGroup dataObjectGroup) {
        ((DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel()).generateDataObjectNode(dataObjectGroup, ResipGraphicApp.getTheWindow().treePane.displayedTreeNode);
        displayedTreeNodeChanged();
    }

    /**
     * Reset displayed node title, after Content change.
     */
    public void resetDisplayedTreeNodeTitle() {
        String title = null;
        try {
            title = displayedTreeNode.getArchiveUnit().getContent().getSimpleMetadata("Title");
        } catch (SEDALibException ignored) {
        }
        if (title == null)
            title = SEDAXMLEventReader.extractNamedElement("Title", displayedTreeNode.getArchiveUnit().getContentXmlData());
        if (title == null)
            title = translateTag("Unknown");
        displayedTreeNode.setTitle(title);
        displayedTreeNodeChanged();
    }

    /**
     * Get the displayed tree node.
     *
     * @return the data object package tree node
     */
    public DataObjectPackageTreeNode getDisplayedTreeNode(){
        return displayedTreeNode;
    }

    /**
     * Get the ArchiveUnit title in the tree.
     *
     * @param archiveUnit the archive unit
     * @return the data object package tree node
     */
    public String getTreeTitle(ArchiveUnit archiveUnit) {
        return ((DataObjectPackageTreeModel)dataObjectPackageTreeViewer.getModel()).findTreeNode(archiveUnit).getTitle();
    }

    /**
     * Refresh TreePaneLabel information.
     */
    public void refreshTreeLabel() {
        if (editedDataObjectPackage == null) {
            ResipLogger.getGlobalLogger().getLogger().warn("No edited data object package");
            return;
        }

        String treeLabel = buildTreeLabel(editedDataObjectPackage);
        dataObjectPackageTreeLabel.setText(treeLabel);
    }

    private String buildTreeLabel(DataObjectPackage dataObjectPackage) {
        int archiveUnitCount = dataObjectPackage.getArchiveUnitCount();
        int dogCount = dataObjectPackage.getDataObjectGroupCount();
        int bdoCount = dataObjectPackage.getBinaryDataObjectCount();
        int pdoCount = dataObjectPackage.getPhysicalDataObjectCount();

        return String.format(
            "Arbre du SIP (%d archiveUnit / %d dog / %d bdo / %d pdo)",
            archiveUnitCount, dogCount, bdoCount, pdoCount
        );
    }

    /**
     * Reset the information tree label and deselect any ArchiveUnit edition.
     */
    public void reset() {
        refreshTreeLabel();
        displayedTreeNode = null;
        try {
            ResipGraphicApp.getTheWindow().auMetadataPane.editArchiveUnit(null);
        } catch (SEDALibException ignored) {
        }
    }

    /**
     * Select for edition an ArchiveUnit determined by it's tree path.
     *
     * @param path the path
     */
    public void selectTreePathItem(TreePath path) {
        DataObjectPackageTreeNode node = (DataObjectPackageTreeNode) path.getLastPathComponent();
        if (node.getArchiveUnit() == null)
            node = (DataObjectPackageTreeNode) path.getParentPath().getLastPathComponent();
        try {
            ResipGraphicApp.getTheWindow().auMetadataPane.editArchiveUnit(node.getArchiveUnit());
        } catch (SEDALibException e) {
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Resip.InOut: Erreur à l'indentation de l'ArchiveUnit ["
                    + node.getArchiveUnit().getInDataObjectPackageId() + "]",e);
        }

        displayedTreeNode = node;
        if (node.getArchiveUnit().getOnDiskPath() != null)
            openSipItemButton.setEnabled(true);
        else
            openSipItemButton.setEnabled(false);
    }

    /**
     * Focus on an ArchiveUnit, selecting it for edition.
     *
     * @param archiveUnit the archive unit
     * @return the data object package tree node of the selected ArchiveUnit
     */
    public DataObjectPackageTreeNode focusArchiveUnit(ArchiveUnit archiveUnit) {
        DataObjectPackageTreeModel model=(DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel();
        DataObjectPackageTreeNode focusNode= model.findTreeNode(archiveUnit);
        TreePath path = new TreePath(model.getPathToRoot(focusNode));

        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        selectTreePathItem(path);
        return focusNode;
    }

    /**
     * Focus on a DataObjectGroup, selecting an ArchiveUnit associated for edition.
     *
     * @param dataObjectGroup the data object group
     * @return the data object package tree node of the selected ArchiveUnit
     */
    public DataObjectPackageTreeNode focusDataObjectGroup(DataObjectGroup dataObjectGroup) {
        DataObjectPackageTreeModel model=(DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel();
        DataObjectPackageTreeNode focusNode= model.findTreeNode(dataObjectGroup);
        TreePath path = new TreePath(model.getPathToRoot(focusNode));

        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        selectTreePathItem(path);
        return (DataObjectPackageTreeNode) focusNode.getParent();
    }

    /**
     * Focus on a DataObject in an ArchiveUnit, selecting for edition the ArchiveUnit and the DataObject.
     *
     * @param archiveUnit the archive unit
     * @param dataObject  the data object
     * @return the data object package tree node of the selected ArchiveUnit
     */
    public DataObjectPackageTreeNode focusDataObject(ArchiveUnit archiveUnit, DataObject dataObject) {
        DataObjectPackageTreeModel model=(DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel();
        DataObjectPackageTreeNode focusNode= model.findTreeNode(archiveUnit);
        TreePath path = new TreePath(model.getPathToRoot(focusNode));

        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        selectTreePathItem(path);
        try {
            ResipGraphicApp.getTheWindow().dogMetadataPane.selectDataObject(dataObject);
        } catch (SEDALibException ignored) {
        }
        return focusNode;
    }

    // global tree methods

    /**
     * Edit a data object package.
     *
     * @param dataObjectPackage the data object package
     */
    public void editDataObjectPackage(DataObjectPackage dataObjectPackage){
        DataObjectPackageTreeModel model = (DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel();
        DataObjectPackageTreeNode top;

        this.editedDataObjectPackage=dataObjectPackage;
        if (dataObjectPackage!=null) {
            top = model.generateDataObjectPackageNodes(dataObjectPackage);
            refreshTreeLabel();
        } else {
            top = null;
            dataObjectPackageTreeLabel.setText("Arbre du SIP");
        }
        model.setRoot(top);
        model.reload();
        displayedTreeNode = null;
    }

    /**
     * The Sort by title tool.
     */
    class SortByTitle implements Comparator<ArchiveUnit> {
        DataObjectPackageTreeModel treeModel;

        public int compare(ArchiveUnit a, ArchiveUnit b) {
            String titleA = treeModel.findTreeNode(a).getTitle().toLowerCase();
            String titleB = treeModel.findTreeNode(b).getTitle().toLowerCase();
            return titleA.compareTo(titleB);
        }

        SortByTitle(DataObjectPackageTreeModel treeModel) {
            this.treeModel = treeModel;
        }
    }

    /**
     * Do sort tree.
     * <p>
     * Except order, change as little as possible graphic form, including expansion and selection.
     */
    public void doSortTree(){
        if (editedDataObjectPackage!=null) {
            DataObjectPackageTreeModel model = (DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel();
            Map<TreePath, Boolean> expansionState = dataObjectPackageTreeViewer.getExpansionState();
            TreePath selectedPath = dataObjectPackageTreeViewer.getSelectionPath();
            TreeDataObjectPackageEditorPanel.SortByTitle sortByTitle = new TreeDataObjectPackageEditorPanel.SortByTitle(model);
            for (Map.Entry<String, ArchiveUnit> pair :
                    editedDataObjectPackage.getAuInDataObjectPackageIdMap().entrySet()) {
                Collections.sort(pair.getValue().getChildrenAuList().getArchiveUnitList(), sortByTitle);
            }
            Collections.sort(editedDataObjectPackage.getGhostRootAu().getChildrenAuList().getArchiveUnitList(),
                    sortByTitle);
            model.reload();
            dataObjectPackageTreeViewer.setExpansionState(expansionState);
            if (selectedPath != null) {
                dataObjectPackageTreeViewer.setSelectionPath(selectedPath);
                dataObjectPackageTreeViewer.scrollPathToVisible(selectedPath);
            }
        }
    }

    /**
     * Do refresh tree, from the DataObjectPackage information.
     * <p>
     * Regenerate all JTree nodes for a DataObjectPackage.
     * Don't recreate nodes but recompute parents and au and og recursiv count,
     * and drop nodes no more linked to DataObjectPackage structure.
     * Change as little as possible graphic form, including expansion and selection.
     */
    public void doRefreshTree(){
        if (editedDataObjectPackage!=null) {
            DataObjectPackageTreeModel model = (DataObjectPackageTreeModel) dataObjectPackageTreeViewer.getModel();
            Map<TreePath, Boolean> expansionState = dataObjectPackageTreeViewer.getExpansionState();
            TreePath selectedPath = dataObjectPackageTreeViewer.getSelectionPath();
            model.actualiseDataObjectPackageNodes();
            model.reload();
            dataObjectPackageTreeViewer.setExpansionState(expansionState);
            if (selectedPath != null) {
                dataObjectPackageTreeViewer.setSelectionPath(selectedPath);
                dataObjectPackageTreeViewer.scrollPathToVisible(selectedPath);
            }
            reset();
            dataObjectPackageTreeViewer.repaint();
        }
    }
}
