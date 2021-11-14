package es.ucm.fdi.mov.deleto.p1.engine;

public abstract class AbstractGraphics {

    protected int _logicW;
    protected int _logicH;
    protected double _scale;

    protected int _translateX=0;
    protected int _translateY=0;

    protected AbstractGraphics(){}
    /**
     * Computes offsets and scale factor based on new dimensions and actual ref dimensions
     */
    protected void recalculateTransform(int w, int h, int xOff, int yOff)
    {
        int actualW = w;
        int actualH = h;

        actualH-=yOff;
        actualW-=(xOff*2);

        //We try width, then height
        int expectedHeight = (int)((_logicH * actualW)/ (float)_logicW);
        int expectedWidth  = (int)((_logicW * actualH)/ (float)_logicH);

        int barWidth = 0;
        int barHeight = 0;

        if(actualH >= expectedHeight) {
            barHeight = (actualH - expectedHeight) / 2;
            _scale = actualW /(float)_logicW;
        }
        else {
            barWidth = (actualW - expectedWidth) / 2;
            _scale = actualH /(float)_logicH;
        }

        _translateX=barWidth+xOff;
        _translateY=barHeight+yOff;
    }

    /**
     * Android helper cos no window border needed
     */
    public void recalculateTransform(int w, int h)
    {
        recalculateTransform(w,h,0,0);
    }
    public int getTranslateX(){return _translateX;}
    public int getTranslateY(){return _translateY;}
    public double getScale(){return _scale;}
}
