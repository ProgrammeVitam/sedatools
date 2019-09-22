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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;

/**
 * The SEDAObjectEditor panel class for elementary editedObject.
 */
public class SEDAObjectEditorSimplePanel extends SEDAObjectEditorPanel {

    /**
     * Instantiates a new SEDAObjectEditor simple panel.
     *
     * @param objectEditor the SEDA object editor
     * @param labelPanel     the label panel
     * @param editPanel      the edit panel
     * @throws SEDALibException the seda lib exception
     */
    public SEDAObjectEditorSimplePanel(SEDAObjectEditor objectEditor, JPanel labelPanel, JPanel editPanel) throws SEDALibException {
        super(objectEditor);
        boolean multiple = ((objectEditor.getFather()!=null ) &&
                objectEditor.getFather().canContainsMultiple(objectEditor.getTag()));
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[]{SEDAObjectEditorConstants.computeLabelWidth() - 20, 10, 10, 0};
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
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(editPanel, gbc);
    }
}
