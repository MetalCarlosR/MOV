package es.ucm.fdi.mov.deleto.p1.logic.tweens;


public class Tween {

    private final ITweenTarget _target;
    private final boolean _loop;

    private double _actualTime;
    private final double _duration;

    private double _factor;

    public enum InterpolationType
    {
        easeIn,
        easeInOut,
        easeOut,
        sin,
        linear
    }

    InterpolationType _type;

    public Tween(boolean loop, double duration, InterpolationType t)
    {
        _loop = loop;
        _duration = duration;
        _type = t;
        _target = null;
    }

    public Tween(ITweenTarget target, double duration, InterpolationType t)
    {
        _duration = duration;
        _target = target;
        _type = t;
        _loop = false;
    }
    public Object get(){
        if(_target!=null)
            return  _target.get();
        else
            return null;
    }

    public double ease(){
        switch (_type)
        {
            case linear:
                return _factor;
            case easeIn:
                return easeInSine(_factor);
            case easeInOut:
                return easeInOutCubic(_factor);
            case easeOut:
                return easeOutQuart(_factor);
            case sin:
                return sin(_factor);
            default:
                return -1;
        }
    }

    public void update(double dt)
    {
        _actualTime+=dt;
        if(finished())
            if(_loop)
                _actualTime = 0;
            else
                return;
        if(_actualTime>0)
        {
            _factor = (_actualTime)/_duration;
            if(_target!=null)
                _target.update(ease());
        }
    }
    public boolean finished()
    {
        return _actualTime > _duration;
    }

    public void delay(double d){
        _actualTime-=d;
    }

    /***
     *Easing functions from: easings.net
     * @param x take a number between 0 and 1
     * @return a number between 0 and 1
     */

    private double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    private double easeOutQuart(double x) {
        return 1 - Math.pow(1 - x, 4);
    }

    private double easeInSine(double x) {
        return 1 - Math.cos((x * Math.PI) / 2);
    }
    private double sin(double x)
    {
        return Math.sin(x);
    }

}