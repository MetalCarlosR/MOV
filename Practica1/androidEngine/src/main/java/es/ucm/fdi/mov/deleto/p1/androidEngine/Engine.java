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
import es.ucm.fdi.mov.deleto.p1.engine.IInput;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Engine implements IEngine, Runnable {

    Graphics _graphics;
    Input _input;
    Audio _audio;


    volatile Boolean _running = true;
    volatile Boolean _closeEngine = false;
    Thread _renderThread = null;
    IApplication _app;
    IApplication _nextApp = null;
    Context _context;

    ICallable _exitFunction;
    public Engine(IApplication app, Context context, String assetsPath, ICallable exit)
    {
        _graphics = new Graphics(context, assetsPath);
        _input = new Input(_graphics);
        _audio = new Audio(context.getAssets(),assetsPath);
        _app = app;
        _exitFunction = exit;
        _context = context;
    }

    @Override
    public void openWeb(String url) {
        _context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public void resume(){
        Log.d("[Engine]", "resume");

        _running = true;
        _renderThread = new Thread(this);
        _renderThread.start();
    };
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
                    // Esto no debería ocurrir nunca...
                }
            } // while(true)
        } // if (_running)
    };

    @Override
    public void run() {
        if (_renderThread != Thread.currentThread()) {
            throw new RuntimeException("run() should not be called directly");
        }
        while (true) {
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
            } else
                break;
        }
        if(_closeEngine)
        {
            _graphics.release();
            _exitFunction.call();
        }
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
        _running = false;
    }




}
