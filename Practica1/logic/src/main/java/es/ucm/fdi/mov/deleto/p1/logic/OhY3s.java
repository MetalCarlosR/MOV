package es.ucm.fdi.mov.deleto.p1.logic;

import com.sun.tools.javac.util.Pair;

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

    IImage image;
    IFont _font;
    IFont _title;
    IFont _subtitle;

    String _currentTip = "";
    Cell   _cellTip = null;
    public OhY3s() {}

    public void newGame(int size) {
        if(size > 4)
            System.err.println("Only Size 4 implemented, sorry bro :C");
        size = 4;
        _grid = new Grid(size);
        _bar = new UIBar();
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
            // TODO:
            // 10. ??

            i++;
        }

        return new Pair<Cell, String>(c, t);
    }

    public void draw() {
        //Draw Title
        _engine.getGraphics().setColor(0xFF000000);
        if(!_currentTip.equals(""))
        {
            _engine.getGraphics().setFont(_subtitle);
            _engine.getGraphics().drawText(_currentTip,(_engine.getGraphics().getWidth())/2,32);
        }
        else
        {
            _engine.getGraphics().setFont(_title);
            _engine.getGraphics().drawText(Integer.toString(_grid.getSize())+"x"+Integer.toString(_grid.getSize()),(_engine.getGraphics().getWidth())/2,32);
        }

        //Draw Grid
        _engine.getGraphics().setFont(_font);
        _grid.draw(_engine.getGraphics(), _font, image,_cellTip);

        //Draw percentage
        _engine.getGraphics().setFont(_subtitle);
        _engine.getGraphics().setColor(0xFF777777);
        _engine.getGraphics().drawText(Integer.toString(_grid.getPercentage())+"%",(_engine.getGraphics().getWidth()/2),540);

        _bar.Draw();
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
        _bar.Init(_engine.getGraphics());
    }

    @Override
    public void onUpdate(double deltaTime) {
    }

    @Override
    public void onRender() {
        draw();
    }

    @Override
    public void onExit() {
        System.out.println("SALIENDO PAPA");
    }

    @Override
    public void onEvent(TouchEvent event) {
        if(event.get_type() == TouchEvent.EventType.TOUCH)
        {
            if(_grid.processClick(event.get_x(),event.get_y()))
            {
                _currentTip = "";
                _cellTip    = null;
            }
            else
            {
                UIBar.Action a = _bar.HandleClick(event.get_x(),event.get_y());
                if(a!= UIBar.Action.NO_ACTION)
                {
                    switch (a){
                        case CLUE:
                            _currentTip = getTip().snd;
                            _cellTip    = getTip().fst;
                            break;
                        case CLOSE:
                            _engine.changeApp(new Menu(Menu.State.SelectSize));
                            break;
                        case UNDO:
                            _grid.undoMove();
                            break;
                        case NO_ACTION:
                            break;
                    }
                }
            }
        }
    }
    // TO DO:
    // private Text _title;
}