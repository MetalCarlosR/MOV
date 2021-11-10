package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.ICallable;

public class CircleButton implements IClickable{
    int _posX;
    int _posY;
    int _rad;

    public  CircleButton(){}

    public int getRad() {
        return _rad;
    }

    public void onClick(){}

    @Override
    public boolean clicked(int x, int y) {
        int dX = x - _posX;
        int dY = y - _posY;
        double mag = (Math.sqrt((dX*dX) + (dY*dY)));

        if(_rad >= mag) {
            onClick();
            return  true;
        }
        else
            return false;
    }
}
