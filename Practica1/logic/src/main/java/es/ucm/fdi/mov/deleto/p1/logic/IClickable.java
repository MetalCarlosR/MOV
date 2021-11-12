package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

/**
 * Interface for buttons to implement
 *  Could be extended to handle both Click and Releases so we can give more feedback on buttons
 */
public interface IClickable {
    /**
     * Function to check if given click falls under IClickable space
     * @param event the TouchEvent that we want to handle
     * @return whether the click was inside bounding box
     */
    public boolean click(TouchEvent event);
}
