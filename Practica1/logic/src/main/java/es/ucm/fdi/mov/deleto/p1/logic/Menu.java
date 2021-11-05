package es.ucm.fdi.mov.deleto.p1.logic;

import es.ucm.fdi.mov.deleto.p1.engine.IApplication;
import es.ucm.fdi.mov.deleto.p1.engine.IEngine;
import es.ucm.fdi.mov.deleto.p1.engine.IFont;
import es.ucm.fdi.mov.deleto.p1.engine.IGraphics;
import es.ucm.fdi.mov.deleto.p1.engine.IImage;
import es.ucm.fdi.mov.deleto.p1.engine.TouchEvent;

public class Menu implements  es.ucm.fdi.mov.deleto.p1.engine.IApplication{
    IFont _title;
    IFont _title2;
    IFont _title3;
    IFont _subtitle;
    IFont _regular;

    IImage _logo;
    IImage _exit;

    static final int BUTTON_RAD = 35;
    String _titleStr = null;

    public enum State{Initial, SelectSize}
    State _state = State.Initial;

    int _buttonsX[];
    int _buttonsY[];

    IEngine _engine;
    public Menu(){
        _titleStr = "Oh Yes";
    }
    public Menu(State state, String titleStr){
        _state = state;
        _titleStr = titleStr;
    }
    @Override
    public void onInit(IEngine engine) {
        _engine = engine;
        IGraphics g = _engine.getGraphics();
        g.setOpacity(1);

        _regular = g.newFont("JosefinSans-Bold.ttf",56,true);
        _title = g.newFont("Molle-Regular.ttf",72,false);
        _title2 = g.newFont("Molle-Regular.ttf",68,false);
        _title3 = g.newFont("JosefinSans-Bold.ttf",60,true);

        _subtitle = g.newFont("JosefinSans-Bold.ttf",24,false);

        _exit = g.newImage("close.png");
        _logo = g.newImage("q42.png");

        _buttonsX = new int[6];
        _buttonsY = new int[6];
    }

    @Override
    public void onUpdate(double deltaTime) {

    }

    @Override
    public void onRender() {
        IGraphics g = _engine.getGraphics();
        switch (_state)
        {
            case Initial:
                g.setFont(_title);
                g.setColor(0xff000000);
                g.drawText("Oh Yes", g.getWidth()/2,(int)(72*0.75f));

                g.setFont(_regular);
                g.drawText("Jugar", g.getWidth()/2,g.getHeight()/2);

                g.setFont(_subtitle);
                g.setColor(0xffc0c0c0);
                g.drawText("Un juego copiado a Q42", g.getWidth()/2,g.getHeight()-132);
                g.drawText("Creado por Martin Kool", g.getWidth()/2,g.getHeight()-94);

                g.drawImage(_logo,g.getWidth()/2,g.getHeight()-32,0.05f,0.05f);
                break;
            case SelectSize:
                int y = 46;
                if(_titleStr == "Oh Yes")
                {
                    y+=10;
                    g.setFont(_title2);
                }
                else
                {
                    g.setFont(_title3);
                }
                g.setColor(0xff000000);
                g.drawText(_titleStr, g.getWidth()/2,y);

                y+=80;
                g.setFont(_subtitle);
                g.drawText("Elija el tamaño a jugar", g.getWidth()/2,y);

                y += 100;
                int padding = 15;
                int x = (g.getWidth() - ((BUTTON_RAD *3*2)+(padding*3)))/2;
                for(int i = 0; i<6; i++)
                {
                    int xX = (x+((i%3)* BUTTON_RAD *2)+(padding*(i%3)))+BUTTON_RAD;
                    int yY = (y+(i/3* BUTTON_RAD *2)+(padding*(i/3)))+BUTTON_RAD;

                    _buttonsX[i]=xX;
                    _buttonsY[i]=yY;

                    g.setColor(i%2==0 ? 0xFF1CC0E0 : 0xFFFF384B);
                    g.fillCircle(xX,yY, BUTTON_RAD);
                    g.setColor(0xffffffff);
                    g.setFont(_subtitle);
                    g.drawText(Integer.toString(4+i),xX,yY,1.5);
                }
                g.setOpacity(0.6f);
                g.drawImage(_exit,g.getWidth()/2,g.getHeight()-64,1.f,1.f);
                g.setOpacity(1.f);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + _state);
        }

    }

    @Override
    public void onExit() {

    }

    @Override
    public void onEvent(TouchEvent event) {
        if(event.get_type() != TouchEvent.EventType.TOUCH)
            return;
        if(_state == State.Initial)
        {
            int SIZE = 56;
            int w = _engine.getGraphics().getWidth();
            int h = _engine.getGraphics().getHeight();

            if(event.get_x() >= w/3 && event.get_x() <= w - w/3 && event.get_y() >= h/2 - SIZE/2 && event.get_y() <= h/2+SIZE/2)
                _state = State.SelectSize;
        }
        else
        {
            int x = event.get_x();
            int y = event.get_y();
            for(int i = 0; i<_buttonsX.length;i++)
            {
                if(  x < _buttonsX[i]+BUTTON_RAD &&
                     x > _buttonsX[i]-BUTTON_RAD &&
                     y < _buttonsY[i]+BUTTON_RAD &&
                     y > _buttonsY[i]-BUTTON_RAD)
                {
                    _engine.changeApp(new OhY3s(4+i));
                    break;
                }
            }
            int iX = (_engine.getGraphics().getWidth()/2);
            int iY = (_engine.getGraphics().getHeight()-64);
            System.out.printf("NO ES UN BOTON DE JUEGO: iPos{%d %d} rPos{%d,%d}\n",iX,iY,x,y);
            if(x > iX-32 && x < iX+32 && y> iY-32 && y < iY+32)
                _engine.exit();
        }
    }
}
