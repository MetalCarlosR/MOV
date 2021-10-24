package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Input implements es.ucm.fdi.mov.deleto.p1.engine.Input {

    ArrayList<TouchEvent> _events;

    public Input(){
        _events = new ArrayList<TouchEvent>();
    }

    @Override
    public List<TouchEvent> getTouchEvents() {
        List<TouchEvent> events = _events;
        _events.clear();
        return events;
    }

    @Override
    public void newTouchEvent(TouchEvent event) {
        _events.add(event);
    }
}
