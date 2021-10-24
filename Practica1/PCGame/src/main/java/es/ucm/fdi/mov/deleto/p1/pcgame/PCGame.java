package es.ucm.fdi.mov.deleto.p1.pcgame;

import es.ucm.fdi.mov.deleto.p1.logic.OhY3s;

public class PCGame {
    public static void main(String[] args){
        OhY3s game = new OhY3s();

        game.newGame(4);

        game.draw();
    }
}