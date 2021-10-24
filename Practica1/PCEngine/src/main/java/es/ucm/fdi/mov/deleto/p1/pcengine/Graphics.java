package es.ucm.fdi.mov.deleto.p1.pcengine;

import org.graalvm.compiler.graph.Graph;

import es.ucm.fdi.mov.deleto.p1.engine.Font;
import es.ucm.fdi.mov.deleto.p1.engine.Image;

public class Graphics implements es.ucm.fdi.mov.deleto.p1.engine.Graphics {

    int _width;
    int _height;

    int _refWidth;
    int _refHeight;

    public Graphics() {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void setResolution(int x, int y) {
        _width = x;
        _height = y;

        if (_refHeight == 0 || _refWidth == 0) {
            _refWidth = _width;
            _refHeight = _height;
        }
    }

    @Override
    public void setRefResolution(int x, int y) {
        _refWidth = x;
        _refHeight = y;
    }

    private int realPositionX(int x) {
        return 0;
    }

    private int realPositionY(int y) {
        return 0;
    }

    @Override
    public void translate(int x, int y) {

    }

    @Override
    public void scale(int x, int y) {

    }

    @Override
    public void drawImage(Image image, int posX, int posY, int scaleX, int scaleY) {

    }

    @Override
    public void clear(int color) {

    }

    @Override
    public void setColor(int color) {

    }

    @Override
    public void fillCircle(int x, int y, int r) {

    }

    @Override
    public void drawText(String text, int x, int y) {

    }

    @Override
    public Image newImage(String name) {
        return null;
    }

    @Override
    public Font newFont(String fileName, int size, boolean isBold) {
        return null;
    }
}
