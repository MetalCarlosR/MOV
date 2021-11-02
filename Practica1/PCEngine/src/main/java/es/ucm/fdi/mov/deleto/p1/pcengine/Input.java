package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import es.ucm.fdi.mov.deleto.p1.engine.IInput;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Input implements IInput {

    ArrayList<TouchEvent> _events;
    Graphics _g;

    public Input(Graphics g){
        _events = new ArrayList<TouchEvent>();
        _g = g;
        _g.getWindow().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newTouchEvent(new TouchEvent(TouchEvent.EventType.TOUCH,_g.refPositionX(e.getX()),_g.refPositionY(e.getY()),e.getID()));
            }

            @Override
          public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                newTouchEvent(new TouchEvent(TouchEvent.EventType.SLIDE,_g.refPositionX(e.getX()),_g.refPositionY(e.getY()),e.getID()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                newTouchEvent(new TouchEvent(TouchEvent.EventType.RELEASE,_g.refPositionX(e.getX()),_g.refPositionY(e.getY()),e.getID()));
            }
        });
    }

    @Override
    public synchronized List<TouchEvent> getTouchEvents() {
        List<TouchEvent> events = new ArrayList<>(_events);
        _events.clear();
        return events;
    }

    @Override
    public synchronized void newTouchEvent(TouchEvent event) {
        _events.add(event);
    }
}
