package es.ucm.fdi.mov.deleto.p1.pcengine;

import javax.swing.JFrame;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import java.io.InputStream;
import java.io.FileInputStream;

/**
 * Prueba de concepto de renderizado activo con Java.
 *
 * La clase incluye el main y hereda de JFrame, incluyendo toda
 * la funcionalidad de la "aplicación". En condiciones normales
 * (una aplicación más compleja) la implementación se distribuiría
 * en más clases y se haría más versátil.
 */
public class Paint extends JFrame {

    /**
     * Constructor.
     *
     * @param title Texto que se utilizará como título de la ventana
     * que se creará.
     */
    public Paint(String title) {
        super(title);
    }

    //--------------------------------------------------------------------

    /**
     * Realiza la inicialización del objeto (inicialización en dos pasos).
     * Se configura el tamaño de la ventana, se habilita el cierre de la
     * aplicación al cerrar la ventana, y se carga la fuente que se usará
     * en la ventana.
     *
     * Debe ser llamado antes de mostrar la ventana (con setVisible()).
     *
     * @return Cierto si todo fue bien y falso en otro caso (se escribe una
     * descripción del problema en la salida de error).
     */
    public boolean init() {

        setSize(400,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Cargamos la fuente del fichero .ttf.
        Font baseFont;
        try (InputStream is = new FileInputStream("Bangers-Regular.ttf")) {
            baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
        }
        catch (Exception e) {
            // Ouch. No está.
            System.err.println("Error cargando la fuente: " + e);
            return false;
        }

        // baseFont contiene el tipo de letra base en tamaño 1. La
        // usamos como punto de partida para crear la nuestra, más
        // grande y en negrita.
        _font = baseFont.deriveFont(Font.BOLD, 40);

        return true;

    } //  init

    //--------------------------------------------------------------------

    /**
     * Realiza la actualización de "la lógica" de la aplicación. En particular,
     * desplaza el rótulo a su nueva posición en su deambular de izquierda
     * a derecha.
     *
     * @param deltaTime Tiempo transcurrido (en segundos) desde la invocación
     * anterior (frame anterior).
     */
    public void update(double deltaTime) {
        int maxX = getWidth() - 300; // 300 : longitud estimada en píxeles del rótulo

        _x += _incX * deltaTime;
        while(_x < 0 || _x > maxX) {
            // Vamos a pintar fuera de la pantalla. Rectificamos.
            if (_x < 0) {
                // Nos salimos por la izquierda. Rebotamos.
                _x = -_x;
                _incX *= -1;
            }
            else if (_x > maxX) {
                // Nos salimos por la derecha. Rebotamos
                _x = 2*maxX - _x;
                _incX *= -1;
            }
        } // while
    }  // update

    //--------------------------------------------------------------------

    /**
     * Dibuja en pantalla el estado actual de la aplicación.
     *
     * @param g Objeto usado para enviar los comandos de dibujado.
     */
    public void render(Graphics g) {

        // Borramos el fondo.
        g.setColor(Color.yellow.darker());
        g.fillRect(0, 0, getWidth(), getHeight());

        // Ponemos el rótulo (si conseguimos cargar la fuente)
        if (_font != null) {
            g.setColor(Color.WHITE);
            g.setFont(_font);
            g.drawString("RENDERIZADO ACTIVO", (int)_x, 100);
        }

    } // render

    //--------------------------------------------------------------------
    //                          Métodos estáticos
    //--------------------------------------------------------------------

    /**
     * Programa principal. Crea la ventana y la configura para usar renderizado
     * activo. Luego entra en el bucle principal de la aplicación que ejecuta
     * el ciclo continuo de actualización/dibujado. Aproximadamente cada segundo
     * escribe en la salida estándar el número de fotogramas por segundo.
     *
     * La aplicación resultante no es interactiva. El usuario no puede hacer
     * nada salvo cerrarla.
     */
    public static void main(String[] args) {

        Paint ventana = new Paint("Paint");
        if (!ventana.init())
            // Ooops. Ha fallado la inicialización.
            return;

        // Vamos a usar renderizado activo. No queremos que Swing llame al
        // método repaint() porque el repintado es continuo en cualquier caso.
        ventana.setIgnoreRepaint(true);

        // Hacemos visible la ventana.
        ventana.setVisible(true);

        // Intentamos crear el buffer strategy con 2 buffers.
        int intentos = 100;
        while(intentos-- > 0) {
            try {
                ventana.createBufferStrategy(2);
                break;
            }
            catch(Exception e) {
            }
        } // while pidiendo la creación de la buffeStrategy
        if (intentos == 0) {
            System.err.println("No pude crear la BufferStrategy");
            return;
        }
        else {
            // En "modo debug" podríamos querer escribir esto.
            //System.out.println("BufferStrategy tras " + (100 - intentos) + " intentos.");
        }

        // Obtenemos el Buffer Strategy que se supone que acaba de crearse.
        java.awt.image.BufferStrategy strategy = ventana.getBufferStrategy();

        // Vamos allá.
        long lastFrameTime = System.nanoTime();

        long informePrevio = lastFrameTime; // Informes de FPS
        int frames = 0;
        // Bucle principal
        while(true) {
            long currentTime = System.nanoTime();
            long nanoElapsedTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;
            double elapsedTime = (double) nanoElapsedTime / 1.0E9;
            ventana.update(elapsedTime);
            // Informe de FPS
            if (currentTime - informePrevio > 1000000000l) {
                long fps = frames * 1000000000l / (currentTime - informePrevio);
                System.out.println("" + fps + " fps");
                frames = 0;
                informePrevio = currentTime;
            }
            ++frames;
            // Pintamos el frame con el BufferStrategy
            do {
                do {
                    Graphics graphics = strategy.getDrawGraphics();
                    try {
                        ventana.render(graphics);
                    }
                    finally {
                        graphics.dispose();
                    }
                } while(strategy.contentsRestored());
                strategy.show();
            } while(strategy.contentsLost());
			/*
			// Posibilidad: cedemos algo de tiempo. es una medida conflictiva...
			try {
				Thread.sleep(1);
			}
			catch(Exception e) {}
			*/
        } // while

        // Si tuvieramos un mecanismo para acabar limpiamente, tendríamos
        // que liberar el BufferStrategy.
        // strategy.dispose();

    } // main

    //--------------------------------------------------------------------
    //                    Atributos protegidos/privados
    //--------------------------------------------------------------------

    /**
     * Fuente usada para escribir el texto que se muestra moviéndose de lado a lado.
     */
    protected Font _font;

    /**
     * Posición x actual del texto (lado izquierdo). Es importante
     * que sea un número real, para acumular cambios por debajo del píxel si
     * la velocidad de actualización es mayor que la del desplazamiento.
     */
    protected double _x = 0;

    /**
     * Velocidad de desplazamiento en píxeles por segundo.
     */
    protected int _incX = 50;

} // class Paint

