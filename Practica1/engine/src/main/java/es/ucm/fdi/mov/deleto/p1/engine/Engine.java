package es.ucm.fdi.mov.deleto.p1.engine;

public interface Engine {

    public void exit();

    public Graphics getGraphics();

    public Input getInput();

    public void changeApp(Application newApp);
}