package es.ucm.fdi.mov.deleto.p1.engine;


/**
 * Engine interface for Application to use
 */
public interface IEngine {


    /**
     * @return our Graphic interface for application to render different shapes, images and text
     */
    IGraphics getGraphics();

    /**
     * @return our Audio interface for application to create and reproduce sound objects
     */

    IAudio getAudio();

    /**
     * Opens requested URL on web browser if available
     * @param url the url to open in string form
     */
    void openURL(String url);

    void changeApp(IApplication newApp);

    void exit();

}