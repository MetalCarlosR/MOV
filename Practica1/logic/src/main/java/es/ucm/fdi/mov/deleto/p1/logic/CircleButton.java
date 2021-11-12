package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

/**
 * Button class to abstract the handling of application clicks
 */
public abstract class CircleButton implements IClickable{
    /**
     * Position and radius of circle
     */
    int _posX;
    int _posY;
    private int _rad;
    int _holded = -1;
    int _originalRad = 0;

    public CircleButton(int x, int y, int buttonRad) {
        _posX=x;
        _posY=y;
        _rad = buttonRad;
        _originalRad = buttonRad;
    }
    public void setRad(int r){_rad=r;_originalRad=r;}
    protected CircleButton() {
    }

    /**
     * Overridable click callback
     */
    protected abstract void clickCallback();

    @Override
    public boolean click(TouchEvent e) {
        if(e.type() == TouchEvent.EventType.SLIDE)
            return false;
        int dX = e.x() - _posX;
        int dY = e.y() - _posY;
        double mag = (Math.sqrt((dX*dX) + (dY*dY)));

        if(_originalRad >= mag)
            {
                if(e.type() == TouchEvent.EventType.TOUCH)
                {
                    _holded = e.id();
                    System.out.println("PRESS");
                    _rad*=.9;
                }
                else if(e.id() == _holded && e.type() == TouchEvent.EventType.RELEASE)
                {
                    System.out.println("PITO");
                    _rad = _originalRad;
                    clickCallback();
                    _holded = -1;
                    return  true;
                }
            }
        else if(e.type() == TouchEvent.EventType.RELEASE && _holded != -1)
        {
            System.out.println("OUT");
            _rad=_originalRad;
            _holded = -1;
        }
        return false;
    }

    public int getRad() {
        return _rad;
    }

}
