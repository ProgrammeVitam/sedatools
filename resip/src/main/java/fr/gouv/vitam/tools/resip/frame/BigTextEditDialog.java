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
package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * The Class BigTextEditDialog.
 * <p>
 * Class for text edition dialog used for long text metadata like Description or TextContent.
 */
public class BigTextEditDialog extends JDialog {

    /**
     * The actions components.
     */
    private JTextArea editTextArea;

    /**
     * The result.
     */
    private String textResult;

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
        TestDialogWindow window = new TestDialogWindow(BigTextEditDialog.class);
    }

    /**
     * Instantiates a new XmlEditDialog for test.
     *
     * @param owner the owner
     * @throws SEDALibException the seda lib exception
     */
    public BigTextEditDialog(JFrame owner) throws SEDALibException, IOException {
        this(owner, FileUtils.readFileToString(new File("resip/src/main/java/fr/gouv/vitam/tools/resip/frame/BigTextEditDialog.java"),"UTF8"),"Code");
    }

    /**
     * Create the dialog.
     *
     * @param owner the owner
     * @param text  the text
     */
    public BigTextEditDialog(JFrame owner, String text, String title) {
        super(owner, "", true);
        GridBagConstraints gbc;
        GridBagLayout gbl;

        setTitle(title);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 600));

        gbl=new GridBagLayout();
        gbl.rowWeights = new double[]{1.0,0.0};
        gbl.columnWeights = new double[]{0.0,1.0,0.0};
        Container contentPane = getContentPane();
        contentPane.setLayout(gbl);

        editTextArea = new JTextArea();
        editTextArea.setText(text);
        editTextArea.setCaretPosition(0);
        editTextArea.setFont(SEDAObjectEditor.EDIT_FONT);
        editTextArea.setLineWrap(true);
        editTextArea.setWrapStyleWord(true);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth=3;
        gbc.gridy = 0;
        JScrollPane editScrollPane=new JScrollPane(editTextArea);
        contentPane.add(editScrollPane, gbc);

        final JButton validateButton = new JButton("Valider");
        validateButton.addActionListener(arg -> buttonValidate());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPane.add(validateButton, gbc);

        final JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(arg -> buttonCancel());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 1;
        contentPane.add(cancelButton, gbc);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelButton.doClick();
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    private void buttonCancel() {
        setVisible(false);
    }

    private void buttonValidate() {
            textResult=editTextArea.getText();
             setVisible(false);
    }

    /**
     * Get the dialog result xml string.
     *
     * @return the xml object
     */
    public String getResult() {
        return textResult;
    }

    /**
     * Get the dialog return value.
     *
     * @return the return value
     */
    public boolean getReturnValue() {
        return !(textResult == null);
    }
}
