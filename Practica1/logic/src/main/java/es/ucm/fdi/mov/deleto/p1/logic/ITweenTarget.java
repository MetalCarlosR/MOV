package es.ucm.fdi.mov.deleto.p1.logic;

public interface ITweenTarget<T>{
    void update(double delta);
    T get();
}
