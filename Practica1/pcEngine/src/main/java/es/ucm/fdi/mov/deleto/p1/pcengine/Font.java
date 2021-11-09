package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.io.FileInputStream;
import java.io.InputStream;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;

public class Font implements IFont {
    private java.awt.Font _font;
    private int _size;
    private boolean _bold;
    private String _path;

    public Font(String font, int size,boolean bold){
        _font = null;
        _size =size;
        _bold = bold;
        _path = font;
    }

    public boolean init() {
        java.awt.Font baseFont;
        try (InputStream is = new FileInputStream(_path)) {
            baseFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
        }
        catch (Exception e) {
            System.err.println("Error loading font: " + e);
            return false;
        }
        _font = baseFont.deriveFont(_bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN,_size);
        System.out.println(_font.getSize());

        return true;
    }

    public  java.awt.Font getFont(){return _font;};

    @Override
    public int getSize() {
        return _size;
    }
}
