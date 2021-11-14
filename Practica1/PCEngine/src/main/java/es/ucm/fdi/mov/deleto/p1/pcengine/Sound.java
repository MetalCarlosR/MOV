package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import es.ucm.fdi.mov.deleto.p1.engine.ISound;

/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/
public class Sound implements ISound {

    /**
     * Internal javax.sound clip
     */
    Clip _clip = null;

    /**
     * Tries to create a sound via filepath
     * @param file the path where the sound file should be contained
     */
    public Sound(String file) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        AudioInputStream inputStream = null;
        inputStream = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
        _clip = AudioSystem.getClip();
        _clip.open(inputStream);
    }

    @Override
    public void play() {
        _clip.start();
    }

    @Override
    public void stop() {
        _clip.stop();
    }
}
