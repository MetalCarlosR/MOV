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
    public void openURL(String url) {
        _context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    protected void closeEngine() {
        //Our thread has died by calling Engine.exit() so we want the Android Application to close
        if(_closeEngine) {
            ((Activity)_context).finish();
            System.exit(0);
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
        _graphics.clear(0xFFFFFFFF);
        _app.onRender();
        _graphics.present();
    }


    public void restoreState() {
        _app.deserialize(_map, this);
        checkNextApp();
    }

    protected void panic(String message)
    {
        AlertDialog error = new AlertDialog.Builder(_context)
                                .setTitle("Fatal Error")
                                .setMessage(message)
                                .setPositiveButton("Ok",(dialogInterface, i) -> exit())
                                .setIcon(android.R.drawable.alert_dark_frame)
                                .show();
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
