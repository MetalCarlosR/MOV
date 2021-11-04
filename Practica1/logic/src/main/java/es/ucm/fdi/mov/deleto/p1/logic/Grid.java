package es.ucm.fdi.mov.deleto.p1.logic;

import com.sun.tools.javac.util.Pair;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Random;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Stack;
import java.util.Vector;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Grid {

    static final int PADDING = 10;

    public Grid(){
        _dirs.add(new Pair<Integer, Integer>(-1,0));
        _dirs.add(new Pair<Integer, Integer>(1,0));
        _dirs.add(new Pair<Integer, Integer>(0,-1));
        _dirs.add(new Pair<Integer, Integer>(0,1));
    }

    public void init(int size)
    {
        _size = size;
        _cells = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                _cells[i][j] = new Cell(j,i, Cell.State.Red);
            }
        }

        //loadGridFromFile("./Assets/examples/ex2.txt");

        randomize(size);
        if(!canBeSolved() || true){
            _freeCells = 0;
            _fixedCells.clear();
            _visibleCells.clear();
            System.err.println("Couldn't generate a new map...\nLoading one from file");
            loadGridFromFile("./Assets/examples/ex2.txt");
        }
        _freeCells = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = getCell(i, j);
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
        _percentage = 0;
    }

    private void loadGridFromFile(String file) {
        _clicked = 0;
        File inputFile = new File(file);
        try (Scanner reader = new Scanner(inputFile)){
            int i = -1;
            while (reader.hasNextLine() && ++i < _size) {
                String data = reader.nextLine();
                String[] cellDefinitions = data.split(" ");
                if(cellDefinitions.length != _size)
                    throw new RuntimeException("Malformed map was given");

                for(int j = 0; j < _size; j++) {
                    Cell c = new Cell(cellDefinitions[j],j,i);
                    _cells[i][j] = c;

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

    private void randomize(int size)
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
            Cell originalCell = getCell(x, y);
            if(!tryBlue(originalCell)) {
                continue;
            }
            // para que vaya de 1 al max
            int n = r.nextInt(size-1)+1;
            ArrayList<Pair<Integer, Integer>> list = new ArrayList<>(_dirs);
            int j = 0;
            // intenta pintar en las 4 direcciones n azules
            while(n > 0 && j < 4 && maxBlue > 0){
                int m = 0;
                if(n > 1) {
                    m = r.nextInt(n-1)+1;
                }
                else m = 1;
                Pair<Integer, Integer> dir = list.get(j);
                //si cambio en la que estoy... lo jodo?
                // primero pinta de azul los que necesite
                int step = 1;
                while(step <= m){
                    Cell c = getCell(x+(dir.fst*step), y+(dir.snd * step));
                    //if(c == null || getCell(c._x + dir.fst, c._y + dir.snd).getNeigh() == size)
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
                Cell c = getCell(j, i);
                if(c.getState() != Cell.State.Red){
                    int n = getVisibleNeighs(c);
                    c.setNeigh(n);
                }
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = getCell(j, i);
                _visibleCells.add(c);
                _freeCells++;
                c.setState(Cell.State.Grey);
            }
        }
    }

    private boolean tryBlue(Cell c){
        c.setState(Cell.State.Blue);
        for (int i = 0; i < _dirs.size(); i++) {
            for (int j = 0; j < _size; j++) {
                Cell ady = getCell(c._x + _dirs.get(i).fst * j, c._y + _dirs.get(i).snd * j);
                if(ady != null){
                    if(getVisibleNeighs(ady) > _size){
                        c.setState(Cell.State.Red);
                        return false;
                    }
                } else break;
            }
        }
        return true;
    }

    private boolean canBeSolved()
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
                Cell c = getCell(x,y);
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
        for(Pair<Integer,Integer> d:_dirs)
        {
            Cell next = getCell(c._x+d.fst, c._y+d.snd);
            if(next!=null && next.getState() != Cell.State.Red)
                return  false;
        }
        return true;
    }

    public int getPercentage(){
        return _percentage;
    }

    public boolean processClick(int x, int y)
    {
        int r = (_lastWidth-_size* PADDING)/(_size*2);
        int originX = (PADDING /2);
        int originY = _lastHeight/8;
        if( y >= originY &&
            y <  ((_size*((2*(r+PADDING))))) &&
            x >= originX &&
            x < (400 - PADDING))
        {
            y-=originY;

            int widthEach = (_lastWidth/_size);
            int centerX = (x % widthEach) + r + PADDING;
            int heightEach = widthEach;
            int centerY = (y % heightEach)+ r + PADDING;
//            System.out.printf("C{%d,%d}\n",centerX,centerY);
//            System.out.printf("P{%d,%d}\n",x,y);
//            System.out.printf("rP{%d,%d}\n",x/widthEach,y/heightEach);

            //if(centerX > PADDING && centerX < 2*r && centerY > PADDING && centerY < 2*r)
            if(true)
            {
                _G.setColor(0xAABBBB00);
                _G.fillCircle(x,y,2*r);
                clickCell(x/widthEach, y/heightEach);
                return true;
            }
        }
        return  false;
    }

    public void draw(IGraphics graphics, IFont font, IImage lock, Cell focus){
        _lastWidth = graphics.getWidth();
        _lastHeight = graphics.getHeight();
        if(_G==null)
            _G = graphics;

        int r = (_lastWidth-_size* PADDING)/(_size*2);
        double textScale = r/((double)(_lastWidth-4* PADDING)/(4*2));
        int originX = (PADDING /2);
        int originY = _lastHeight/8;

        graphics.setColor(0xFF000000);
        graphics.fillRect(5,5,10,10);
        graphics.fillRect(400-5,600-5,10,10);

        for(int i = 0; i < _size; i++) {
            int y = (originY+(i)*(r*2)+ PADDING *i)+r;
            for(int j = 0; j < _size; j++)
            {
                Cell cel = getCell(j,i);
                Cell.State state = cel.getState();

                int x = (originX+(j)*(r*2)+ PADDING *j)+r;

                if(cel == focus)
                {
                    int ring = 2;

                    graphics.setColor(0xFF000000);
                    graphics.fillCircle(x,y,(r+ring));
                }
                if(cel == pito){
                    graphics.setColor(0xFFFFFF00);
                }
                else graphics.setColor(state == Cell.State.Blue ?0xFF1CC0E0 : state == Cell.State.Red ? 0xFFFF384B : 0xFFEEEEEE);
                graphics.fillCircle(x,y,r);

                if(cel.getState() == Cell.State.Blue && cel.isLocked())
                {
                    graphics.setColor(0xFFFFFFFF);
                    graphics.setFont(font);
                    graphics.drawText(Integer.toString(cel.getNeigh()), x,y,textScale);
                }
                else if(cel.getState() == Cell.State.Red && cel.isLocked())
                {
                    graphics.setOpacity(0.2f);
                    graphics.drawImage(lock, x,y,(float)(0.65f*textScale),(float)(0.65f*textScale));
                    graphics.setOpacity(1.0f);
                }
            }
        }

    }

    public boolean clickCell(int x, int y)
    {
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
        else if(c.snd == Cell.State.Red)
            _clicked++;

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

    // TODO: bruh, revisar
    public boolean checkWin(){
        if(_percentage >= 99 && _mistakes == 0){
            return getFixedCells().size() + getIsolatedCells().size() == 0;
        }
        return false;
    }

    public Cell getCell(int x, int y){
        if(x < _size && x >= 0 && y < _size && y >= 0)
            return _cells[y][x];
        else return null;
    }

    public int getSize() {return _size; };

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
                Pair<Integer, Integer> sel = null;
                for (Pair<Integer, Integer> d: _dirs) {
                    if((sel=getPossibleGrowthInDir(c, d))!=null)
                        break;
                }
                clues.addFirst(new Clue(c,"This number can see all its dots",new Cell(sel.fst, sel.snd, Cell.State.Red)));
                return clues.getFirst();
            }
            // 8. solo te queda una direccion en la que mirar
            else if(getNumPossibleDirs(c) == 1)
            {
                Pair<Integer, Integer> sel = null;
                for (Pair<Integer, Integer> d: _dirs) {
                    if((sel=getPossibleGrowthInDir(c, d))!=null)
                        break;
                }
                clues.addFirst(new Clue(c,"Only one direction remains for this number to look in", new Cell(sel.fst,sel.snd, Cell.State.Blue)));
                return clues.getFirst();
            }
            // 2. si añades uno, te pasas porque pasa a ver los azules de detras
            {
                Pair<Integer,Integer> sel =  getPossibleNeighs(c);
                if (sel!=null)
                {
                    clues.addFirst(new Clue(c, "Looking further in one direction would exceed this number",new Cell(sel.fst,sel.snd, Cell.State.Red)));
                    return clues.getFirst();
                }
            }
            // 3. 5. y 9.

            Cell obvious = getDirForObviousBlueDot(c);
            // 3. y 9. hay un punto que tiene que ser clicado si o si
            if(obvious != null )//&& _grid.getCell(c._x+obvious.fst,c._y+obvious.snd).getState()== Cell.State.Grey
            {
                clues.addFirst(new Clue(c,"One specific dot is included in all solutions imaginable",new Cell(obvious._x,obvious._y, Cell.State.Blue)));
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
    private int getVisibleNeighInDir(Cell c, Pair<Integer,Integer> d){
        int n = 0;
        int x = c._x+d.fst;
        int y = c._y+d.snd;
        while(  getCell(x,y) !=null  &&
                getCell(x,y).getState()== Cell.State.Blue)
        {
            x+= d.fst;
            y+= d.snd;
            n++;
        }

        return n;
    }

    // si pongo todos los grises de una direccion a azul tendria n neighbours
    private int getPossibleNeighInDir(Cell c, Pair<Integer, Integer> d){
        int n = 0;
        int x = c._x+d.fst;
        int y = c._y+d.snd;
        while(getCell(x,y) != null &&
              getCell(x,y).getState() != Cell.State.Red)
        {
            x+= d.fst;
            y+= d.snd;
            n++;
        }
        return n;
    }

    // devuelve si hay un gris en una direccion, su posición si no devuelve null
    private Pair<Integer, Integer> getPossibleGrowthInDir(Cell c, Pair<Integer, Integer> d){
        int x = c._x + d.fst;
        int y = c._y + d.snd;

        while(getCell(x,y) != null ){
            if(getCell(x,y).getState() == Cell.State.Grey)
                return  new Pair<Integer, Integer>(x,y);
            else if(getCell(x,y).getState() == Cell.State.Red)
                return  null;
            x+= d.fst;
            y+= d.snd;
        }
        return null;
    }

    // TODO: Optimizable
    // devuelve el numero de cells azules visibles
    public int getVisibleNeighs(Cell c){
        int n = 0;
        for (Pair<Integer, Integer> d: _dirs) {
            n+= getVisibleNeighInDir(c, d);
        }
        return n;
    }

    // devuelve si en alguna direccion, al añadir uno, superas el numero de vecinos
    public Pair<Integer, Integer> getPossibleNeighs(Cell c){
        int n = 0;
        int visible = getVisibleNeighs(c);
        if(c.getState() != Cell.State.Blue)
            return null;
        for (Pair<Integer, Integer> d: _dirs) {
            Cell ghostCell = null;
            int i = 1;
            for(;i<_size;i++)
            {
                ghostCell = getCell(c._x + (d.fst*i), c._y + (d.snd*i));
                if(ghostCell!=null && ghostCell.getState() == Cell.State.Grey)
                {
                    n = getVisibleNeighInDir(ghostCell, d);
                    if(n + visible + 1 > c.getNeigh())
                        return new Pair<Integer, Integer>(ghostCell._x,ghostCell._y);
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

        for (Pair<Integer, Integer> d: _dirs) {
            if(getPossibleGrowthInDir(c, d)!=null)
                n++;
        }

        return n;
    }

    public Cell getDirForObviousBlueDot(Cell c){
        Pair <Integer, Integer> dir = null;

        int neigh [] = new int[_dirs.size()];
        int totalVis = 0;
        int max = 0;
        int id = 0;
        int sum = 0;

        int i = 0;
        for (Pair<Integer, Integer> d: _dirs) {
            int vis = getVisibleNeighInDir(c, d);
            neigh[i] = getPossibleNeighInDir(c, d);
            neigh[i] = Math.min(c.getNeigh(), neigh[i]);
            neigh[i] -= vis;
            if(max < neigh[i]){
                max = neigh[i];
                id = i;
            }
            totalVis += vis;
            sum += neigh[i];
            i++;
        }

        max *= 2;

        int numDirs = 0;

        for(i = 0; i < _dirs.size(); i++){
            if(neigh[i] >= c.getNeigh() - totalVis)
                numDirs++;
        }

        if(numDirs > 1)
            return null;

        for(i = 0; i < _dirs.size(); i++){
            max -= neigh[i];
        }

        if(max > 0 || (max == 0 && sum == c.getNeigh() - totalVis))
            dir = _dirs.get(id);

        if(dir == null)
            return null;

        Cell cell = getCell(c._x + dir.fst, c._y + dir.snd);
        while(cell != null){
            if(cell.getState() == Cell.State.Grey)
                return cell;

            cell = getCell(cell._x + dir.fst, cell._y + dir.snd);
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

    public Vector<Cell> getIsolatedCells(){
        Vector<Cell> ret = new Vector<Cell>();
        for(Cell c : _isolated){
            if(c.getState() != Cell.State.Red)
                ret.add(c);
        }
        return ret;
    }

    private Cell[][] _cells;

    private Vector<Cell> _fixedCells = new Vector<Cell>();
    private Vector<Cell> _visibleCells = new Vector<Cell>();
    private Vector<Cell> _isolated = new Vector<Cell>();

    public ArrayList<Pair<Integer, Integer>> _dirs = new ArrayList<>();

    private int _size = 0;
    private int _percentage = 0;
    private int _freeCells = 0;
    private int _clicked = 0;
    private int _mistakes = 0;

    private int _lastWidth = 0;
    private int _lastHeight = 0;
    private IGraphics _G;

    public Cell pito;

    private Stack<Pair<Cell, Cell.State>> undoStack = new Stack<>();
}