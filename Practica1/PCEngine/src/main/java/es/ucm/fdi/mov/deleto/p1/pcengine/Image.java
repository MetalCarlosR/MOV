package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.io.IOException;

import es.ucm.fdi.mov.deleto.p1.engine.IImage;

/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/

public class Image implements IImage {

    java.awt.Image _image;

    protected Image(String name) throws IOException {
        _image =  javax.imageio.ImageIO.read(new java.io.File(name));
    }

    @Override
    public int getWidth() {
        return _image.getWidth(null);
    }

    @Override
    public int getHeight() {
        return _image.getHeight(null);
    }

    public java.awt.Image getImage() {
        return _image;
    }
}
