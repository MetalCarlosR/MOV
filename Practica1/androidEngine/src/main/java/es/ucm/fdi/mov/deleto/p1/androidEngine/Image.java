package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;

import es.ucm.fdi.mov.deleto.p1.engine.IImage;


/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/

public class Image implements IImage {

    android.graphics.Bitmap _platformImage;
    /**
     * Tries to create a image via filepath
     * @param path the path where the sound file should be contained
     * @param am Asset Manager for android to open the file
     */
    public Image(AssetManager am, String path) throws IOException {
        _platformImage = BitmapFactory.decodeStream(am.open(path));
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
