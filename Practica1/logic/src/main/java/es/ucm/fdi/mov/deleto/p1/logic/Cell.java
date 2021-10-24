package es.ucm.fdi.mov.deleto.p1.logic;

public class Cell {
    public Cell(int x, int y, int n, boolean f){
        _x = x;
        _y = y;
        _neigh = n;
        _fixed = f;
    }

    /*
        This method is used when reading an example from a file

        Data is:
        [Number of neighbours + f/l] if fixed or not
        "3f"
     */
    public void setCell(String data){
        _neigh = Character.getNumericValue(data.charAt(0));
        _fixed = data.charAt(1) == 'f';
    }

    public void setCell(int neighbours, boolean fixed){

    }

    public int getNeigh() {
        return _neigh;
    }

    public boolean isFixed() {
        return _fixed;
    }

    // if its 0 its red
    // if not blue
    int _neigh = 0;
    boolean _fixed = false;
    int _x = 0;
    int _y = 0;
}
