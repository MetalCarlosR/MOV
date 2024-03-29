package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

import es.ucm.fdi.mov.deleto.p1.engine.ISound;

/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/

public class Sound implements ISound {

    MediaPlayer _mp = null;

    /**
     * Tries to create a sound via filepath
     * @param file the path where the sound file should be contained
     * @param am Asset Manager for android to open the file
     */
    public Sound(AssetManager am, String file){
        _mp = new MediaPlayer();
        _mp.reset();
        try {
            AssetFileDescriptor afd = am.openFd(file);
            _mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),afd.getLength());
            _mp.prepare();
            _mp.setOnCompletionListener(MediaPlayer::release);
        } catch (IOException e) {
            System.err.println("Couldn't load audio file " + file);
            e.printStackTrace();
        }
    }

    @Override
    public void play() {
        _mp.start();
    }

    @Override
    public void stop() {
        _mp.stop();
    }
}
