package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.security.cert.TrustAnchor;

import es.ucm.fdi.mov.deleto.p1.engine.Application;
import es.ucm.fdi.mov.deleto.p1.engine.Graphics;
import es.ucm.fdi.mov.deleto.p1.engine.Input;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Engine implements es.ucm.fdi.mov.deleto.p1.engine.Engine {

    Graphics _graphics;
    Input _input;

    Application _app;
    Application _nextApp = null;

    boolean running = false;

    public Engine(Application app) {
        _app = app;
        _graphics = new es.ucm.fdi.mov.deleto.p1.pcengine.Graphics();
        _input = new es.ucm.fdi.mov.deleto.p1.pcengine.Input();
    }

    public void run() {

        while (true) {
            running = true;
            _app.onInit();
            while (running) {
                _app.onUpdate();
                for (TouchEvent ev : _input.getTouchEvents()) {
                    _app.onEvent(ev);
                }
                _app.onRender();
            }
            _app.onExit();

            if (_nextApp != null) {
                _app = _nextApp;
                _nextApp = null;
            } else
                break;
        }
    }

    @Override
    public void exit() {
        running = false;
    }

    @Override
    public Graphics getGraphics() {
        return _graphics;
    }

    @Override
    public Input getInput() {
        return _input;
    }

    @Override
    public void changeApp(Application newApp) {
        _nextApp = newApp;
        exit();
    }
}