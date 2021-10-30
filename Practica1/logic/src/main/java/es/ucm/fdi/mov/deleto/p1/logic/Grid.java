package es.ucm.fdi.mov.deleto.p1.logic;

import com.sun.tools.javac.util.Pair;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Stack;
import java.util.Vector;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;

public class Grid {
    public Grid(int size){
        _size = size;
        _cells = new Cell[size][size];
        initializeGrid();
    }

    private void initializeGrid() {
        _freeCells = 0;
        _clicked = 0;
        for(int i = 0; i < _size; i++) {
            for(int j = 0; j < _size; j++)
                _cells[i][j] = new Cell(j, i, 0,true);
        }
        File myObj = new File("./Assets/examples/ex2.txt");
        try (Scanner myReader = new Scanner(myObj)){
            int i = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] pairs = data.split(" ");
                for(int j = 0; j < _size; j++) {
                    _cells[i][j].setCell(pairs[j]);
                    if(!_cells[i][j].isLocked())
                        _freeCells++;
                    else _fixedCells.add(_cells[i][j]);
                }
                i++;
            }

            for(i = 0; i < _size; i++){
                for(int j = 0; j < _size; j++){
                    boolean isolated = true;
                    // si esta vacia
                    if(_cells[i][j].getNeigh() == 0){
                        //mira en las 4 direcciones si alguien la ve
                        if(i-1 >= 0 && _cells[i-1][j].getNeigh() > 0
                        || j-1 >= 0 && _cells[i][j-1].getNeigh() > 0
                        || i+1 < _size && _cells[i+1][j].getNeigh() > 0
                        || j+1 < _size && _cells[i][j+1].getNeigh() > 0
                        )
                            isolated = false;
                        if(isolated)
                            _isolated.add(_cells[i][j]);
                    }
                }
            }
        }
        catch (FileNotFoundException e){
            // TO DO: hacemos esto really?
            System.out.println("Couldn't open the file");
            System.out.println(System.getProperty("user.dir"));
            System.out.println("Generating one by default\n");
            e.printStackTrace();
        }
    }

    public void _draw(IGraphics graphics, IFont font){
        int padding = 5;
        int r = (graphics.getWidth()-padding*_size)/(_size*2);

        int x = (r-padding)/2;
        int y = 80;



        graphics.setColor(0xFF000000);
        graphics.fillRect(0,0,10,10);
        graphics.fillRect(400-10,600-10,10,10);

        for(int i = 0; i < _size; i++) {
            for(int j = 0; j < _size; j++)
            {
                char ch = ' ';
                Cell cel = _cells[i][j];
                Cell.State state = cel.getState();

                if (cel.isLocked()){
                    if(cel.getNeigh() == 0){
                        ch = 'X';
                    }
                    else ch = (char)('0' + cel.getNeigh());
                }
                else{
                    if(cel.getState() == Cell.State.Blue)
                        ch = 'O';
                    else if(cel.getState() == Cell.State.Red)
                        ch = '+';
                }
                graphics.setColor(state == Cell.State.Blue ?0xFF1CC0E0 : state == Cell.State.Red ? 0xFFFF384B : 0xFFEEEEEE);
                graphics.fillCircle(x+(j)*(r*2+padding)+padding,
                                    y+(i)*(r*2+padding)+padding,
                                     r);
                graphics.setColor(0xFFFFFFFF);
                if(cel.getState() == Cell.State.Blue && cel.isLocked())
                    graphics.drawText(Integer.toString(cel._neigh),
                            x+(j)*(r*2+padding)+padding+(r-12)/2,
                            y+(i)*(r*2+padding)+padding+(r+14)/2 );
            }
            //System.out.println("|");
        }
        for(int j = 0; j < _size; j++)
        {
            //System.out.print("+---");
        }
        //System.out.println("+");

//        System.out.println(_percentage + "%");
        graphics.setColor(0xFF000000);
        graphics.drawText(Integer.toString(_percentage)+"%",(graphics.getWidth()/2)-12,540);
    }

    public void draw(){
        for(int i = 0; i < _size; i++) {
            for(int j = 0; j < _size; j++)
            {
                System.out.print("+---");
            }
            System.out.println("+");
            for(int j = 0; j < _size; j++)
            {
                char ch = ' ';
                Cell cel = _cells[i][j];
                if (cel.isLocked()){
                    if(cel.getNeigh() == 0){
                        ch = 'X';
                    }
                    else ch = (char)('0' + cel.getNeigh());
                }
                else{
                    if(cel.getState() == Cell.State.Blue)
                        ch = 'O';
                    else if(cel.getState() == Cell.State.Red)
                        ch = '+';
                }
                System.out.print("| " + ch + " ");
            }
            System.out.println("|");
        }
        for(int j = 0; j < _size; j++)
        {
            System.out.print("+---");
        }
        System.out.println("+");

        System.out.println(_percentage + "%");
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

    // TO DO: bruh
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

    // TO DO: Optimizable
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