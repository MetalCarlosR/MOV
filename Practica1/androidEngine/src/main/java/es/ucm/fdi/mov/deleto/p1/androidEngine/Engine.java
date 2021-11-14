package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;


/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/
public class Engine implements IEngine, Runnable {

    Graphics _graphics;
    Input _input;
    Audio _audio;


    //Main loop stop condition
    volatile Boolean _running = true;

    //This extra boolean is needed because android cycle might end up stopping our main loop
    //but we need to properly recover
    volatile Boolean _closeEngine = false;

    // Map for storing the state when we recover the app
    Map<String,String> _map = null;

    // We need this thread to take control of the rendering and update loop
    Thread _renderThread = null;

    //Android activity context
    Context _context;

    IApplication _app;
    IApplication _nextApp = null;

    public Engine(IApplication app, Context context, String assetsPath,
                  int canvasWidth, int canvasHeight, int screenWidth, int screenHeight, Bundle bundle)
    {
        _app = app;
        _context = context;

        _graphics = new Graphics(context, assetsPath);
        _graphics.setResolution(canvasWidth,canvasHeight);
        _graphics.recalculateTransform(screenWidth,screenHeight);

        _input = new Input(_graphics.getView());
        _input.setScale((float)_graphics.getScale(),_graphics.getTranslateX(), _graphics.getTranslateY());

        _audio = new Audio(context.getAssets(),assetsPath);

        if(bundle == null)
            return;

        _map = new HashMap<>();
        for(String g : bundle.keySet()) {
            _map.put(g, bundle.getString(g));
        }
        if(_map != null && _map.get("_Saved").equals("True")) {
            restoreState(_map);
        }

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


            //Init app and start measuring time
            _app.onInit(this);

            long lastFrameTime = System.nanoTime();

            while (_running) {
                //Get delta time and call update
                long currentTime = System.nanoTime();
                long nanoElapsedTime = currentTime - lastFrameTime;
                lastFrameTime = currentTime;
                double elapsedTime = (double) nanoElapsedTime / 1.0E9;

                _app.onUpdate(elapsedTime);

                //Synchronized call to get events and forward to application
                List<TouchEvent> evs = _input.getTouchEvents();
                for (TouchEvent ev : evs) {
                    _app.onEvent(ev);
                }
                _graphics.prepareFrame();
                _graphics.clear(0xFFFFFFFF);
                _app.onRender();
                _graphics.present();
            }
            //running has been set to false, even on switch app want to call onExit
            _app.onExit();

            //if we have a requested next app, then set running to true and switch to it
            checkNextApp();
        }

        //Our thread has died by calling Engine.exit() so we want the Android Application to close
        if(_closeEngine) {
            ((Activity)_context).finish();
            System.exit(0);
        }
    }

    private void checkNextApp() {
        if (_nextApp != null) {
            _app = _nextApp;
            _nextApp = null;
            _running = true;
        }
    }

    /**
     *
     * @param map  ---------------------------------------------------------------------------------
     */
    public void restoreState(Map<String, String> map) {
        _app.deserialize(map, this);
        checkNextApp();
    }


    /**
     * Serialize and returns the current state of the application
     */
    public Map<String, String> getState() {

        Map<String,String> map = _app.serialize();

        if(map == null){
            map = new HashMap<>();
            map.put("_Saved","False");
        }
        else
            map.put("_Saved","True");

        return map;
    }

    @Override
    public void exit() {
        _closeEngine = true;
        _running = false;
    }

    @Override
    public void changeApp(IApplication newApp) {
        _nextApp = newApp;
        _running = false;
    }

    /***********
     * Getters *
     ***********/
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

}
