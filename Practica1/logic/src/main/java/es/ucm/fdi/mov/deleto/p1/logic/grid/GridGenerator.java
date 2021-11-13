package es.ucm.fdi.mov.deleto.p1.logic.grid;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

import es.ucm.fdi.mov.deleto.p1.engine.Vec2;
import es.ucm.fdi.mov.deleto.p1.logic.buttons.Cell;


/**
 * Purely static class to aid on grid generation
 * Can generate solvable puzzles randomly or read them from file.
 *
 * We could probably optimize this further or make it more readable but
 * current priorities are on adding extra functionality and feedback to the gameplay itself.
 */
final class GridGenerator {
    private GridGenerator(){}

    /**
     * Creates a new grid randomly, and if its not able, loads one from file
     * @param solver the solver to test
     */
    public static void Generate(GridSolver solver){
        // generates the grid
        AddRandomBlueCells(solver);
        if(!CanBeSolved(solver)){
            // if its not able, load it from file
            solver.reset();
            System.err.println("Couldn't generate a new map...\nLoading one from file");
            LoadGridFromFile("./Assets/examples/ex2.txt",solver);
        }

        // once its created
        solver._freeCells = 0;
        // sets the cells to the states and arrays they belong
        for (int i = 0; i < solver._grid.getSize(); i++) {
            for (int j = 0; j < solver._grid.getSize(); j++) {
                Cell c = solver._grid.getCell(i, j);
                if(IsIsolated(c, solver)){
                    solver._isolated.add(c);
                    solver._fixedCells.remove(c);
                }
                if(!c.isLocked())
                {
                    c.setState(Cell.State.Grey);
                    solver._freeCells++;
                }
            }
        }
        // cleans the isolated cells from the algorithm and sets them free
        for(Cell is : solver._isolated){
            if(is.isLocked()){
                is.unlock();
                is.setState(Cell.State.Grey);
                solver._freeCells++;
            }
        }
    }

    /**
     * Adds blue dots randomly into the grid,
     * following the rules from the game, such as no cell will see more than size blue cells
     * At the end, there won't be any locked cells yet, only the neighbours of each cell
     * @param solver must be initialized all cells to red
     */
    private static void AddRandomBlueCells(GridSolver solver)
    {
        int size = solver._grid.getSize();
        Random r = new Random();
        int maxBlue = (int)(0.8*size*size);

        // paint maxBlue cells of blue
        while(maxBlue > 0){
            int x = r.nextInt(size);
            int y = r.nextInt(size);
            Cell originalCell = solver._grid.getCell(x, y);
            // try setting blue the cell you are on
            if(!TryBlue(originalCell,solver)) {
                continue;
            }
            // number of neighbours of this cell
            int totalNeighOfCell = r.nextInt(size-1)+1;
            int initialTotalNeighs = totalNeighOfCell;
            int j = 0;
            // try painting in blue a random number of cells in each direction
            while(totalNeighOfCell > 0 && j < 4 && maxBlue > 0){
                int totalNeighsInDir;
                if(totalNeighOfCell > 1) {
                    totalNeighsInDir = r.nextInt(totalNeighOfCell-1)+1;
                }
                else totalNeighsInDir = 1;
                Vec2<Integer> dir = solver._dirs.get(j);
                int step = 1;
                while(step <= totalNeighsInDir){
                    Cell c = solver._grid.getCell(x+(dir.x()*step), y+(dir.y() * step));
                    // if its able to change to blue, do it, and try again
                    if(c != null && TryBlue(c,solver)){
                        step++;
                        maxBlue--;
                    }
                    // if not, this direction is not longer an option
                    else break;
                }
                j++;
                totalNeighOfCell -= (step-1);
            }
            // if it couldn't add any neighbour, reset the cell
            if(initialTotalNeighs == totalNeighOfCell)
                originalCell.setState(Cell.State.Red);
        }
        // after setting the state of each cell, we must update the neighbour of every cell
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = solver._grid.getCell(j, i);
                if(c.getState() != Cell.State.Red){
                    int n = solver.visibleNeighbours(c);
                    c.setNeigh(n);
                }
            }
        }
        // and we need to reset the visible cells to a grey state for the next step
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = solver._grid.getCell(j, i);
                solver._visibleCells.add(c);
                solver._freeCells++;
                c.setState(Cell.State.Grey);
            }
        }
    }

    /**
     * Try setting one cell to blue if its neighbours allow it
     * @param c the cell that will be tested
     * @param solver the grid to chek if it can be blue
     * @return whether we can set it or not
     */
    private static boolean TryBlue(Cell c, GridSolver solver){
        c.setState(Cell.State.Blue);
        // searches in all 4 directions
        for (int i = 0; i < solver._dirs.size(); i++) {
            // and search if adding to blue the cell, will exceed the neighbours of one of its neighbours
            for (int j = 0; j < solver._grid.getSize(); j++) {
                Cell ady = solver._grid.getCell(c.col() + solver._dirs.get(i).x() * j, c.row() + solver._dirs.get(i).y() * j);
                if(ady != null){
                    // if one of them exceeds the maximum, the cell is reset
                    if(solver.visibleNeighbours(ady) > solver._grid.getSize()){
                        c.setState(Cell.State.Red);
                        return false;
                    }
                } else break;
            }
        }
        return true;
    }

    /**
     * Checks if a puzzle can be solved, following the tips a player would receive
     * @param solver solver the solver to test
     * @return whether the grid corresponding to the solver is solvable or no
     */
    public static boolean CanBeSolved(GridSolver solver)
    {
        Random r = new Random();
        boolean solved = false;

        // while the game isn't solved yet
        while(!solved){
            Clue clue = solver.getClue();
            // see if there is any clue remaining
            if(clue == null || clue.correctState() == null){
                // if there is not any new clue, add a new grey cell as locked
                int rand = r.nextInt(solver._visibleCells.size());
                Cell c = solver._visibleCells.get(rand);
                c.lock();
                if(c.getNeigh() > 0)
                    c.setState(Cell.State.Blue);
                else c.setState(Cell.State.Red);
                solver._visibleCells.remove(rand);
                solver._freeCells--;
                solver._fixedCells.add(c);
            }
            else{
                // if there are still clues, keep following them until the game is over or there isn't any
                int x = clue.correctState().col();
                int y = clue.correctState().row();
                Cell c = solver._grid.getCell(x,y);
                if(!c.isLocked()){
                    solver._visibleCells.remove(c);
                    c.setState(clue.correctState().getState());
                }
                else{
                    // if the clue returns a locked cell to edit
                    System.err.println("Clue should not be editable: ");
                    System.err.println(c.col() + " " + c.row());
                    System.err.println(clue.message());
                }

            }
            solved = solver._visibleCells.size() == 0;
        }

        Clue c = solver.getClue();
        return c == null || c.correctState() == null;
    }

    /**
     * Loads a grid from file
     * The file format must be a size number of lines which contains the information for each cell
     * separated by spaces.
     * Each cell will be formatted like:
     * "3f" or "2l" the number representing the neighbours and the f to indicate if its free, and l if its locked
     * @param file file name with the puzzle
     * @param solver grid in which it will be stored
     */
    private static void LoadGridFromFile(String file, GridSolver solver) {
        File inputFile = new File(file);
        try (Scanner reader = new Scanner(inputFile)){
            int i = -1;
            while (reader.hasNextLine() && ++i < solver._grid.getSize()) {
                String data = reader.nextLine();
                String[] cellDefinitions = data.split(" ");
                if(cellDefinitions.length != solver._grid.getSize())
                    throw new RuntimeException("Malformed map was given");

                for(int j = 0; j < solver._grid.getSize(); j++) {
                    Cell c = new Cell(cellDefinitions[j],j,i);
                    solver._grid.setCell(c,j,i);

                    if(c.isLocked())
                        solver._fixedCells.add(c);
                }
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            System.err.println("Couldn't open the file");
            System.err.println(System.getProperty("user.dir"));
            System.err.println("Generating one by default\n");
            throw new RuntimeException("Map generation not implemented yet");
        }
    }

    /**
     * Checks if a cell is isolated from the rest, meaning, no other cell is able to see it
     * @param c The cell to check
     * @param solver The rest of the grid
     * @return whether is isolated or not
     */
    private static boolean IsIsolated(Cell c, GridSolver solver)
    {
        for(Vec2<Integer> d:solver._dirs)
        {
            Cell next = solver._grid.getCell(c.col() +d.x(), c.row() +d.y());
            if(next!=null && next.getState() != Cell.State.Red)
                return  false;
        }
        return true;
    }
}
