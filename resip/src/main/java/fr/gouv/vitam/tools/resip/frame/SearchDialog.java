package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.SearchThread;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnitRefList;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SearchDialog extends JDialog{
    public JTextField searchTextField;
    public JButton searchButton;
    public JButton nextButton;
    public JButton previousButton;
    public JCheckBox regExpCheckBox;
    public JCheckBox caseCheckBox;
    public JCheckBox metadataCheckBox;
    public JLabel resultLabel;

    private MainWindow mainWindow;
    private DataObjectPackageTreeModel dataObjectPackageTreeModel;
    public List<ArchiveUnit> searchResult;
    public int searchResultPosition;
    public boolean searchRunning;

    /**
     * Create the dialog.
     *
     * @param owner the owner
     * @param title the title
     */
    public SearchDialog(JFrame owner, String title) {
        super(owner, title, false);

        mainWindow=(MainWindow)owner;
        searchRunning=false;

        setBounds(100, 100, 400, 100);
        setMinimumSize(new Dimension(400, 100));
        setMaximumSize(new Dimension(400, 100));
        getContentPane().setLayout(new GridBagLayout());
        searchTextField = new JTextField();
        searchTextField.setText("");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(searchTextField, gbc);

        searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(getClass().getResource("/001-search.png")));
        searchButton.setText("");
        searchButton.setMaximumSize(new Dimension(24, 24));
        searchButton.setMinimumSize(new Dimension(24, 24));
        searchButton.setPreferredSize(new Dimension(24, 24));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        getContentPane().add(searchButton, gbc);
        searchButton.addActionListener(arg0 -> buttonSearch());

        nextButton = new JButton();
        nextButton.setIcon(new ImageIcon(getClass().getResource("/003-down.png")));
        nextButton.setText("");
        nextButton.setMaximumSize(new Dimension(24, 24));
        nextButton.setMinimumSize(new Dimension(24, 24));
        nextButton.setPreferredSize(new Dimension(24, 24));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        getContentPane().add(nextButton, gbc);
        nextButton.addActionListener(arg0 -> buttonNext());

        previousButton = new JButton();
        previousButton.setIcon(new ImageIcon(getClass().getResource("/002-up.png")));
        previousButton.setText("");
        previousButton.setMaximumSize(new Dimension(24, 24));
        previousButton.setMinimumSize(new Dimension(24, 24));
        previousButton.setPreferredSize(new Dimension(24, 24));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        getContentPane().add(previousButton, gbc);
        previousButton.addActionListener(arg0 -> buttonPrevious());

        regExpCheckBox = new JCheckBox();
        regExpCheckBox.setText("RegExp");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        getContentPane().add(regExpCheckBox, gbc);
        regExpCheckBox.setEnabled(false);

        caseCheckBox = new JCheckBox();
        caseCheckBox.setText("Respecter la casse");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridwidth =1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        getContentPane().add(caseCheckBox, gbc);

        metadataCheckBox = new JCheckBox();
        metadataCheckBox.setText("Métadonnées");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridwidth =1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        getContentPane().add(metadataCheckBox, gbc);

        resultLabel = new JLabel();
        resultLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridwidth =3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        getContentPane().add(resultLabel, gbc);

        getRootPane().setDefaultButton(searchButton);
    }

    public void focusArchiveUnit(ArchiveUnit au){
        TreePath path = new TreePath(dataObjectPackageTreeModel.getPathToRoot(dataObjectPackageTreeModel.findTreeNode(au)));
        mainWindow.getDataObjectPackageTreePaneViewer().setExpandsSelectedPaths(true);
        mainWindow.getDataObjectPackageTreePaneViewer().setSelectionPath(path);
        mainWindow.getDataObjectPackageTreePaneViewer().scrollPathToVisible(path);
        mainWindow.dataObjectPackageTreeItemClick(path);
    }

    void buttonSearch(){
        if (searchRunning)
            resultLabel.setText("En cours");
        else {
            searchRunning=true;
            dataObjectPackageTreeModel = (DataObjectPackageTreeModel) mainWindow.getDataObjectPackageTreePaneViewer().getModel();
            SearchThread st = new SearchThread(this, mainWindow.getApp().currentWork.getDataObjectPackage().getGhostRootAu());
            st.execute();
        }
    }

    void buttonNext(){
        if (searchResult.size()>0) {
            searchResultPosition = Math.min(searchResultPosition+1,searchResult.size()-1);
            resultLabel.setText((searchResultPosition+1)+"/"+searchResult.size());
            focusArchiveUnit(searchResult.get(searchResultPosition));
        }
    }

    void buttonPrevious(){
        if (searchResult.size()>0) {
            searchResultPosition = Math.max(searchResultPosition-1,0);
            resultLabel.setText((searchResultPosition+1)+"/"+searchResult.size());
            focusArchiveUnit(searchResult.get(searchResultPosition));
        }
    }
}
