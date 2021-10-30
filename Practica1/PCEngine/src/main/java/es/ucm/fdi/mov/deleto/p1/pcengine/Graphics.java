package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.IInput;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

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
    java.awt.Font _actualFont;


    JFrame _window;
    BufferStrategy _strategy;
    java.awt.Graphics _buffer;

    Color _color;

    int _scaleX;
    int _scaleY;

    static final int WINDOW_MENU_HIGHT = 23;
    static final int WINDOW_BORDER = 8;

    public Graphics(String name, String path) {
        _window = new JFrame(name);
        _window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _window.setVisible(true);
        _window.setIgnoreRepaint(true);

        _path = path;
        _size = new Dimension();

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

        _originX=0;
        _originY=0;


        _window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                int actualW = _window.getWidth();
                int actualH = _window.getHeight();

                int leftOffset = 0;
                int topOffset = 0;

                //We start the canvas skipping the window decorations
                _originX= WINDOW_BORDER;
                _originY= WINDOW_MENU_HIGHT + WINDOW_BORDER;

                //Respecting the aspect ratio this would be the expected width and height
                int expectedWidth  = (int)(actualH * _refFactor);
                int expectedHeight = (int)(actualW / _refFactor);

                //If the expectedWidth doesn't fit we add height to the bars
                //Otherwise add width
                if( expectedWidth > actualW+ WINDOW_BORDER)
                    topOffset = (actualH-WINDOW_MENU_HIGHT + WINDOW_BORDER - expectedHeight)/2;
                else
                    leftOffset = (actualW+ WINDOW_BORDER - expectedWidth)/2;

                _originX += leftOffset;
                _originY +=  topOffset;

                _endX = actualW-_originX;
                _endY = actualH-_originY;
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
        _window.setSize(x+(WINDOW_BORDER *2), (y+ WINDOW_BORDER *2)+ WINDOW_MENU_HIGHT);

        _refWidth  = x;
        _refHeight = y;

        _refFactor = (double)x/(double)y;

    }
    public JFrame getWindow(){return  _window;};

    private int realPositionX(int x) {
        return _originX+ (x * (_endX-_originX)/_refWidth);
    }

    public int refPositionX(int x)
    {
        return (int)((x-_originX)*((double)_refWidth / (_endX-_originX)));
    }

    private int realPositionY(int y) {
        return _originY+y * (_endY-_originY)/_refHeight;
    }

    public int refPositionY(int x)
    {
        return (int)((x-_originY)*((double)_refHeight / (_endY-_originY)));
    }

    private  int realLength(int length){
        return length * (_endX-_originX)/_refWidth;
    }

    public boolean swapBuffers() {
        //debug
        setColor(0xFFFFFF00);
        _buffer.fillRect(_endX-10,_endY+10,10,10);
        _buffer.setColor(_color);
        //debug

        _buffer.dispose();
        _buffer = _strategy.getDrawGraphics();
        //Enable antialiasing for the new buffer
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        //Check if resize was applied during rendering, if so then repaint
        boolean repeat = !_size.equals(_window.getSize());
        if(!repeat)
            _strategy.show();
        return repeat;
    }

    @Override
    public void clear(int color) {
        _size = _window.getSize();
        Color aux = _color;
        setColor(color);
        _buffer.fillRect(0, 0, _window.getWidth(), _window.getHeight());
        setColor(aux.getRGB());
    }
    @Override
    public void translate(int x, int y) {

    }

    @Override
    public void scale(int x, int y) {

    }

    @Override
    public void setOpacity(float opacity) {
        ((Graphics2D) _buffer).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    }

    @Override
    public void drawImage(IImage image, int posX, int posY, float scaleX, float scaleY) {
        Image im   = (Image) image;
        int width  = (int) (realLength(im.getWidth()) * scaleX);
        int height = (int) (realLength(im.getHeight())* scaleY);
        _buffer.drawImage(im.getImage(), realPositionX(posX)-width/2, realPositionY(posY)-height/2,width,height, null);
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
        _actualFont=f.getFont();
    }

    @Override
    public void fillCircle(int x, int y, int r) {
        _buffer.fillOval(realPositionX(x), realPositionY(y), realLength(r*2), realLength(r*2));
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        _buffer.fillRect(realPositionX(x), realPositionY(y), realLength(w), realLength(h));
    }

    @Override
    public void drawText(String text, int x, int y) {
        _buffer.setFont(_actualFont.deriveFont(_actualFont.getStyle(),realLength(_actualFont.getSize())));
        FontMetrics fm = _buffer.getFontMetrics();
        int fX = (fm.stringWidth(text))/2;
        int fY = fm.getHeight()/2;
        _buffer.drawString(text, realPositionX(x)-fX, realPositionY(y)+fY/2);
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
