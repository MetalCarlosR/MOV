package es.ucm.fdi.mov.deleto.p1.AEngine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public class Graphics extends SurfaceView implements IGraphics {

    private static final String TAG = "[Graphics]";
    SurfaceHolder _holder;
    Canvas _canvas;
    Paint _currentPaint;
    String _assetPath;

    int _logicW;
    int _logicH;
    float _scale;
    int _translateX;
    int _translateY;


    public Graphics(Context context, String appName, String assetPath) {
        super(context);
        _currentPaint = new Paint();
        _holder = getHolder();
        _canvas = _holder.lockCanvas();
        _assetPath = assetPath;
    }


    public void setScreenSize(int x, int y)
    {
        int realWidth = x;
        int realHeight = y;
        //We try width, then height
        int expectedHeight = (int)((_logicH *realWidth)/ (float)_logicW);
        int expectedWidth  = (int)((_logicW *realHeight)/ (float)_logicH);

        int barWidth = 0;
        int barHeight = 0;

        if(realHeight >= expectedHeight) {
            barHeight = (realHeight - expectedHeight) / 2;
            _scale = realWidth/(float)_logicW;
        }
        else {
            barWidth = (realWidth - expectedWidth) / 2;
            _scale = realHeight/(float)_logicH;
        }
        _translateX=barWidth;
        _translateY=barHeight;
        Log.e(TAG, String.format("setScreenSize: offsets{%d %d} Scale{%f %f} Real{%d,%d} Expected{%d,%d}",_translateX,_translateY,_scale,_scale, realWidth,realHeight,expectedWidth,expectedHeight));
    }

    @Override
    public void setResolution(int x, int y) {
        _logicW = x;
        _logicH = y;
    }
    @Override
    public int getLogicWidth()
    {
        return 400;
    }

    @Override
    public int getLogicHeight()
    {
        return  600;
    }

    @Override
    public void setOpacity(float opacity) {
        //_currentColor.setAlpha((int)(opacity*255));
    }

    @Override
    public void drawImage(IImage image, int posX, int posY, float scaleX, float scaleY) {
        Image im   = (Image) image;
        int width  = (int) ((im.getWidth()) * scaleX);
        int height = (int) ((im.getHeight())* scaleY);
        _canvas.drawBitmap(im.getScaled(width, height), (posX)-width/(float)2, (posY)-height/(float)2, _currentPaint);
    }

    @Override
    public void clear(int color) {
        int prev = _currentPaint.getColor();
        _currentPaint.setColor(color);
        _canvas.drawRect(0,0,_canvas.getWidth(), _canvas.getHeight(), _currentPaint);
        _currentPaint.setColor(prev);
        _canvas.translate(_translateX,_translateY);
        _canvas.scale(_scale,_scale);
    }

    public void prepareFrame()
    {
        while (!_holder.getSurface().isValid());
        _canvas = _holder.lockCanvas();
    }
    public void release(){  };
    public void present()
    {
        _holder.unlockCanvasAndPost(_canvas);
    }
    @Override
    public void setColor(int color) {
        _currentPaint.setColor(color);
    }

    @Override
    public void setFont(IFont font) {
        Font f = (Font)font;
        _currentPaint.setTypeface(f.getFont());
        _currentPaint.setTextSize(f.getSize());
    }

    @Override
    public void fillCircle(int x, int y, double r) {
        _canvas.drawCircle(x,y,(int)r, _currentPaint);
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        _canvas.drawRect(x-w/(float)2,y-h/(float)2,w,h, _currentPaint);
    }

    @Override
    public Vec2<Integer> drawText(String text, int x, int y) {
        return drawText(text,x,y,1);
    }

    @Override
    public Vec2<Integer> drawText(String text, int x, int y, double scale) {

        Paint.FontMetrics fm = _currentPaint.getFontMetrics();
        Rect bounds = new Rect();
        _currentPaint.getTextBounds(text,0,text.length(),bounds);
//        String[] splits = text.split("\n");
//
//        int fY =  bounds.height()/2;
//        int i = 0;
//        if(splits.length > 1)
//            fY-= bounds.height()/2;
//        int fX=0;
//
//        for(String s : splits)
//        {
//            fY+= (bounds.height()+fm.leading+fm.ascent*i++);
//            fX = (bounds.width())/2;
//            _canvas.drawText(text,fX-bounds.width()/(float)2, fY+bounds.height()/(float)2,_currentColor);
//        }
//        int xX = (x)-fX;
//        int yY = (y)+fY/2;

        _canvas.drawText(text,x-bounds.width()/(float)2,y-bounds.height()/(float)2,_currentPaint);
        Log.e(TAG, "drawText:"+x);
        Log.e(TAG, "drawText:"+y);
        return new Vec2<>(0,0);
    }

    @Override
    public IImage newImage(String name) {
        return new Image(getContext().getAssets(),_assetPath + "sprites/"+ name);
    }

    @Override
    public IFont newFont(String fileName, int size, boolean isBold) {
        return new Font(getContext().getAssets(), _assetPath+"fonts/"+fileName,size,isBold);
    }
}
