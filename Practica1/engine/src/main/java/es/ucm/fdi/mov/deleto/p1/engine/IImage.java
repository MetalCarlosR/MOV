package es.ucm.fdi.mov.deleto.p1.engine;

/**
 * Image Interface that application can use independent of platform
 */
public interface IImage {

    /**
     * Getters that return the width and height of the image
     */
    int getWidth();
    int getHeight();
}
