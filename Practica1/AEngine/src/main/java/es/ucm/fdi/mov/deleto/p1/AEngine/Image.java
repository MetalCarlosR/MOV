package es.ucm.fdi.mov.deleto.p1.AEngine;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;

import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Image implements IImage {

    android.graphics.Bitmap _platformImage;

    public Image(AssetManager am, String path)
    {
        try {
            _platformImage = BitmapFactory.decodeStream(am.open(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public android.graphics.Bitmap getHandler()
    {
        return _platformImage;
    }

    @Override
    public int getWidth() {
        return  _platformImage.getWidth();
    }

    @Override
    public int getHeight() {
        return _platformImage.getHeight();
    }

    public Bitmap getScaled(int width, int height) {
        return Bitmap.createScaledBitmap(_platformImage,width,height,true);
    }
}
