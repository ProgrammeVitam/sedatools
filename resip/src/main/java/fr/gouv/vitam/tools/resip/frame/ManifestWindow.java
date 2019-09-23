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

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor.*;

/**
 * The Class BigTextEditDialog.
 * <p>
 * Class for text edition dialog used for long text metadata like Description or TextContent.
 */
public class ManifestWindow extends JFrame implements SearchListener {

    /**
     * The actions components.
     */
    private RSyntaxTextArea xmlTextArea;
    private JLabel statusLabel;

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
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ResipException, InterruptedException {
        ResipGraphicApp rga = new ResipGraphicApp(null);
        Thread.sleep(1000);
        ManifestWindow mw = new ManifestWindow();
    }

    /**
     * Instantiates a new ManifestWindow.
     **/
    public ManifestWindow() throws InterruptedException {
        GridBagConstraints gbc;
        GridBagLayout gbl;

        setTitle("Visualisation du manifest généré le " + LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toString());

        setPreferredSize(new Dimension(1024, 600));
        setMinimumSize(new Dimension(700, 300));

        gbl = new GridBagLayout();
        gbl.rowWeights = new double[]{1.0, 0.0};
        gbl.columnWeights = new double[]{1.0, 0,0, 0.0};
        gbl.columnWidths = new int[]{400, 0, 200};
        Container contentPane = getContentPane();
        contentPane.setLayout(gbl);

        xmlTextArea = new RSyntaxTextArea(20, 120);
        xmlTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        SyntaxScheme scheme = xmlTextArea.getSyntaxScheme();
        scheme.getStyle(Token.MARKUP_TAG_DELIMITER).foreground = COMPOSITE_LABEL_MARKUP_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_NAME).foreground = COMPOSITE_LABEL_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE).foreground = COMPOSITE_LABEL_MARKUP_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE_VALUE).foreground = COMPOSITE_LABEL_ATTRIBUTE_COLOR;
        xmlTextArea.setCodeFoldingEnabled(true);
        xmlTextArea.setFont(MainWindow.DETAILS_FONT);
        xmlTextArea.setText("En attente de génération du manifest...");
        xmlTextArea.setCaretPosition(0);
        xmlTextArea.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.gridy = 0;
        RTextScrollPane editScrollPane = new RTextScrollPane(xmlTextArea);
        contentPane.add(editScrollPane, gbc);

        ManifestFindToolBar findToolBar = new ManifestFindToolBar(this);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPane.add(findToolBar, gbc);

        JSeparator separator=new JSeparator(SwingConstants.VERTICAL);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPane.add(separator, gbc);


        statusLabel = new JLabel("Prêt");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 1;
        contentPane.add(statusLabel, gbc);

        pack();
        setLocationRelativeTo(ResipGraphicApp.getTheApp().mainWindow);
        setVisible(true);
    }

    /**
     * Set text.
     *
     * @param text the text
     */
    public void setText(String text) {
        xmlTextArea.setText(text);
        xmlTextArea.setCaretPosition(0);
    }

    @Override
    public void searchEvent(SearchEvent e) {

        SearchEvent.Type type = e.getType();
        SearchContext context = e.getSearchContext();
        SearchResult result;

        switch (type) {
            default: // Prevent FindBugs warning later
            case MARK_ALL:
                result = SearchEngine.markAll(xmlTextArea, context);
                break;
            case FIND:
                result = SearchEngine.find(xmlTextArea, context);
                if (!result.wasFound()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(xmlTextArea);
                }
                break;
        }
        String text;
        if (result.wasFound()) {
            text = "Trouvé; Occurences marquées: " + result.getMarkedCount();
        } else if (type == SearchEvent.Type.MARK_ALL) {
            if (result.getMarkedCount() > 0) {
                text = "Occurences marquées: " + result.getMarkedCount();
            } else {
                text = "";
            }
        } else {
            text = "Trouvé";
        }
        statusLabel.setText(text);

    }

    @Override
    public String getSelectedText() {
        return xmlTextArea.getSelectedText();
    }

}
