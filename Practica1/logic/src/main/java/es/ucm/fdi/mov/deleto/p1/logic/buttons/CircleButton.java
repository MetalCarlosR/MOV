package es.ucm.fdi.mov.deleto.p1.logic.buttons;

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
    int _held = -1;
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
                    _held = e.id();
                    System.out.println("PRESS");
                    _rad*=.9;
                }
                else if(e.id() == _held && e.type() == TouchEvent.EventType.RELEASE)
                {
                    System.out.println("PITO");
                    _rad = _originalRad;
                    clickCallback();
                    _held = -1;
                    return  true;
                }
            }
        else if(e.type() == TouchEvent.EventType.RELEASE && _held != -1)
        {
            System.out.println("OUT");
            _rad=_originalRad;
            _held = -1;
        }
        return false;
    }

    public int getRad() {
        return _rad;
    }
    public int x(){return _posX;}
    public int y(){return _posY;}

}
