package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Graphics implements IGraphics {

    int _refWidth;
    int _refHeight;

    String _path;

    JFrame _window;
    BufferStrategy _strategy;
    java.awt.Graphics _buffer;

    Color _color;

    public Graphics(JFrame window, String path) {
        _window = window;
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
        _path = path;

        // We try to create 2 buffers
        while (true) {
            try {
                _window.createBufferStrategy(2);
                break;
            } catch (Exception e) {
            }
        }
        _strategy = _window.getBufferStrategy();
        _buffer = _strategy.getDrawGraphics();
        setColor(0xFFFFFFFF);
    }

    @Override
    public int getWidth() {
        return _window.getWidth();
    }

    @Override
    public int getHeight() {
        return _window.getHeight();
    }

    @Override
    public void setResolution(int x, int y) {
        _window.setSize(x, y);

        if (_refHeight == 0 || _refWidth == 0) {
            _refWidth = getWidth();
            _refHeight = getHeight();
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

    public void swapBuffers() {
        _buffer.dispose();
        _buffer = _strategy.getDrawGraphics();
        _buffer.setColor(_color);
        _strategy.show();
    }

    @Override
    public void translate(int x, int y) {

    }

    @Override
    public void scale(int x, int y) {

    }

    @Override
    public void drawImage(IImage image, int posX, int posY, int scaleX, int scaleY) {
        Image im = (Image) image;
        _buffer.drawImage(im.getImage(), posX, posY, null);
    }

    @Override
    public void clear(int color) {
        _buffer.clearRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void setColor(int color) {
        _color = new Color(color, true);
        _buffer.setColor(_color);
    }

    @Override
    public void setFont(IFont font) {
        Font f = (Font) font;
        // _buffer.setFont(f.getFont());
    }

    @Override
    public void fillCircle(int x, int y, int r) {
        _buffer.fillOval(x, y, r, r);
    }

    @Override
    public void drawText(String text, int x, int y) {
        _buffer.drawString(text, x, y);
    }

    @Override
    public IImage newImage(String name) {

        return new Image(_path + "sprites/" + name);
    }

    @Override
    public IFont newFont(String fileName, int size, boolean isBold) {
        return new Font(fileName, size, isBold);
    }
}
