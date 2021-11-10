package es.ucm.fdi.mov.deleto.p1.engine;

/**
 * Simple Vec2 class that contains 2 numbers
 * @param <T> number type to hold
 */
public class Vec2 <T extends Number> {

    private T _x;
    private T _y;

    public Vec2(T x, T y)
    {
        _x=x;
        _y=y;
    }

    public void setX(T x){_x=x;}
    public void setY(T y){_y=y;}
    public void setXY(T x, T y){_x=x;_y=y;}
    public T x() {return _x;}
    public T y() {return _y;}
}
