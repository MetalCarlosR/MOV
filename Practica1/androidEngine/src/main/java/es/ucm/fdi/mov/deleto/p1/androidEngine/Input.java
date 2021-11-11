package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Input  {
    List<TouchEvent> _events;
    float _scale = 1;
    int _offX;
    int _offY;

    /**
     * Sets up callbacks to store events on our event list
     *
     * @param view where to get the events from
     */
    public Input(View view)
    {
        _events = new ArrayList<>();

         //Setup activity callbacks to store events in our representation
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                newTouchEvent(TouchEvent.EventType.RELEASE,(int)((motionEvent.getX()-_offX)/_scale),
                                                                          (int)((motionEvent.getY()-_offY)/_scale),1);
                return false;
            }
        });
    }

    /**
     * Store scale parameters to scale to logic coordinates events before reporting them back
     * @param scale scale
     * @param offX x coordinate where the canvas starts
     * @param offY y coordinate where the canvas starts
     */
    public  void setScale(float scale, int offX, int offY)
    {
        _scale = scale;
        _offX=offX;
        _offY=offY;
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
        _events.add(new TouchEvent(type, x, y, id));
    }
}
