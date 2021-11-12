package es.ucm.fdi.mov.deleto.p1.logic;


public class Tween {

    ITweenTarget _target;

    private double _actualTime;
    double _duration;

    private double scale;

    public enum InterpolationType
    {
        easeIn,
        easeInOut,
        easeOut,
        LINEAR
    }

    InterpolationType _type;


    public Tween(ITweenTarget target, double duration, InterpolationType t)
    {
        _duration = duration;
        _target = target;
        _type = t;
    }
    public Object get(){return  _target.get();}
    public void update(double dt)
    {
        _actualTime+=dt;
        if(finished())
           return;
        scale = (_actualTime)/_duration;
        System.out.println(scale);

        switch (_type)
        {
            case LINEAR:
                _target.update(scale);
                break;
            case easeIn:
                _target.update(easeInSine(scale));
                break;
            case easeInOut:
                _target.update(easeInOutCubic(scale));
                break;
            case easeOut:
                _target.update(easeOutQuart(scale));
                break;
        }
    }
    public boolean finished()
    {
        return _actualTime > _duration;
    }

    private double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    private double easeOutQuart(double x) {
        return 1 - Math.pow(1 - x, 4);
    }

    private double easeInSine(double x) {
        return 1 - Math.cos((x * Math.PI) / 2);
    }

    //Easing functions from: easings.net
}

/*
*
*
* Cell
*
*
* void setTween(0xff00ff)
* * {
* * * _teweener = new Tween(new ColorModulator(Cell.GetColorFromState(_state,Cell.getPreviousState)));
* * }
* void setTween(0.f)
* void tween(double t)
* {
*    0xff00ff00 -> 0xffff0000
* }
*
*
*
*
*
* */