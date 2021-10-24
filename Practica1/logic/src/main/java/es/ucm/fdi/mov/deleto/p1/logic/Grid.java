package es.ucm.fdi.mov.deleto.p1.logic;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

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
                _cells[i][j] = new Cell(i, j, 0,true);
        }
        File myObj = new File("./examples/ex1.txt");
        try (Scanner myReader = new Scanner(myObj)){
            int i = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] pairs = data.split(" ");
                for(int j = 0; j < _size; j++) {
                    _cells[i][j].setCell(pairs[j]);
                    if(!_cells[i][j].isLocked())
                        notFixed++;
                }
                i++;
            }

            if(notFixed > 0)
                _percentInc = 100.0f / notFixed;
        }
        catch (FileNotFoundException e){
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
        return _cells[y][x];
    }

    private Cell[][] _cells;
    private int _size = 0;
    private int _percentage = 0;
    private float _percentInc = 0;
    private int _mistakes = 0;
}