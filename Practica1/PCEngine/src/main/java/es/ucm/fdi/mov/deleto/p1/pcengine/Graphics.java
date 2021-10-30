package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
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

    int _endX;
    int _endY;

    Dimension _size;
    String _path;

    JFrame _window;
    BufferStrategy _strategy;
    java.awt.Graphics _buffer;

    Color _color;

    int _scaleX;
    int _scaleY;

    static final int BORDE_H = 31;
    static final int BORDE_W = 8;

    public Graphics(JFrame window, String path) {
        _window = window;
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
        _path = path;
        _size = new Dimension();
        // We try to create 2 buffers
        while (true) {
            try {
                _window.createBufferStrategy(2);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        _strategy = _window.getBufferStrategy();
        _buffer = _strategy.getDrawGraphics();
        setColor(0xFFFFFFFF);

        System.out.println(_originY);
        _originX=0;
        System.out.println(_window.getContentPane().getHeight());
        _window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                int actualW = _window.getWidth();
                int actualH = _window.getHeight();
                //  MINIMO ANCHO NECESARIO
                if((double)actualH * _refFactor > actualW+BORDE_W)
                {
                    //CABE EL ANCHO Y NO EL ALTO, BANDAS ARRIBA Y ABAJO
                        int border = ((actualH-14) - (int)((actualW) / _refFactor));
                    if(border < 0)
                        border=0;
                    _originX = BORDE_W;
                    _originY = BORDE_H + border/2;
                }
                else{
                    int bandas = (actualW+10 - (int)(Math.floor((actualH) * _refFactor)));
                    if(bandas < 0)
                        bandas=0;
                    _originX = BORDE_W+bandas/2;
                    _originY = BORDE_H;
//                    _endX = _originX +(int)(actualH*_refFactor) - bandas/2;
                }
                _endX = actualW-_originX;
                _endY = actualH-_originY+4;
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
        _window.setSize(x+16, (y+8)+BORDE_H);

        //_window.setSize(_window.getWidth()+insetWide, _window.getHeight()+insetTall);

        _refWidth  = x;
        _refHeight = y;

        _refFactor = (double)x/(double)y;

    }

    private int realPositionX(int x) {
        return _originX+x * (_endX-_originX)/_refWidth;
    }

    private int realPositionY(int y) {
        return _originY+y * (_endY-_originY)/_refHeight;
    }

    private  int realLength(int length){
        return length * (_endX-_originX)/_refWidth;
    }

    public boolean swapBuffers() {
        setColor(0xFFFFFF00);
//        _buffer.fillRect(_originX, _originY,_endX,_endY);
        _buffer.fillRect(_endX-10,_endY+10,10,10);
        _buffer.dispose();
        _buffer = _strategy.getDrawGraphics();
        _buffer.setColor(_color);
        _strategy.show();
        boolean repeat = !_size.equals(_window.getSize());
        if(repeat)
            System.out.println("AAAAAA");
        return repeat;
    }

    @Override
    public void clear(int color) {
        Color aux = _color;
        setColor(color);
        _buffer.fillRect(0, 0, _window.getWidth(), _window.getHeight());
        setColor(aux.getRGB());
        _size = _window.getSize();
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
        _buffer.fillOval(realPositionX(x), realPositionY(y), realLength(r), realLength(r));
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        _buffer.fillRect(realPositionX(x), realPositionY(y), realLength(w), realLength(h));
    }

    @Override
    public void drawText(String text, int x, int y) {
        _buffer.setFont(_buffer.getFont().deriveFont(_buffer.getFont().getSize()*(_endX-_originX)/_refWidth));
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
