package es.ucm.fdi.mov.deleto.p1.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public class GridGenerator {

    public static void Generate(GridSolver solver){
        randomize(solver);
        if(!canBeSolved(solver)){
            solver.reset();
            System.err.println("Couldn't generate a new map...\nLoading one from file");
            loadGridFromFile("./Assets/examples/ex2.txt",solver);
        }
        solver._freeCells = 0;
        for (int i = 0; i < solver._grid.getSize(); i++) {
            for (int j = 0; j < solver._grid.getSize(); j++) {
                Cell c = solver._grid.getCell(i, j);
                if(solver.isIsolated(c)){
                    solver._isolated.add(c);
                }
                if(!c.isLocked())
                {
                    c.setState(Cell.State.Grey);
                    solver._freeCells++;
                }
            }
        }
        for(Cell is : solver._isolated){
            if(is.isLocked()){
                is.unlock();
                is.setState(Cell.State.Grey);
                solver._freeCells++;
            }
        }
    }

    private static void randomize(GridSolver solver)
    {
        int size = solver._grid.getSize();
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
            Cell originalCell = solver._grid.getCell(x, y);
            if(!tryBlue(originalCell,solver)) {
                continue;
            }
            // para que vaya de 1 al max
            int n = r.nextInt(size-1)+1;
            ArrayList<Vec2<Integer>> list = new ArrayList<>(solver._dirs);
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
                    Cell c = solver._grid.getCell(x+(dir.x()*step), y+(dir.y() * step));
                    //if(c == null || getCell(c._x + dir.x(), c._y + dir.y()).getNeigh() == size)
                    if(c != null && tryBlue(c,solver)){
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
                Cell c = solver._grid.getCell(j, i);
                if(c.getState() != Cell.State.Red){
                    int n = solver.getVisibleNeighs(c);
                    c.setNeigh(n);
                }
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = solver._grid.getCell(j, i);
                solver._visibleCells.add(c);
                solver._freeCells++;
                c.setState(Cell.State.Grey);
            }
        }
    }

    private static boolean tryBlue(Cell c, GridSolver solver){
        c.setState(Cell.State.Blue);
        for (int i = 0; i < solver._dirs.size(); i++) {
            for (int j = 0; j < solver._grid.getSize(); j++) {
                Cell ady = solver._grid.getCell(c._x + solver._dirs.get(i).x() * j, c._y + solver._dirs.get(i).y() * j);
                if(ady != null){
                    if(solver.getVisibleNeighs(ady) > solver._grid.getSize()){
                        c.setState(Cell.State.Red);
                        return false;
                    }
                } else break;
            }
        }
        return true;
    }

    public static boolean canBeSolved(GridSolver solver)
    {
        Random r = new Random();
        boolean solved = false;

        while(!solved){
            Clue clue = solver.getClue();
            if(clue == null || clue.getCorrectState() == null){
                // si no hay pista, añadir una nueva celda gris como locked
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
                // hacerle caso a la pista e intentar resolverlo
                int x = clue.getCorrectState()._x;
                int y = clue.getCorrectState()._y;
                Cell c = solver._grid.getCell(x,y);
                if(!c.isLocked()){
                    solver._visibleCells.remove(c);
                    c.setState(clue.getCorrectState().getState());
                }
                else{
                    System.err.println("La pista debería ser editable: ");
                    System.err.println(c._x + " " + c._y);
                    System.err.println(clue.getMessage());
                }

            }
            solved = solver._visibleCells.size() == 0;
        }

        Clue c = solver.getClue();
        return c == null || c.getCorrectState() == null;
    }


    private static void loadGridFromFile(String file, GridSolver solver) {
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
            // TODO: hacemos esto really?
            e.printStackTrace();
            System.err.println("Couldn't open the file");
            System.err.println(System.getProperty("user.dir"));
            System.err.println("Generating one by default\n");
            throw new RuntimeException("Map generation not implemented yet");
        }
    }
}
