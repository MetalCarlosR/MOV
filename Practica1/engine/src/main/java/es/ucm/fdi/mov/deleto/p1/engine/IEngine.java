package es.ucm.fdi.mov.deleto.p1.engine;


/**
 * Engine interface for Application to use
 */
public interface IEngine {


    /**
     * @return our Graphic interface for application to render different shapes, images and text
     */
    public IGraphics getGraphics();

    /**
     * @return our Audio interface for application to create and reproduce sound objects
     */

    public IAudio getAudio();

    /**
     * Opens requested URL on web browser if available
     * @param url the url to open in string form
     */
    public void openURL(String url);

    public void changeApp(IApplication newApp);

    public void exit();

}