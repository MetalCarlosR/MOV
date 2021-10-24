package es.ucm.fdi.mov.deleto.p1.engine;

public interface IApplication {

    public void onInit(IEngine engine);

    public void onUpdate(double deltaTime);

    public void onRender();

    public void onExit();

    public void onEvent(TouchEvent event);
}
