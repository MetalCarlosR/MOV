package es.ucm.fdi.mov.deleto.p1.logic;

import jdk.internal.net.http.common.Pair;

public class Clue {
    private String _message;
    private Cell _cell;
    private Cell _correctState;

    public  Clue(Cell cell, String m, Cell correct)
    {
        _cell=cell;
        _message=m;
        _correctState=correct;
    }

    public Cell getCorrectState(){return _correctState;};
    public Cell getCell(){return _cell;};
    public String getMessage(){return _message;};
}
