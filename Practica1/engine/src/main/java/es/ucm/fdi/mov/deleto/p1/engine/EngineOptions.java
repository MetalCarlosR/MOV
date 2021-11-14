package es.ucm.fdi.mov.deleto.p1.engine;

/**
 *
 * Utility class to hold some common engine configuration
 * We could extend it and add specific configuration for both engine implementations but for now
 * this suffices
 */
public class EngineOptions {
    public String assetsPath;
    public String imagesPath;
    public String  fontsPath;
    public String  audioPath;
    public int logicWidth;
    public int logicHeight;
    public int realWidth;
    public int realHeight;
    public int clearColor;

    public EngineOptions( String p,String i, String f, String audio, int w, int h, int cC)
    {
        assetsPath = p;
        imagesPath = i;
        fontsPath = f;
        audioPath = audio;
        logicWidth = w;
        logicHeight = h;
        clearColor = cC;
    }
}
