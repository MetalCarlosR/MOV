package es.ucm.fdi.mov.deleto.p1.logic;

public class OhY3s {
    public OhY3s(){

    }

    public void newGame(int size){
        _grid = new Grid(size);
    }

    public void click(int x, int y){
        _grid.changeState(x, y);
    }

    public String getTip(){
        return "Activate monke mode";
    }

    public void draw(){
        _grid.draw();
    }

    private Grid _grid;
    private UIBar _bar;
    // TO DO:
    // private Text _title;
}