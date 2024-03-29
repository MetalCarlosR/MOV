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

import es.ucm.fdi.mov.deleto.p1.engine.AbstractEngine;
import es.ucm.fdi.mov.deleto.p1.engine.EngineOptions;
import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IAudio;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

/*****************************************************
 * All interface methods documented on the interface *
 *****************************************************/

public class Engine extends AbstractEngine {
    /**
     * Reference to the corresponding platform interface implementations
     */
    Graphics _graphics;
    Input _input;
    Audio _audio;

    //Store applicationName, to use it on temp files
    String _appName;


    /**
     *  Constructor, simply calls new on every platform engine
     * @param app the app to initiate.
     * @param appName name of the window to create
     * @param options object with configuration for this engine
     * @param askBeforeExit if this string is set, we display this message before closing the window
     */
    public Engine(IApplication app, String appName, EngineOptions options, final String askBeforeExit) {
        _app = app;
        _appName = appName;

        //Initializes interfaces with given options
        _graphics = new Graphics(this,appName, options,askBeforeExit);
        _audio = new Audio(options.assetsPath+options.audioPath,this);
        _input = new Input(_graphics);

        //We check if a previous state was saved
        restoreState();

        //Start the loop
        _renderThread = new Thread(this);
        _renderThread.start();
    }


    @Override
    protected void pollEvents() {
        //Synchronized call to get events and forward to application
        List<TouchEvent> evs = _input.getTouchEvents();
        for (TouchEvent ev : evs) {
            _app.onEvent(ev);
        }
    }

    @Override
    protected void render() {
        //We try to render in a loop because swing's swap buffer can fail
        do {
            _graphics.clear();
            _app.onRender();
        }while(_running && _graphics.swapBuffers());
    }

    @Override
    protected void closeEngine() {
        //Try to save the state of the game for later reopening
        if(_closeEngine) {
            saveState();
            _graphics.release();
        }
    }

    /**************************
     * Temporary State Storage*
     * *  Read saveState() and storeState() on parent class for further information
     **************************/

    /**
     *  Creates a new file on the temporal folder of the os
     *  where we serialize and store the information of the current state.
     */
    @Override
    public void saveState()
    {
        String path = System.getProperty("java.io.tmpdir")+_appName + ".txt";
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
    public void restoreState()
    {
        String path = System.getProperty("java.io.tmpdir")+_appName + ".txt";
        Map<String, String> ldapContent = new HashMap<String, String>();
        Properties properties = new Properties();
        try (FileInputStream stream = new FileInputStream(path)){
            properties.load(stream);

        } catch (IOException e) {
            return ;
        }

        for (String key : properties.stringPropertyNames()) {
            ldapContent.put(key, properties.get(key).toString());
        }

        _app.deserialize(ldapContent, this);
        //We need to check if the application has
        //recuested to change app state
        checkNextApp();
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {
        _closeEngine = true;
        _running = false;
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