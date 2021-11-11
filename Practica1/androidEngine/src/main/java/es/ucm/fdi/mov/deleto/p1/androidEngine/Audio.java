package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.content.res.AssetManager;

import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.ISound;

public class Audio implements IAudio {

    String _path;
    AssetManager _am;

    Sound[] _circularList = new Sound[32]; //We allow up to 32 simultaneous sounds
    int _listIndex = 0;

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
        Sound sound = (Sound) newSound(filePath);
        _circularList[_listIndex++%32] = sound;
        sound.play();
    }
}
