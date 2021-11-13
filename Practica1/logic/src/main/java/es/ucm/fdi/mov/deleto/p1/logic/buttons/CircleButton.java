package es.ucm.fdi.mov.deleto.p1.logic.buttons;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

/**
 * Button class to abstract the handling of application clicks
 */
public abstract class CircleButton extends IButton {
    /**
     * Position and radius of circle
     */
    int _posX;
    int _posY;
    private int _rad;
    int _originalRad = 0;

    public CircleButton(int x, int y, int buttonRad) {
        _posX=x;
        _posY=y;
        _rad = buttonRad;
        _originalRad = buttonRad;
    }
    public void setRad(int r){_rad=r;_originalRad=r;}

    /**
     * For empty constructor for empty cells
     */
    protected CircleButton() {}

    /**
     * Overridable click callback
     */
    protected abstract void clickCallback();

    @Override
    protected boolean hit(int x, int y) {
        int dX = x - _posX;
        int dY = y - _posY;
        double mag = (Math.sqrt((dX*dX) + (dY*dY)));

       return  _originalRad >= mag;
    }

    @Override
    protected void held() {
        _rad = (int) (_originalRad * .9f);
    }

    @Override
    protected void reset() {
        _rad = _originalRad;
    }

    public int getRad() {
        return _rad;
    }
    public int x(){return _posX;}
    public int y(){return _posY;}

}
