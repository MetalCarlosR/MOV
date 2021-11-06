package es.ucm.fdi.mov.deleto.p1.AEngine;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class Font implements IFont {

    android.graphics.Typeface _font;
    int _size;
    boolean _bold;

    public Font(AssetManager am, String path, int size, boolean bold)
    {
        _font = Typeface.createFromAsset(am ,path);
        _size = size;
        _bold = bold;
    }
    public android.graphics.Typeface getFont(){return _font;};
    public boolean isBold(){return _bold;}
    @Override
    public int getSize() {
        return _size;
    }
}