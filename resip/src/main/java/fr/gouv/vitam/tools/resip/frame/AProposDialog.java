package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AProposDialog extends JDialog {
    private JTextArea messageTextArea;
    private JButton okButton;
    private JButton reinitPrefsButton;

    public String message;

    public AProposDialog(JFrame owner, String message) {
        super(owner, "A propos de...", true);
        getContentPane().setPreferredSize(new Dimension(400, 100));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{1.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0.1, 0.1};
        getContentPane().setLayout(gridBagLayout);

        this.message = message;
        messageTextArea = new JTextArea();
        messageTextArea.setText(message);
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setBackground(getContentPane().getBackground());
        messageTextArea.setFont(UIManager.getDefaults().getFont("Label.font"));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(messageTextArea, gbc);

        okButton = new JButton();
        okButton.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(okButton, gbc);
        okButton.addActionListener(arg0 -> buttonOK());

        reinitPrefsButton = new JButton();
        reinitPrefsButton.setText("Réinitialiser les préférences");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(reinitPrefsButton, gbc);
        reinitPrefsButton.addActionListener(arg0 -> buttonReinitPrefs());

        pack();
        setLocationRelativeTo(owner);
    }

    private void buttonOK() {
        this.setVisible(false);
    }

    private void buttonReinitPrefs() {
        try{Prefs.getInstance().reinitialisePrefs();}
        catch (Exception e) {
            JOptionPane.showMessageDialog(ResipGraphicApp.getTheApp().mainWindow, "Erreur fatale, réinitialisation des préférences impossible \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Erreur fatale, réinitialisation des préférences impossible \n->" + e.getMessage());
        }
        this.setVisible(false);
    }
}
