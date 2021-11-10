package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class Cell extends CircleButton{

    public void setTransform(int x, int y, int r, double scale)
    {
        _posX = x;
        _posY = y;
        _rad = r;
        _scale = scale;
    }

    //Si pones el opacity a -1 estás en realidad diciendo que pinte los números e ignore el locked. De nada
    public void setOpacity(double opacity) {
        _opacity=opacity;
    }

    enum State{ Grey, Blue, Red }


    public Cell(int x, int y, State s)
    {
        super(null);
        _x=x;
        _y=y;
        _locked=false;
        _neigh = 0;
        _state = s;
        _opacity = 1;
    }

    public Cell(String data, int x, int y)
    {
        super(null);
        setCell(data);
        _x=x;
        _y=y;
    }


    public void onUpdate(double deltaTime) {
        if(_focus)
        {
            _focusTime+=deltaTime;
        }
        if(_excitedTimer > 0)
        {
            _excitedTimer -=deltaTime;
        }
    }
    public void draw(IGraphics graphics, IImage lock, IFont font, int color) {
        graphics.setOpacity(Math.max(_opacity==-1?1:(float)_opacity,0));

        Cell.State state = getState();

        double radius = _rad;

        if(_excitedTimer > 0)
            radius+=(((int)((easeInOutCubic(_excitedTimer / EXCITED_DURATION))*8))%2)*2;;
        if(_focus)
        {
            radius += (((Math.sin(_focusTime*2)+1)/2)*3);
            graphics.setColor(0xFF000000);
            graphics.fillCircle(_posX,_posY,(radius+ (double) _focusRingSize));
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
    static public int getColorByState(Cell.State state)
    {
        return state == Cell.State.Blue ?0xFF1CC0E0 : state == Cell.State.Red ? 0xFFFF384B : 0xFFEEEEEE;
    }

    public void draw(IGraphics graphics, IImage lock, IFont font) {
        draw(graphics,lock,font,getColorByState(_state));
    }

    /***
     * Method to initialize a cell by passing a string. Used when reading maps from files
     *
     * @param data string of structure "N{f/l}" where N is the number of neighbours,
     *             l means its locked and f means its free
     *
     *             Example: "0f 0l 2l 2f"
     */
    private void setCell(String data){
        if(data == null)
        {
            System.err.println("Invalid data string given to SetCell");
            return;
        }
        _neigh = Character.getNumericValue(data.charAt(0));
        _locked = data.charAt(1) == 'l';
        if(_locked){
            if(_neigh > 0)
                _state = State.Blue;
            else _state = State.Red;
        }
    }

    public void setState(State s){
        _state = s;
    }

    public boolean changeState(){
        if(_locked)
            return false;
        else{
            _state = _state == State.Blue ? State.Red : _state == State.Red ? State.Grey : State.Blue;
            return true;
        }
    }

    public State getState() {
        return _state;
    }

    public int getNeigh() {
        return _neigh;
    }

    public void showLockedGraphics()
    {
        _showLockedGraphics = !_showLockedGraphics;
        _excitedTimer = EXCITED_DURATION;
    }

    private double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    public boolean isLocked() {
        return _locked;
    }
    public  void lock()
    {
        _locked=true;
    }
    public  void unlock()
    {
        _locked=false;
    }
    public void unfocus() {
        _focus = false;
    }
    public void focus(){
        _focus = true;
    }
    public void setNeigh(int n)
    {
        _neigh=n;
    }
    private int _neigh = 0;
    private boolean _locked = false;
    private boolean _focus = false;
    int _x = 0;
    int _y = 0;
    private double _scale;

    int _focusRingSize = 2;
    double _focusTime = 0;
    double _opacity = 1;

    double _excitedTimer;
    static final double EXCITED_DURATION = 0.75;

    private State _state = State.Grey;

    static private boolean _showLockedGraphics = false;
}
