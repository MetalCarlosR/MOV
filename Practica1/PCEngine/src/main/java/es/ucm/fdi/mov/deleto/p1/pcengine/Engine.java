package es.ucm.fdi.mov.deleto.p1.pcengine;

import java.awt.Desktop;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/

public class Engine implements IEngine {
    /**
     * Reference to the corresponding platform interface implementations
     */
    Graphics _graphics;
    Input _input;
    Audio _audio;

    /**
     * Current running application
     */
    IApplication _app;

    /**
     * Next application, to switch between application states and abstract different states
     *                   more easily
     */
    IApplication _nextApp = null;

    /**
     * Signals if we need to exit the current main loop
     */
    boolean running = true;

    /**
     *  Constructor, simply calls new on every platform engine
     * @param app the app to initiate.
     * @param appName name of the window to create
     * @param assetsPath path where assets will be located
     * @param width window initial width
     * @param height window initial height
     */
    public Engine(IApplication app, String appName, String assetsPath, int width, int height) {
        _app = app;

        _graphics = new Graphics(this,appName, assetsPath, width, height);
        _audio = new Audio(assetsPath);
        _input = new Input(_graphics);

        if(restoreState());
            System.out.println("Restoring State");
    }

    /**
     * Main loop will execute until exit() is called
     */
    public void run() {

        /**
         * Outer loop needed for app switching
         */
        while (running) {
            long lastFrameTime = System.nanoTime();
            _app.onInit(this);

            while (running) {
                //Get delta time and call update
                long currentTime = System.nanoTime();
                long nanoElapsedTime = currentTime - lastFrameTime;
                lastFrameTime = currentTime;
                double elapsedTime = (double) nanoElapsedTime / 1.0E9;
                _app.onUpdate(elapsedTime);

                //Synchronized call to get events and forward to application
                List<TouchEvent> evs = _input.getTouchEvents();
                for (TouchEvent ev : evs) {
                    _app.onEvent(ev);
                }
                //We try to render in a loop because swing's swap buffer can fail
                do {
                    _graphics.clear(0xFFFFFFFF);
                    _app.onRender();
                }while(_graphics.swapBuffers());
            }
            //running has been set to false, even on switch app want to call onExit
            _app.onExit();

            //if we have a requested next app, then set running to true and switch to it
            checkNextApp();
        }
        //Try to save the state of the game for later reopening
        saveState();
        _graphics.release();
    }

    /**
     *  Creates a new file on the temporal folder of the os
     *      where we serialize and store the information of the current state
     *      of the game.
     */
    public void saveState()
    {
        String path = System.getProperty("java.io.tmpdir")+"saved0hY3State.txt";
        System.err.println(path);
        Map<String, String> ldapContent = _app.serialize();
        if(ldapContent == null)
            return;
        Properties properties = new Properties();

        for (Map.Entry<String,String> entry : ldapContent.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        try {
            properties.store(new FileOutputStream(path), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  Try to find a previous saved state of a game to load.
     *  If it finds it, calls the app to deserialize its contents.
     *  Continues normally otherwise.
     */
    public boolean restoreState()
    {
        String path = System.getProperty("java.io.tmpdir")+"saved0hY3State.txt";
        Map<String, String> ldapContent = new HashMap<String, String>();
        Properties properties = new Properties();
        try (FileInputStream stream = new FileInputStream(path)){
            properties.load(stream);

        } catch (IOException e) {
            return false;
        }

        for (String key : properties.stringPropertyNames()) {
            ldapContent.put(key, properties.get(key).toString());
        }

        _app.deserialize(ldapContent, this);
        checkNextApp();
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void checkNextApp() {
        if (_nextApp != null) {
            _app = _nextApp;
            _nextApp = null;
            running = true;
        }
    }

    @Override
    public void exit() {
        running = false;
    }

    @Override
    public void changeApp(IApplication newApp) {
        _nextApp = newApp;
        exit();
    }

    @Override
    public void openURL(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        else System.err.println("Could not open url: "+url);
    }


    /*******************
     * Error handling
     *******************/

    protected void panic(String error, String title)
    {
        JOptionPane.showMessageDialog(_graphics._window, error, title,JOptionPane.ERROR_MESSAGE);
        _graphics._window.dispatchEvent(new WindowEvent(_graphics._window, WindowEvent.WINDOW_CLOSING));
    }

    /***********
     * Getters *
     ***********/

    @Override
    public IGraphics getGraphics() {
        return _graphics;
    }

    @Override
    public IAudio getAudio() {
        return _audio;
    }
}