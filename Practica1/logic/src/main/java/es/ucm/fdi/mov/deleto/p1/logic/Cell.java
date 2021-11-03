package es.ucm.fdi.mov.deleto.p1.logic;

public class Cell {

    enum State{ Grey, Blue, Red }

    /**
     *
     * @param x logical X coordinate [from 0 to grid size]
     * @param y logical Y coordinate [from 0 to grid size]
     * @param n number of neighbours
     * @param f whether it is locked or not, i.e it's state can be changed by the player
     */
    public Cell(int x, int y, int n, boolean f){
        _x = x;
        _y = y;
        _neigh = n;
        _locked = f;
    }
    public Cell(int x, int y, State s)
    {
        _x=x;
        _y=y;
        _locked=false;
        _neigh = 0;
        _state = s;
    }

    public Cell(String data, int x, int y)
    {
        setCell(data);
        _x=x;
        _y=y;
    }

    /***
     * Method to initialize a cell by passing a string. Used when reading maps from files
     *
     * @param data string of structure "N{f/l}" where N is the number of neighbours,
     *             l means its locked and f means its free
     *
     *             Example: "0f 0l 2l 2f"
     */
    private void setCell(String data){
        if(data == null)
        {
            System.err.println("Invalid data string given to SetCell");
            return;
        }
        _neigh = Character.getNumericValue(data.charAt(0));
        _locked = data.charAt(1) == 'l';
        if(_locked){
            if(_neigh > 0)
                _state = State.Blue;
            else _state = State.Red;
        }
    }

    public void setState(State s){
        _state = s;
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
//        if(!isLocked())
//            throw new RuntimeException("AAAAAAAAAAAAAAAAAAAA");
        return _neigh;
    }

    public boolean isLocked() {
        return _locked;
    }
    public  void lock()
    {
        _locked=true;
    }
    public  void unlock()
    {
        _locked=false;
    }
    public void setNeigh(int n)
    {
        _neigh=n;
    }
    private int _neigh = 0;
    private boolean _locked = false;
    int _x = 0;
    int _y = 0;

    private State _state = State.Grey;
}
