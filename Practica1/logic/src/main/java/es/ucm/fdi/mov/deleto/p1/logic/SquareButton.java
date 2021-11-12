package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class SquareButton implements IClickable{
    @Override
    public boolean click(TouchEvent event) {
        return false;
    }
}
