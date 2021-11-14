package es.ucm.fdi.mov.deleto.p1.androidEngine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

import es.ucm.fdi.mov.deleto.p1.engine.AbstractGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.EngineOptions;
import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public class Graphics extends AbstractGraphics implements IGraphics {

    private static final String TAG = "[Graphics]";
    SurfaceHolder _holder;
    Canvas _canvas;
    Paint _currentPaint;
    String _fontPath;
    String _imagePath;
    float _opacity = 1;
    Engine _engine;
    SurfaceView _view;

    int _clearColor;

    public Graphics(Context context, EngineOptions options, Engine engine) {
        _currentPaint = new Paint();
        _currentPaint.setFilterBitmap(true);
        _currentPaint.setAntiAlias(true);
        _currentPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG );
        _clearColor = options.clearColor;
        _view = new SurfaceView(context);
        _holder = _view.getHolder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _canvas = _holder.lockHardwareCanvas();
        }
        else
            _canvas = _holder.lockCanvas();

        _fontPath = options.assetsPath+options.fontsPath;
        _imagePath = options.assetsPath+options.imagesPath;
        _engine = engine;
    }

    @Override
    public void setResolution(int width, int height) {
        _logicW = width;
        _logicH = height;
    }
    @Override
    public int getLogicWidth()
    {
        return _logicW;
    }

    @Override
    public int getLogicHeight()
    {
        return  _logicH;
    }

    @Override
    public void setOpacity(float opacity) {
        _opacity = opacity;
    }

    @Override
    public void drawImage(IImage image, int posX, int posY, float scaleX, float scaleY) {
        Image im   = (Image) image;
        int width  = (int) ((im.getWidth()) * scaleX);
        int height = (int) ((im.getHeight())* scaleY);

        _currentPaint.setAlpha((int)(255*_opacity));
        _canvas.drawBitmap(im.getScaled(width, height), (posX)-width/(float)2, (posY)-height/(float)2, _currentPaint);
    }

    public void clear() {
        int prev = _currentPaint.getColor();
        _currentPaint.setColor(_clearColor);
        _canvas.drawRect(0,0,_canvas.getWidth(), _canvas.getHeight(), _currentPaint);
        _currentPaint.setColor(prev);
        _canvas.translate(_translateX,_translateY);
        _canvas.scale((float)_scale,(float)_scale);
    }

    public void prepareFrame()
    {
        while (!_holder.getSurface().isValid()){};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _canvas = _holder.lockHardwareCanvas();
        }else
            _canvas = _holder.lockCanvas();
    }
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
        _currentPaint.setAlpha((int)(255*_opacity));
        _canvas.drawCircle(x,y,(float)r, _currentPaint);
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

        Rect bounds = new Rect();
        int h =bounds.height();
        float prev = _currentPaint.getTextSize();
        _currentPaint.setTextSize(_currentPaint.getTextSize()*(float)scale);
        _currentPaint.getTextBounds(text,0,text.length(),bounds);
        Paint.FontMetrics fm = _currentPaint.getFontMetrics();

        String[] splits = text.split("\n");

        int fY =  (int)(fm.leading-fm.ascent)/2;
        int i = 0;
        if(splits.length > 1)
            fY-= bounds.height()/2;
        int fX=0;

        for(String s : splits)
        {
            _currentPaint.getTextBounds(s,0,s.length(),bounds);
            fY+= (h+bounds.height()+fm.leading-fm.ascent)*i++;
            fX = (bounds.width())/2;
            _canvas.drawText(s,x-fX, y+fY/2,_currentPaint);
        }
        int xX = (x)+fX;
        int yY = (y)+fY/2;

        _currentPaint.setTextSize(prev);

        return new Vec2<>(xX,yY);
    }

    @Override
    public IImage newImage(String name) {
        try {
            return new Image(_view.getContext().getAssets(),_imagePath+ name);
        } catch (IOException e) {
            _engine.panic("Could not find image "+name+" please try re-installing our application");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IFont newFont(String fileName, int size, boolean isBold) {
        try {
            return new Font(_view.getContext().getAssets(), _fontPath+fileName,size,isBold);
        } catch (Exception e) {
            _engine.panic("Could not find image "+fileName+" please try re-installing our application");
            e.printStackTrace();
            return null;
        }
    }

    public View getView() {
        return _view;
    }
}
