package es.ucm.fdi.mov.deleto.p1.engine;

public abstract class AbstractEngine implements IEngine,  Runnable {

    static int frame = 0;
    //This extra boolean is needed because android cycle might end up stopping our main loop
    //but we need to properly recover
    volatile protected Boolean _closeEngine = false;

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
        if (_renderThread != Thread.currentThread()) {
            throw new RuntimeException("run() should not be called directly");
        }
        while (_running) {
            int lost = 0;
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

                _app.onUpdate(elapsedTime);

                pollEvents();

                render();

                try {
                    long diff = (long) ((long)(System.nanoTime()-lastFrameTime)/1.0E6);
                    if( diff< 16l)
                        Thread.sleep((long) (16-diff));
                    else
                        System.err.println("NO LLEGOOOO"+lost++);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //running has been set to false, even on switch app want to call onExit
            _app.onExit();

            System.err.println("Se perdió: "+lost+" frames -> "+ ((double)lost/(double)frame)*100+"%");

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
     * Android specific resume method for application life cycle.
     */
    public void resume(){
        System.out.println("RESUME");
        _running = true;
        _renderThread = new Thread(this);
        _renderThread.start();
    };

    /**
     * Android specific pause method for application life cycle
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
    };

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
