package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.content.res.AssetManager;

import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.ISound;

public class Audio implements IAudio {

    String _path;
    AssetManager _am;

    public Audio(AssetManager am,String path){
        _path = path;
        _am = am;
    }

    @Override
    public ISound newSound(String filePath) {
        return new Sound(_am,_path +"audio/"+ filePath);
    }

    @Override
    public void createAndPlay(String filePath) {
        newSound(filePath).play();
    }
}
