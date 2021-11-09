package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.ICallable;

public class CircleButton implements IClickable{
    int _posX;
    int _posY;
    int _rad;
    ICallable _callback;

    public  CircleButton(ICallable callback){
        _callback = callback;
    }

    public int getRad() {
        return _rad;
    }

    @Override
    public boolean clicked(int x, int y) {
        int dX = x - _posX;
        int dY = y - _posY;
        double mag = (Math.sqrt((dX*dX) + (dY*dY)));

        if(_rad >= mag) {
            if(_callback != null)
                _callback.call();
            return  true;
        }
        else
            return false;
    }
}
