package es.ucm.fdi.mov.deleto.p1.logic;

public class OhY3s {
    public OhY3s(){

    }

    public void newGame(int size){
        _grid = new Grid(size);
    }

    public void draw(){
        _grid.draw();
    }

    private Grid _grid;
    // TO DO:
    // private Text _title;
    // private UIBar _bar;
}