package es.ucm.fdi.mov.deleto.p1.pcengine;

import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.ISound;

public class Audio implements IAudio {

    String _path;

    public Audio(String path){
        _path = path;
    }

    @Override
    public ISound newSound(String filePath){
        return new Sound(_path +"audio/"+ filePath);
    }

    public void createAndPlay(String filePath){
        newSound(filePath).play();
    }
}
