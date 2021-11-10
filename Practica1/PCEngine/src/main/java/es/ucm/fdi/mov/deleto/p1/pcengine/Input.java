package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Input {

    ArrayList<TouchEvent> _events = new ArrayList<>();
    Graphics _graphics;

    /**
     * Register mouse callbacks on window to populate the event list
     * @param g graphics object to get access to window and scaling methods
     */
    public Input(Graphics g){
        _graphics = g;
        _graphics.getWindow().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newTouchEvent(TouchEvent.EventType.TOUCH,e.getX(),e.getY(),e.getID());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                newTouchEvent(TouchEvent.EventType.SLIDE,e.getX(),e.getY(),e.getID());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                newTouchEvent(TouchEvent.EventType.RELEASE,e.getX(),e.getY(),e.getID());
            }
        });
    }

    /**
     * Must be synchronized or some events might be lost.
     * Clears the event list after copy.
     *
     * @return a copy off all the events since the last call.
     */
    public synchronized List<TouchEvent> getTouchEvents() {
        List<TouchEvent> events = new ArrayList<>(_events);
        _events.clear();
        return events;
    }

    /**
     * Adds the given event to the current list.
     * Events will be stored in logical coordinates based on the moment they were produced,
     * i.e setResolution between events can produce unexpected event positions.
     *
     * @param type of event to create
     * @param x position where it occurred
     * @param y position where it occurred
     * @param id of the 'click' that created the event
     */
    public synchronized void newTouchEvent(TouchEvent.EventType type, int x, int y, int id) {
        _events.add(new TouchEvent(type, _graphics.refPositionX(x), _graphics.refPositionY(y),id));
    }
}
