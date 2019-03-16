package fr.gouv.vitam.tools.resip.frame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TargetDialog extends JDialog {
    private JTextArea questionTextArea;
    private JButton continueButton;
    private JButton cleanDirectoryButton;
    private JButton changeDirectoryButton;
    private JButton cancelButton;

    public static int STATUS_CONTINUE=3;
    public static int STATUS_CLEAN=2;
    public static int STATUS_CHANGE=1;
    public static int STATUS_CANCEL=0;

    public int returnValue;
    public String target;

    public TargetDialog(JFrame owner, String target) {
        super(owner, "Alerte", true);
        getContentPane().setPreferredSize(new Dimension(600, 200));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{1.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0.1, 0.1,0.1,0.1};
        getContentPane().setLayout(gridBagLayout);

        this.target=target;
        questionTextArea = new JTextArea();
        questionTextArea.setText("L'import que vous demandez nécessite l'extraction " +
                "de fichiers dans un répertoire temporaire déjà utilisé.\n\n" +
                target + "\n\n" + "Que voulez-vous faire avec ce répertoire?");
        questionTextArea.setEditable(false);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setBackground(getContentPane().getBackground());
        questionTextArea.setFont(questionTextArea.getFont().deriveFont(Font.BOLD, questionTextArea.getFont().getSize()));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(questionTextArea, gbc);

        continueButton = new JButton();
        continueButton.setText("Continuer");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(continueButton, gbc);
        continueButton.addActionListener(arg0 -> buttonContinue());

        cleanDirectoryButton = new JButton();
        cleanDirectoryButton.setText("Effacer et continuer");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(cleanDirectoryButton, gbc);
        cleanDirectoryButton.addActionListener(arg0 -> buttonCleanDirectory());

        changeDirectoryButton = new JButton();
        changeDirectoryButton.setText("En changer");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(changeDirectoryButton, gbc);
        changeDirectoryButton.addActionListener(arg0 -> buttonChangeDirectory());

        cancelButton = new JButton();
        cancelButton.setText("Annuler");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(cancelButton, gbc);
        cancelButton.addActionListener(arg0 -> buttonCancel());

        pack();
        setLocationRelativeTo(owner);
    }

    private void buttonContinue(){
        returnValue =STATUS_CONTINUE;
        this.setVisible(false);
    }

    private void buttonCleanDirectory(){
        returnValue =STATUS_CLEAN;
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

    private void buttonChangeDirectory(){
        String newTarget=chooseDiskName(target);
        if (newTarget==null)
            return;
        target=newTarget;
        if (Files.exists(Paths.get(newTarget))){
            questionTextArea.setText("Le nouveau répertoire temporaire choisi est aussi déjà utilisé.\n\n" +
                    target + "\n\n" + "Que voulez-vous faire avec ce répertoire?");
            return;
        }
        returnValue =STATUS_CHANGE;
        this.setVisible(false);
    }

    private void buttonCancel(){
        returnValue =STATUS_CANCEL;
        this.setVisible(false);
    }
}
