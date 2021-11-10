package es.ucm.fdi.mov.deleto.p1.engine;


/**
 * Interface of what an application must implement to use our engine
 */
public interface IApplication {

    /**
     * If you need to use the engine to fetch platform dependent resources on your application
     * this is the best place to do so, as it's only called once before the main loop starts
     * @param engine IEngine instance is passed for the application to use
     */
    public void onInit(IEngine engine);

    /**
     * Called on every frame of the main loop
     * @param deltaTime time since last frame on ms
     */
    public void onUpdate(double deltaTime);

    /**
     * Called on every frame to let application forward calls to the engine rendering interface
     * engine will handle display buffers
     */
    public void onRender();

    /**
     * Called on application exit. Use this to properly close threads or other resources
     */
    public void onExit();

    /**
     * Every event supported for our engine will be forwarded to this method so application may
     * react to them
     * @param event platform independent event
     */
    public void onEvent(TouchEvent event);
}
