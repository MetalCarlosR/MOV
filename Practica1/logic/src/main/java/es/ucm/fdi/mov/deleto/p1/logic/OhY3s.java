package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class OhY3s implements IApplication {

    IEngine _engine;
    private Grid _grid;
    private UIBar _bar;

    double aaaa = 0;
    IImage image;

    public OhY3s() {

    }

    public void newGame(int size) {
        _grid = new Grid(size);
    }

    public void click(int x, int y) {
        _grid.changeState(x, y);
    }

    public String getTip() {
        return "Activate monke mode";
    }

    public void draw() {
        _grid.draw();
    }

    @Override
    public void onInit(IEngine engine) {
        _engine = engine;
        _engine.getGraphics().setColor(0xFF0000FF);
        image = _engine.getGraphics().newImage("close.png");
    }

    @Override
    public void onUpdate(double deltaTime) {
        aaaa += deltaTime * 100;
    }

    @Override
    public void onRender() {
        _engine.getGraphics().fillCircle((int)aaaa % _engine.getGraphics().getWidth() - 50, 100, 100);
        _engine.getGraphics().drawImage(image,(int)aaaa % _engine.getGraphics().getWidth() - 50, 600, 1, 1);
    }

    @Override
    public void onExit() {

    }

    @Override
    public void onEvent(TouchEvent event) {

    }
    // TO DO:
    // private Text _title;
}