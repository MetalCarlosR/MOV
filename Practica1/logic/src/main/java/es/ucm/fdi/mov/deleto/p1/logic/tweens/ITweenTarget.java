package es.ucm.fdi.mov.deleto.p1.logic.tweens;

/**
 * Read Tween class for more information on this interface.
 */

public interface ITweenTarget<T>{
    void update(double delta);
    T get();
}
