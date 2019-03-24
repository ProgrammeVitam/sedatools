package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

public class TestDialogWindow extends JFrame implements ActionListener {

    public Class<? extends JDialog> dialogClass;
    public JDialog it;

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
