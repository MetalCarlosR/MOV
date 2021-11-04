package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Cell {

    public int getRadius() {
        return _radius;
    }

    public double getScale() {
        return _scale;
    }

    public void setRadius(int r)
    {
        _radius=r;
    }
    public void setScale(double s)
    {
        _scale = s;
    }

    enum State{ Grey, Blue, Red }


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

    public void draw(int x, int y, int r, double scale, IGraphics graphics, IImage lock, IFont font, int color) {
        Cell.State state = getState();

        if(_focus)
        {
            int ring = 2;

            graphics.setColor(0xFF000000);
            graphics.fillCircle(x,y,(r+ring));
        }
        graphics.setColor(color);
        graphics.fillCircle(x,y,r);

        if(getState() == Cell.State.Blue && isLocked())
        {
            graphics.setColor(0xFFFFFFFF);
            graphics.setFont(font);
            graphics.drawText(Integer.toString(getNeigh()), x,y,scale);
        }
        else if(getState() == Cell.State.Red && isLocked())
        {
            graphics.setOpacity(0.2f);
            graphics.drawImage(lock, x,y,(float)(0.65f*scale),(float)(0.65f*scale));
            graphics.setOpacity(1.0f);
        }
    }

    public void draw(int x, int y, int r, double scale, IGraphics graphics, IImage lock, IFont font) {
        Cell.State state = getState();

        draw(x,y,r,scale,graphics,lock,font,state == Cell.State.Blue ?0xFF1CC0E0 : state == Cell.State.Red ? 0xFFFF384B : 0xFFEEEEEE);
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
    public void unfocus() {
        _focus = false;
    }
    public void focus(){
        _focus = true;
    }
    public void setNeigh(int n)
    {
        _neigh=n;
    }
    private int _neigh = 0;
    private boolean _locked = false;
    private boolean _focus = false;
    int _x = 0;
    int _y = 0;
    private  int _radius;
    private double _scale;

    private State _state = State.Grey;
}
