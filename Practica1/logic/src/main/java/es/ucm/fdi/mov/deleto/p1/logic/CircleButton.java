package es.ucm.fdi.mov.deleto.p1.logic;

/**
 * Button class to abstract the handling of application clicks
 */
public abstract class CircleButton implements IClickable{
    /**
     * Position and radius of circle
     */
    int _posX;
    int _posY;
    int _rad;

    public CircleButton(int x, int y, int buttonRad) {
        _posX=x;
        _posY=y;
        _rad = buttonRad;
    }

    protected CircleButton() {
    }

    /**
     * Overridable click callback
     */
    protected abstract void clickCallback();

    @Override
    public boolean click(int x, int y) {
        int dX = x - _posX;
        int dY = y - _posY;
        double mag = (Math.sqrt((dX*dX) + (dY*dY)));

        if(_rad >= mag) {
            clickCallback();
            return  true;
        }
        else
            return false;
    }

    public int getRad() {
        return _rad;
    }

}
