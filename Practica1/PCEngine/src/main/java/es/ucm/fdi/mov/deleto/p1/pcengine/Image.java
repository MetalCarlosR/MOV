package es.ucm.fdi.mov.deleto.p1.pcengine;

import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Image implements IImage {

    java.awt.Image _image;

    public Image(String name) {
        try {
            _image =  javax.imageio.ImageIO.read(new java.io.File(name));
        } catch (Exception e) {
            System.err.println("Can't find file " + name);
        }
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
