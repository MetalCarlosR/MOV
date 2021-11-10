package es.ucm.fdi.mov.deleto.p1.logic;

/**
 * Button class to abstract the handling of application clicks
 */
public abstract class RectangleButton implements IClickable{
    /**
     * Position and radius of circle
     */
    int _posX;
    int _posY;
    int _width;
    int _height;

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
    public boolean click(int x, int y) {
        int dX = Math.abs(x - _posX);
        int dY = Math.abs(y - _posY);

        if( dX <= _width && dY <=_height) {
            clickCallback();
            return  true;
        }
        else
            return false;
    }

}
