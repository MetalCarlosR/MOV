package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ucm.fdi.mov.deleto.p1.engine.AbstractEngine;
import es.ucm.fdi.mov.deleto.p1.engine.EngineOptions;
import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;


/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/
public class Engine extends AbstractEngine {
    Graphics _graphics;
    Input _input;
    Audio _audio;

    // Map for storing the state when we recover the app
    Map<String,String> _map = null;

    //Android activity context
    Context _context;

    public Engine(IApplication app, Context context, Bundle bundle, EngineOptions options)
    {
        _app = app;
        _context = context;

        _graphics = new Graphics(context, options,this);
        _graphics.setResolution(options.logicWidth,options.logicHeight);
        _graphics.recalculateTransform(options.realWidth, options.realHeight);

        _input = new Input(_graphics.getView());
        _input.setScale((float)_graphics.getScale(),_graphics.getTranslateX(), _graphics.getTranslateY());

        _audio = new Audio(context.getAssets(),options.assetsPath+options.audioPath);

        if(bundle == null)
            return;

        _map = new HashMap<>();
        for(String g : bundle.keySet()) {
            _map.put(g, bundle.getString(g));
        }
        if(_map != null && _map.get("_Saved").equals("True")) {
            restoreState();
        }
    }




    @Override
    protected void pollEvents() {
        //Synchronized call to get events and forward to application
        List<TouchEvent> evs = _input.getTouchEvents();
        for (TouchEvent ev : evs) {
            _app.onEvent(ev);
        }
    }

    @Override
    protected void render() {
        _graphics.prepareFrame();
        _graphics.clear();
        _app.onRender();
        _graphics.present();
    }
    @Override
    protected void closeEngine() {
        //Our thread has died by calling Engine.exit() so we want the Android Application to close
        if(_closeEngine) {
            ((Activity)_context).finish();
            System.exit(0);
        }
    }

    /**************************
     * Temporary State Storage*
     * *  Read saveState() and storeState() on parent class for further information
     **************************/

    /**
     * @return the current state to give AndroidOS to store
     */
    public Map<String, String> getState() {
        saveState();
        return _map;
    }

    /**
     * Serialize and stores the current state of the application, called by Android Kernel
     */
    @Override
    public void saveState() {
        _map = _app.serialize();

        if(_map == null){
            _map = new HashMap<>();
            _map.put("_Saved","False");
        }
        else
            _map.put("_Saved","True");
    }

    /**
     *  Try to find a previous saved state of a game to load.
     *  If it finds it, calls the app to deserialize its contents.
     *  Continues normally otherwise.
     */
    @Override
    public void restoreState() {
        _app.deserialize(_map, this);
        //We need to check if the application has
        //recuested to change app state
        checkNextApp();
    }

    @Override
    public void exit() {
        _closeEngine = true;
        _running = false;
    }

    @Override
    public void openURL(String url) {
        _context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
    /*******************
     * Error handling
     *******************/

    protected void panic(String message)
    {
        new AlertDialog.Builder(_context)
                .setTitle("Fatal Error")
                .setMessage(message)
                .setPositiveButton("Ok",(dialogInterface, i) -> exit())
                .setIcon(android.R.drawable.alert_dark_frame)
                .show();
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
