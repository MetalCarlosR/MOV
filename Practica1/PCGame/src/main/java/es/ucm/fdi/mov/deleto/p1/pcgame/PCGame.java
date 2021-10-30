package es.ucm.fdi.mov.deleto.p1.pcgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.logic.Menu;
import es.ucm.fdi.mov.deleto.p1.logic.OhY3s;
import es.ucm.fdi.mov.deleto.p1.pcengine.Engine;

public class PCGame {
    public static void main(String[] args){
        System.setProperty("sun.awt.noerasebackground", "true");

        _game = new Menu();

        //_game.newGame(size);

        Engine engine = new Engine(_game,"Oh Yes","./assets/");

//        engine.getGraphics().setResolution(1080/2,2220/2);
        engine.getGraphics().setResolution(400,600);

        engine.run();
    }

    static IApplication _game;
    static boolean _running = true;
    static int size = 4;
}