package es.ucm.fdi.mov.deleto.p1.logic.buttons;

import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;

public abstract class TextButton extends RectangleButton {


    int _color;
    int _pressed;
    int _current;
    String _text;
    /**
     * whole width and height.
     * x and y centered
     */
    public TextButton(int x, int y, int w, int h, String text, int color, int pressed) {
        super(x, y, w, h);
        _color = color;
        _current = color;
        _pressed = pressed;
        _text = text;
    }

    public void draw(IGraphics g, IFont f)
    {
        g.setFont(f);
        g.setColor(_current);
        g.drawText(_text,_posX,_posY);
    }

    @Override
    protected void held() {
        _current=_pressed;
    }

    @Override
    protected void reset() {
        _current=_color;
    }
}
