package es.ucm.fdi.mov.deleto.p1.logic;

/**
 * Interface for buttons to implement
 *  Could be extended to handle both Click and Releases so we can give more feedback on buttons
 */
public interface IClickable {
    /**
     * Function to check if given click falls under IClickable space
     * @param x logic x coordinate
     * @param y logic y coordinate
     * @return whether the click was inside bounding box
     */
    public boolean click(int x, int y);
}
