package es.ucm.fdi.mov.deleto.p1.logic.grid;

import java.util.ArrayDeque;
import java.util.Deque;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;
import es.ucm.fdi.mov.deleto.p1.logic.Constants;
import es.ucm.fdi.mov.deleto.p1.logic.buttons.Cell;

/**
 * Has most of the game state:
 *      Cells      -> Array of cells that compose the current game
 *      Solver     -> Helper class that generates clues for the user on demand
 *      UndoStack  -> Stack of the changes made by the user to allow going back on them
 */
public class Grid {
    /*********
     * Logic *
     *********/
    //Grid internal representation
    private final Cell[][] _cells;

    //Size of puzzle, mount of cells will be _size*_size because its always a square
    private final int _size;
    //Solution percentage
    private int _percentage;

    //GirdSolver is the one in charge of generating clues and a solvable puzzle
    private final GridSolver _gridSolver;
    private final Deque<Cell> undoStack = new ArrayDeque<>();


    //Enum for result reporting on grid clicks
    public enum ClickResult{
        MISSED,
        FREE,
        LOCKED
    }

    /************
     * Graphics *
     ************/

    //Graphic object for rendering
    private IGraphics _G;

    //Constants for Cell drawing
    static final int BORDER = 30;
    static final int BASE_PADDING = 40; //Based on a 4x4 grid
    static int _padding = 0;            //Will be set based on grid size


    //Image for cells to show locked graphic
    private IImage _cellLockImage;

    //Where we start rendering the grid buttons
    int _originX;
    int _originY;

    //This debug cell helps us indicate where the last clue was generated
    public Cell debugCell;

    /**
     * Construct a puzzle of size^2 cells. With the help of the gridSolver helper class we ensure
     * the puzzle to be solvable
     *
     * @param size number of cells on each side of the square grid
     * @param level string that defines level cells, can be empty for random level
     */
    public Grid(int size, String level){
        _gridSolver = new GridSolver(this);
        _size = size;
        _cells = new Cell[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                _cells[i][j] = new Cell(j,i, Cell.State.Red);

        if(!level.equals(""))
            _gridSolver.generateLevel(level);
        else if(size>0)
            _gridSolver.generateLevel();
        _percentage = 0;
    }

    /**
     * Sets up all the graphics attributes, loads resources and calculates the transform of each cell.
     * With transform we mean it's position and scale in logic coordinates. The scale changes between
     * different board size because we have smaller cells on larger grids.
     *
     * @param graphics Abstract engine interface that allows to generate resources and draw in all
     *                 supported platforms
     */
    public void init(IGraphics graphics) {
        _G = graphics;
        _cellLockImage = graphics.newImage("lock.png");

        int logicWidth = graphics.getLogicWidth();
        int logicHeight = graphics.getLogicHeight();

        //compute grid starting position, cell padding and radius based on grid size
        _padding = BASE_PADDING /_size;
        int r = ((logicWidth -BORDER) -_size* _padding)/(_size*2);
        _originX = (_padding /2 + BORDER/2);
        _originY = logicHeight / 8 + BORDER;

        //Set common scale and opacity for this set of cells
        Cell.setScale(r/((double)(logicWidth -4* _padding)/(4*2)));
        Cell.setOpacity(1);

        //We compute the position for each cell only once on logic coordinates
        for(int i = 0; i < _size; i++) {
            int y = (_originY+(i)*(r*2)+ _padding *i)+r;
            for(int j = 0; j < _size; j++)
            {
                int x = (_originX+(j)*(r*2)+ _padding *j)+r;
                getCell(j,i).setTransform(x,y,r);
            }
        }
    }


    /**
     * We try to process the click to change the state of a cell
     * @param ev the TouchEvent we want te cell to process
     * @return whether we clicked on a free cell a locked cell or we missed all cells
     */
    public ClickResult processClick(TouchEvent ev)
    {
        for(Cell[] row : _cells)
            for(Cell ce : row)
                if(ce.click(ev))
                    return clickCell(ce);

        //In case we don't click inside the grid
        return ClickResult.MISSED;
    }

    /**
     * We try to change the state of {x,y} cell while keeping the stack of undo changes properly
     * updated
     *
     * @param c the cell clicked
     * @return whether we clicked on a free cell a locked cell or a free one
     */
    public ClickResult clickCell(Cell c) {
        if(c.isLocked()){
            return  ClickResult.LOCKED;
        }
        //If it wasn't locked we update de undo stack and then te cell and percentage
        else {
            Cell first = undoStack.peekFirst();
            if (first == null || first.col() != c.x() || first.row() != c.y())
                undoStack.addFirst(new Cell(c.col(), c.row(), c.getPreviousState()));

            computePercentage();
            return ClickResult.FREE;
        }
    }

    /**
     * We calculate current clicked cells to display percentage on screen.
     *
     * This could be optimized to always have the amount of clicked cells updated on clicks and undos.
     * But this is not that slow because we only compute it when one of those events occurs.
     */
    public void computePercentage() {
        //Amount of clicked cells
        int _clicked = 0;
        for (Cell[] row : _cells)
            for(Cell cell : row)
                if(!cell.isLocked() && cell.getState()!= Cell.State.Grey)
                    _clicked++;

        _percentage =  (100 * _clicked) / _gridSolver._freeCells;
    }

    /**
     * Call draw on every cell
     * @param font the font to render the fixed numbers with
     */
    public void draw(IFont font){
        for(int i = 0; i < _size; i++) {
            for(int j = 0; j < _size; j++)
            {
                if(Constants.DEBUG && getCell(j,i) == debugCell)
                    getCell(j,i).draw(_G, _cellLockImage, font, 0xffc0c0c0);
                else
                    getCell(j,i).draw(_G, _cellLockImage, font);
            }
        }

    }

    /**
     * Blocks all the cells
     */
    public void lockCells() {
        //Lock all cells
        for (Cell[] line: _cells) {
            for (Cell c:line) {
                c.lock();
            }
        }
    }


    /**
     * We update the all the animations
     * @param deltaTime the ms since the previous update
     */
    public void onUpdate(double deltaTime) {
        //Forward call so each cell can update its animations
        for(Cell[] line : _cells)
            for(Cell c : line)
                c.onUpdate(deltaTime);
    }


    /**
     * Forwards petition to grid solver to get a Clue on the current grid state adn returns it
     * @return a clue for the current grid state
     */
    public Clue getClue(){
        return  _gridSolver.getClue();
    }

    /**
     * Undoes the last change the player made
     * @return the cell we reestablished
     */
    public Cell undoMove(){
        if(undoStack.size() == 0)
            return null;

        Cell c = undoStack.removeFirst();
        Cell cReal = getCell(c.col(), c.row());

        Cell.State s = c.getState();
        cReal.setState(s);

        computePercentage();
        return c;
    }


    /**
     * Percentage getter
     * @return the amount of free cells that have been clicked in percentage [0..100]
     */
    public int getPercentage(){
        return _percentage;
    }

    /**
     * Check whether the current puzzle is completed and correct
     * @return true on correct grid
     */
    public boolean checkWin(){
        return _gridSolver.solved();
    }

    /**
     * Getter for cells
     * @param x the X coordinate, from 0..Size
     * @param y the Y coordinate, from 0..Size
     * @return the requested cell or null if outside of range
     */
    public Cell getCell(int x, int y){
        if(x < _size && x >= 0 && y < _size && y >= 0)
            return _cells[y][x];
        else return null;
    }

    /**
     * Set the given cell to the position given in the grid
     * @param cell the cell to save
     * @param x the X coordinate, from 0..Size
     * @param y the Y coordinate, from 0..Size
     */
    public void setCell(Cell cell, int x, int y){
        if(x < _size && x >= 0 && y < _size && y >= 0)
            _cells[y][x] = cell;
    }

    /**
     * size getter
     * @return the amount of cells a side of the grid has
     */
    public int getSize() { return _size; }
}