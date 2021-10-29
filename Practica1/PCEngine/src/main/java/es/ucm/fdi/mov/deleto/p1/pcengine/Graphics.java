package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    double _refFactor;
    int _virtualW;
    int _virtualH;

    int _originX;
    int _originY;

    String _path;

    JFrame _window;
    BufferStrategy _strategy;
    java.awt.Graphics _buffer;

    Color _color;

    int _scaleX;
    int _scaleY;

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

        _window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                int actualW = _window.getWidth();
                int actualH = _window.getHeight();
                //  MINIMO ANCHO NECESARIO
                if((double)actualH * _refFactor > actualW)
                {
                    //CABE EL ANCHO Y NO EL ALTO, BANDAS ARRIBA Y ABAJO
                }
                else{
                    //CABE EL ALTO Y NO EL ANCHO, BANDAS IZQUIERDA Y DERECHA
                }
            }
        });
    }
    public void release()
    {
        _window.setVisible(false);
        _window.dispose();
    }

    @Override
    public int getWidth() {
        return _refWidth;
    }

    @Override
    public int getHeight() {
        return _refHeight;
    }

    @Override
    public void setResolution(int x, int y) {
        _window.setSize(x, y);
        _refWidth  = x;
        _refHeight = y;

        _refFactor = (double)x/(double)y;
    }

    private int realPositionX(int x) {
        return (_virtualW/_refWidth)*x+_originX;
    }

    private int realPositionY(int y) {
        return (_virtualH/_refHeight)*y+_originX;
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
        Color aux = _color;
        setColor(color);
        _buffer.fillRect(0, 0, _window.getWidth(), _window.getHeight());
        setColor(aux.getRGB());
    }

    @Override
    public void setColor(int color) {
        _color = new Color(color, true);
        _buffer.setColor(_color);
    }

    @Override
    public void setFont(IFont font) {
        Font f = (Font) font;
        _buffer.setFont(f.getFont());
    }

    @Override
    public void fillCircle(int x, int y, int r) {
        _buffer.fillOval(realPositionX(x), realPositionY(y), r, r);
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        _buffer.fillRect(realPositionX(x), realPositionY(y), w, h);
    }

    @Override
    public void drawText(String text, int x, int y) {
        _buffer.drawString(text, realPositionX(x), realPositionY(y));
    }

    @Override
    public IImage newImage(String name) {

        return new Image(_path + "sprites/" + name);
    }

    @Override
    public IFont newFont(String fileName, int size, boolean isBold) {
        Font ret = new Font(_path + "fonts/"+fileName, size, isBold);
        ret.init();
        return ret;
    }
}
