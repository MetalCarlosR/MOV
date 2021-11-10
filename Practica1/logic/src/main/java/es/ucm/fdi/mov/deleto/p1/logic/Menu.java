package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Menu implements  es.ucm.fdi.mov.deleto.p1.engine.IApplication{

    /**
     * State
     */
    public enum State{Initial, SelectSize}
    State _state = State.Initial;
    String _titleStr = "Oh Yes";

    static final int BUTTON_RAD = 35;
    IEngine _engine;

    /**
     * Resources
     */
    IFont _titleFont;
    IFont _winMessageFont;
    IFont _regular;

    IImage _logo;
    IImage _exit;

    /**
     * Buttons
     */
    CircleButton[] _sizeButtons;
    RectangleButton _startGameButton;
    RectangleButton _exitButton;
    RectangleButton _creditButton;


    //Default constructor
    public Menu(){}

    //Constructor called by game when the puzzle is completed or on back button pressed
    public Menu(State state, String titleStr){
        _state = state;
        _titleStr = titleStr;
    }

    /**
     * Resource creation
     * @param engine interface to create platform depending resources
     */
    @Override
    public void onInit(IEngine engine) {
        _engine = engine;
        IGraphics g = _engine.getGraphics();
        g.setOpacity(1);

        _titleFont = g.newFont("Molle-Regular.ttf",72,false);
        _winMessageFont = g.newFont("JosefinSans-Bold.ttf",60,true);

        _regular = g.newFont("JosefinSans-Bold.ttf",24,false);

        _exit = g.newImage("close.png");
        _logo = g.newImage("q42.png");

        _startGameButton = new RectangleButton(g.getLogicWidth()/2,g.getLogicHeight()/2,56,56) {
            @Override
            protected void clickCallback() {
                _state = State.SelectSize;
            }
        };

        _exitButton = new RectangleButton(g.getLogicWidth()/2,g.getLogicHeight()-_exit.getHeight(),_exit.getWidth(),_exit.getHeight()) {
            @Override
            protected void clickCallback() {
                _engine.exit();
            }
        };

        _creditButton = new RectangleButton(g.getLogicWidth()/2,g.getLogicHeight()-132,200,36) {
            @Override
            protected void clickCallback() {
                _engine.openURL("https://github.com/MetalCarlosR/MOV/tree/main/Practica1");
            }
        };

        _sizeButtons = new CircleButton[6];

        /**
         * Create centered 'Select Size' buttons
         */
        final int padding = 20;
        final int offset = padding+(g.getLogicWidth()/3)-BUTTON_RAD*3;
        final int margin = (g.getLogicWidth() - offset*2 - BUTTON_RAD*2)/2;
        for (int i = 0; i < 6; i++) {
            final int size = 4+i;
            final int x = margin+ offset*(i%3) + BUTTON_RAD*(i%3);
            final int y = 240 + offset*(i/3)+ BUTTON_RAD*(i/3);
            _sizeButtons[i] = new CircleButton(x,y,BUTTON_RAD) {
                @Override
                protected void clickCallback() {
                    _engine.changeApp(new OhY3s(size));
                }
            };
        }
    }



    /**
     * Check if any button can handle the event
     * @param event the event to handle
     */
    @Override
    public void onEvent(TouchEvent event) {
        //We ignore all non release buttons
        //Possible improvements adding hover, click and release callbacks to buttons
        if(event.type() == TouchEvent.EventType.CLOSE_REQUEST)
            _engine.exit();
        if(event.type() != TouchEvent.EventType.RELEASE)
            return;

        int x = event.x();
        int y = event.y();

        //On Initial state check the 'Start Game' and 'Credit button'
        if(_state == State.Initial)
        {
            if(_startGameButton.click(x,y))
                return;
            _creditButton.click(x,y);
        }
        else if(_state == State.SelectSize)
        {
            //We try to click on every select size button
            for(IClickable button : _sizeButtons)
                if(button.click(x,y))
                    return;

            //If no other button was clicked, then check if exit button was pressed
            _exitButton.click(x,y);
        }
    }


    /**
     * Draws all menu elements based on menu state
     */
    @Override
    public void onRender() {
        IGraphics g = _engine.getGraphics();

        /**
         * We always draw the title independent of state
         */
        int y = 46;
        if(_titleStr.equals("Oh Yes"))
        {
            y+=8;
            g.setFont(_titleFont);
        }
        else
            g.setFont(_winMessageFont);
        g.setColor(0xff000000);

        //Draw title and increase y by returned last drawn y position
        y+=g.drawText(_titleStr, g.getLogicWidth()/2,y).y();

        //Rest of texts use this font
        g.setFont(_regular);

        //Draw rest based on state
        switch (_state)
        {
            /**
             * Draw the play button and credits
             */
            case Initial:
                //Draw play button
                g.drawText("Play", _startGameButton._posX, _startGameButton._posY, 2.5);

                //Draw bottom credits
                g.setColor(0xffa0a0e0);
                g.drawText("Deleto Studios copy of a Q42", g.getLogicWidth()/2,g.getLogicHeight()-132);
                g.setColor(0xffc0c0c0);
                g.drawText("game made by Martin Kool",     g.getLogicWidth()/2,g.getLogicHeight()-94);

                g.drawImage(_logo,g.getLogicWidth()/2,g.getLogicHeight()-32,0.05f,0.05f);
                break;
            /**
             * Draws subtitle, select size buttons and exit button
             */
            case SelectSize:
                //Draw subtitle
                g.drawText("Choose a size to play", g.getLogicWidth()/2,y);

                //Draw select size buttons
                for(int i = 0; i<6; i++)
                {
                    int btX = _sizeButtons[i]._posX; int btY = _sizeButtons[i]._posY;

                    //Draw circle button with alternating color
                    g.setColor(i%2==0 ? 0xFF1CC0E0 : 0xFFFF384B);
                    g.fillCircle(btX,btY, BUTTON_RAD);
                    g.setColor(0xffffffff);

                    //Draw number on circle center
                    g.drawText(Integer.toString(4+i),btX,btY,1.5);
                }

                //Draw exit button
                g.setOpacity(0.6f);
                g.drawImage(_exit, _exitButton._posX, _exitButton._posY, 1.f,1.f);
                g.setOpacity(1.f);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + _state);
        }
    }

    /**
     * No need for update, we don't have animations on menu
     */
    @Override
    public void onUpdate(double deltaTime) {}

    /**
     * No need for exit
     */
    @Override
    public void onExit() {}
}
