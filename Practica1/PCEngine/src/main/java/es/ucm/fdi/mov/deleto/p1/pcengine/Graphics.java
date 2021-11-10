package es.ucm.fdi.mov.deleto.p1.pcengine;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/

public class Graphics implements IGraphics {

    /*********************
     * Stuff for scaling *
     *********************/

    static int WINDOW_MENU_HEIGHT = 23;
    static int WINDOW_BORDER = 8;

    int    _refWidth;
    int    _refHeight;
    double _refFactor;

    int _originX=0;
    int _originY=0;

    int _endX=0;
    int _endY=0;

    Dimension _size;

    /******************
     * Renderer State *
     ******************/

    String _assetsPath;
    java.awt.Font _actualFont;
    Color _actualColor = new Color(0);

    JFrame _window;
    BufferStrategy _strategy;
    java.awt.Graphics _buffer;

    /**
     * Sets up window and configures rendering
     * @param name title of the window
     * @param assetPath directory where assets will be fetched from
     * @param width window initial width
     * @param height window initial height
     */
    public Graphics(String name, String assetPath, int width, int height) {

         //Setting up window
        _window = new JFrame(name);
        _window.setSize(width, height);

        _window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _window.setVisible(true);

        _window.setIgnoreRepaint(true);
        Toolkit.getDefaultToolkit().setDynamicLayout(false);

        WINDOW_BORDER = _window.getInsets().right;
        WINDOW_MENU_HEIGHT = _window.getInsets().top - WINDOW_BORDER;

        _assetsPath = assetPath;
        _size = new Dimension();

         //Create and get buffer strategy
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

         //Set up resize callback to compute new scaling factors and offsets
        _window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                int actualW = _window.getWidth();
                int actualH = _window.getHeight();

                int leftOffset = 0;
                int topOffset = 0;

                //We start the canvas skipping the window decorations
                _originX= WINDOW_BORDER;
                _originY= WINDOW_MENU_HEIGHT + WINDOW_BORDER;

                //Respecting the aspect ratio this would be the expected width and height
                int expectedWidth  = (int)(actualH * _refFactor);
                int expectedHeight = (int)(actualW / _refFactor);

                //If the expectedWidth doesn't fit we add height to the bars
                //Otherwise add width
                if( expectedWidth > actualW+ WINDOW_BORDER)
                    topOffset = (actualH- WINDOW_MENU_HEIGHT + WINDOW_BORDER - expectedHeight)/2;
                else
                    leftOffset = (actualW+ WINDOW_BORDER - expectedWidth)/2;

                _originX += leftOffset;
                _originY +=  topOffset;

                _endX = actualW-_originX;
                _endY = actualH-_originY;
            }
        });
    }

    /**************************
     * Interface draw methods *
     **************************/
    @Override
    public void fillCircle(int x, int y, double r) {
        _buffer.fillOval((int)realPositionX(x-r), (int)realPositionY(y-r), realLength(r*2.0), realLength(r*2.0));
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        _buffer.fillRect(realPositionX(x-w/2), realPositionY(y-h/2), realLength(w), realLength(h));
    }

    @Override
    public Vec2<Integer> drawText(String text, int x, int y) {
        return drawText(text,x,y,1);
    }

    /**
     * As explained in the interface, splits newlines and centers text to given coordinates.
     */
    @Override
    public Vec2<Integer>  drawText(String text, int x, int y, double scale) {
        String[] splits = text.split("\n");
        _buffer.setFont(_actualFont.deriveFont(_actualFont.getStyle(),realLength((int)(_actualFont.getSize()*scale))));
        FontMetrics fm = _buffer.getFontMetrics();
        int fY = fm.getHeight()/2;
        int i = 0;
        if(splits.length > 1)
            fY-= fm.getHeight()/2;
        int fX=0;

        for(String s : splits)
        {
            fY+= (fm.getHeight()+fm.getLeading()+fm.getMaxAscent())*i++;

            fX = (fm.stringWidth(s))/2;
            _buffer.drawString(s, realPositionX(x)-fX, realPositionY(y)+fY/2);
        }
        int xX, yY;
        xX = realPositionX(x)-fX;
        yY = realPositionY(y)+fY/2;
        return new Vec2<>(refPositionX(xX+(fm.stringWidth(splits[splits.length-1]))),refPositionY(yY));
    }

    @Override
    public void drawImage(IImage image, int posX, int posY, float scaleX, float scaleY) {
        Image im   = (Image) image;
        int width  = (int) (realLength(im.getWidth()) * scaleX);
        int height = (int) (realLength(im.getHeight())* scaleY);
        _buffer.drawImage(im.getImage(), realPositionX(posX)-width/2, realPositionY(posY)-height/2,width,height, null);
    }


    /**********************
     * Render and present *
     **********************/

    public boolean swapBuffers() {
        //debug
        setColor(0xFFFFFF00);
        _buffer.fillRect(_endX-10,_endY+10,10,10);
        _buffer.setColor(_actualColor);
        //debug

        _buffer.dispose();
        _buffer = _strategy.getDrawGraphics();
        //Enable antialiasing for the new buffer
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

        //Check if resize was applied during rendering, if so then repaint
        boolean repeat = !_size.equals(_window.getSize());
        if(!repeat)
        {
            _strategy.show();
        }
        return repeat;
    }

    public void clear(int color) {
        _size = _window.getSize();
        Color aux = _actualColor;
        setColor(color);
        _buffer.fillRect(0, 0, _window.getWidth(), _window.getHeight());
        setColor(aux.getRGB());
    }

    /*******************
     * Factory Methods *
     *******************/

    @Override
    public IImage newImage(String name) {
        return new Image(_assetsPath + "sprites/" + name);
    }

    @Override
    public IFont newFont(String fileName, int size, boolean isBold) {
        Font ret = new Font(_assetsPath + "fonts/"+fileName, size, isBold);
        ret.init();
        return ret;
    }

    /**
     * Swing needs to release it's resources on close
     */
    public void release()
    {
        _window.setVisible(false);
        _window.dispose();
    }


    /*******************
     * Scaling Methods *
     *******************/



    private int realPositionX(int x) {
        return _originX+ (x * (_endX-_originX)/_refWidth);
    }

    private double realPositionX(double x) {
        return _originX+ (x * (_endX-_originX)/(double) _refWidth);
    }

    public int refPositionX(int x)
    {
        return (int)((x-_originX)*((double)_refWidth / (_endX-_originX)));
    }

    private int realPositionY(int y) {
        return _originY+y * (_endY-_originY)/_refHeight;
    }
    private double realPositionY(double y) {
        return _originY+y * (_endY-_originY)/(double)_refHeight;
    }

    protected int refPositionY(int y)
    {
        return (int)((y-_originY)*((double)_refHeight / (_endY-_originY)));
    }

    private  int realLength(double length){
        return (int)(length * (_endX-_originX)/(double)_refWidth);
    }

    @Override
    public void setOpacity(float opacity) {
        ((Graphics2D) _buffer).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    }


    @Override
    public void setColor(int color) {
        _actualColor = new Color(color, true);
        _buffer.setColor(_actualColor);
    }

    @Override
    public void setFont(IFont font) {
        Font f = (Font) font;
        _buffer.setFont(f.getFont());
        _actualFont=f.getFont();
    }


    @Override
    public int getLogicWidth() {
        return _refWidth;
    }

    @Override
    public int getLogicHeight() {
        return _refHeight;
    }

    @Override
    public void setResolution(int width, int height) {
        _refWidth  = width;
        _refHeight = height;
        _refFactor = (double) width /(double) height;
    }

    public JFrame getWindow(){return  _window;};
}
