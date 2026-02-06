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
package fr.gouv.vitam.tools.mailextract;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * MailExtractGraphicApp class for the graphic application.
 */
public class MailExtractGraphicApp implements ActionListener, Runnable {

    /**
     * The main window.
     */
    private MailExtractMainWindow mainWindow;

    /**
     * The parameters
     */
    private String type;
    private String user, password, hostname;
    int port;
    private String container, folder;
    private String rootpath, outputname;

    StoreExtractorOptions storeExtractorOptions;

    private boolean debug = false;
    private String verbatim;
    private boolean english;

    /**
     * Instantiates a new mail extract graphic app with beginning parameters.
     *
     * @param type                  the type of the mail extraction process
     * @param user                  the username for authentication
     * @param password              the password for authentication
     * @param hostname              the server address
     * @param port                  the server port
     * @param container             the container information
     * @param folder                the folder to extract emails from
     * @param rootpath              the root path for saving extracted mails
     * @param outputname            the output name for the extraction
     * @param storeExtractorOptions the options for the store extractor
     * @param debug                 the debug flag indicating whether debug mode is enabled
     * @param verbatim              the verbosity level for logging
     * @param english               the flag indicating if English language is used
     */
    MailExtractGraphicApp(
        String type,
        String user,
        String password,
        String hostname,
        int port,
        String container,
        String folder,
        String rootpath,
        String outputname,
        StoreExtractorOptions storeExtractorOptions,
        boolean debug,
        String verbatim,
        boolean english
    ) {
        this.type = type;
        this.user = user;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
        this.container = container;
        this.folder = folder;
        this.rootpath = rootpath;
        this.outputname = outputname;
        this.storeExtractorOptions = storeExtractorOptions;
        this.debug = debug;
        this.verbatim = verbatim;
        this.english = english;

        EventQueue.invokeLater(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            mainWindow = new MailExtractMainWindow(this);
            insertOptions();
            mainWindow.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // insert all parameters from the command line in the graphic fields
    private void insertOptions() {
        switch (type) {
            case "pst":
                mainWindow.localRadioButton.doClick();
                mainWindow.pstRadioButton.doClick();
                mainWindow.containerField.setText(container);

                break;
            case "mbox":
                mainWindow.localRadioButton.doClick();
                mainWindow.mboxRadioButton.doClick();
                mainWindow.containerField.setText(container);

                break;
            case "eml":
                mainWindow.localRadioButton.doClick();
                mainWindow.emlRadioButton.doClick();
                mainWindow.containerField.setText(container);

                break;
            case "thunderbird":
                mainWindow.localRadioButton.doClick();
                mainWindow.thunderbirdRadioButton.doClick();
                mainWindow.containerField.setText(container);

                break;
            case "msg":
                mainWindow.localRadioButton.doClick();
                mainWindow.msgRadioButton.doClick();
                mainWindow.containerField.setText(container);

                break;
            case "imap":
                mainWindow.protocoleRadioButton.doClick();
                mainWindow.imapRadioButton.doClick();
                mainWindow.userField.setText(user);
                mainWindow.passwordField.setText(password);
                mainWindow.serverField.setText((port != -1) ? hostname + ":" + port : hostname);
                break;
            case "imaps":
                mainWindow.protocoleRadioButton.doClick();
                mainWindow.imapsRadioButton.doClick();
                mainWindow.userField.setText(user);
                mainWindow.passwordField.setText(password);
                mainWindow.serverField.setText((port != -1) ? hostname + ":" + port : hostname);
                break;
        }

        mainWindow.folderField.setText(folder);
        mainWindow.savedirField.setText(rootpath);
        mainWindow.nameField.setText(outputname);

        mainWindow.keepOnlyDeepCheckBox.setSelected(storeExtractorOptions.keepOnlyDeepEmptyFolders);
        mainWindow.dropEmptyFoldersCheckBox.setSelected(storeExtractorOptions.dropEmptyFolders);
        mainWindow.warningCheckBox.setSelected(storeExtractorOptions.warningMsgProblem);
        mainWindow.extractMessageCheckBox.setSelected(storeExtractorOptions.extractMessages);
        mainWindow.extractContactCheckBox.setSelected(storeExtractorOptions.extractContacts);
        mainWindow.extractAppointmentCheckBox.setSelected(storeExtractorOptions.extractAppointments);
        mainWindow.extractContentCheckBox.setSelected(storeExtractorOptions.extractElementsContent);
        mainWindow.extractListCheckBox.setSelected(storeExtractorOptions.extractElementsList);
        mainWindow.extractmessagetextfileCheckBox.setSelected(storeExtractorOptions.extractMessageTextFile);
        mainWindow.extractmessagetextmetadataCheckBox.setSelected(storeExtractorOptions.extractMessageTextMetadata);
        mainWindow.extractfiletextfileCheckBox.setSelected(storeExtractorOptions.extractFileTextFile);
        mainWindow.extractfiletextmetadataCheckBox.setSelected(storeExtractorOptions.extractFileTextMetadata);
        mainWindow.charsetComboBox.setSelectedItem(storeExtractorOptions.defaultCharsetName);
        mainWindow.namesLengthField.setText(Integer.toString(storeExtractorOptions.namesLength));

        mainWindow.debugCheckBox.setSelected(debug);
        mainWindow.englishCheckBox.setSelected(english);

        // convert from normalized log level name to the choice list log level
        for (int i = 0; i < 7; i++) {
            if (verbatim.equals(loglevelStrings[i])) {
                verbatim = mainWindow.loglevelGraphicStrings[i];
                break;
            }
        }
        mainWindow.loglevelComboBox.setSelectedItem(verbatim);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        // global panel local or protocole enable/disable
        if (command.equals("local")) {
            mainWindow.thunderbirdRadioButton.setEnabled(true);
            mainWindow.pstRadioButton.setEnabled(true);
            mainWindow.mboxRadioButton.setEnabled(true);
            mainWindow.emlRadioButton.setEnabled(true);
            mainWindow.msgRadioButton.setEnabled(true);
            mainWindow.containerLabel.setEnabled(true);
            mainWindow.containerField.setEnabled(true);
            mainWindow.containerButton.setEnabled(true);
            mainWindow.imapRadioButton.setEnabled(false);
            mainWindow.imapsRadioButton.setEnabled(false);
            mainWindow.serverLabel.setEnabled(false);
            mainWindow.serverField.setEnabled(false);
            mainWindow.userLabel.setEnabled(false);
            mainWindow.userField.setEnabled(false);
            mainWindow.passwordLabel.setEnabled(false);
            mainWindow.passwordField.setEnabled(false);
        } else if (command.equals("protocole")) {
            mainWindow.thunderbirdRadioButton.setEnabled(false);
            mainWindow.pstRadioButton.setEnabled(false);
            mainWindow.mboxRadioButton.setEnabled(false);
            mainWindow.emlRadioButton.setEnabled(false);
            mainWindow.msgRadioButton.setEnabled(false);
            mainWindow.containerLabel.setEnabled(false);
            mainWindow.containerField.setEnabled(false);
            mainWindow.containerButton.setEnabled(false);
            mainWindow.imapRadioButton.setEnabled(true);
            mainWindow.imapsRadioButton.setEnabled(true);
            mainWindow.serverLabel.setEnabled(true);
            mainWindow.serverField.setEnabled(true);
            mainWindow.userLabel.setEnabled(true);
            mainWindow.userField.setEnabled(true);
            mainWindow.passwordLabel.setEnabled(true);
            mainWindow.passwordField.setEnabled(true);
        } else if (command.equals("container")) {
            String filename = selectPath(mainWindow.containerField.getText(), false);
            if (filename != null) mainWindow.containerField.setText(filename);
        } else if (command.equals("savedir")) {
            String dirname = selectPath(mainWindow.savedirField.getText(), true);
            if (dirname != null) mainWindow.savedirField.setText(dirname);
        } else if (command.equals("list")) doAction(LIST_ACTION);
        else if (command.equals("stat")) doAction(STAT_ACTION);
        else if (command.equals("extract")) doAction(EXTRACT_ACTION);
        else if (command.equals("empty")) doAction(EMPTY_LOG);
    }

    // get a file and/or directory from a standard selection dialog
    // if dirBool is true only directory can be selected
    private String selectPath(String folder, boolean dirBool) {
        File file;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(
            (dirBool ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES)
        );
        fileChooser.setFileHidingEnabled(false);
        file = new File(folder);
        if (file.exists()) fileChooser.setSelectedFile(file);
        int returnVal = fileChooser.showOpenDialog(this.mainWindow);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            return (file.getAbsolutePath());
        } else return null;
    }

    /**
     * The Constant LIST_ACTION.
     */
    static final int LIST_ACTION = 1;

    /**
     * The Constant STAT_ACTION.
     */
    static final int STAT_ACTION = 2;

    /**
     * The Constant EXTRACT_ACTION.
     */
    static final int EXTRACT_ACTION = 3;

    /**
     * The Constant EMPTY_LOG.
     */
    static final int EMPTY_LOG = 4;

    // do one of the 3 actions
    private void doAction(int actionNumber) {
        parseParams();

        if (actionNumber == EMPTY_LOG) {
            mainWindow.consoleTextArea.setText("");
        } else new MailExtractThread(
            mainWindow,
            actionNumber,
            type,
            hostname,
            port,
            user,
            password,
            container,
            folder,
            rootpath,
            outputname,
            storeExtractorOptions,
            verbatim,
            debug
        ).start();
    }

    /**
     * The loglevel strings.
     */
    String[] loglevelStrings = { "OFF", "GLOBAL", "WARNING", "FOLDER", "MESSAGE_GROUP", "MESSAGE", "MESSAGE_DETAILS" };

    // get the global parameters from the graphic fields
    private void parseParams() {
        // local
        if (mainWindow.localRadioButton.isSelected()) {
            if (mainWindow.thunderbirdRadioButton.isSelected()) type = "thunderbird";
            else if (mainWindow.emlRadioButton.isSelected()) type = "eml";
            else if (mainWindow.msgRadioButton.isSelected()) type = "msg";
            else if (mainWindow.mboxRadioButton.isSelected()) type = "mbox";
            else if (mainWindow.pstRadioButton.isSelected()) type = "pst";
            container = mainWindow.containerField.getText();
        }
        // server
        else {
            if (mainWindow.imapsRadioButton.isSelected()) type = "imaps";
            else type = "imap";
            String server = mainWindow.serverField.getText();
            if (server.indexOf(':') >= 0) {
                hostname = server.substring(0, server.indexOf(':'));
                port = Integer.parseInt(server.substring(server.indexOf(':') + 1));
            } else hostname = server;
            user = mainWindow.userField.getText();
            password = mainWindow.passwordField.getText();
        }
        folder = mainWindow.folderField.getText();
        rootpath = mainWindow.savedirField.getText();
        outputname = mainWindow.nameField.getText();

        storeExtractorOptions.keepOnlyDeepEmptyFolders = mainWindow.keepOnlyDeepCheckBox.isSelected();
        storeExtractorOptions.dropEmptyFolders = mainWindow.dropEmptyFoldersCheckBox.isSelected();
        storeExtractorOptions.warningMsgProblem = mainWindow.warningCheckBox.isSelected();
        storeExtractorOptions.extractMessages = mainWindow.extractMessageCheckBox.isSelected();
        storeExtractorOptions.extractContacts = mainWindow.extractContactCheckBox.isSelected();
        storeExtractorOptions.extractAppointments = mainWindow.extractAppointmentCheckBox.isSelected();
        storeExtractorOptions.extractElementsContent = mainWindow.extractContentCheckBox.isSelected();
        storeExtractorOptions.extractElementsList = mainWindow.extractListCheckBox.isSelected();
        storeExtractorOptions.extractMessageTextFile = mainWindow.extractmessagetextfileCheckBox.isSelected();
        storeExtractorOptions.extractMessageTextMetadata = mainWindow.extractmessagetextmetadataCheckBox.isSelected();
        storeExtractorOptions.extractFileTextFile = mainWindow.extractfiletextfileCheckBox.isSelected();
        storeExtractorOptions.extractFileTextMetadata = mainWindow.extractfiletextmetadataCheckBox.isSelected();

        debug = mainWindow.debugCheckBox.isSelected();

        try {
            storeExtractorOptions.namesLength = Integer.parseInt(mainWindow.namesLengthField.getText());
        } catch (NumberFormatException e) {
            mainWindow.namesLengthField.setText(Integer.toString(storeExtractorOptions.namesLength));
        }

        storeExtractorOptions.defaultCharsetName = (String) mainWindow.charsetComboBox.getSelectedItem();

        // convert from log level name in the choice list to normalized log
        // level
        verbatim = (String) mainWindow.loglevelComboBox.getSelectedItem();
        for (int i = 0; i < 7; i++) {
            if (verbatim.equals(mainWindow.loglevelGraphicStrings[i])) {
                verbatim = loglevelStrings[i];
                break;
            }
        }
    }
}
