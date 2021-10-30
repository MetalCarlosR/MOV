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
    IFont _subtitle;
    IFont _regular;

    IImage _logo;
    IImage _exit;

    static final int BUTTON_RAD = 35;

    public enum State{Initial, SelectSize}
    State _state = State.Initial;

    int _buttonsX[];
    int _buttonsY[];

    IEngine _engine;
    public Menu(){}
    public Menu(State state){
        _state = state;
    }
    @Override
    public void onInit(IEngine engine) {
        _engine = engine;
        IGraphics g = _engine.getGraphics();
        _regular = g.newFont("JosefinSans-Bold.ttf",56,true);
        _title = g.newFont("Molle-Regular.ttf",72,false);
        _title2 = g.newFont("Molle-Regular.ttf",68,false);
        _subtitle = g.newFont("JosefinSans-Bold.ttf",38,false);

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

                g.drawImage(_logo,g.getWidth()/2,g.getHeight()-16,0.05f,0.05f);
                break;
            case SelectSize:
                int y =(int)(68*0.75f);
                g.setFont(_title2);
                g.setColor(0xff000000);
                g.drawText("Oh Yes", g.getWidth()/2,y);

                y+=80;
                g.setFont(_subtitle);
                g.drawText("Elija el tamaño a jugar", g.getWidth()/2,y);

                y += 60;
                int padding = 15;
                int x = (g.getWidth() - ((BUTTON_RAD *3*2)+(padding*3)))/2;
                for(int i = 0; i<6; i++)
                {
                    int xX = x+((i%3)* BUTTON_RAD *2)+(padding*(i%3));
                    int yY = y+(i/3* BUTTON_RAD *2)+(padding*(i/3));

                    _buttonsX[i]=xX;
                    _buttonsY[i]=yY;

                    g.setColor(i%2==0 ? 0xFF1CC0E0 : 0xFFFF384B);
                    g.fillCircle(xX,yY, BUTTON_RAD);
                    g.setColor(0xffffffff);
                    g.setFont(_subtitle);
                    g.drawText(Integer.toString(4+i),xX+ BUTTON_RAD,yY+ BUTTON_RAD);
                }
                g.setOpacity(0.6f);
                g.drawImage(_exit,g.getWidth()/2,g.getHeight()-16,1.f,1.f);
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
        if(event.get_type() == TouchEvent.EventType.RELEASE)
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
            for(int i = 0; i<_buttonsX.length;i++)
            {
                if( event.get_x() < _buttonsX[i]+2*BUTTON_RAD &&
                    event.get_x() > _buttonsX[i] &&
                    event.get_y() < _buttonsY[i]+2*BUTTON_RAD &&
                    event.get_y() > _buttonsY[i])
                {
                    OhY3s a = new OhY3s();
                    System.out.printf("HAS TOCADO {%d}",4+i);
                    a.newGame(4+i);
                    _engine.changeApp(a);
                    break;
                }
            }
        }
    }
}
