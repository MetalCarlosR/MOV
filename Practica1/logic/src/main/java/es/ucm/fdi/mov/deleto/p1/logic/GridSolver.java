package es.ucm.fdi.mov.deleto.p1.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.stream.IntStream;

import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public class GridSolver {

    public final Vector<Cell> _fixedCells = new Vector<Cell>();
    public final Vector<Cell> _visibleCells = new Vector<Cell>();
    public final Vector<Cell> _isolated = new Vector<Cell>();

    public ArrayList<Vec2<Integer>> _dirs = new ArrayList<>();

    public int _freeCells = 0;

    Grid _grid;

    public GridSolver(Grid grid){
        _dirs.add(new Vec2<Integer>(-1,0));
        _dirs.add(new Vec2<Integer>(1,0));
        _dirs.add(new Vec2<Integer>(0,-1));
        _dirs.add(new Vec2<Integer>(0,1));
        _grid = grid;
    }

    public void init(){
        GridGenerator.Generate(this);
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

        Clue worstClue = null;

        Vec2<Integer> sel = new Vec2<>(-1,-1);
        for(Cell c : fixed){
            int visibleNeigh = getVisibleNeighs(c);
            // First mistake, a cell sees too much neighbours {4.}
            if(visibleNeigh > c.getNeigh())
                return new Clue(c,"This number sees a bit too much", null);
            // Already can see all its neighbours and has remaining open paths {1.}
            else if(visibleNeigh == c.getNeigh() && getNumPossibleDirs(c, sel)>0)
                return new Clue(c,"This number can see all its dots\n ",new Cell(sel.x(), sel.y(), Cell.State.Red));
            //Not enough neighbours and only one direction remains {8.}
            else if(getNumPossibleDirs(c, sel) == 1)
                return new Clue(c,"Only one direction remains for\nthis number to look in", new Cell(sel.x(),sel.y(), Cell.State.Blue));
            // Growing in this direction would exceed the cell neighbours {2.}
            Vec2<Integer> impossibleCell =  getPossibleNeighs(c);
            if (impossibleCell!=null)
                return new Clue(c, "Looking further in one direction\nwould exceed this number",new Cell(impossibleCell.x(),impossibleCell.y(), Cell.State.Red));

            Vec2<Integer> obvious = getDirForObviousBlueDot(c);
            // 3. y 9. hay un punto que tiene que ser clicado si o si
            if(obvious != null )
                return new Clue(c,"One specific dot is included\nin all solutions imaginable",new Cell(obvious.x(),obvious.y(), Cell.State.Blue));

            // Last possible clue, only given in case of player mistake {5.}
            else worstClue = new Clue(c,"This number can't see enough",null);
        }

        // Isolated clues can only be red  {6. y 7.}
        for (Cell c: isolated) {
            Cell r = new Cell(c._x,c._y, Cell.State.Red);
            if(c.getState() == Cell.State.Grey)
                return new Clue(c,"This one should be easy...", r);
            else if (c.getState() == Cell.State.Blue)
                return new Clue(c,"A blue dot should always see at least one other",r);
        }

        //If no better clue has been found we return one of the errors stored in the worstClue
        return worstClue;
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

    // TODO: We can optimize by computing this only once and updating every state change only on afected cells
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
    public int getNumPossibleDirs(Cell c, Vec2<Integer>out){
        int n = 0;

        for (Vec2<Integer> d: _dirs) {
            Vec2<Integer> p = getPossibleGrowthInDir(c, d);
            if(p!=null)
            {
                if(n == 0 && out!=null)
                    out.setXY(p.x(),p.y());
                n++;
            }
        }
        return n;
    }

    public Vec2<Integer> getDirForObviousBlueDot(Cell c){
        int neigh [] = new int[_dirs.size()];
        int id = 0;
        int sum = 0;

        Vec2<Integer>obvious = new Vec2<>(-1,-1);

        //We save all the possible neighbours we can get in each dir in an local array.
        //accumulate the sum of all and save the direction with the largest amount of possible neighs.
        for (int i = 0; i<_dirs.size();i++) {
            neigh[i] = Math.min(c.getNeigh(),getPossibleNeighInDir(c, _dirs.get(i)));
            sum += neigh[i];
            if(neigh[id] < neigh[i]){ id = i; }
        }

        //We subtract all the possible neighbours to the max direction.
        // (Times 2 because we will subtract it from it self in the next loop)
        int max = neigh[id]*2;
        for(int v: neigh){
            max -= v;
        }

        //We return the max direction only if it has neighs and won't exceed the maximum
        if((max >= 0 && sum <= (c.getNeigh()-getVisibleNeighs(c))))
            return getPossibleGrowthInDir(c,_dirs.get(id));
            //If we make getPossibleNeighInDir take an out parameter and save it this search could be optimized out
        else
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

    public Vector<Cell> getIsolatedCells(){
        Vector<Cell> ret = new Vector<Cell>();
        for(Cell c : _isolated){
            if(c.getState() != Cell.State.Red) ret.add(c);
        }
        return ret;
    }


    public boolean isIsolated(Cell c)
    {
        for(Vec2<Integer> d:_dirs)
        {
            Cell next = _grid.getCell(c._x+d.x(), c._y+d.y());
            if(next!=null && next.getState() != Cell.State.Red)
                return  false;
        }
        return true;
    }

}