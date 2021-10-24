package es.ucm.fdi.mov.deleto.p1.engine;

public interface IEngine {

    public void exit();

    public IGraphics getGraphics();

    public IInput getInput();

    public void changeApp(IApplication newApp);
}