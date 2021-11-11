package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.ICallable;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Engine implements IEngine, Runnable {

    Graphics _graphics;
    Input _input;
    Audio _audio;


    //Main loop stop condition
    volatile Boolean _running = true;

    //This extra boolean is needed because android cycle might end up stopping our main loop
    //but we need to properly recover
    volatile Boolean _closeEngine = false;

    // We need this thread to take control of the rendering and update loop
    Thread _renderThread = null;

    //Android activity context
    Context _context;

    IApplication _app;
    IApplication _nextApp = null;

    //Callback to close application on user demand
    ICallable _exitFunction;

    public Engine(IApplication app, Context context, String assetsPath, ICallable exit,
                  int canvasWidth, int canvasHeight, int screenWidth, int screenHeight)
    {
        _app = app;
        _exitFunction = exit;
        _context = context;

        _graphics = new Graphics(context, assetsPath);
        _graphics.setResolution(canvasWidth,canvasHeight);
        _graphics.setScreenSize(screenWidth,screenHeight);

        _input = new Input(_graphics);
        _input.setScale(_graphics._scale,_graphics._translateX, _graphics._translateY);

        _audio = new Audio(context.getAssets(),assetsPath);
     }

    @Override
    public void openURL(String url) {
        _context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    /**
     * Android specific resume method for application life cycle.
     */
    public void resume(){
        Log.d("[Engine]", "resume");

        _running = true;
        _renderThread = new Thread(this);
        _renderThread.start();
    };

    /**
     * Android specific pause method for application life cycle
     */
    public void pause(){
        Log.d("[Engine]", "pause");
        if (_running) {
            _running = false;
            while (true) {
                try {
                    _renderThread.join();
                    _renderThread = null;
                    break;
                } catch (InterruptedException ie) {
                    ie.printStackTrace(); //this should never happen
                }
            }
        }
    };

    @Override
    public void run() {
        if (_renderThread != Thread.currentThread()) {
            throw new RuntimeException("run() should not be called directly");
        }
        while (_running) {
            _running = true;
            _app.onInit(this);

            long lastFrameTime = System.nanoTime();
            while (_running) {
                long currentTime = System.nanoTime();
                long nanoElapsedTime = currentTime - lastFrameTime;
                lastFrameTime = currentTime;
                double elapsedTime = (double) nanoElapsedTime / 1.0E9;
                _app.onUpdate(elapsedTime);
                List<TouchEvent> evs = _input.getTouchEvents();
                for (TouchEvent ev : evs) {
                    _app.onEvent(ev);
                }
                _graphics.prepareFrame();
                _graphics.clear(0xFFFFFFFF);
                _app.onRender();
                _graphics.present();
            }
            _app.onExit();

            if (_nextApp != null) {
                _app = _nextApp;
                _nextApp = null;
                _running = true;
            } else
                break;
        }

        //Our thread has died by calling Engine.exit() so we want the Android Application to close
        if(_closeEngine)
            _exitFunction.call();
    }

    @Override
    public void exit() {
        _closeEngine = true;
        _running = false;
    }

    @Override
    public Graphics getGraphics() {
        return _graphics;
    }

    public Input getInput() {
        return _input;
    }

    @Override
    public IAudio getAudio() {
        return _audio;
    }

    @Override
    public void changeApp(IApplication newApp) {
        _nextApp = newApp;
        _running = false;
    }
}
