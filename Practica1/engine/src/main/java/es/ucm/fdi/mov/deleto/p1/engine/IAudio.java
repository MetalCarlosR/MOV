package es.ucm.fdi.mov.deleto.p1.engine;

/**
 * Audio Engine interface for platform independent simple audio playing needs
 */
public interface IAudio {

    /**
     * Constructs a sound from given path.
     * Use this, for example, for pre-loading large music files on application init.
     * @param filePath path to sound file
     * @return
     */
    public ISound newSound(String filePath);

    /**
     * Creates as in newSound but plays immediately, use this for small sound effects
     * @param filePath path to sound file
     */
    public void createAndPlay(String filePath);
}
