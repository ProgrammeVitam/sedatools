package fr.gouv.vitam.tools.resip.frame;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class NumericFilter extends DocumentFilter {
    @Override
    public void replace(FilterBypass fb, int offs, int length,
                        String str, AttributeSet a) throws BadLocationException {
        str = str.replaceAll("[^0-9]", "");
        super.replace(fb, offs, length, str, a);
    }

    @Override
    public void insertString(FilterBypass fb, int offs, String str,
                             AttributeSet a) throws BadLocationException {
        str = str.replaceAll("[^0-9]", "");
        super.insertString(fb, offs, str, a);
    }
}