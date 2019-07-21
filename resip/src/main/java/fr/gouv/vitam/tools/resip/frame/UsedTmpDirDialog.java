package fr.gouv.vitam.tools.resip.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The class UsedTmpDirDialog.
 * <p>
 * Class for temporary extraction directory collision treatment dialog.
 */
public class UsedTmpDirDialog extends JDialog {

    /**
     * The actions components.
     */
    private JPanel optionalActionPanel;
    private JPanel subInformationPanel;

    /**
     * The constant STATUS_CONTINUE.
     */
    public static int STATUS_CONTINUE = 3;
    /**
     * The constant STATUS_CLEAN.
     */
    public static int STATUS_CLEAN = 2;
    /**
     * The constant STATUS_CHANGE.
     */
    public static int STATUS_CHANGE = 1;
    /**
     * The constant STATUS_CANCEL.
     */
    public static int STATUS_CANCEL = 0;

    /**
     * The result.
     */
    private int returnValue;
    private String target;

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
        TestDialogWindow window = new TestDialogWindow(UsedTmpDirDialog.class);
    }

    /**
     * Instantiates a new UsedTmpDirDialog for test.
     *
     * @param owner the owner
     */
    public UsedTmpDirDialog(JFrame owner) {
        this(owner, "C:\\User\\Test", "Test-tmpdir");
    }

    /**
     * Create the dialog.
     *
     * @param owner   the owner
     * @param baseDir the base dir
     * @param subDir  the sub dir
     */
    public UsedTmpDirDialog(JFrame owner, String baseDir, String subDir) {
        super(owner, "Attention", true);
        GridBagConstraints gbc;

        target = Paths.get(baseDir, subDir).toString();

        this.setPreferredSize(new Dimension(600, 240));
        this.setResizable(false);

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
        label1.setIcon(new ImageIcon(getClass().getResource("/icon/edit-shredder.png")));
        label1.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        informationPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setFont(label2.getFont().deriveFont(Font.BOLD));
        label2.setText("Le répertoire d'extraction est déjà utilisé. Que voulez-vous faire?");
        gbc = new GridBagConstraints();
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(5, 5, 5, 5);
        informationPanel.add(label2, gbc);

        subInformationPanel = new JPanel();
        subInformationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        informationPanel.add(subInformationPanel, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Répertoire de travail :");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        gbc.insets = new Insets(5, 5, 5, 5);
        subInformationPanel.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText(baseDir);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        subInformationPanel.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Sous-répertoire :");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 5, 5);
        subInformationPanel.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText(subDir);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        subInformationPanel.add(label6, gbc);

        final JPanel mainActionPanel = new JPanel();
        mainActionPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(mainActionPanel, gbc);
        JCheckBox moreOptionsCheckBox = new JCheckBox();
        moreOptionsCheckBox.setEnabled(true);
        moreOptionsCheckBox.setIcon(new ImageIcon(getClass().getResource("/icon/list-add.png")));
        moreOptionsCheckBox.setSelectedIcon(new ImageIcon(getClass().getResource("/icon/list-remove.png")));
        moreOptionsCheckBox.setText("Plus d'options");
        moreOptionsCheckBox.addItemListener(arg -> moreOptionsEvent(arg));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainActionPanel.add(moreOptionsCheckBox, gbc);
        JButton buttonCancel = new JButton();
        buttonCancel.setText("Annuler");
        buttonCancel.addActionListener(arg -> buttonCancel());
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainActionPanel.add(buttonCancel, gbc);
        JButton buttonDelete = new JButton();
        buttonDelete.setText("Effacer");
        buttonDelete.addActionListener(arg -> buttonCleanDirectory());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainActionPanel.add(buttonDelete, gbc);

        optionalActionPanel = new JPanel();
        optionalActionPanel.setLayout(new GridBagLayout());
        optionalActionPanel.setVisible(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(optionalActionPanel, gbc);
        JButton changeButton = new JButton();
        changeButton.setText("Changer");
        changeButton.addActionListener(arg -> buttonChangeDirectory());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        optionalActionPanel.add(changeButton, gbc);
        JButton continueButton = new JButton();
        continueButton.setText("Continuer");
        continueButton.addActionListener(arg -> buttonContinue());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 5, 5, 5);
        optionalActionPanel.add(continueButton, gbc);

        final JSeparator separator = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(separator, gbc);

        final JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(explanationPanel, gbc);
        final JLabel label7 = new JLabel();
        label7.setIcon(new ImageIcon(getClass().getResource("/icon/large-dialog-information.png")));
        label7.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        explanationPanel.add(label7, gbc);
        JTextArea explanationTextAres = new JTextArea();
        explanationTextAres.setLineWrap(true);
        explanationTextAres.setFont(new JLabel().getFont());
        explanationTextAres.setBackground(UIManager.getColor("Dialog.background"));
        explanationTextAres.setFocusable(false);
        explanationTextAres.setMinimumSize(new Dimension(500, 70));
        explanationTextAres.setText("Le répertoire dans lequel vous allez extraire des informations temporaires pour traitement (issues d'un SIP, DIP, conteneur de mails...) n'est pas vide.\n" +
                "Vous pouvez effacer ce répertoire et continuer ou annuler.\n" +
                "En cliquant sur \"Plus d'options\" vous pouvez aussi continuer sur place au risque de mélanger des informations de différentes extractions ou encore changer de répertoire.");
        explanationTextAres.setWrapStyleWord(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 5);
        explanationPanel.add(explanationTextAres, gbc);

        pack();
        optionalActionPanel.setVisible(false);
        pack();
        setLocationRelativeTo(owner);
    }

    // actions

    private void moreOptionsEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            optionalActionPanel.setVisible(true);
            Dimension dim = this.getSize();
            pack();
            dim.height = dim.height + optionalActionPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        } else if (event.getStateChange() == DESELECTED) {
            optionalActionPanel.setVisible(false);
            Dimension dim = this.getSize();
            dim.height = dim.height - optionalActionPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        }
    }

    private void buttonContinue() {
        returnValue = STATUS_CONTINUE;
        this.setVisible(false);
    }

    private void buttonCleanDirectory() {
        returnValue = STATUS_CLEAN;
        this.setVisible(false);
    }

    private String chooseDiskName(String from) {
        JFileChooser fileChooser = new JFileChooser(from);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            try {
                return fileChooser.getSelectedFile().getCanonicalPath();
            } catch (IOException e) {
                return null;
            }
        return null;
    }

    private void changeDir(String dir) {
        GridBagConstraints gbc;
        Dimension dim = this.getSize();

        // change the subInformationPanel cause the workDir reference is no more convenient
        subInformationPanel.removeAll();
        final JLabel label3bis = new JLabel();
        label3bis.setText("Répertoire :");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 5, 5);
        subInformationPanel.add(label3bis, gbc);
        JLabel baseDirLabel = new JLabel();
        baseDirLabel.setText(dir);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        subInformationPanel.add(baseDirLabel, gbc);

        pack();
        dim.height = this.getHeight();
        this.setSize(dim);
        this.setPreferredSize(dim);
        pack();
    }

    private void buttonChangeDirectory() {
        String newTarget = chooseDiskName(target);
        if (newTarget == null)
            return;
        target = newTarget;
        if (Files.exists(Paths.get(newTarget))) {
            changeDir(target);
            return;
        }
        returnValue = STATUS_CHANGE;
        this.setVisible(false);
    }

    private void buttonCancel() {
        returnValue = STATUS_CANCEL;
        this.setVisible(false);
    }

    /**
     * Get the return value.
     *
     * @return the return value
     */
    public int getReturnValue() {
        return returnValue;
    }

    /**
     * Get the dialog result target.
     *
     * @return the target
     */
    public String getResult() {
        return target;
    }
}
