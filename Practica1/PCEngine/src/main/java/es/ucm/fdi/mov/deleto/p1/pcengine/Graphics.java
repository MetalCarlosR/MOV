package es.ucm.fdi.mov.deleto.p1.pcengine;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import es.ucm.fdi.mov.deleto.p1.engine.AbstractGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.EngineOptions;
import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.Vec2;
import sun.util.resources.cldr.ext.CurrencyNames_ce;

/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/

public class Graphics extends AbstractGraphics implements IGraphics   {

    /*********************
     * Stuff for scaling *
     *********************/

    protected static int WINDOW_MENU_HEIGHT = 23;
    protected static int WINDOW_BORDER = 8;

    private Dimension _size;

    /******************
     * Renderer State *
     ******************/

    private java.awt.Font _actualFont;
    private Color _actualColor = new Color(0);

    protected JFrame _window;
    private final BufferStrategy _strategy;
    private java.awt.Graphics _buffer;
    protected Engine _engine;

    String _imagePath;
    String _fontPath;

    int _clearColor;

    /**
     * Sets up window and configures rendering
     * @param name title of the window
     * @param options options with path and window size
     * @param askBeforeExit if this string is set, we display this message before closing the window
     */
    public Graphics(Engine engine, String name, EngineOptions options, final String askBeforeExit) {

        _engine = engine; //To report errors

         //Setting up window
        _window = new JFrame(name);
        _window.setSize(options.realWidth, options.realHeight);
        _window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE );
        _window.setVisible(true);
        WINDOW_BORDER = _window.getInsets().right;
        WINDOW_MENU_HEIGHT = _window.getInsets().top - WINDOW_BORDER;
        _clearColor = options.clearColor;

        _imagePath = options.assetsPath+options.imagesPath;
        _fontPath = options.assetsPath+options.fontsPath;
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


        _window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(!askBeforeExit.equals(""))
                {
                    int i=JOptionPane.showConfirmDialog(null, askBeforeExit,"Exit",JOptionPane.YES_NO_OPTION);
                    if(i==0) {
                        _engine.exit();
                    }
                }
                else _engine.exit();
            }
            @Override
            public void windowIconified(WindowEvent e) {
                _engine.pause();
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                _engine.resume();
            }
        });
        setResolution(options.realWidth,options.realHeight);
    }

    /**************************
     * Interface draw methods *
     **************************/
    @Override
    public void fillCircle(int x, int y, double r) {
        _buffer.fillOval((int)(x-r), (int)(y-r), (int)(r*2), (int)(r*2));
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        _buffer.fillRect((x-w/2), (y-h/2), (w), (h));
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
        _buffer.setFont(_actualFont.deriveFont(_actualFont.getStyle(),((int)(_actualFont.getSize()*scale))));
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
            _buffer.drawString(s, (x)-fX, (y)+fY/2);
        }
        int xX=0, yY=0;
        xX = x+fX;
        yY = y+fY/2;
        return new Vec2<>(xX,(yY));
    }

    @Override
    public void drawImage(IImage image, int posX, int posY, float scaleX, float scaleY) {
        Image im   = (Image) image;
        int width  = (int) ((im.getWidth()) * scaleX);
        int height = (int) ((im.getHeight())* scaleY);
        _buffer.drawImage(im.getImage(), (posX)-width/2, (posY)-height/2,width,height, null);
    }


    /**********************
     * Render and present *
     **********************/

    public boolean swapBuffers() {

        _buffer.dispose();
        _buffer = _strategy.getDrawGraphics();

        this.recalculateTransform(_window.getWidth(),_window.getHeight(),WINDOW_BORDER,WINDOW_MENU_HEIGHT);

        //Enable antialiasing for the new buffer
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D)_buffer).setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

        //Check if resize was applied during rendering, if so then repaint
        boolean repeat = _strategy.contentsRestored();
        if(!repeat)
        {
            _strategy.show();
        }
        return repeat || _strategy.contentsLost();
    }

    public void clear() {
        _size = _window.getSize();

        Color aux = _actualColor;
        setColor(_clearColor);
        _buffer.fillRect(0, 0, _window.getWidth(), _window.getHeight());
        setColor(aux.getRGB());

        ((Graphics2D)_buffer).translate(_translateX,_translateY);
        ((Graphics2D)_buffer).scale(_scale,_scale);
    }

    /*******************
     * Factory Methods *
     *******************/

    @Override
    public IImage newImage(String name) {
        try {
            return new Image(_imagePath+name);
        } catch (IOException e) {
            _engine.panic("could not load image at: "+ _imagePath+name,"Image loading error");
            return null;
        }
    }

    @Override
    public IFont newFont(String fileName, int size, boolean isBold) {
        Font ret = new Font(_fontPath+fileName, size, isBold);
        try
        {
            ret.init();
        }
        catch (IOException e)
        {
            _engine.panic("could not load font at: "+_fontPath+fileName,"Font loading error");
        }
        return ret;
    }

    /**
     * Swing needs to release it's resources on close
     */
    public void release()
    {
        _window.setVisible(false);
        _window.dispose();
        System.exit(0);
    }

    /*******************
     * Scaling Methods *
     *******************/
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
        return _logicW;
    }

    @Override
    public int getLogicHeight() {
        return _logicH;
    }

    @Override
    public void setResolution(int width, int height) {
        _logicW = width;
        _logicH = height;
    }

    public JFrame getWindow(){return  _window;};
}
