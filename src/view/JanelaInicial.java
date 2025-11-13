package view;

import controller.ControladorJogo;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JanelaInicial extends Frame {

    private static final long serialVersionUID = 1L;

    private final ControladorJogo controlador;
    private final Choice escolhaQtd;

    public JanelaInicial(ControladorJogo ctrl) {   // <- renomeei o parâmetro
        super("Banco Imobiliário — Nova Partida");
        this.controlador = ctrl;

        setSize(new Dimension(480, 240));          // dentro do limite 1280x800
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { dispose(); }
        });

        Label titulo = new Label("Nova Partida", Label.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titulo, BorderLayout.NORTH);

        Panel centro = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        Label lbl = new Label("Número de jogadores (3 a 6):");
        escolhaQtd = new Choice();
        escolhaQtd.add("3"); escolhaQtd.add("4"); escolhaQtd.add("5"); escolhaQtd.add("6");
        centro.add(lbl);
        centro.add(escolhaQtd);
        add(centro, BorderLayout.CENTER);

        Panel barra = new Panel(new FlowLayout(FlowLayout.CENTER));
        Button iniciar = new Button("Iniciar jogo");
        iniciar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int n = Integer.parseInt(escolhaQtd.getSelectedItem());
                dispose();
                JanelaInicial.this.controlador.iniciarNovaPartida(n); // <- usa o CAMPO
            }
        });
        barra.add(iniciar);
        add(barra, BorderLayout.SOUTH);
    }

    public void mostrar() {
        setLocationRelativeTo(null); // helper abaixo
        setVisible(true);
    }

    // Helper AWT para centralizar (AWT não tem setLocationRelativeTo)
    private void setLocationRelativeTo(Frame relative) {
        java.awt.Dimension scr = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int x = (scr.width - getWidth()) / 2;
        int y = (scr.height - getHeight()) / 2;
        setLocation(Math.max(0, x), Math.max(0, y));
    }
}
