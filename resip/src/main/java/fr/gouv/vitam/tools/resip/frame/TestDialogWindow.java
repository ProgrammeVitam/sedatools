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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

/**
 * The type Test dialog window.
 */
public class TestDialogWindow extends JFrame implements ActionListener {

    /**
     * The Dialog class.
     */
    public Class<? extends JDialog> dialogClass;
    /**
     * The It.
     */
    public JDialog it;

    /**
     * Create menu j menu bar.
     *
     * @return the j menu bar
     */
    JMenuBar createMenu() {
        JMenuBar menuBar;
        JMenu UIMenu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        UIMenu = new JMenu("UI en cours de visu");
        menuBar.add(UIMenu);

        menuItem = new JMenuItem("System");
        menuItem.addActionListener(this);
        UIMenu.add(menuItem);

        menuItem = new JMenuItem("CrossPlatform");
        menuItem.addActionListener(this);
        UIMenu.add(menuItem);
        return menuBar;
    }

    /**
     * Instantiates a new Test dialog window.
     *
     * @param dialogClass the dialog class
     * @throws ClassNotFoundException          the class not found exception
     * @throws UnsupportedLookAndFeelException the unsupported look and feel exception
     * @throws InstantiationException          the instantiation exception
     * @throws IllegalAccessException          the illegal access exception
     * @throws NoSuchMethodException           the no such method exception
     * @throws InvocationTargetException       the invocation target exception
     */
    public TestDialogWindow(Class<? extends JDialog> dialogClass) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        super("TestDialog");
        this.dialogClass=dialogClass;

        if (System.getProperty("os.name").toLowerCase().contains("win"))
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        else
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        JMenuBar menuBar = createMenu();
        setJMenuBar(menuBar);

        java.net.URL imageURL = getClass().getClassLoader().getResource("VitamIcon96.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            setIconImage(icon.getImage());
        }
        this.setTitle(ResipGraphicApp.getAppName());

        getContentPane().setPreferredSize(new Dimension(200, 50));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        pack();

        this.setVisible(true);
        it = dialogClass.getConstructor(JFrame.class).newInstance(this);
        it.setLocationByPlatform(true);
        it.setVisible(true);
    }

    public void actionPerformed(final ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        if (source instanceof JMenuItem) {
            String action = ((JMenuItem) source).getText();
            if (action == null)
                System.err.println("unknown menu action");
            else
                switch (action) {
                    // File Menu
                    case "System":
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                            MainWindow.LABEL_FONT=UIManager.getFont("Label.font");
                            MainWindow.CLICK_FONT=UIManager.getFont("Button.font");
                            MainWindow.BOLD_LABEL_FONT = MainWindow.LABEL_FONT.deriveFont(MainWindow.LABEL_FONT.getStyle() | Font.BOLD);
                            MainWindow.TREE_FONT=MainWindow.LABEL_FONT;
                            MainWindow.DETAILS_FONT=MainWindow.LABEL_FONT.deriveFont(MainWindow.LABEL_FONT.getSize()+(float)2.0);
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
                        }
                        break;
                    case "CrossPlatform":
                        try {
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                            MainWindow.LABEL_FONT=UIManager.getFont("Label.font");
                            MainWindow.CLICK_FONT=UIManager.getFont("Button.font");
                            MainWindow.BOLD_LABEL_FONT = MainWindow.LABEL_FONT.deriveFont(MainWindow.LABEL_FONT.getStyle() | Font.BOLD);
                            MainWindow.TREE_FONT=MainWindow.LABEL_FONT;
                            MainWindow.DETAILS_FONT=MainWindow.LABEL_FONT.deriveFont(MainWindow.LABEL_FONT.getSize()+(float)2.0);
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
                        }
                        break;
                }
            it.setVisible(false);
            try {
                it = dialogClass.getConstructor(JFrame.class).newInstance(this);
                it.setLocationByPlatform(true);
                it.setVisible(true);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            }
        }
    }
}
