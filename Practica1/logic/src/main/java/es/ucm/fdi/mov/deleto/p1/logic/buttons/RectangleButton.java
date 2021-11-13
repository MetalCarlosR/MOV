package es.ucm.fdi.mov.deleto.p1.logic.buttons;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

/**
 * Button class to abstract the handling of application clicks
 */
public abstract class RectangleButton implements IClickable{
    /**
     * Position and sides of rectangle. For now final, no need to change.
     */
    private final int _posX;
    private final int _posY;
    private final int _width;
    private final int _height;

    protected abstract void clickCallback();

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
    public boolean click(TouchEvent ev) {
        int dX = Math.abs(ev.x() - _posX);
        int dY = Math.abs(ev.y() - _posY);

        if( dX <= _width && dY <=_height) {
            clickCallback();
            return  true;
        }
        else
            return false;
    }
    public int x(){return _posX;}
    public int y(){return _posY;}
}
