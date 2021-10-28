package es.ucm.fdi.mov.deleto.p1.logic;

public class Cell {

    enum State{
        Grey,
        Blue,
        Red

    }

    public Cell(int x, int y, int n, boolean f){
        _x = x;
        _y = y;
        _neigh = n;
        _locked = f;
    }

    /*
        This method is used when reading an example from a file

        Data is:
        [Number of neighbours + free/locked] if fixed or not
        "3f"
     */
    public void setCell(String data){
        _neigh = Character.getNumericValue(data.charAt(0));
        _locked = data.charAt(1) == 'l';
        if(_locked){
            if(_neigh > 0)
                _state = State.Blue;
            else _state = State.Red;
        }
    }

    public void setCell(int neighbours, boolean locked){

    }

    public boolean changeState(){
        if(_locked)
            return false;
        else{
            _state = _state == State.Blue ? State.Red : _state == State.Red ? State.Grey : State.Blue;
            return true;
        }
    }

    public State getState() {
        return _state;
    }

    public int getNeigh() {
        return _neigh;
    }

    public boolean isLocked() {
        return _locked;
    }

    // if 0 its red
    // if not blue
    int _neigh = 0;
    boolean _locked = false;
    int _x = 0;
    int _y = 0;

    State _state = State.Grey;
}
