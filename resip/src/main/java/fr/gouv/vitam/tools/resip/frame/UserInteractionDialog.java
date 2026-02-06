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
package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The type User interaction dialog.
 */
public class UserInteractionDialog extends JDialog {

    /**
     * The Information dialog.
     */
    static public final int INFORMATION_DIALOG = 0;
    /**
     * The Warning dialog.
     */
    static public final int WARNING_DIALOG = 1;
    /**
     * The Error dialog.
     */
    static public final int ERROR_DIALOG = 2;
    /**
     * The Important dialog.
     */
    static public final int IMPORTANT_DIALOG = 3;

    /**
     * The actions components.
     */
    final private JPanel explanationPanel;

    /**
     * The return value.
     */
    private int returnValue;

    // Dialog test context

    /**
     * The entry point of dialog test.
     *
     * @param args the input arguments
     * @throws ClassNotFoundException          the class not found exception
     * @throws UnsupportedLookAndFeelException the unsupported look and feel exception
     * @throws InstantiationException          the instantiation exception
     * @throws IllegalAccessException          the illegal access exception
     * @throws NoSuchMethodException           the no such method exception
     * @throws InvocationTargetException       the invocation target exception
     */
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        TestDialogWindow window = new TestDialogWindow(UserInteractionDialog.class);
    }

    /**
     * Instantiates a new UserInteractionDialog for test.
     *
     * @param owner the owner
     */
    public UserInteractionDialog(JFrame owner) {
        this(owner, "This is a warning message, with a question to answer...", "Important", WARNING_DIALOG,
                "This is explanation");
    }


    /**
     * Gets user answer.
     *
     * @param owner                    the owner
     * @param message                  the message
     * @param title                    the title
     * @param kind                     the kind
     * @param optionalExplationMessage the optional explation message
     * @return the user answer
     */
    static public int getUserAnswer(JFrame owner, String message, String title, int kind, String optionalExplationMessage) {
        UserInteractionDialog dialog = new UserInteractionDialog(owner, message, title, kind, optionalExplationMessage);
        dialog.setVisible(true);
        return dialog.returnValue;
    }

    /**
     * Create the dialog.
     *
     * @param owner                    the owner
     * @param message                  the message
     * @param title                    the title
     * @param kind                     the kind
     * @param optionalExplationMessage the optional explation message
     */
    public UserInteractionDialog(JFrame owner, String message, String title, int kind, String optionalExplationMessage) {
        super(owner, title, true);
        GridBagConstraints gbc;

        this.setMinimumSize(new Dimension(400,100));
        this.setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        final JPanel informationPanel = new JPanel();
        informationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(informationPanel, gbc);

        final JLabel label1 = new JLabel();
        switch (kind) {
            case INFORMATION_DIALOG:
                label1.setIcon(new ImageIcon(getClass().getResource("/icon/large-dialog-information.png")));
                break;
            case WARNING_DIALOG:
                label1.setIcon(new ImageIcon(getClass().getResource("/icon/large-dialog-warning.png")));
                break;
            case ERROR_DIALOG:
                label1.setIcon(new ImageIcon(getClass().getResource("/icon/large-dialog-error.png")));
                break;
            case IMPORTANT_DIALOG:
                label1.setIcon(new ImageIcon(getClass().getResource("/icon/large-emblem-important.png")));
                break;
        }
        label1.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        informationPanel.add(label1, gbc);

        JTextArea messageTextArea = new JTextArea();
        messageTextArea.setLineWrap(true);
        messageTextArea.setFont(new JLabel().getFont());
        messageTextArea.setBackground(UIManager.getColor(MainWindow.GENERAL_BACKGROUND));
        messageTextArea.setFocusable(false);
        messageTextArea.setText(message);
        messageTextArea.setWrapStyleWord(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 5);
        informationPanel.add(messageTextArea, gbc);

        final JPanel mainActionPanel = new JPanel();
        mainActionPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(mainActionPanel, gbc);
        if (optionalExplationMessage != null) {
            JCheckBox moreOptionsCheckBox = new JCheckBox();
            moreOptionsCheckBox.setEnabled(true);
            moreOptionsCheckBox.setIcon(new ImageIcon(getClass().getResource("/icon/list-add.png")));
            moreOptionsCheckBox.setSelectedIcon(new ImageIcon(getClass().getResource("/icon/list-remove.png")));
            moreOptionsCheckBox.setText("En savoir plus");
            moreOptionsCheckBox.addItemListener(arg -> moreExplanationEvent(arg));
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            mainActionPanel.add(moreOptionsCheckBox, gbc);
        }
        if (kind == WARNING_DIALOG) {
            JButton buttonCancel = new JButton();
            buttonCancel.setText("Annuler");
            gbc = new GridBagConstraints();
            gbc.gridx = 2-(optionalExplationMessage == null?1:0);
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            if (optionalExplationMessage != null)
                gbc.anchor = GridBagConstraints.EAST;
            else
                gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(5, 5, 5, 5);
            buttonCancel.addActionListener(arg->buttonCancel());
            mainActionPanel.add(buttonCancel, gbc);
        }
        JButton buttonOK = new JButton();
        buttonOK.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 1-(optionalExplationMessage == null?1:0);
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        if (optionalExplationMessage != null)
            gbc.anchor = GridBagConstraints.EAST;
        else
            gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        buttonOK.addActionListener(arg->buttonOK());
        mainActionPanel.add(buttonOK, gbc);

        explanationPanel = new JPanel();
        explanationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(explanationPanel, gbc);
        if (optionalExplationMessage != null) {
            final JSeparator separator = new JSeparator();
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            explanationPanel.add(separator, gbc);
            final JLabel label7 = new JLabel();
            label7.setIcon(new ImageIcon(getClass().getResource("/icon/dialog-information.png")));
            label7.setText("");
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            explanationPanel.add(label7, gbc);
            JTextArea explanationTextArea = new JTextArea();
            explanationTextArea.setLineWrap(true);
            explanationTextArea.setFont(new JLabel().getFont());
            explanationTextArea.setBackground(UIManager.getColor("Dialog.background"));
            explanationTextArea.setFocusable(false);
            explanationTextArea.setText(optionalExplationMessage);
            explanationTextArea.setWrapStyleWord(true);
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 5, 5);
            explanationPanel.add(explanationTextArea, gbc);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                buttonCancel();
            }
        });

        pack();
        explanationPanel.setVisible(false);
        pack();
        setLocationRelativeTo(owner);
    }

    // actions

    private void moreExplanationEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            Dimension dim = this.getSize();
            explanationPanel.setVisible(true);
            pack();
            dim.height = dim.height + explanationPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        } else if (event.getStateChange() == DESELECTED) {
            Dimension dim = this.getSize();
            dim.height = dim.height - explanationPanel.getHeight();
            explanationPanel.setVisible(false);
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        }
    }

    private void buttonCancel() {
        returnValue = ResipGraphicApp.KO_DIALOG;
        setVisible(false);
    }

    private void buttonOK() {
        returnValue = ResipGraphicApp.OK_DIALOG;
        setVisible(false);
    }
}
