package es.ucm.fdi.mov.deleto.p1.logic.buttons;


import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public abstract class ImageButton extends RectangleButton {

    private IImage _image;
    private float _scale;
    private float _opacity = 0;

    public ImageButton(IImage image, int x, int y, float scale) {
        super(x, y, (int)((image.getWidth())*scale), (int)((image.getHeight())*scale));
        _scale = scale;
        _image = image;
    }
    public void draw(IGraphics g, float opacity)
    {
        g.setOpacity(_opacity==0?opacity:_opacity);
        g.drawImage(_image,_posX,_posY,_scale,_scale);
    }

    @Override
    protected void held() {
        _opacity = (float) .9;
    }

    @Override
    protected void reset() {
        _opacity = 0;
    }
}
