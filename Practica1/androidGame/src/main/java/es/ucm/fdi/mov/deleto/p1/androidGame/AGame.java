package es.ucm.fdi.mov.deleto.p1.androidGame;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.mov.deleto.p1.androidEngine.Engine;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import es.ucm.fdi.mov.deleto.p1.androidEngine.Input;
import es.ucm.fdi.mov.deleto.p1.engine.ICallable;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;
import es.ucm.fdi.mov.deleto.p1.logic.Menu;

public class AGame extends AppCompatActivity implements  ICallable{

    Engine _engine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.println(Log.INFO,"[MAIN]", "Initializing Android Game Launcher");
        _engine = new Engine(new Menu(),this, "", this);
        this.setTheme(R.style.Theme_AppCompat_NoActionBar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(_engine.getGraphics());

        Point size =new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        _engine.getGraphics().setResolution(400,600);
        _engine.getGraphics().setScreenSize(size.x,size.y);
        ((Input)_engine.getInput()).setScale(_engine.getGraphics().getScale(), _engine.getGraphics().getOffsets().x(),_engine.getGraphics().getOffsets().y());

    }

    @Override
    public void call() {
        finish();
        System.exit(0);
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
    public void onBackPressed() {
        _engine.getInput().newTouchEvent(new TouchEvent(TouchEvent.EventType.CLOSE_REQUEST,0,0,1));
    }
}