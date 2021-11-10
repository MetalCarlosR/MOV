package es.ucm.fdi.mov.deleto.p1.engine;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public interface IAudio {
    public ISound newSound(String file);

    public void createAndPlay(String file);
}
