package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import es.ucm.fdi.mov.deleto.p1.engine.ISound;

public class Sound implements ISound {

    Clip _clip = null;
    public Sound(String file) {
        AudioInputStream inputStream = null;
        try {
            inputStream = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
            _clip = AudioSystem.getClip();
            _clip.open(inputStream);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Couldn't load audio file " + file);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Couldn't load audio file " + file);
            e.printStackTrace();
        }
        catch (LineUnavailableException e) {
            System.err.println("Couldn't load audio file " + file);
            e.printStackTrace();
        }
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
