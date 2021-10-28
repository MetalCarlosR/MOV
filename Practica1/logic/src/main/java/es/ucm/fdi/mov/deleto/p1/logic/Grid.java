package es.ucm.fdi.mov.deleto.p1.logic;

import com.sun.tools.javac.util.Pair;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Vector;

public class Grid {
    public Grid(int size){
        _size = size;
        _cells = new Cell[size][size];
        initializeGrid();
    }

    private void initializeGrid() {
        int notFixed = 0;
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
                        notFixed++;
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

            if(notFixed > 0)
                _percentInc = 100.0f / notFixed;
        }
        catch (FileNotFoundException e){
            // TO DO: hacemos esto really?
            System.out.println("Couldn't open the file");
            System.out.println(System.getProperty("user.dir"));
            System.out.println("Generating one by default\n");
            e.printStackTrace();
        }
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

    public boolean changeState(int x, int y){
        Cell.State empty = getCell(x, y).getState();
        if(!getCell(x, y).changeState())
            System.out.println("Couldn't click on " + x + " " + y);
        else {
            if(empty == Cell.State.Grey)
                _percentage += _percentInc;
            else if(empty == Cell.State.Red)_percentage -= _percentInc;
        }

        return checkWin();
    }

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

        List<Pair<Integer, Integer>> dirs = new ArrayList<>();
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

        List<Pair<Integer, Integer>> dirs = new ArrayList<>();
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

        List<Pair<Integer, Integer>> dirs = new ArrayList<>();
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

        List<Pair<Integer, Integer>> dirs = new ArrayList<>();
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

    public List<Cell> getTipCells(){
        //List<Cell> ret = new Vector<Cell>(_fixedCells);
        List<Cell> ret = new Vector<Cell>();
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

    private List<Cell> _fixedCells = new Vector<Cell>();
    private List<Cell> _isolated = new Vector<Cell>();
    private int _size = 0;
    private int _percentage = 0;
    private float _percentInc = 0;
    private int _mistakes = 0;
}