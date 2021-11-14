package es.ucm.fdi.mov.deleto.p1.pcgame;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.logic.Constants;
import es.ucm.fdi.mov.deleto.p1.logic.gameStates.Menu;
import es.ucm.fdi.mov.deleto.p1.pcengine.Engine;

public class PCGame {
    public static void main(String[] args){

        _game = new Menu();

        Constants.OPTIONS.assetsPath = "./assets/";
        Constants.OPTIONS.realHeight = 600;
        Constants.OPTIONS.realWidth = 400;
        Engine engine = new Engine(_game,"Oh Yes",Constants.OPTIONS, "Are you sure?");
        engine.start();
    }

    static IApplication _game;
}