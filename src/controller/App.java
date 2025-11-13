package controller;

import java.awt.EventQueue;
import view.JanelaInicial;

public class App {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                ControladorJogo controlador = new ControladorJogo();
                JanelaInicial janela = new JanelaInicial(controlador);
                janela.mostrar();
            }
        });
    } 
}
