package es.ucm.fdi.mov.deleto.p1.agame;


import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.mov.deleto.p1.AEngine.Engine;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import es.ucm.fdi.mov.deleto.p1.AEngine.Input;
import es.ucm.fdi.mov.deleto.p1.logic.Menu;

public class AGame extends AppCompatActivity {

    Engine _engine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.println(Log.INFO,"[MAIN]", "Initializing Android Game Launcher");
        _engine = new Engine(new Menu(),this, "OhYes","");
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
}