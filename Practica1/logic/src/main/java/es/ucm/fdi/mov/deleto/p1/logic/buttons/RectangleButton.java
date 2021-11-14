package es.ucm.fdi.mov.deleto.p1.logic.buttons;


/**
 * Button class to abstract the handling of application clicks
 */
public abstract class RectangleButton extends BaseButton {
    /**
     * Position and sides of rectangle. For now final, no need to change.
     */
    protected final int _posX;
    protected final int _posY;
    private final int _width;
    private final int _height;


    /**
     * whole width and height.
     * x and y centered
     */
    public RectangleButton(int x, int y, int w, int h){
        _posX=x;
        _posY=y;
        _width=w;
        _height=h;
    }

    /**
     * Overridable click callback
     */
    @Override
    protected boolean hit(int x, int y) {
        int dX = Math.abs(x - _posX);
        int dY = Math.abs(y - _posY);

        return ( dX <= _width/2 && dY <=_height/2);
    }
    public int x(){return _posX;}
    public int y(){return _posY;}
}
