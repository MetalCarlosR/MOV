package es.ucm.fdi.mov.deleto.p1.androidGame;


import android.graphics.Point;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import es.ucm.fdi.mov.deleto.p1.androidEngine.Engine;
import es.ucm.fdi.mov.deleto.p1.engine.ICallable;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;
import es.ucm.fdi.mov.deleto.p1.logic.Menu;

public class AGame extends AppCompatActivity implements  ICallable{

    Engine _engine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        _engine = new Engine(new Menu(),this, "", this,
                400, 600, size.x, size.y);

        this.setTheme(R.style.Theme_AppCompat_NoActionBar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(_engine.getGraphics());
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

    /**
     * We can only capture here the onBackPressed event so we forward it to the engine
     */
    @Override
    public void onBackPressed() {
        _engine.getInput().newTouchEvent(TouchEvent.EventType.CLOSE_REQUEST,0,0,1);
    }

    /**
     * This specific method is needed for our engine to have the ability to close the Activity
     */
    @Override
    public void call() {
        finish();
        System.exit(0);
    }

}