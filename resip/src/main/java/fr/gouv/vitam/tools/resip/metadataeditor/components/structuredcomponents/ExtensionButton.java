package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ExtensionButton extends JButton implements ActionListener {


    @FunctionalInterface
    public interface GetExtensionList {
        List<Pair<String,String>> getExtensionList() throws SEDALibException;
    }

    @FunctionalInterface
    public interface DoExtend {
        void doExtend(ActionEvent event);
    }

    JPopupMenu popupMenu;
    GetExtensionList getExtensionList;
    DoExtend doExtend;

    public ExtensionButton(GetExtensionList getExtensionList, DoExtend doExtend) {
        super("...");
        this.getExtensionList = getExtensionList;
        this.doExtend = doExtend;
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("...")) {
            popupMenu = new JPopupMenu("...");
            List<Pair<String,String>> extensionList = null;
            try {
                extensionList = getExtensionList.getExtensionList();
            } catch (SEDALibException e) {
                extensionList = null;
            }
            if ((extensionList != null) && !extensionList.isEmpty()) {
                for (Pair<String,String> names : extensionList) {
                    JMenuItem mi = new JMenuItem(names.getValue());
                    mi.addActionListener(this);
                    mi.setActionCommand(names.getKey());
                    popupMenu.add(mi);
                }
                popupMenu.show(this, 0, this.getBounds().height);
            }
        }
        else
            doExtend.doExtend(ev);
    }
}
