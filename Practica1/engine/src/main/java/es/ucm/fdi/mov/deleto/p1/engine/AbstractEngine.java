package es.ucm.fdi.mov.deleto.p1.engine;

public abstract class AbstractEngine implements IEngine {

    //Main loop stop condition
    volatile protected Boolean _running = true;

    // We need this thread to take control of the rendering and update loop
    protected Thread _renderThread = null;

    /**
     * Current running application
     */
    protected IApplication _app;

    /**
     * Next application, to switch between application states and abstract different states
     *                   more easily
     */
    protected IApplication _nextApp = null;


    public void run() {
//        if (_renderThread != Thread.currentThread()) {
//            throw new RuntimeException("run() should not be called directly");
//        }
        while (_running) {

            //Init app and start measuring time
            _app.onInit(this);

            long lastFrameTime = System.nanoTime();

            while (_running) {
                //Get delta time and call update
                long currentTime = System.nanoTime();
                long nanoElapsedTime = currentTime - lastFrameTime;
                lastFrameTime = currentTime;
                double elapsedTime = (double) nanoElapsedTime / 1.0E9;

                _app.onUpdate(elapsedTime);

                pollEvents();

                render();
            }
            //running has been set to false, even on switch app want to call onExit
            _app.onExit();

            //if we have a requested next app, then set running to true and switch to it
            checkNextApp();
        }
        closeEngine();
    }


    protected void checkNextApp() {
        if (_nextApp != null) {
            _app = _nextApp;
            _nextApp = null;
            _running = true;
        }
    }

    @Override
    public void changeApp(IApplication newApp) {
        _nextApp = newApp;
        _running = false;
    }

    /**
     * --------- TO DO ------------
     */
    protected abstract void  closeEngine();

    /**
     * --------- TO DO ------------
     */
    protected abstract void pollEvents();

    /**
     * --------- TO DO ------------
     */
    protected abstract void render();

    /**
     * --------- TO DO ------------
     */
    public abstract void restoreState();

}
