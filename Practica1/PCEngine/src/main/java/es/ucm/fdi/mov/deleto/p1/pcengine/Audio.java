package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.ISound;

public class Audio implements IAudio {


    String _path;
    Engine _engine;
    public Audio(String path, Engine engine){
        _path = path;
    }

    @Override
    public ISound newSound(String filePath){
        try {
            return new Sound(_path + filePath);
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            _engine.panic("Error loading sound file at: "+filePath, "Audio error");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void createAndPlay(String filePath){
        newSound(filePath).play();
    }
}
