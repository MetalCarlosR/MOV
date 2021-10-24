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
                }
                i++;
            }
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
                char c = ' ';
                if (_cells[i][j].isFixed()){
                    if(_cells[i][j].getNeigh() == 0){
                        c = 'X';
                    }
                    else c = (char)('0' + _cells[i][j].getNeigh());
                }
                System.out.print("| " + c + " ");
            }
            System.out.println("|");
        }
        for(int j = 0; j < _size; j++)
        {
            System.out.print("+---");
        }
        System.out.println("+");
    }

    public Cell getCell(int x, int y){
        return _cells[x][y];
    }

    private Cell[][] _cells;
    private int _size = 0;
}