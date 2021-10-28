package es.ucm.fdi.mov.deleto.p1.pcgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import es.ucm.fdi.mov.deleto.p1.logic.OhY3s;
import es.ucm.fdi.mov.deleto.p1.pcengine.Engine;

public class PCGame {
    public static void main(String[] args){
        _game = new OhY3s();

        _game.newGame(size);

        _game.draw();

        Engine engine = new Engine(_game,"Pito","./assets/");

        engine.getGraphics().setResolution(1080/2,2220/2);

        engine.run();
//        while(_running){
//            // clears the console
//            //System.out.print("\033[H\033[2J");
//            //System.out.flush();
//            //System.out.print(String.format("%c[%d;%df",0x1B,0,0));
//
//            _game.draw();
//
//            try{
//                readCommand();
//            } catch (IOException e) {
//                System.out.println("Couldn't load the console");
//                System.out.println("Shutting down...");
//                e.printStackTrace();
//                _running = false;
//            }
//        }
    }

    static OhY3s _game;
    static boolean _running = true;
    static int size = 4;

    public static void readCommand() throws IOException {
        {
            // Enter data using BufferReader
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            // Reading data using readLine
            String rawInp = reader.readLine();
            String[] input = rawInp.split(" ");

            switch (input[0]){
                case "click":{
                    int x = -1;
                    int y = -1;
                    try{
                        if(input.length < 3)
                            throw new RuntimeException();
                        x = Integer.valueOf(input[1]);
                        y = Integer.valueOf(input[2]);
                        if (x < 0 || y < 0 || x >= size || y >= size)
                            throw new RuntimeException();
                    }
                    catch (RuntimeException e){
                        System.out.println("Click command expects 2 valid ints");
                        break;
                    }
                    _game.click(x, y);
                    break;
                }
                case "exit":{
                    _running = false;
                    System.out.println("Exiting...");
                    break;
                }
                case "undo":{
                    System.out.println("NOT IMPLEMENTED YET");
                    break;
                }
                case "tip":{
                    System.out.println(_game.getTip());
                    break;
                }
                case "settings":{
                    System.out.println("NOT IMPLEMENTED YET");
                    break;
                }
                case "about":{
                    System.out.println("NOT IMPLEMENTED YET");
                    break;
                }
                default:{
                    System.out.println("Unknown command:\n" + rawInp);
                    break;
                }
            }
        }
    }
}