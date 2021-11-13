package es.ucm.fdi.mov.deleto.p1.pcgame;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.logic.gameStates.Menu;
import es.ucm.fdi.mov.deleto.p1.pcengine.Engine;

public class PCGame {
    public static void main(String[] args){

        _game = new Menu();

        Engine engine = new Engine(_game,"Oh Yes","./assets/",400,600);
        engine.getGraphics().setResolution(400,600);
        engine.run();
    }

    static IApplication _game;
}