package es.ucm.fdi.mov.deleto.p1.logic;

import com.sun.tools.javac.util.Pair;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Stack;
import java.util.Vector;

import javax.security.auth.login.LoginException;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Grid {

    static final int PADDING = 10;

    public Grid(int size){
        _size = size;
        _cells = new Cell[size][size];
        initializeGrid();
    }

    private void initializeGrid() {
        _freeCells = 0;
        _clicked = 0;
        File inputFile = new File("./Assets/examples/ex2.txt");
        try (Scanner reader = new Scanner(inputFile)){
            int i = -1;
            while (reader.hasNextLine() && ++i < _size) {
                String data = reader.nextLine();
                String[] cellDefinitions = data.split(" ");
                if(cellDefinitions.length != _size)
                    throw new RuntimeException("Malformed map was given");

                for(int j = 0; j < _size; j++) {
                    Cell c = new Cell(cellDefinitions[j]);
                    _cells[i][j] = c;

                    if(c.isLocked())
                        _fixedCells.add(c);
                }
            }
            _freeCells = (_size*_size)-_fixedCells.size();

            for(i = 0; i < _size; i++){
                for(int j = 0; j < _size; j++){
                    //If it neighs count its 0
                    if(_cells[i][j].getNeigh() == 0){
                        //Checks if any adjacent cells can see it
                        if(    getCell(i-1,j).getNeigh() > 0 || getCell(i,j-1).getNeigh() > 0 ||
                               getCell(i+1,j).getNeigh() > 0 || getCell(i,j+1).getNeigh() > 0 )
                            continue;
                        else
                            _isolated.add(_cells[i][j]);
                    }
                }
            }
        }
        catch (FileNotFoundException e){
            // TODO: hacemos esto really?
            e.printStackTrace();
            System.err.println("Couldn't open the file");
            System.err.println(System.getProperty("user.dir"));
            System.err.println("Generating one by default\n");
            throw new RuntimeException("Map generation not implemented yet");
        }
    }

    public int getPercentage(){
        return _percentage;
    }

    public boolean processClick(int x, int y)
    {
        int r = (400-_size* PADDING)/(_size*2);
        int originX = (PADDING /2);
        int originY = 600/8;
        if( y >= originY &&
            y <  originY+(600 - (_size*(r+PADDING))) &&
            x >= originX &&
            x < (400 - PADDING))
        {
            int xX = (x)/(2*r+PADDING);
            int yY = (y-originY)/(2*r+PADDING);

            int cX = originX+(xX)*(r*2)+ PADDING *xX+r;
            int cY = originY+(yY)*(r*2)+ PADDING *yY+r;

            if((x-originX < cX+r && x-originX > cX-r) && (y-PADDING < cY + r && y-PADDING> cY-r))
            {
                clickCell(xX,yY);
                return  true;
            }
            return false;
        }
        return false;
    }

    public void draw(IGraphics graphics, IFont font, IImage lock, Cell focus){
        int r = (graphics.getWidth()-_size* PADDING)/(_size*2);
        int originX = (PADDING /2);
        int originY = graphics.getHeight()/8;

        graphics.setColor(0xFF000000);
        graphics.fillRect(5,5,10,10);
        graphics.fillRect(400-5,600-5,10,10);


        for(int i = 0; i < _size; i++) {
            int y = (originY+(i)*(r*2)+ PADDING *i)+r;
            for(int j = 0; j < _size; j++)
            {
                Cell cel = _cells[i][j];
                Cell.State state = cel.getState();

                int x = (originX+(j)*(r*2)+ PADDING *j)+r;

                if(cel == focus)
                {
                    int ring = (int)(r*0.15f);

                    graphics.setColor(0xFF000000);
                    graphics.fillCircle(x,y,(int)(r+ring));
                }
                graphics.setColor(state == Cell.State.Blue ?0xFF1CC0E0 : state == Cell.State.Red ? 0xFFFF384B : 0xFFEEEEEE);
                graphics.fillCircle(x,y,r);

                if(cel.getState() == Cell.State.Blue && cel.isLocked())
                {
                    graphics.setColor(0xFFFFFFFF);
                    graphics.setFont(font);
                    graphics.drawText(Integer.toString(cel._neigh), x,y);
                }
                else if(cel.getState() == Cell.State.Red && cel.isLocked())
                {
                    graphics.setOpacity(0.2f);
                    graphics.drawImage(lock, x,y,0.65f,0.65f);
                    graphics.setOpacity(1.0f);
                }
            }
        }

    }
    public boolean clickCell(int x, int y){
        Cell c = getCell(x,y);
        undoStack.push(new Pair<>(c,c.getState()));
        return changeState(x, y);
    }

    public boolean undoMove(){
        if(undoStack.empty())
            return false;

        Pair<Cell, Cell.State> c = undoStack.pop();

        c.fst.setState(c.snd);
        if(c.snd == Cell.State.Grey)
            _clicked--;
        else if(c.snd == Cell.State.Red)
            _clicked++;

        _percentage =  (100 * _clicked) / _freeCells;

        return  true;
    }

    public boolean changeState(int x, int y){
        if(!getCell(x, y).changeState())
            System.out.println("Couldn't click on " + x + " " + y);
        else {
            Cell.State state = getCell(x, y).getState();
            if(state == Cell.State.Grey)
                _clicked--;
            else if(state == Cell.State.Blue) _clicked++;

            _percentage =  (100 * _clicked) / _freeCells;
        }

        return checkWin();
    }

    // TODO: bruh
    private boolean checkWin(){
        if(_percentage >= 99 && _mistakes == 0){
            return true;
        }
        return false;
    }

    public Cell getCell(int x, int y){
        if(x < _size && x >= 0 && y < _size && y >= 0)
            return _cells[y][x];
        else return null;
    }

    public int getSize() {return _size; };

    // devuelve todos los azules ya visibles de una cell
    private int getVisibleNeighInDir(Cell c, Pair<Integer,Integer> d){
        int n = 0;
        int x = c._x + d.fst;
        int y = c._y + d.snd;
        while(x < _size && x >= 0
                && y < _size && y >= 0){
            if(getCell(x,y).getState() != Cell.State.Blue)
                break;
            n++;
            x+= d.fst;
            y+= d.snd;
        }

        return n;
    }

    // si pongo todos los grises de una direccion a azul tendria n neighbours
    private int getPossibleNeighInDir(Cell c, Pair<Integer, Integer> d){
        int n = 0;
        int x = c._x + d.fst;
        int y = c._y + d.snd;
        while(x < _size && x >= 0
                && y < _size && y >= 0){
            if(getCell(x,y).getState() == Cell.State.Red)
                break;
            n++;
            x+= d.fst;
            y+= d.snd;
        }

        return n;
    }

    // devuelve si hay un gris en una direccion
    private boolean getPossibleGrowthInDir(Cell c, Pair<Integer, Integer> d){
        int x = c._x + d.fst;
        int y = c._y + d.snd;
        while(x < _size && x >= 0
                && y < _size && y >= 0){
            if(getCell(x,y).getState() == Cell.State.Grey)
                return true;
            x+= d.fst;
            y+= d.snd;
        }
        return false;
    }

    // TODO: Optimizable
    // devuelve el numero de cells azules visibles
    public int getVisibleNeighs(Cell c){
        int n = 0;

        ArrayList<Pair<Integer, Integer>> dirs = new ArrayList<>();
        dirs.add(new Pair<Integer, Integer>(-1,0));
        dirs.add(new Pair<Integer, Integer>(1,0));
        dirs.add(new Pair<Integer, Integer>(0,-1));
        dirs.add(new Pair<Integer, Integer>(0,1));

        for (Pair<Integer, Integer> d: dirs) {
            n+= getVisibleNeighInDir(c, d);
        }

        return n;
    }

    // devuelve si en alguna direccion, al añadir uno, superas el numero de vecinos
    public boolean getPossibleNeighs(Cell c){
        int n = 0;
        int visible = getVisibleNeighs(c);

        ArrayList<Pair<Integer, Integer>> dirs = new ArrayList<>();
        dirs.add(new Pair<Integer, Integer>(-1,0));
        dirs.add(new Pair<Integer, Integer>(1,0));
        dirs.add(new Pair<Integer, Integer>(0,-1));
        dirs.add(new Pair<Integer, Integer>(0,1));

        for (Pair<Integer, Integer> d: dirs) {
            Cell ghostCell = getCell(c._x + d.fst, c._y + d.snd);
            if(ghostCell != null){
                n = getVisibleNeighInDir(ghostCell, d);
                if(n + 1 + visible > c.getNeigh())
                    return true;
            }
        }

        return false;
    }

    // devuelve el numero de direcciones posibles en las que crecer
    public int getNumPossibleDirs(Cell c){
        int n = 0;

        ArrayList<Pair<Integer, Integer>> dirs = new ArrayList<>();
        dirs.add(new Pair<Integer, Integer>(-1,0));
        dirs.add(new Pair<Integer, Integer>(1,0));
        dirs.add(new Pair<Integer, Integer>(0,-1));
        dirs.add(new Pair<Integer, Integer>(0,1));

        for (Pair<Integer, Integer> d: dirs) {
            if(getPossibleGrowthInDir(c, d))
                n++;
        }

        return n;
    }

    public Pair<Integer, Integer> getDirForObviousBlueDot(Cell c){
        Pair <Integer, Integer> dir = null;

        ArrayList<Pair<Integer, Integer>> dirs = new ArrayList<>();
        dirs.add(new Pair<Integer, Integer>(-1,0));
        dirs.add(new Pair<Integer, Integer>(1,0));
        dirs.add(new Pair<Integer, Integer>(0,-1));
        dirs.add(new Pair<Integer, Integer>(0,1));

        int neigh [] = new int[dirs.size()];
        int max = 0;
        int id = 0;
        int sum = 0;

        int i = 0;
        for (Pair<Integer, Integer> d: dirs) {
            neigh[i] = getPossibleNeighInDir(c, d);
            if(max < neigh[i]){
                max = neigh[i];
                id = i;
            }
            sum += neigh[i];
            i++;
        }

        max *= 2;

        for(i = 0; i < dirs.size(); i++){
            max -= neigh[i];
        }

        if(max > 0 || max == 0 && sum == c.getNeigh())
            dir = dirs.get(id);
        return dir;

    }

    public Vector<Cell> getTipCells(){
        //List<Cell> ret = new Vector<Cell>(_fixedCells);
        Vector<Cell> ret = new Vector<Cell>();
        for(Cell c : _fixedCells){
            if(getVisibleNeighs(c) != c.getNeigh() && c.getState() != Cell.State.Red)
                ret.add(c);
        }
        for(Cell c : _isolated){
            if(c.getState() != Cell.State.Red)
                ret.add(c);
        }
        return ret;
    };

    private Cell[][] _cells;

    private Vector<Cell> _fixedCells = new Vector<Cell>();
    private Vector<Cell> _isolated = new Vector<Cell>();
    private int _size = 0;
    private int _percentage = 0;
    private int _freeCells = 0;
    private int _clicked = 0;
    private int _mistakes = 0;

    private Stack<Pair<Cell, Cell.State>> undoStack = new Stack<>();
}