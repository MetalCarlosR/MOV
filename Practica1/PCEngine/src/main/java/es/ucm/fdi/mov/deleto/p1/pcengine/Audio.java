package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.ISound;

public class Audio implements IAudio {

    String _path;

    public Audio(String path){
        _path = path;
    }

    @Override
    public ISound newSound(String file){
        return new Sound(_path +"audio/"+ file);
    }

    public void createAndPlay(String file){
        newSound(file).play();
    }
}
