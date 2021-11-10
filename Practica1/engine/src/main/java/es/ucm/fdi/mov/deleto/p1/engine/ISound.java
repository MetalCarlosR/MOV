package es.ucm.fdi.mov.deleto.p1.engine;

/**
 * Sound Interface that application can use independent of platform
 */
public interface ISound {
    /**
     * Plays the sound
     */
    public void play();

    /**
     * Stops the sound
     */
    public void stop();
}
