package es.ucm.fdi.mov.deleto.p1.logic.buttons;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

/**
 * Abstract class for buttons to implement
 */
public abstract class BaseButton {

    //Tracks last click id
    int _id = -1;

    /**
     *  If given event was TOUCH and inside bounds we store id and show held graphics,
     *  if it was RELEASE and by the stored ID we reset and call callback.
     *  We keep the state accordingly otherwise
     *
     * @param event the TouchEvent that we want to handle
     * @return whether the click was inside bounding box
     */
    public boolean click(TouchEvent event)
    {
        if(event.type() == TouchEvent.EventType.SLIDE)
            return false;


        if( hit(event.x(),event.y()))
        {
            if(event.type()== TouchEvent.EventType.TOUCH) {
                _id = event.id();
                held();
            }
            else if(event.id() == _id && event.type() == TouchEvent.EventType.RELEASE)
            {
                clickCallback();
                _id = -1;
                reset();
                return true;
            }
        }
        else if(event.type() == TouchEvent.EventType.RELEASE && _id != -1)
        {
            _id = -1;
            reset();
        }
        return false;
    }

    //Method to check if current shape falls under x and y
    protected abstract boolean hit(int x, int y);

    //Actual callback
    protected abstract void clickCallback();

    //Override to change style
    protected abstract void held();

    //Override to change style
    protected abstract void reset();
}
