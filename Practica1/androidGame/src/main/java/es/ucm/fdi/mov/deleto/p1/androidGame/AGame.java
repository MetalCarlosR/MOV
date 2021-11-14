package es.ucm.fdi.mov.deleto.p1.androidGame;


import android.graphics.Point;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import es.ucm.fdi.mov.deleto.p1.androidEngine.Engine;
import es.ucm.fdi.mov.deleto.p1.engine.EngineOptions;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;
import es.ucm.fdi.mov.deleto.p1.logic.Constants;
import es.ucm.fdi.mov.deleto.p1.logic.gameStates.Menu;

public class AGame extends AppCompatActivity{

    Engine _engine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        Constants.OPTIONS.realWidth=size.x;
        Constants.OPTIONS.realHeight=size.y;
        _engine = new Engine(new Menu(),this,savedInstanceState, Constants.OPTIONS);

        this.setTheme(R.style.Theme_AppCompat_NoActionBar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(_engine.getGraphics().getView());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        _engine.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        _engine.pause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Map<String,String> map = _engine.getState();
        for(String g : map.keySet()) {
            outState.putString(g,map.get(g));
        }
    }

    /**
     * We can only capture here the onBackPressed event so we forward it to the engine.
     * This event occurs when the user presses the back button on the device
     */
    @Override
    public void onBackPressed() {
        _engine.getInput().newTouchEvent(TouchEvent.EventType.CLOSE_REQUEST,0,0,1);
    }
}