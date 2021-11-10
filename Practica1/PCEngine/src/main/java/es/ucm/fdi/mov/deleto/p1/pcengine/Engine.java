package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.util.List;

import javax.swing.JFrame;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IInput;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Engine implements IEngine {
    Graphics _graphics;
    Input _input;
    Audio _audio;

    IApplication _app;
    IApplication _nextApp = null;

    boolean running = false;

    public Engine(IApplication app, String appName, String assetsPath) {
        _app = app;
        _graphics = new Graphics(appName,assetsPath);
        _audio = new Audio(assetsPath);
        _input = new Input(_graphics);
    }

    public void run() {

        while (true) {
            running = true;
            _app.onInit(this);

            long lastFrameTime = System.nanoTime();
            while (running) {
                long currentTime = System.nanoTime();
                long nanoElapsedTime = currentTime - lastFrameTime;
                lastFrameTime = currentTime;
                double elapsedTime = (double) nanoElapsedTime / 1.0E9;
                _app.onUpdate(elapsedTime);
                List<TouchEvent> evs = _input.getTouchEvents();
                for (TouchEvent ev : evs) {
                    _app.onEvent(ev);
                }
                do {
                    _graphics.clear(0xFFFFFFFF);
                    _app.onRender();
                }while(_graphics.swapBuffers());

            }
            _app.onExit();

            if (_nextApp != null) {
                _app = _nextApp;
                _nextApp = null;
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
    public IAudio getAudio() {
        return _audio;
    }

    @Override
    public void changeApp(IApplication newApp) {
        _nextApp = newApp;
        exit();
    }
}