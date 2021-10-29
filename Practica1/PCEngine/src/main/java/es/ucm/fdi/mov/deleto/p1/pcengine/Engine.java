package es.ucm.fdi.mov.deleto.p1.pcengine;

import javax.swing.JFrame;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IInput;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Engine implements IEngine {

    Graphics _graphics;
    Input _input;

    IApplication _app;
    IApplication _nextApp = null;

    boolean running = false;

    public Engine(IApplication app, String appName, String assetsPath) {
        _app = app;
        _graphics = new Graphics(new JFrame(appName),assetsPath);
        _input = new Input();
    }

    public void run() {

        while (true) {
            System.out.println("entrando");

            running = true;
            _app.onInit(this);

            // Vamos allá.
            long lastFrameTime = System.nanoTime();

            long informePrevio = lastFrameTime; // Informes de FPS
            while (running) {
                long currentTime = System.nanoTime();
                long nanoElapsedTime = currentTime - lastFrameTime;
                lastFrameTime = currentTime;
                double elapsedTime = (double) nanoElapsedTime / 1.0E9;
                _app.onUpdate(elapsedTime);
                for (TouchEvent ev : _input.getTouchEvents()) {
                    _app.onEvent(ev);
                }
                _graphics.clear(0xFF000000);
                _app.onRender();
                _graphics.swapBuffers();
            }
            _app.onExit();

            if (_nextApp != null) {
                _app = _nextApp;
                _nextApp = null;
                System.out.println("cagamos");
            } else
                break;
        }
        _graphics.release();
    }

    @Override
    public void exit() {
        running = false;
    }

    @Override
    public IGraphics getGraphics() {
        return _graphics;
    }

    @Override
    public IInput getInput() {
        return _input;
    }

    @Override
    public void changeApp(IApplication newApp) {
        _nextApp = newApp;
        exit();
    }
}