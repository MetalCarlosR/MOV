package es.ucm.fdi.mov.deleto.p1.engine;

public class TouchEvent {

    enum EventType {
        TOUCH,
        RELEASE,
        SLIDE
    }

    // Position in screen
    int _x;
    int _y;

    // Id of the button ( or finger) used
    int _id;

    // Type of event
    EventType _type;

    public TouchEvent(EventType type, int x, int y, int id){
        _type = type;
        _x = x;
        _y = y;
        _id = id;
    }

    public EventType get_type() {
        return _type;
    }

    public int get_x() {
        return _x;
    }

    public int get_y() {
        return _y;
    }

    public int get_id() {
        return _id;
    }
}
