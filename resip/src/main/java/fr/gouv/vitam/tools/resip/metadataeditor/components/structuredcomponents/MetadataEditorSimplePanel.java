package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;

public class MetadataEditorSimplePanel extends MetadataEditorPanel {

    JPanel labelPanel, editPanel;

    public MetadataEditorSimplePanel(MetadataEditor metadataEditor, JPanel labelPanel, JPanel editPanel) throws SEDALibException {
        super(metadataEditor);
        boolean multiple = metadataEditor.isMultiple();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[]{MetadataEditorConstants.computeLabelWidth() - 20, 10, 10, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        setLayout(gbl);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(labelPanel, gbc);

        JButton lessButton = new JButton();
        lessButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-remove-very-small.png")));
        lessButton.setText("");
        lessButton.setMaximumSize(new Dimension(8, 8));
        lessButton.setMinimumSize(new Dimension(8, 8));
        lessButton.setPreferredSize(new Dimension(8, 8));
        lessButton.setBorderPainted(false);
        lessButton.setContentAreaFilled(false);
        lessButton.setFocusPainted(false);
        lessButton.setFocusable(false);
        lessButton.addActionListener(arg -> lessButton());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(lessButton, gbc);

        if (multiple) {
            JButton addButton = new JButton();
            addButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-add-very-small.png")));
            addButton.setText("");
            addButton.setMaximumSize(new Dimension(8, 8));
            addButton.setMinimumSize(new Dimension(8, 8));
            addButton.setPreferredSize(new Dimension(8, 8));
            addButton.setBorderPainted(false);
            addButton.setContentAreaFilled(false);
            addButton.setFocusPainted(false);
            addButton.setFocusable(false);
            addButton.addActionListener(arg -> addButton());
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 2;
            gbc.gridy = 0;
            add(addButton, gbc);
        }

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(editPanel, gbc);
    }
}
