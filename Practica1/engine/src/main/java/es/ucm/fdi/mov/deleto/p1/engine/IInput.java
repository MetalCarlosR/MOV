package es.ucm.fdi.mov.deleto.p1.engine;

import java.util.List;

public interface IInput {

    List<TouchEvent> getTouchEvents();

    public void newTouchEvent(TouchEvent event);
}
