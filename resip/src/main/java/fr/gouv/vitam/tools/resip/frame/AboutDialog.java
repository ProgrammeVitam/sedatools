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
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.parameters.TreatmentParameters;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The type About dialog.
 */
public class AboutDialog extends JDialog {
    private JTextArea messageTextArea;
    private JButton okButton;
    private JButton reinitPrefsButton;

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
        TestDialogWindow window = new TestDialogWindow(AboutDialog.class);
    }

    /**
     * Instantiates a new AboutDialog for test.
     *
     * @param owner the owner
     */
    public AboutDialog(JFrame owner) {
        this(owner, "This is a test about");
    }


    /**
     * Instantiates a new About dialog.
     *
     * @param owner   the owner
     * @param message the message
     */
    public AboutDialog(JFrame owner, String message) {
        super(owner, "A propos de...", true);
        GridBagConstraints gbc;

        setPreferredSize(new Dimension(550, 250));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{1.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0,0,1.0,0};
        getContentPane().setLayout(gridBagLayout);

        try {
            Image vitamImage= ImageIO.read(getClass().getResourceAsStream("/small-logo_vitam.png"));
            ImagePanel vitamPanel = new ImagePanel(vitamImage);
            vitamPanel.setPreferredSize(new Dimension(281,100));
            vitamPanel.setMaximumSize(new Dimension(281,100));
            vitamPanel.setMinimumSize(new Dimension(281,100));
            vitamPanel.setSize(new Dimension(281,100));
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.weightx=0.0;
            gbc.gridwidth=3;
            gbc.insets = new Insets(5, 5, 5, 5);
            getContentPane().add(vitamPanel, gbc);
        } catch (IOException ignored) {
        }

        JTextArea messageTextArea = new JTextArea();
        messageTextArea.setText(message);
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setBackground(getContentPane().getBackground());
        messageTextArea.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx=1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(messageTextArea, gbc);

        JLabel urlLabel = new JLabel();
        urlLabel.setText("<html>Aller sur <a href=\"www.programmevitam.fr\">www.programmevitam.fr</a>");
        urlLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.programmevitam.fr/pages/ressources/resip"));
                } catch (URISyntaxException | IOException ex) {
                    //It looks like there's a problem
                }
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx=1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(urlLabel, gbc);

        JButton okButton = new JButton();
        okButton.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(okButton, gbc);
        okButton.addActionListener(arg0 -> buttonOK());

        JButton reinitPrefsButton = new JButton();
        reinitPrefsButton.setText("Réinitialiser les préférences");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth=2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(reinitPrefsButton, gbc);
        reinitPrefsButton.addActionListener(arg0 -> buttonReinitPrefs());

        JButton savePrefsButton = new JButton();
        savePrefsButton.setText("Sauver");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(savePrefsButton, gbc);
        savePrefsButton.addActionListener(arg0 -> buttonSavePrefs());

        JButton importPrefsButton = new JButton();
        importPrefsButton.setText("Importer");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(importPrefsButton, gbc);
        importPrefsButton.addActionListener(arg0 -> buttonImportPrefs());

        pack();
        setLocationRelativeTo(owner);
    }

    private void buttonOK() {
        this.setVisible(false);
    }

    private void buttonReinitPrefs() {
        try {
            Preferences.getInstance().reinitialisePrefs();
            ResipGraphicApp.getTheApp().treatmentParameters =new TreatmentParameters(Preferences.
                    getInstance());
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow, "Erreur fatale, réinitialisation des préférences impossible \n->" + e.getMessage(), "Erreur",
                    UserInteractionDialog.ERROR_DIALOG, null);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Erreur fatale, réinitialisation des préférences impossible",e);
        }
        this.setVisible(false);
    }

    private void buttonSavePrefs() {
        try {
            JFileChooser fileChooser = new JFileChooser(Preferences.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(ResipGraphicApp.getTheApp().mainWindow) == JFileChooser.APPROVE_OPTION) {
                Preferences.getInstance().save(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow, "Sauvegarde des préférences impossible \n->" + e.getMessage(), "Erreur",
                    UserInteractionDialog.ERROR_DIALOG, null);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Sauvegarde des préférences impossible",e);
        }
        this.setVisible(false);
    }

    private void buttonImportPrefs() {
        try {
            JFileChooser fileChooser = new JFileChooser(Preferences.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(ResipGraphicApp.getTheApp().mainWindow) == JFileChooser.APPROVE_OPTION) {
                Preferences.getInstance().load(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow, "Import des préférences impossible \n->" + e.getMessage(), "Erreur",
                    UserInteractionDialog.ERROR_DIALOG, null);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Import des préférences impossible",e);
        }
        this.setVisible(false);
    }
}
