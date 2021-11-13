package es.ucm.fdi.mov.deleto.p1.logic.grid;

import es.ucm.fdi.mov.deleto.p1.logic.buttons.Cell;

/**
 * Simple class to hold clue information.
 *
 *   We use clue cell to signal to the user the cell that originates the clue
 *   And we need correctState cell to facilitate grid generation and give a little extra feedback
 *      to the user (the dot on the title)
 */
public class Clue {
    private final String _message;
    private final Cell _cell;
    private final Cell _correctState;

    public  Clue(Cell cell, String m, Cell correct)
    {
        _cell=cell;
        _message=m;
        _correctState=correct;
    }

    public Cell correctState(){return _correctState;};
    public Cell cell(){return _cell;};
    public String message(){return _message;};
}
