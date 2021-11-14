package es.ucm.fdi.mov.deleto.p1.engine;

public abstract class AbstractEngine implements IEngine,  Runnable {

    static int frame = 0;

    /**This extra boolean is needed because application cycle might end up stopping our main loop
            but we need to properly recover and only close on exit demand.
     */
    volatile protected Boolean _closeEngine = false;

    //Main loop stop condition
    volatile protected Boolean _running = true;

    /**
     * We need this thread to take control of the main loop only when the application is on focus
     */
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

    protected AbstractEngine(){}

    public void run() {
        //This main loop is supposed to be ran on a different thread, as to get better
        // use of platform resources
        if (_renderThread != Thread.currentThread()) {
            throw new RuntimeException("run() should not be called directly");
        }
        while (_running) {
            //Init app and start measuring time
            _app.onInit(this);

            long lastFrameTime = System.nanoTime();

            while (_running) {
                frame++;
                //Get delta time and call update
                long currentTime = System.nanoTime();
                long nanoElapsedTime = currentTime - lastFrameTime;
                lastFrameTime = currentTime;
                double elapsedTime = (double) nanoElapsedTime / 1.0E9;

                pollEvents();

                _app.onUpdate(elapsedTime);

                render();

                try {
                    long diff = (long) ((System.nanoTime()-lastFrameTime) /1.0E6);
                    if( diff< 16L)
                        Thread.sleep(16-diff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //running has been set to false, even on switch app want to call onExit
            _app.onExit();

           //if we have a requested next app, then set running to true and switch to it
            checkNextApp();
        //otherwise exit and closeEngine
        }
        closeEngine();
    }


    /**
     * Checks if a next app has been set to swap states
     */
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
     * Continues the main loop after pausing the application
     */
    public void resume(){
        System.out.println("RESUME");
        _running = true;
        _renderThread = new Thread(this);
        _renderThread.start();
    }

    /**
     * Pause method to stop main loop without closing application
     */
    public void pause(){
        System.out.println("PAUSE");
        if (_running) {
            _running = false;
            while (true) {
                try {
                    _renderThread.join();
                    _renderThread = null;
                    break;
                } catch (InterruptedException ie) {
                    ie.printStackTrace(); //this should never happen
                }
            }
        }
    }

    /**
     * Properly close all resources upon exiting main loop
     */
    protected abstract void  closeEngine();

    /**
     * Platform specific way to poll all the events and send them to the current app
     * to react.
     */
    protected abstract void pollEvents();

    /**
     * Platform specific way to render, we need this because Desktop rendering can
     * fail and needs to re-send rendering commands to application to repeat a frame
     */
    protected abstract void render();

    /**
     * Platform specific way to save and restore the state of the application.
     * This can be optionally implemented by applications that want to restore previous state
     * when unexpectedly closed. For example if the application was a text editor you would
     * probably want to store the current unsaved file somewhere to restored next time the
     * app is launched.
     *
     * This saved state is temporary and managed by host OS. For permanently stored data
     * we could further extend IEngine interface to facilitate some SaveData and LoadData
     * methods that call serialize/deserialize and stored them on given paths.
     */
    public abstract void restoreState();
    public abstract void saveState();
}
