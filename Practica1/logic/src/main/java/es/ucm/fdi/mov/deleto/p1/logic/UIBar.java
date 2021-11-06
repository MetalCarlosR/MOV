package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;

public class UIBar {

    IImage _close;
    IImage _undo;
    IImage _clue;

    IGraphics _g;
    public enum Action{CLOSE, UNDO, CLUE,NO_ACTION};

    int posX[];
    int posY[];

    int _h, _w;
    float _scale;

    public void init(IGraphics g){
        _g=g;
        _close = _g.newImage("close.png");
        _undo = _g.newImage("history.png");
        _clue = _g.newImage("eye.png");
        posX = new int[Action.values().length-1];
        posY = new int[Action.values().length-1];
    }
    public void Draw()
    {
        _w = 64;
        _h = 64;
        _scale = .5f;
        int padding = ((_g.getLogicWidth()-(3*(int)(_w * _scale))) / 4);
        _g.setOpacity(0.8f);

        int sW = (int)(_scale * _w);
        int sH = (int)(_scale * _h);

        int x = padding+(sW/2);
        int y = _g.getLogicHeight()-(sH);

        _g.drawImage(_close, x, y, _scale, _scale);

        posX[0]=x;
        posY[0]=y;

        x = ((padding*2)+(sW))  +(sW/2);
        _g.drawImage(_undo,x ,y, _scale, _scale);

        posX[1]=x;
        posY[1]=y;

        x = ((padding*3)+(sW*2))+(sW/2);
        _g.drawImage(_clue,  x  , y, _scale, _scale);
        posX[2]=x;
        posY[2]=y;
    }
    public Action HandleClick(int x, int y){
        for (int i = 0; i<posX.length;i++)
        {
            int maxX =posX[i]+(int)(_w*_scale);
            int minX =posX[i]-(int)(_w*_scale);

            int maxY =posY[i]+(int)(_h*_scale);
            int minY =posY[i]-(int)(_h*_scale);

            if( x > minX && x<maxX && y < maxY && y> minY )
            {
                return (i==0) ? Action.CLOSE : i==1 ? Action.UNDO : Action.CLUE;
            }
        }
        return Action.NO_ACTION;
    }
}
