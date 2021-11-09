package es.ucm.fdi.mov.deleto.p1.logic;

import java.util.Random;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.ICallable;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;
import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public class OhY3s implements IApplication {

    IEngine _engine;
    private final Grid _grid;
    private final UIBar _bar;

    IImage image;
    IFont _font;
    IFont _title;
    IFont _subtitle;

    String _currentMessage = "";
    float _messageScale = 1f;
    Cell _focusedCell = null;
    Cell.State _clueState;
    float _focusedOpacity = 1;

    public OhY3s(int size) {
        _grid = new Grid();
        _bar = new UIBar();
        newGame(size);
    }
    public void newGame(int size) {
        _grid.init(size);
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
        _grid.onUpdate(deltaTime);
        if(_focusedCell != null && _clueState != null)
            _focusedOpacity+=deltaTime;
    }

    @Override
    public void onRender() {
        //Draw Title
        drawTitle();

        //Draw Grid
        _engine.getGraphics().setFont(_font);
        _grid.draw(_font, image);

        //Draw percentage
        _engine.getGraphics().setFont(_subtitle);
        _engine.getGraphics().setColor(0xFF777777);
        _engine.getGraphics().drawText(String.format("%d%%", _grid.getPercentage()),(_engine.getGraphics().getLogicWidth()/2),500);

        //Draw bottom UI bar
        _bar.Draw();
    }

    /***
     * Draws the current message (clue or undo) and the little dot of the suggested clue
     */
    private void drawTitle()
    {
        //Set up title style
        IGraphics g = _engine.getGraphics();
        g.setColor(0xFF000000);
        g.setFont(_title);

        String title = _currentMessage;
        Vec2<Integer> dot;
        float scale = _messageScale;

        //If there is no message writes NxN, where N is the current grid size
        if(_currentMessage.equals("")) {
            title = String.format("%dx%d",_grid.getSize(),_grid.getSize());
            scale = 1;
        }

        //We draw the text and save the dot position in case we need it
        dot = g.drawText(title,(g.getLogicWidth())/2,46,scale);

        //If we wrote a clue, then we can draw the dot
        if(!_currentMessage.equals("") && _focusedCell != null)
            drawDot(g,dot);
    }

    /***
     * Draws a blinking dot that indicates the suggested clue state (Red or Blue)
     * @param g Graphics context
     * @param dot Dot position
     */
    private void drawDot(IGraphics g, Vec2<Integer>dot)
    {
        //Set up dot style
        g.setOpacity(((float)Math.sin(_focusedOpacity*2)+1)/2);
        g.setColor(Cell.getColorByState(_clueState));
        final int r = 6;
        //This xOffset is needed to center the dot and we change it based on the last character of
        //the last string. Space character is used so an empty second line is not ignored
        int xOff =(_currentMessage.charAt(_currentMessage.length()-1) == '-'  ? r - r/2:  r+r/2);

        //Draw and Reset Opacity
        g.fillCircle(dot.x()+ xOff, dot.y()-r,r);
        g.setOpacity(1.f);
    }

    @Override
    public void onExit() {
        System.out.println("Closing 0hY3s");
    }

    @Override
    public void onEvent(TouchEvent event) {
        if(event.get_type() == TouchEvent.EventType.RELEASE)
        {
            if(clickOnGrid(event) || clickOnBottomBar(event))
            {
                //playUISound() ?
            }
        }
    }

    /***
     * Game grid tries to handle the event
     *
     * @param event touch event to be checked by grid
     * @return whether the event was produced on grid bounding box
     */
    private boolean clickOnGrid(TouchEvent event)
    {
        if(_grid.processClick(event.get_x(),event.get_y()))
        {
            _currentMessage = "";
            if(_focusedCell !=null)
                _focusedCell.unfocus();
            _focusedCell = null;
            _grid.debugCell = null;
            checkWin();
            return true;
        }
        else
            return false;
    }

    /***
     * On full board: if we win then we set grid state to transition back to menu, otherwise we get a new clue for incorrect state
     */
    private void checkWin()
    {
        if(_grid.getPercentage() == 100) {
            if (_grid.checkWin()) {
                _currentMessage = WIN_MESSAGES[new Random().nextInt(WIN_MESSAGES.length)];
                _messageScale = 1;
                _grid.setTransition(new ICallable() {
                    @Override
                    public void call() {
                        _engine.changeApp(new Menu(Menu.State.SelectSize, _currentMessage));
                    }
                });
            } else
                handleNewClue();
        }
    }
    private  boolean clickOnBottomBar(TouchEvent event)
    {
        UIBar.Action action = _bar.HandleClick(event.get_x(),event.get_y());
        if(action == UIBar.Action.NO_ACTION) {
            return false;
        }

        if(_focusedCell !=null)
            _focusedCell.unfocus();

        switch (action){
            case CLUE:
                handleNewClue();
                break;
            case UNDO:
                handleUndo();
                break;
            case CLOSE:
                _engine.changeApp(new Menu(Menu.State.SelectSize, "Oh Yes"));
                break;
            default:
                throw new IllegalStateException("Unexpected action returned: " + action);
        }
        return  true;
    }

    private void handleNewClue()
    {
        if(_focusedCell != null){
            _currentMessage = "";
            _focusedCell.unfocus();
            _focusedCell = null;
            _grid.debugCell = null;
        }
        else {
            _grid.debugCell = null;
            Clue clue = _grid.getClue();
            if (clue != null) {
                _currentMessage = clue.getMessage();
                _messageScale = 0.5f;
                _focusedCell = clue.getCell();
                _focusedCell.focus();
                if (clue.getCorrectState() != null) {
                    _grid.debugCell = _grid.getCell(clue.getCorrectState()._x, clue.getCorrectState()._y);
                    _clueState = clue.getCorrectState().getState();
                }
                _focusedOpacity = 0;
            }
        }
    }

    private void handleUndo()
    {
        Cell undo = _grid.undoMove();
        _messageScale = 0.5f;

        if(undo != null){
            _focusedCell = _grid.getCell(undo._x, undo._y);
            _focusedCell.focus();
            Cell.State state = undo.getState();
            _currentMessage = String.format("This tile was reversed to %s",
                    state == Cell.State.Grey ? "it's\nempty state." :
                            state == Cell.State.Blue ?  "blue.":"red.");
        }
        else
            _currentMessage = "Nothing to undo.";
    }

    final String[] WIN_MESSAGES = new String[]{"Wonderful","Spectacular","Marvelous","Outstanding",
                                               "Remarkable","Shazam","Impressive","Great","Well done",
                                               "Fabulous","Clever","Dazzling","Fantastic","Excellent",
                                               "Nice","Super","Awesome","Ojoo","Brilliant","Splendid",
                                               "Exceptional","Magnificent","Yay"};
}