package es.ucm.fdi.mov.deleto.p1.logic;

import java.util.ArrayDeque;
import java.util.Deque;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Grid {

    static int PADDING = 10;
    static final int BORDER = 30;

    private Cell[][] _cells;

    private int _size = 0;
    private int _percentage = 0;
    private int _clicked = 0;

    private IGraphics _G;

    int _originX;
    int _originY;

    public Cell debugCell;

    private  GridSolver _gridSolver;

    private final Deque<Cell> undoStack = new ArrayDeque<>();

    private double _actualTransitionTime;
    private boolean _startTransition = false;
    private ICallable _onTransition;

    public Grid(){
        _startTransition = false;
        _gridSolver = new GridSolver(this);
    }

    public void init(int size)
    {
        _size = size;
        _cells = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                _cells[i][j] = new Cell(j,i, Cell.State.Red);
            }
        }

        _gridSolver.init();

        _percentage = 0;
    }

    public void setGraphics(IGraphics graphics) {
        _G = graphics;
        int logicWidth = graphics.getLogicWidth();
        int logicHeight = graphics.getLogicHeight();

        PADDING = 40/_size;
        int r = ((logicWidth -BORDER) -_size* PADDING)/(_size*2);
        _originX = (PADDING /2 + BORDER/2);
        _originY = logicHeight / 8 + BORDER;

        for(int i = 0; i < _size; i++) {
            int y = (_originY+(i)*(r*2)+ PADDING *i)+r;
            for(int j = 0; j < _size; j++)
            {
                int x = (_originX+(j)*(r*2)+ PADDING *j)+r;
                getCell(j,i).setTransform(x,y,r,r/((double)(logicWidth -4* PADDING)/(4*2)));
            }
        }
    }


    public int getPercentage(){
        return _percentage;
    }

    public boolean processClick(int x, int y)
    {
        int r = getCell(0,0).getRad();

        if( y >= _originY &&
            y <  _originY+(_size*(2*(r+PADDING))) &&
            x >= _originX &&
            x < (400 - (PADDING+BORDER)/2))
        {
            int widthEach = (2*r)+PADDING;
            int heightEach = widthEach;
            int arrayX = (x - _originX)/widthEach;
            int arrayY = (y - _originY)/heightEach;

            if (getCell(arrayX,arrayY).clicked(x ,y))
                    clickCell(arrayX,arrayY);
            return true;
        }
        return  false;
    }

    public void draw(IFont font, IImage lock){
        for(int i = 0; i < _size; i++) {
            for(int j = 0; j < _size; j++)
            {
                if(getCell(j,i) == debugCell)
                    getCell(j,i).draw(_G, lock, font, 0xffc0c0c0);
                else
                    getCell(j,i).draw(_G, lock, font);
            }
        }

    }

    public void onUpdate(double deltaTime) {
        double opacity=1;
        if(_startTransition)
        {
            _actualTransitionTime+=deltaTime;
            //in seconds
            double TRANSITION_TARGET_DELAY = 1;
            if(_actualTransitionTime < TRANSITION_TARGET_DELAY)
                opacity = -1;
            else
            {
                //in seconds
                double TRANSITION_TARGET_TIME = 2;
                opacity = 1 - ((_actualTransitionTime- TRANSITION_TARGET_DELAY)/ TRANSITION_TARGET_TIME);
                if(opacity<= 0)
                {
                    _startTransition = false;
                    _onTransition.call();
                    return;
                }
            }
        }
        for(Cell[] fila : _cells)
            for(Cell c : fila) {
                c.onUpdate(deltaTime);
                if(_startTransition)
                    c.setOpacity(opacity);
            }
    }

    public void clickCell(int x, int y) {
        Cell c = getCell(x,y);
        if(c!=null)
        {
            undoStack.addFirst(new Cell(c._x, c._y, c.getState()));
            if(!getCell(x, y).changeState())
                getCell(x,y).showLockedGraphics();
            else {
                Cell.State state = getCell(x, y).getState();
                if(state == Cell.State.Grey)
                    _clicked--;
                else if(state == Cell.State.Blue) _clicked++;

                _percentage =  (100 * _clicked) / _gridSolver._freeCells;
            }
        }
    }

    public Clue getClue(){
        return  _gridSolver.getClue();
    }

    public Cell undoMove(){
        if(undoStack.size() == 0)
            return null;

        Cell c = undoStack.removeFirst();
        Cell cReal = getCell(c._x, c._y);

        Cell.State s = c.getState();
        cReal.setState(s);
        if(s == Cell.State.Grey)
            _clicked--;
        else if(s == Cell.State.Red)
            _clicked++;

        _percentage =  (100 * _clicked) / _gridSolver._freeCells;

        return c;
    }


    public boolean checkWin(){
        return _gridSolver.solved();
    }

    public Cell getCell(int x, int y){
        if(x < _size && x >= 0 && y < _size && y >= 0)
            return _cells[y][x];
        else return null;
    }

    public void setCell(Cell cell, int x, int y){
        if(x < _size && x >= 0 && y < _size && y >= 0)
            _cells[y][x] = cell;
    }

    public int getSize() {return _size; };


    public void setTransition(ICallable onTransition) {
        _startTransition = true;
        _onTransition = onTransition;
    }

}