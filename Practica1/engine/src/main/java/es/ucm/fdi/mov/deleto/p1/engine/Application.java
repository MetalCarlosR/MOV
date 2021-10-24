package es.ucm.fdi.mov.deleto.p1.engine;

public interface Application {

    public void onInit();

    public void onUpdate();

    public void onRender();

    public void onExit();

    public void onEvent(TouchEvent event);
}
