package es.ucm.fdi.mov.deleto.p1.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public class GridSolver {

    private final Vector<Cell> _fixedCells = new Vector<Cell>();
    private final Vector<Cell> _visibleCells = new Vector<Cell>();
    private final Vector<Cell> _isolated = new Vector<Cell>();

    public ArrayList<Vec2<Integer>> _dirs = new ArrayList<>();

    private int _freeCells = 0;

    Grid _grid;

    public GridSolver(Grid grid){
        _dirs.add(new Vec2<Integer>(-1,0));
        _dirs.add(new Vec2<Integer>(1,0));
        _dirs.add(new Vec2<Integer>(0,-1));
        _dirs.add(new Vec2<Integer>(0,1));
        _grid = grid;
    }

    public void init(){
        randomize(_grid.getSize());
        if(!canBeSolved()){
            reset();
            System.err.println("Couldn't generate a new map...\nLoading one from file");
            loadGridFromFile("./Assets/examples/ex2.txt");
        }
        _freeCells = 0;
        for (int i = 0; i < _grid.getSize(); i++) {
            for (int j = 0; j < _grid.getSize(); j++) {
                Cell c = _grid.getCell(i, j);
                if(isIsolated(c)){
                    _isolated.add(c);
                }
                if(!c.isLocked())
                {
                    c.setState(Cell.State.Grey);
                    _freeCells++;
                }
            }
        }
        for(Cell is : _isolated){
            if(is.isLocked()){
                is.unlock();
                is.setState(Cell.State.Grey);
                _freeCells++;
            }
        }
    }

    public void reset(){
        _freeCells = 0;
        _fixedCells.clear();
        _visibleCells.clear();
    }

    public Clue getClue()
    {
        Vector<Cell> fixed = getFixedCells();
        Collections.shuffle(fixed);

        Vector<Cell> isolated = getIsolatedCells();
        Collections.shuffle(isolated);

        Deque<Clue> clues = new ArrayDeque<>(fixed.size()+isolated.size());

        for(Cell c : fixed){
            // TODO: si se optimiza, no hace falta
            int visibleNeigh = getVisibleNeighs(c);
            // 4. ve demasiados cells
            if(visibleNeigh > c.getNeigh()){
                clues.addFirst(new Clue(c,"This number sees a bit too much", null));
                return clues.getFirst();
            }
            // 1. ya los ve todos
            else if(visibleNeigh == c.getNeigh() && getNumPossibleDirs(c)>0){
                Vec2<Integer> sel = null;
                for (Vec2<Integer> d: _dirs) {
                    if((sel=getPossibleGrowthInDir(c, d))!=null)
                        break;
                }
                clues.addFirst(new Clue(c,"This number can see all its dots\n ",new Cell(sel.x(), sel.y(), Cell.State.Red)));
                return clues.getFirst();
            }
            // 8. solo te queda una direccion en la que mirar
            else if(getNumPossibleDirs(c) == 1)
            {
                Vec2<Integer> sel = null;
                for (Vec2<Integer> d: _dirs) {
                    if((sel=getPossibleGrowthInDir(c, d))!=null)
                        break;
                }
                clues.addFirst(new Clue(c,"Only one direction remains for\nthis number to look in", new Cell(sel.x(),sel.y(), Cell.State.Blue)));
                return clues.getFirst();
            }
            // 2. si añades uno, te pasas porque pasa a ver los azules de detras
            {
                Vec2<Integer> sel =  getPossibleNeighs(c);
                if (sel!=null)
                {
                    clues.addFirst(new Clue(c, "Looking further in one direction\nwould exceed this number",new Cell(sel.x(),sel.y(), Cell.State.Red)));
                    return clues.getFirst();
                }
            }

            Cell obvious = getDirForObviousBlueDot(c);
            // 3. y 9. hay un punto que tiene que ser clicado si o si
            if(obvious != null )//&& _grid._grid.getCell(c._x+obvious.x(),c._y+obvious.y()).getState()== Cell.State.Grey
            {
                clues.addFirst(new Clue(c,"One specific dot is included\nin all solutions imaginable",new Cell(obvious._x,obvious._y, Cell.State.Blue)));
                return clues.getFirst();
            }
            // 5. no ve los suficientes
            else
            {
                if(clues.size()>0)
                    clues.addLast(new Clue(c,"This number can't see enough",null));
                else
                    clues.addFirst(new Clue(c,"This number can't see enough",null));
                continue;
            }
            // TODO: 10. ??
        }
        // 6. y 7.
        for (Cell c: isolated) {

            Cell r = new Cell(c._x,c._y, Cell.State.Red);
            if(c.getState() == Cell.State.Grey)
            {
                clues.addFirst(new Clue(c,"This one should be easy...", r));
                return clues.getFirst();
            }
            else if (c.getState() == Cell.State.Blue)
            {
                clues.addFirst(new Clue(c,"A blue dot should always see at least one other",r));
                return clues.getFirst();
            }
        }

        return clues.size() >0 ? clues.getFirst() : null;
    }

    // devuelve todos los azules ya visibles de una cell
    private int getVisibleNeighInDir(Cell c, Vec2<Integer> d){
        int n = 0;
        int x = c._x+d.x();
        int y = c._y+d.y();
        while( _grid.getCell(x,y) !=null  && _grid.getCell(x,y).getState()== Cell.State.Blue)
        {
            x+= d.x();
            y+= d.y();
            n++;
        }

        return n;
    }

    // si pongo todos los grises de una direccion a azul tendria n neighbours
    private int getPossibleNeighInDir(Cell c, Vec2<Integer> d){
        int n = 0;
        int x = c._x+d.x();
        int y = c._y+d.y();
        while(_grid.getCell(x,y) != null &&
                _grid.getCell(x,y).getState() != Cell.State.Red)
        {
            x+= d.x();
            y+= d.y();
            n++;
        }
        return n;
    }

    // devuelve si hay un gris en una direccion, su posición si no devuelve null
    private Vec2<Integer> getPossibleGrowthInDir(Cell c, Vec2<Integer> d){
        int x = c._x + d.x();
        int y = c._y + d.y();

        while(_grid.getCell(x,y) != null ){
            if(_grid.getCell(x,y).getState() == Cell.State.Grey)
                return  new Vec2<Integer>(x,y);
            else if(_grid.getCell(x,y).getState() == Cell.State.Red)
                return  null;
            x+= d.x();
            y+= d.y();
        }
        return null;
    }

    // TODO: Optimizable
    // devuelve el numero de cells azules visibles
    public int getVisibleNeighs(Cell c){
        int n = 0;
        for (Vec2<Integer> d: _dirs) {
            n+= getVisibleNeighInDir(c, d);
        }
        return n;
    }

    // devuelve si en alguna direccion, al añadir uno, superas el numero de vecinos
    public Vec2<Integer> getPossibleNeighs(Cell c){
        int n = 0;
        int visible = getVisibleNeighs(c);
        if(c.getState() != Cell.State.Blue)
            return null;
        for (Vec2<Integer> d: _dirs) {
            Cell ghostCell = null;
            int i = 1;
            for(;i<_grid.getSize();i++)
            {
                ghostCell = _grid.getCell(c._x + (d.x()*i), c._y + (d.y()*i));
                if(ghostCell!=null && ghostCell.getState() == Cell.State.Grey)
                {
                    n = getVisibleNeighInDir(ghostCell, d);
                    if(n + visible + 1 > c.getNeigh())
                        return new Vec2<Integer>(ghostCell._x,ghostCell._y);
                    break;
                }
                else if(ghostCell!= null && ghostCell.getState()== Cell.State.Red)
                    break;
            }
        }

        return null;
    }

    // devuelve el numero de direcciones posibles en las que crecer
    public int getNumPossibleDirs(Cell c){
        int n = 0;
        for (Vec2<Integer> d: _dirs) {
            if(getPossibleGrowthInDir(c, d)!=null)
                n++;
        }

        return n;
    }

    public Cell getDirForObviousBlueDot(Cell c){
        Vec2<Integer> dir = null;

        int neigh [] = new int[_dirs.size()];
        int totalVis = getVisibleNeighs(c);
        int max = 0;
        int id = 0;
        int sum = 0;

        int i = 0;
        for (Vec2<Integer> d: _dirs) {
            int vis = getVisibleNeighInDir(c, d);
            neigh[i] = getPossibleNeighInDir(c, d);
            neigh[i] = Math.min(c.getNeigh(), neigh[i]);
            if(max < neigh[i]){
                max = neigh[i];
                id = i;
            }
            sum += neigh[i];
            i++;
        }

        max *= 2;

        for(i = 0; i < _dirs.size(); i++){
            max -= neigh[i];
        }

        if((max > 0 && sum / (c.getNeigh()-totalVis) <= 1) || (max == 0 && sum == c.getNeigh()))
            dir = _dirs.get(id);

        if(dir == null)
            return null;

        Cell cell = _grid.getCell(c._x + dir.x(), c._y + dir.y());
        while(cell != null){
            if(cell.getState() == Cell.State.Grey)
                return cell;

            cell = _grid.getCell(cell._x + dir.x(), cell._y + dir.y());
        }

        return null;
    }

    public Vector<Cell> getFixedCells(){
        Vector<Cell> ret = new Vector<Cell>();
        for(Cell c : _fixedCells){
            if(((getVisibleNeighs(c) != c.getNeigh())|| getPossibleNeighs(c)!=null)&&
                    c.getState() != Cell.State.Red)//
                ret.add(c);
        }
        return  ret;
    }

    //5:35 AM - Si peta de repente el mapa -> if(c.getState() != Cell.State.Red))
    public Vector<Cell> getIsolatedCells(){
        Vector<Cell> ret = new Vector<Cell>();
        for(Cell c : _isolated){
            if(c.getState() != Cell.State.Red) ret.add(c);
        }
        return ret;
    }


    public boolean canBeSolved()
    {
        Random r = new Random();
        boolean solved = false;

        while(!solved){
            Clue clue = getClue();
            if(clue == null || clue.getCorrectState() == null){
                // si no hay pista, añadir una nueva celda gris como locked
                int rand = r.nextInt(_visibleCells.size());
                Cell c = _visibleCells.get(rand);
                c.lock();
                if(c.getNeigh() > 0)
                    c.setState(Cell.State.Blue);
                else c.setState(Cell.State.Red);
                _visibleCells.remove(rand);
                _freeCells--;
                _fixedCells.add(c);
            }
            else{
                // hacerle caso a la pista e intentar resolverlo
                int x = clue.getCorrectState()._x;
                int y = clue.getCorrectState()._y;
                Cell c = _grid.getCell(x,y);
                if(!c.isLocked()){
                    _visibleCells.remove(c);
                    c.setState(clue.getCorrectState().getState());
                }
                else{
                    System.err.println("La pista debería ser editable: ");
                    System.err.println(c._x + " " + c._y);
                    System.err.println(clue.getMessage());
                }

            }
            solved = _visibleCells.size() == 0;
        }

        Clue c = getClue();
        return c == null || c.getCorrectState() == null;
    }

    private boolean isIsolated(Cell c)
    {
        for(Vec2<Integer> d:_dirs)
        {
            Cell next = _grid.getCell(c._x+d.x(), c._y+d.y());
            if(next!=null && next.getState() != Cell.State.Red)
                return  false;
        }
        return true;
    }


    public void loadGridFromFile(String file) {
        File inputFile = new File(file);
        try (Scanner reader = new Scanner(inputFile)){
            int i = -1;
            while (reader.hasNextLine() && ++i < _grid.getSize()) {
                String data = reader.nextLine();
                String[] cellDefinitions = data.split(" ");
                if(cellDefinitions.length != _grid.getSize())
                    throw new RuntimeException("Malformed map was given");

                for(int j = 0; j < _grid.getSize(); j++) {
                    Cell c = new Cell(cellDefinitions[j],j,i);
                    _grid.setCell(c,j,i);

                    if(c.isLocked())
                        _fixedCells.add(c);
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

    public void randomize(int size)
    {
        Random r = new Random();
        int maxBlue = r.nextInt((int)(size*size*0.3)) + (int)(size*size*0.5);
//        int maxBlue = r.nextInt(size*size - 2*size);
        // hacemos esto en un numero aleatorio de veces
        // primero, elegimos aleatoriamente casillas, y le damos un numero aleatorio de vecinos
        // tenemos que actualizar los vecinos de las nuevas casillas

        // pinta maxBlue de azules
        while(maxBlue > 0){
            int x = r.nextInt(size);
            int y = r.nextInt(size);
            Cell originalCell = _grid.getCell(x, y);
            if(!tryBlue(originalCell)) {
                continue;
            }
            // para que vaya de 1 al max
            int n = r.nextInt(size-1)+1;
            ArrayList<Vec2<Integer>> list = new ArrayList<>(_dirs);
            int j = 0;
            // intenta pintar en las 4 direcciones n azules
            while(n > 0 && j < 4 && maxBlue > 0){
                int m = 0;
                if(n > 1) {
                    m = r.nextInt(n-1)+1;
                }
                else m = 1;
                Vec2<Integer> dir = list.get(j);
                //si cambio en la que estoy... lo jodo?
                // primero pinta de azul los que necesite
                int step = 1;
                while(step <= m){
                    Cell c = _grid.getCell(x+(dir.x()*step), y+(dir.y() * step));
                    //if(c == null || getCell(c._x + dir.x(), c._y + dir.y()).getNeigh() == size)
                    if(c != null && tryBlue(c)){
                        step++;
                        maxBlue--;
                    }
                    else break;
                }
                j++;
                n -= (step-1);
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = _grid.getCell(j, i);
                if(c.getState() != Cell.State.Red){
                    int n = getVisibleNeighs(c);
                    c.setNeigh(n);
                }
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = _grid.getCell(j, i);
                _visibleCells.add(c);
                _freeCells++;
                c.setState(Cell.State.Grey);
            }
        }
    }

    private boolean tryBlue(Cell c){
        c.setState(Cell.State.Blue);
        for (int i = 0; i < _dirs.size(); i++) {
            for (int j = 0; j < _grid.getSize(); j++) {
                Cell ady = _grid.getCell(c._x + _dirs.get(i).x() * j, c._y + _dirs.get(i).y() * j);
                if(ady != null){
                    if(getVisibleNeighs(ady) > _grid.getSize()){
                        c.setState(Cell.State.Red);
                        return false;
                    }
                } else break;
            }
        }
        return true;
    }

    public int getFreeCells() {
        return _freeCells;
    }
}