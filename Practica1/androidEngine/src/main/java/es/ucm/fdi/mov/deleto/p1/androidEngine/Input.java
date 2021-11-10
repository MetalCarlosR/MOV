package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Input  {
    ArrayList<TouchEvent> _events;
    float _scale = 1;
    int _offX;
    int _offY;

    public Input(View view)
    {
        _events = new ArrayList<>();
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                newTouchEvent(new TouchEvent(TouchEvent.EventType.RELEASE,(int)((motionEvent.getX()-_offX)/_scale),
                                                                          (int)((motionEvent.getY()-_offY)/_scale),1));
                return false;
            }
        });
    }
    public  void setScale(float scale, int offX, int offY)
    {
        _scale = scale;
        _offX=offX;
        _offY=offY;
    }

    public synchronized List<TouchEvent> getTouchEvents() {
        List<TouchEvent> events = new ArrayList<>(_events);
        _events.clear();
        return events;
    }

    public synchronized void newTouchEvent(TouchEvent event) {
        _events.add(event);
    }
}
