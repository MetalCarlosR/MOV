package es.ucm.fdi.mov.deleto.p1.logic;

import com.sun.tools.javac.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Vector;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class OhY3s implements IApplication {

    IEngine _engine;
    private Grid _grid;
    private UIBar _bar;

    double aaaa = 0;
    IImage image;
    IFont _font;
    IFont _title;
    IFont _subtitle;

    public OhY3s() {

    }

    public void newGame(int size) {
        _grid = new Grid(size);
    }

    public void click(int x, int y) {
        _grid.clickCell(x, y);
        System.out.println("Neighbours "+ _grid.getVisibleNeighs(_grid.getCell(x,y)));
    }

    public Pair<Cell, String> getTip() {
        Vector<Cell> cells = _grid.getTipCells();

        Collections.shuffle(cells);

        String t = "";
        Cell c = null;

        int i = 0;
        while(i < cells.size() && t == ""){
            //int rand = (int)Math.floor(Math.random()*(cells.size()));
            c = cells.get(i);

            // TO DO: si se optimiza, no hace falta
            int visibleNeigh = _grid.getVisibleNeighs(c);

            // Estan desordenados para que muestre los casos especificos o por optimizacion
            // 6. y 7. para aislados si esta en azul o en gris
            if(c.getNeigh() == 0){
                if(c.getState() == Cell.State.Grey)
                    t = "This one should be easy...";
                else if (c.getState() == Cell.State.Blue)
                    t = "A blue dot should always see at least one other";
            }
            // 4. ve demasiados cells
            else if(visibleNeigh > c.getNeigh()){
                t = "This number sees a bit too much";
            }
            // 1. ya los ve todos
            else if(visibleNeigh == c.getNeigh()){
                t = "This number can see all its dots";
            }
            // 8. solo te queda una direccion en la que mirar
            else if(_grid.getNumPossibleDirs(c) == 1){
                t = "Only one direction remains for this number to look in";
            }
            // 2. si añades uno, te pasas porque pasa a ver los azules de detras
            else if(_grid.getPossibleNeighs(c)){
                t = "Looking further in one direction would exceed this number";
            }
            // 3. 5. y 9.
            else{
                Pair<Integer, Integer> obvious = _grid.getDirForObviousBlueDot(c);
                // 3. y 9. hay un punto que tiene que ser clicado si o si
                if(obvious != null){
                    t = "One specific dot is included in all solutions imaginable";
                }
                // 5. no ve los suficientes
                else if(obvious == null){
                    t = "This number can't see enough";
                }
            }
            // TO DO:
            // 10. ??

            i++;
        }

        return new Pair<Cell, String>(c, t);
    }

    public void _draw() {
        //Draw Title
        _engine.getGraphics().setColor(0xFF000000);
        _engine.getGraphics().setFont(_title);
        _engine.getGraphics().drawText(Integer.toString(_grid.getSize())+"x"+Integer.toString(_grid.getSize()),(_engine.getGraphics().getWidth())/2,32);

        //Draw Grid
        _engine.getGraphics().setFont(_font);
        _grid._draw(_engine.getGraphics(), _font, image);

        //Draw percentage
        _engine.getGraphics().setFont(_subtitle);
        _engine.getGraphics().setColor(0xFF777777);
        _engine.getGraphics().drawText(Integer.toString(_grid.get_percentage())+"%",(_engine.getGraphics().getWidth()/2),540);

        //TODO Draw bottom bar
    }


    @Override
    public void onInit(IEngine engine) {
        _engine = engine;
        _engine.getGraphics().setColor(0xFFFFFFFF);
        image = _engine.getGraphics().newImage("lock.png");
        _font = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",64,true);
        _title = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",72,true);
        _subtitle = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",24,false);
        _engine.getGraphics().setFont(_font);
    }

    @Override
    public void onUpdate(double deltaTime) {
        aaaa += deltaTime * 100;

        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        // Reading data using readLine
        String rawInp = "";
        /*
        try {
            rawInp = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        String[] input = rawInp.split(" ");
//
//        switch (input[0]){
//            case "click":{
//                int x = -1;
//                int y = -1;
//                try{
//                    if(input.length < 3)
//                        throw new RuntimeException();
//                    x = Integer.valueOf(input[1]);
//                    y = Integer.valueOf(input[2]);
//                    if (x < 0 || y < 0 || x >= _grid.getSize() || y >= _grid.getSize())
//                        throw new RuntimeException();
//                }
//                catch (RuntimeException e){
//                    System.out.println("Click command expects 2 valid ints");
//                    break;
//                }
//                click(x, y);
//                break;
//            }
//            case "exit":{
//                // TO DO: que pare
//                System.out.println("Exiting...");
//                _engine.exit();
//                break;
//            }
//            case "undo":{
//                if(_grid.undoMove())
//                    System.out.println("Undoing last move...");
//                else System.out.println("Couldn't undo last move");
//                break;
//            }
//            case "tip":{
//                Pair<Cell, String> tip = getTip();
//                System.out.println("Position: " + tip.fst._x + " " + tip.fst._y + "\n" + tip.snd);
//                break;
//            }
//            case "settings":{
//                System.out.println("NOT IMPLEMENTED YET");
//                break;
//            }
//            case "about":{
//                System.out.println("NOT IMPLEMENTED YET");
//                break;
//            }
//            default:{
//                System.out.println("Unknown command:\n" + rawInp);
//                break;
//            }
//        }

    }

    @Override
    public void onRender() {
        _draw();
    }

    @Override
    public void onExit() {
        System.out.println("SALIENDO PAPA");
    }

    @Override
    public void onEvent(TouchEvent event) {

    }
    // TO DO:
    // private Text _title;
}