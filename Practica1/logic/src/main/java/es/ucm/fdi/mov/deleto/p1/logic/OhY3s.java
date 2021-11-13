package es.ucm.fdi.mov.deleto.p1.logic;

import java.util.Locale;
import java.util.Random;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;
import es.ucm.fdi.mov.deleto.p1.engine.Vec2;
import es.ucm.fdi.mov.deleto.p1.logic.buttons.Cell;
import es.ucm.fdi.mov.deleto.p1.logic.grid.Clue;
import es.ucm.fdi.mov.deleto.p1.logic.grid.Grid;
import es.ucm.fdi.mov.deleto.p1.logic.tweens.Tween;

public class OhY3s implements IApplication {

    //Engine reference
    IEngine _engine;

    //Logic game objects containing buttons and game state
    private final Grid _grid;
    private final UIBar _bar;

    private Tween _fader = null;

    //Resources
    IFont _font;
    IFont _title;
    IFont _subtitle;

    //Title state
    static String _currentMessage = "";
    float _messageScale = 1f;

    //Title's dot state
    float _dotAlpha = 1;
    Cell.State _dotState;

    //The current cell we are focused on
    Cell _focusedCell = null;

    //Random messages to show on level correctly completed
    final String[] WIN_MESSAGES = new String[]{"Wonderful","Spectacular","Marvelous","Outstanding",
                                                "Remarkable","Shazam","Impressive","Great","Well done",
                                                "Fabulous","Clever","Dazzling","Fantastic","Excellent",
                                                "Nice","Super","Awesome","Ojoo","Brilliant","Splendid",
                                                "Exceptional","Magnificent","Yay"};

    /**
     * Creates a new Grid and Bottom Bar objects.
     * @param size the amount of cells each side of the new square grid will have
     */
    public OhY3s(int size) {
        _grid = new Grid(size);
        _bar = new UIBar();
    }


    /**
     * Load all resources needed and save the engine reference.
     * @param engine instance of engine interface.
     */
    @Override
    public void onInit(IEngine engine) {

        //Engine setup
        _engine = engine;

        //TODO: We should store the clear color as a member of engine and set it here
        _engine.getGraphics().setColor(0xFFFFFFFF);

        //Resource loading
        _font = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",64,true);
        _title = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",60,true);
        _subtitle = _engine.getGraphics().newFont("JosefinSans-Bold.ttf",24,false);

        //Calls init on each object so they can load their resources
        _bar.init(_engine.getGraphics());
        _grid.init(_engine.getGraphics());
    }

    /**
     * Called on each frame, only used to handle animations
     * @param deltaTime time since last frame
     */
    @Override
    public void onUpdate(double deltaTime) {
        if(_fader != null)
        {
            _fader.update(deltaTime);
            Cell.setOpacity(1-_fader.ease());
            if(_fader.finished())
                _engine.changeApp(new Menu(Menu.State.SelectSize));
        }
        _grid.onUpdate(deltaTime);
        if(_focusedCell != null && _dotState != null)
            _dotAlpha +=deltaTime;
    }

    /**
     * Draws title, percentage, and forwards call to both bottom button bar and grid objects
     */
    @Override
    public void onRender() {
        //Draw Title
        drawTitle();

        //Draw Grid
        _engine.getGraphics().setFont(_font);
        _grid.draw(_font);

        //Draw percentage
        _engine.getGraphics().setFont(_subtitle);
        _engine.getGraphics().setColor(0xFF777777);
        _engine.getGraphics().drawText(String.format("%d%%", _grid.getPercentage()),(_engine.getGraphics().getLogicWidth()/2),500);

        //Draw bottom UI bar
        _bar.Draw();
    }

    /**
     * Draws the current message (clue or undo) and the little dot of the suggested clue
     */
    private void drawTitle()
    {
        //Set up title style
        IGraphics g = _engine.getGraphics();
        g.setColor(0xFF000000);
        g.setFont(_title);

        //Local variables
        String title = _currentMessage;
        Vec2<Integer> dot;
        float scale = _messageScale;

        //If there is no message writes NxN, where N is the current grid size
        if(_currentMessage.equals("")) {
            title = String.format(Locale.ENGLISH, "%dx%d",_grid.getSize(),_grid.getSize());
            scale = 1;
        }

        //We draw the text and save the dot position in case we need it
        dot = g.drawText(title,(g.getLogicWidth())/2,46,scale);

        //If we wrote a clue, then we can draw the dot
        if(!_currentMessage.equals("") && _focusedCell != null)
            drawDot(g,dot);
    }

    /**
     * Draws a blinking dot that indicates the suggested clue state (Red or Blue)
     * @param g Graphics context
     * @param dot Dot position
     */
    private void drawDot(IGraphics g, Vec2<Integer>dot)
    {
        //Set up dot style
        g.setOpacity(((float)Math.sin(_dotAlpha *2)+1)/2);
        g.setColor(Cell.getColorByState(_dotState));
        final int r = 6;

        //This xOffset is needed to center the dot and we change it based on the last character of
        //the last string. Space character is used so an empty second line is not ignored
        int xOff =(_currentMessage.charAt(_currentMessage.length()-1) == ' '  ? r - r/2 : r+r/2);

        //Draw and Reset Opacity
        g.fillCircle(dot.x()+ xOff, dot.y()-r,r);
        g.setOpacity(1.f);
    }

    /**
     * Called by engine with every event generated by the platform
     * @param event contains position and type of the current event to process
     */
    @Override
    public void onEvent(TouchEvent event) {

        if(event.type() == TouchEvent.EventType.CLOSE_REQUEST){
            //this happens when we press back button on android build
            _engine.changeApp(new Menu(Menu.State.SelectSize));
            _currentMessage = "Oh Yes";
        }
        //If we are transitioning we ignore all game events
        else if(_fader==null)
        {
            //We don't use moves on this game, so we filter them out
            if(event.type() == TouchEvent.EventType.TOUCH || event.type() == TouchEvent.EventType.RELEASE)
            {
                if(clickOnGrid(event))
                    return;
                if(event.type()== TouchEvent.EventType.RELEASE)
                    clickOnBottomBar(event);
            }
        }

    }

    /**
     * Game grid tries to handle the event
     *
     * @param event touch event to be checked by grid
     * @return whether the event was produced on grid bounding box
     */
    private boolean clickOnGrid(TouchEvent event)
    {
        Grid.ClickResult res = _grid.processClick(event);
        if(res != Grid.ClickResult.MISSED)
        {
            _currentMessage = "";
            if(_focusedCell !=null)
                _focusedCell.unfocus();
            _focusedCell = null;
            _grid.debugCell = null;
            _engine.getAudio().createAndPlay(res == Grid.ClickResult.FREE ? "Click.wav" : "ClickLock.wav");
            checkWin();
            return true;
        }
        return false;
    }

    /**
     * On full board: if we win then we set grid state to transition back to menu, otherwise we get a new clue for incorrect state
     */
    private void checkWin()
    {
        if(_grid.getPercentage() >= 100) {
            if (_grid.checkWin()) {
                _currentMessage = WIN_MESSAGES[new Random().nextInt(WIN_MESSAGES.length)];
                _messageScale = 1;
                _grid.lockCells();
                _engine.getAudio().createAndPlay("Win.wav");

                //Start transition
                double DELAY = 1;
                double FADE_DURATION = 2;
                _fader = new Tween(null,FADE_DURATION,Tween.InterpolationType.easeOut);
                _fader.delay(DELAY);
            } else {
                _engine.getAudio().createAndPlay("ClueFail.wav");
                handleNewClue();
            }
        }
    }

    /**
     * Handles a click on bottom game bar of buttons. Three buttons: Clue, Undo and GoBack
     * @param event
     * @return
     */

    private  boolean clickOnBottomBar(TouchEvent event)
    {
        UIBar.Action action = _bar.HandleClick(event.x(),event.y());
        if(action == UIBar.Action.NO_ACTION) {
            return false;
        }

        //Whether we click on a clue or undo, last focused cell must be reset
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
                //Create a new Menu app on SelectSize state and change to it
                _engine.changeApp(new Menu(Menu.State.SelectSize));
                _currentMessage = "Oh Yes";
                break;
            default:
                throw new IllegalStateException("Unexpected action returned: " + action);
        }
        return  true;
    }

    /**
     *  Resets the Message and focused cell if it was set, otherwise ask for a new clue and display it.
     */
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
                _currentMessage = clue.message();
                _messageScale = 0.5f;
                _focusedCell = clue.cell();
                _focusedCell.focus();
                if (clue.correctState() != null) {
                    _grid.debugCell = _grid.getCell(clue.correctState().col(), clue.correctState().row());
                    _dotState = clue.correctState().getState();
                }
                _dotAlpha = 0;
                _engine.getAudio().createAndPlay("Clue.wav");
            }
        }
    }

    /**
     *  Undoes last player action and display on title message it's previous state
     */
    private void handleUndo()
    {
        Cell undo = _grid.undoMove();
        _messageScale = 0.5f;

        if(undo != null){
            _focusedCell = _grid.getCell(undo.col(), undo.row());
            _focusedCell.focus();
            Cell.State state = undo.getState();
            _currentMessage = String.format("This tile was reversed to %s",
                    state == Cell.State.Grey ? "it's\nempty state." :
                            state == Cell.State.Blue ?  "blue.":"red.");

            _dotState = state;
            _engine.getAudio().createAndPlay("Undo.wav");
        }
        else{
            _currentMessage = "Nothing to undo.";
            _engine.getAudio().createAndPlay("ClueFail.wav");
        }
    }

    /**
     * Exit callback called by engine when this application closes. No resource freeing needed.
     */
    @Override
    public void onExit() {
        System.out.println("Closing 0hY3s");
    }
}