package es.ucm.fdi.mov.deleto.p1.logic;

import com.sun.tools.javac.util.Pair;

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
    float _messageScale = 1f;
    Cell _focusedCell = null;
    Cell.State _clueState;
    float _focusedOpacity = 1;

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
        _messageScale = 0.5f;
        if(c != null && c.getCorrectState() != null)
            _grid.pito = _grid.getCell(c.getCorrectState()._x, c.getCorrectState()._y);
        return c;
    }

    public void draw() {
        //Draw Title
        _engine.getGraphics().setColor(0xFF000000);
        if(!_currentMessage.equals(""))
        {
            _engine.getGraphics().setFont(_title);
            Pair<Integer, Integer> dot = _engine.getGraphics().drawText(_currentMessage,(_engine.getGraphics().getWidth())/2,46,_messageScale);
            if(_focusedCell!=null)
            {
                _engine.getGraphics().setOpacity(((float)Math.sin(_focusedOpacity)+1)/2);
                _engine.getGraphics().setColor(_clueState == Cell.State.Blue ?0xFF1CC0E0 : _clueState == Cell.State.Red ? 0xFFFF384B : 0xFFEEEEEE);
                final int r = 6;
                int xOff =(_currentMessage.charAt(_currentMessage.length()-1) == ' '  ? r - r/2:  r+r/2);
                _engine.getGraphics().fillCircle(dot.fst+ xOff, dot.snd-r,r);
                _engine.getGraphics().setOpacity(1.f);
            }
        }
        else
        {
            _engine.getGraphics().setFont(_title);
            _engine.getGraphics().drawText(Integer.toString(_grid.getSize())+"x"+Integer.toString(_grid.getSize()),(_engine.getGraphics().getWidth())/2,46);
        }

        //Draw Grid
        _engine.getGraphics().setFont(_font);
        _grid.draw(_font, image);

        //Draw percentage
        _engine.getGraphics().setFont(_subtitle);
        _engine.getGraphics().setColor(0xFF777777);
        _engine.getGraphics().drawText(Integer.toString(_grid.getPercentage())+"%",(_engine.getGraphics().getWidth()/2),500);

        _bar.Draw();
    }


    @Override
    public void onInit(IEngine engine) {
        _engine = engine;
        _engine.getGraphics().setColor(0xFFFFFFFF);
        image = _engine.getGraphics().newImage("lock.png");
        _font = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",64,true);
        _title = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",60,true);
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
        }else{
            _grid.onUpdate(deltaTime);
            if(_focusedCell != null && _clueState != null)
                _focusedOpacity+=deltaTime;
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
                        final String selectedTitle = messages[new Random().nextInt(messages.length)];
                        _currentMessage = selectedTitle;
                        _messageScale = 1;

                        _grid.setTransition(new ICallable() {
                            @Override
                            public void call() {
                                _engine.changeApp(new Menu(Menu.State.SelectSize, selectedTitle));
                            }
                        });
                    } else
                    {
                        Clue t = getTip();
                        _focusedOpacity = 0;
                        _focusedCell = t.getCell();
                        _currentMessage = t.getMessage();
                        _clueState = t.getCorrectState().getState();
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
                            _clueState = tip.getCorrectState().getState();
                            _focusedOpacity = 0;
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
                                        _currentMessage = "This tile was reversed to it's\nempty state.";
                                        break;
                                    case Blue:
                                        _currentMessage = "This tile was reversed to blue.";
                                        break;
                                    case Red:
                                        _currentMessage = "This tile was reversed to red.";
                                        break;
                                    default:
                                        _currentMessage = "ERROR UNDO: Invalid cell state given";
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