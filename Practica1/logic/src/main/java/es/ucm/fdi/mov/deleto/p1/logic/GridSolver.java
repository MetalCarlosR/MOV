package es.ucm.fdi.mov.deleto.p1.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public class GridSolver {

    public final Vector<Cell> _fixedCells = new Vector<Cell>();
    public final Vector<Cell> _visibleCells = new Vector<Cell>();
    public final Vector<Cell> _isolated = new Vector<Cell>();

    public final List<Vec2<Integer>> _dirs = Arrays.asList(new Vec2<Integer >(-1, 0),
                                                           new Vec2<Integer>(1 , 0),
                                                           new Vec2<Integer>(0 ,-1),
                                                           new Vec2<Integer>(0 , 1));

    public int _freeCells = 0;
    Grid _grid;

    /**
     * GridSolver constructor
     * @param grid the associated grid for this solver
     */
    public GridSolver(Grid grid){
        _grid = grid;
    }

    /**
     * Generates the grid using this solver to guarantee its possible to solve it
     */
    public void generateLevel(){
        GridGenerator.Generate(this);
    }

    /**
     * Using the utility functions creates a Clue object with a cell that can be improved towards
     * a complete board, the necessary change to make to the board and a descriptive message.
     *
     * @return the first 'decent' clue found, if the first one has little information we try until
     *          a better one is found or we run out of clues
     */
    public Clue getClue()
    {
        //Get and shuffle all the fixed cells that can produce clues
        Vector<Cell> fixed = filterFixedCells();
        Collections.shuffle(fixed);

        //Get and shuffle all the isolated cells that can produce clues
        Vector<Cell> isolated = filterIsolatedCells();
        Collections.shuffle(isolated);

        //In case we don't find a decent clue
        Clue worstClue = null;


        // Clues described in instructions referenced inside { }
        for(Cell c : fixed){
            int visibleNeigh = visibleNeighbours(c);
            // First mistake, a cell sees too much neighbours {4.}
            if(visibleNeigh > c.getNeigh())
                return new Clue(c,"This number sees a bit too much", null);
            else
            {
                //Selected coordinates to create the solved state for clues more easily
                Vec2<Integer> sel = new Vec2<>(-1,-1);

                // Already can see all its neighbours and has remaining open paths {1.}
                int openDirs = openDirections(c, sel);
                if(visibleNeigh == c.getNeigh() && openDirs>0)
                    return new Clue(c,"This number can see all its dots\n\01",new Cell(sel.x(), sel.y(), Cell.State.Red));

                //Not enough neighbours and only one direction remains {8.}
                else if(openDirs == 1)
                    return new Clue(c,"Only one direction remains for\nthis number to look in", new Cell(sel.x(),sel.y(), Cell.State.Blue));
            }

            // Growing in this direction would exceed the cell neighbours {2.}
            Vec2<Integer> impossibleCell =  canDiscardDirection(c);
            if (impossibleCell!=null)
                return new Clue(c, "Looking further in one direction\nwould exceed this number",new Cell(impossibleCell.x(),impossibleCell.y(), Cell.State.Red));

            Vec2<Integer> contained = containedInAllSolutions(c);
            // {3. y 9.}
            if(contained != null )
                return new Clue(c,"One specific dot is included\nin all solutions imaginable",new Cell(contained.x(),contained.y(), Cell.State.Blue));

            // Last possible clue, only given in case of player mistake {5.}
            else worstClue = new Clue(c,"This number can't see enough",null);
        }

        // Isolated clues can only be red  {6. y 7.}
        for (Cell c: isolated) {
            return new Clue(c,(c.getState() == Cell.State.Grey) ?
                        "This one should be easy...":"A blue dot should always see at least one other",
                        new Cell(c._col,c._row, Cell.State.Red));
        }

        //If no better clue has been found we return one of the errors stored in the worstClue
        return worstClue;
    }

    /**
     * Checks whether the current grid is solved or not
     * @return if its already solved or not
     */
    public boolean solved() {
        return  getClue() == null && filterFixedCells().size() == 0 && filterIsolatedCells().size() == 0;
    }

    /**
     * Resets fixed, visible vectors amd sets freeCell count to zero
     */
    public void reset(){
        _freeCells = 0;
        _fixedCells.clear();
        _visibleCells.clear();
    }


    /**
     * Returns all visible (connected) blue dots from a given cell in a given direction
     * @param c the cell to start looking from
     * @param d the direction to follow
     * @return the amount of neighbours
     */
    private int visibleNeighboursInDirection(Cell c, Vec2<Integer> d){
        int n = -1;
        while( c !=null  && c.getState() == Cell.State.Blue) {
            c = _grid.getCell(c._col + d.x(), c._row + d.y());
            n++;
        }
        return n;
    }

    /**
     * Returns the maximum amount of neighbours a given cell can have on a given direction
     * if all its visible grey cells in the path where to be changed to blue
     * @param c the cell to start looking from
     * @param d the direction to follow
     * @return the amount of possible neighbours
     */
    private int potentialNeighboursInDirection(Cell c, Vec2<Integer> d){
        int n = -1;
        while( c !=null  && c.getState() != Cell.State.Red) {
            c = _grid.getCell(c._col + d.x(), c._row + d.y());
            n++;
        }
        return n;
    }

    /**
     * Tries to find the first grey cell from the given cell following the given direction
     * @param c the cell to start looking from
     * @param d the direction to follow
     * @return the position of the first found grey or null otherwise.
     */
    private Vec2<Integer> firstFreeInDirection(Cell c, Vec2<Integer> d){
        c = _grid.getCell(c._col + d.x(),c._row + d.y());
        while( c!= null ){
            if(c.getState() == Cell.State.Grey)
                return  new Vec2<Integer>(c._col,c._row);
            else if(c.getState() == Cell.State.Red)
                return  null;
            c = _grid.getCell(c._col + d.x(),c._row + d.y());
        }
        return null;
    }


    /**
     * Computes all visible neighbours of a given cell in all possible directions
     * @param c the cell to inspect
     * @return the amount of visible neighbours
     */
    public int visibleNeighbours(Cell c){
        int n = 0;
        for (Vec2<Integer> d: _dirs) {
            n+= visibleNeighboursInDirection(c, d);
        }
        return n;
    }

    /**
     * Checks if growing in one direction would exceed the amount of neighbours of a given cell
     * @param c the cell to check
     * @return the cell we need to turn red or null if no such direction found
     */
    public Vec2<Integer> canDiscardDirection(Cell c){
        if(c.getState() != Cell.State.Blue)
            return null;

        int visible = visibleNeighbours(c);
        for (Vec2<Integer> d: _dirs) {
            int i = 1;
            Cell ghostCell = null;
            do {
                ghostCell = _grid.getCell(c._col + (d.x()*i), c._row + (d.y()*i));
                if(ghostCell == null)
                    break;
                if(ghostCell.getState() == Cell.State.Grey)
                {
                    int n = 0;
                    ghostCell.setState(Cell.State.Blue);
                    n = visibleNeighboursInDirection(ghostCell, d);
                    ghostCell.setState(Cell.State.Grey);
                    if(n + visible + 1 > c.getNeigh())
                        return new Vec2<Integer>(ghostCell._col,ghostCell._row);
                    break;
                }
                else if(ghostCell.getState()== Cell.State.Red)
                    break;
                i++;
            }while (true);
        }
        return null;
    }

    /**
     * Calculates the number of possible directions to grow, i.e with grey cells and no blocking red cells.
     * @param c the cell to inspect
     * @param out if given, the last grey cell coordinates found will be set. This way we save having to search it later
     * @return the amount of open paths
     */
    public int openDirections(Cell c, Vec2<Integer>out){
        int n = 0;
        Vec2<Integer> first;
        for (Vec2<Integer> d: _dirs) {
            first = firstFreeInDirection(c, d);
            if(first != null)
            {
                n++;
                if(out!=null)
                    out.setXY(first.x(),first.y());
            }
        }
        return n;
    }

    /**
     * Tries to find an accessible grey cell that is included in all possible solutions
     * @param c the cell to inspect
     * @return such cell coordinates if found, null otherwise
     */
    public Vec2<Integer> containedInAllSolutions(Cell c){
        int[] neigh = new int[_dirs.size()];
        int id = 0;
        int sum = 0;

        //We save all the possible neighbours we can get in each dir in an local array.
        //accumulate the sum of all and save the direction with the largest amount of possible neighs.
        for (int i = 0; i<_dirs.size();i++) {
            neigh[i] = Math.min(c.getNeigh(), potentialNeighboursInDirection(c, _dirs.get(i)));
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
        if((max >= 0 && sum <= (c.getNeigh()- visibleNeighbours(c))))
            return firstFreeInDirection(c,_dirs.get(id));
            //If we make potentialNeighboursInDirection take an out parameter and save it this search may be optimized out
        else
            return null;
    }

    /**
     * Fixed cell getter, only used to find clues so we filter out correct fixed cells
     * @return filtered fixed cells
     */
    private Vector<Cell> filterFixedCells(){
        Vector<Cell> ret = new Vector<Cell>();
        for(Cell c : _fixedCells){
            if(((visibleNeighbours(c) != c.getNeigh()) || canDiscardDirection(c)!=null)
                    && c.getState() != Cell.State.Red)
                ret.add(c);
        }
        return  ret;
    }

    /**
     * Isolated cell getter, only used to find clues so we filter out correct isolated cells
     * @return filtered isolated cells
     */
    private Vector<Cell> filterIsolatedCells(){
        Vector<Cell> ret = new Vector<Cell>();
        for(Cell c : _isolated){
            if(c.getState() != Cell.State.Red) ret.add(c);
        }
        return ret;
    }



}