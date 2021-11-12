package es.ucm.fdi.mov.deleto.p1.engine;

/**
 * Simple touch event that encapsulates both computer clicks and touch-devices taps
 */
public class TouchEvent implements Comparable<TouchEvent> {

    public enum EventType {
        TOUCH,
        RELEASE,
        SLIDE,
        CLOSE_REQUEST
    }
    // Type of event
    EventType _type;

    // Position in screen
    int _x;
    int _y;

    // Id of the button ( or finger) used
    int _id;
    
    public TouchEvent(EventType type, int x, int y, int id){
        _type = type;
        _x = x;
        _y = y;
        _id = id;
    }

    @Override
    public int compareTo(TouchEvent event) {
        return _type.compareTo(event.type());
    }
    /***********
     * Getters *
     ***********/
    public EventType type() {
        return _type;
    }

    public int x() {
        return _x;
    }

    public int y() {
        return _y;
    }

    public int id() {
        return _id;
    }
}
