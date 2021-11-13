package es.ucm.fdi.mov.deleto.p1.logic.buttons;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.logic.tweens.ColorModulator;
import es.ucm.fdi.mov.deleto.p1.logic.tweens.Tween;

public class Cell extends CircleButton{

    /**
     * Common state for all cells:
     *      _scale: factor to apply to radius based on board size
     *      _showLockedGraphics: when we click on a locked cell we show all locked cells
     */
    static private boolean _showLockedGraphics = false;
    static private double _scale = 1;


    /**
     * Logic state
     */
    public enum State{ Grey, Blue, Red }
    private State _state = State.Grey;

    private int _neigh = 0;                 // Amount of blue Cells we can see in all directions {N,S,E,W}
    private boolean _locked = false;        // Whether the player can change the state of the cell or not
    private final int _col;                 // Grid column
    private final int _row;                 // Grid row


    /**
     * Graphic state
     */
    static final int _focusRingSize = 2; // The amount of pixels the focused cell ring occupies
    static  double _opacity = 1;         // Used when transitioning, fades out the circles

    boolean _excited;                    // Used to animate cell if it is excited i.e. clicked when locked
    static final double EXCITED_DURATION = 0.75; // Amount of seconds the excitement lasts

    private boolean _focus = false;      // Focused cells are rendered with a ring on it and an animation

    private Tween _tweener;

    /**
     * Default cell constructor
     * @param x column
     * @param y row
     * @param s state to be initialized with
     */
    public Cell(int x, int y, State s)
    {
        _col =x;
        _row =y;
        _state = s;
    }

    /**
     * Constructor used by methods that construct a known cell form a string
     * @param data string that defines state, see setCell(String) method below
     * @param x column
     * @param y row
     */
    public Cell(String data, int x, int y)
    {
        setCell(data);
        _col =x;
        _row =y;
    }

    /**
     * Method to initialize a cell by passing a string. Used when reading maps from files
     *
     * @param data string of structure "N{f/l}" where N is the number of neighbours,
     *             l means its locked and f{b/r/g} means its free and its color
     *
     *             Examples: "0f" "0l" "2l" 2f"
     */
    private void setCell(String data){
        if(data == null)
        {
            System.err.println("Invalid data string given to SetCell");
            return;
        }
        _neigh = Character.getNumericValue(data.charAt(0));
        char def = data.charAt(1);
        _locked = def == 'l';
        if(_locked){
            if(_neigh == 0)
                _state = State.Red;
            else
                _state = State.Blue;
        }
        else
            _state = def == 'b' ? State.Blue : def == 'r' ? State.Red : State.Grey;
    }

    public String toString()
    {
        String s = Integer.toString(_neigh)+(_locked ? 'l':(_state==State.Blue)? 'b':(_state==State.Red)?'r':'g');
        System.out.println(s);
        return  s;
    }

    /**
     * Method to be called on successful click, defined by CircleButton super class
     */
    @Override
    public void clickCallback(){
        if(!_locked) {
            _state = _state == State.Blue ? State.Red : _state == State.Red ? State.Grey : State.Blue;
            _tweener = new Tween(new ColorModulator(getColorByState(getPreviousState()),getColorByState(_state)),0.500, Tween.InterpolationType.easeOut);
        }
        else
            toggleLockedGraphics();
    }

    /**
     * Updates animation timers
     * @param deltaTime time since previous update
     */
    public void onUpdate(double deltaTime) {
        if(_tweener != null)
            _tweener.update(deltaTime);
    }

    /**
     *  Re-starts the excited timer and toggles the boolean that indicates if we show locked cells
     *  or not
     */
    private void toggleLockedGraphics()
    {
        _showLockedGraphics = !_showLockedGraphics;
        _excited = true;
        _tweener = new Tween(null,EXCITED_DURATION, Tween.InterpolationType.easeInOut);
    }

    /**
     * Draws a cell with given color, image and font only if needed by state
     * @param graphics the graphics object to paint with
     * @param lock the image to use if we are showing locked graphics and this is a red cell
     * @param font the font to use on blue locked cells
     * @param color the color to paint this cell with
     */
    public void draw(IGraphics graphics, IImage lock, IFont font, int color) {
        graphics.setOpacity(Math.max((float)_opacity,0));

        double radius = (double) getRad();

        if(_excited)
        {
            radius-=(((int)(_tweener.ease()*8))%2)*2;
            _excited = !_tweener.finished();
        }
        if(_focus)
        {
            graphics.setColor(0xFF000000);
            graphics.fillCircle(_posX,_posY,(radius+ (double) _focusRingSize));
            radius -= ((_tweener.ease())*3)*_scale;
        }
        graphics.setColor(color);
        graphics.fillCircle(_posX,_posY,radius);

        if(getState() == Cell.State.Blue && isLocked())
        {
            graphics.setColor(0xFFFFFFFF);
            graphics.setFont(font);
            graphics.drawText(Integer.toString(getNeigh()), _posX,_posY,(_scale));
        }
        else if(_showLockedGraphics && getState() == Cell.State.Red && isLocked())
        {
            graphics.setOpacity((float)Math.max(Math.min(_opacity,0.2),0));
            graphics.drawImage(lock, _posX,_posY,(float)(0.65f*_scale),(float)(0.65f*_scale));
            graphics.setOpacity(Math.max((float)_opacity,0));
        }
    }

    /**
     * Function that calls draw with color by state
     */
    public void draw(IGraphics graphics, IImage lock, IFont font) {
        draw(graphics,lock,font,(_tweener == null || _tweener.get() == null) ? getColorByState(_state) : (Integer)_tweener.get());
    }

    /**
     * Returns color corresponding to given state. Used to draw cells and the clue hinting color.
     * @param state Cell.State to get corresponding color from
     * @return color in integer
     */
    static public int getColorByState(Cell.State state)
    {
        return state == Cell.State.Blue ?0xFF1CC0E0 : state == Cell.State.Red ? 0xFFFF384B : 0xFFEEEEEE;
    }


    /********************************************************
     * Setters and getters, nothing worth notting from here *
     *******************************************************/

    public void setTransform(int x, int y, int r)
    {
        _posX = x;
        _posY = y;
        setRad(r);
    }

    public void setState(State s){
        _state = s;
    }

    public static void setOpacity(double opacity) {
        _opacity=opacity;
    }

    public static void setScale(double s)
    {
        _scale = s;
    }

    public void lock()
    {
        _locked=true;
    }

    public void unlock()
    {
        _locked=false;
    }

    public void unfocus() {
        _focus = false;
    }

    public void focus(){
        _focus = true;
        _tweener = new Tween(true,1, Tween.InterpolationType.easeInOut);
    }

    public void setNeigh(int n)
    {
        _neigh=n;
    }

    public State getState() {
        return _state;
    }

    //Used by undo, gets the logical previous state
    public State getPreviousState()
    {
        return _state == State.Blue ? State.Grey : _state == State.Red ? State.Blue : State.Red;
    }

    public int getNeigh() {
        return _neigh;
    }

    public boolean isLocked() {
        return _locked;
    }
    public int col()
    {
        return _col;
    }
    public int row()
    {
        return _row;
    }

}
