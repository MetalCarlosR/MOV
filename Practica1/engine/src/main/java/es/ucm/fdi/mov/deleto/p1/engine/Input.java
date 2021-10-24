package es.ucm.fdi.mov.deleto.p1.engine;

import java.util.List;

public interface Input {

    List<TouchEvent> getTouchEvents();

    public void newTouchEvent(TouchEvent event);
}
