package es.ucm.fdi.mov.deleto.p1.logic;

import java.util.Random;

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

    String _currentMessage = "";
    Cell _focusedCell = null;
    boolean _newGame = false;
    public OhY3s() {
        _grid = new Grid();
        _bar = new UIBar();
    }

    public void newGame(int size) {
//        if(size > 4)
//            System.err.println("Only Size 4 implemented, sorry bro :C");
//        size = 4;
        _grid.init(size);
    }

    public Clue getTip() {
//        Vector<Cell> cells = _grid.getTipCells();
        Clue c = _grid.getClue();
        if(c != null && c.getCorrectState() != null)
            _grid.pito = _grid.getCell(c.getCorrectState()._x, c.getCorrectState()._y);
        return c;
    }

    public void draw() {
        //Draw Title
        _engine.getGraphics().setColor(0xFF000000);
        if(!_currentMessage.equals(""))
        {
            _engine.getGraphics().setFont(_subtitle);
            _engine.getGraphics().drawText(_currentMessage,(_engine.getGraphics().getWidth())/2,32);
        }
        else
        {
            _engine.getGraphics().setFont(_title);
            _engine.getGraphics().drawText(Integer.toString(_grid.getSize())+"x"+Integer.toString(_grid.getSize()),(_engine.getGraphics().getWidth())/2,32);
        }

        //Draw Grid
        _engine.getGraphics().setFont(_font);
        _grid.draw(_font, image);

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
        _bar.init(_engine.getGraphics());
        _grid.setGraphics(_engine.getGraphics());
    }

    @Override
    public void onUpdate(double deltaTime) {
        if(_newGame)
        {
            newGame(_grid.getSize());
            _newGame = false;
        }
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
        if(event.get_type() == TouchEvent.EventType.RELEASE)
        {
            if(_grid.processClick(event.get_x(),event.get_y()))
            {
                _currentMessage = "";
                if(_focusedCell !=null)
                    _focusedCell.unfocus();
                _focusedCell = null;
                _grid.pito = null;
                if(_grid.getPercentage() == 100) {
                    if (_grid.checkWin()) {
                        System.out.println("NEW GAME");
                        String[] messages = new String[]{
                                "Wonderful",
                                "Spectacular",
                                "Marvelous",
                                "Outstanding",
                                "Remarkable",
                                "Shazam",
                                "Impressive",
                                "Great",
                                "Well done",
                                "Fabulous",
                                "Clever",
                                "Dazzling",
                                "Fantastic",
                                "Excellent",
                                "Nice",
                                "Super",
                                "Awesome",
                                "Ojoo",
                                "Brilliant",
                                "Splendid",
                                "Exceptional",
                                "Magnificent",
                                "Yay"};
                        _engine.changeApp(new Menu(Menu.State.SelectSize,messages[new Random().nextInt(messages.length)] ));
                    } else
                    {
                        Clue t = getTip();
                        _focusedCell = t.getCell();
                        _currentMessage = t.getMessage();
                    }
                }
            }
            else
            {
                UIBar.Action a = _bar.HandleClick(event.get_x(),event.get_y());
                if(a!= UIBar.Action.NO_ACTION)
                {
                    switch (a){
                        case CLUE:
                            _grid.pito = null;
                            Clue tip = getTip();
                            if(_focusedCell !=null)
                                _focusedCell.unfocus();
                            _currentMessage = tip.getMessage();
                            _focusedCell = tip.getCell();
                            _focusedCell.focus();
                            break;
                        case CLOSE:
                            _engine.changeApp(new Menu(Menu.State.SelectSize, "Oh Yes"));
                            break;
                        case UNDO:
                            Cell undo = _grid.undoMove();
                            if(undo != null){
                                _focusedCell = _grid.getCell(undo._x, undo._y);
                                _focusedCell.focus();
                                switch (undo.getState()){
                                    case Grey:
                                        _currentMessage = "This tile was reversed to its empty state.";
                                        break;
                                    case Blue:
                                        _currentMessage = "This tile was reversed to blue.";
                                        break;
                                    case Red:
                                        _currentMessage = "This tile was reversed to red.";
                                        break;
                                    default:
                                        System.err.println("ERROR UNDO: Invalid cell state given");
                                        break;
                                }
                            }
                            else {
                                _currentMessage = "Nothing to undo.";
                            }
                            break;
                        case NO_ACTION:
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + a);
                    }
                }
            }
        }
    }
}