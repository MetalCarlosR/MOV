package es.ucm.fdi.mov.deleto.p1.pcengine;

import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.ISound;

public class Audio implements IAudio {


    String _rootAssetsPath;

    public Audio(String path){
        _rootAssetsPath = path;
    }

    @Override
    public ISound newSound(String filePath){
        return new Sound(_rootAssetsPath +"audio/"+ filePath);
    }

    @Override
    public void createAndPlay(String filePath){
        newSound(filePath).play();
    }
}
