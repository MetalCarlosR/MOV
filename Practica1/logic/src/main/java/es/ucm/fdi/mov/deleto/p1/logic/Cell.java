package es.ucm.fdi.mov.deleto.p1.logic;

public class Cell {
    // if its 0 its red
    // if not blue
    int _neigh = 0;
    boolean _fixed = false;
    int _x = 0;
    int _y = 0;

    public Cell(int x, int y, int n, boolean f){
        _x = x;
        _y = y;
        _neigh = n;
        _fixed = f;
    }

    public int getNeigh() {
        return _neigh;
    }

    public boolean isFixed() {
        return _fixed;
    }
}
