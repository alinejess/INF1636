package view;

import view.TabuleiroCanvas;
import controller.ControladorJogo;

import model.EventoJogo;
import model.GameModelo;
import model.OuvinteJogo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Choice;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class JanelaTabuleiro extends Frame implements OuvinteJogo {

    private static final long serialVersionUID = 1L;

    private final ControladorJogo controlador;
    private final GameModelo modelo;
    private final TabuleiroCanvas canvas;

    // Controles da barra inferior
    private final Choice escolhaD1 = new Choice();
    private final Choice escolhaD2 = new Choice();
    private final Button btnForcar = new Button("Lançar (forçar)");
    private final Button btnAleatorio = new Button("Lançar (aleatório)");
    private final Button btnEncerrar = new Button("Encerrar turno");
    
    public JanelaTabuleiro(ControladorJogo controlador, GameModelo modelo) {
    	
        super("Banco Imobiliário — Tabuleiro");
        // this.canvas = new TabuleiroCanvas(modelo, controlador);
        this.controlador = controlador;
        this.modelo = modelo;

        setSize(new Dimension(1000, 700));
        setResizable(false);
        setLayout(new BorderLayout());
        
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });

        // Canvas Java2D
        canvas = new TabuleiroCanvas(modelo, controlador);
        add(canvas, BorderLayout.CENTER);

        // Barra inferior (controles de jogada)
        Panel barra = new Panel(new FlowLayout(FlowLayout.LEFT));
        for (int i = 1; i <= 6; i++) {
            escolhaD1.add(Integer.toString(i));
            escolhaD2.add(Integer.toString(i));
        }
        
        barra.add(escolhaD1);
        barra.add(escolhaD2);
        barra.add(btnForcar);
        barra.add(btnAleatorio);
        barra.add(btnEncerrar);
        add(barra, BorderLayout.SOUTH);
        
        // Ações dos botões
        btnForcar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                int d1 = Integer.parseInt(escolhaD1.getSelectedItem());
                int d2 = Integer.parseInt(escolhaD2.getSelectedItem());
                // via controlador (recomendado)
                controlador.lancarForcado(d1, d2);
            }
        });
        btnAleatorio.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                controlador.lancarAleatorio();
            }
        });
        btnEncerrar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                controlador.encerrarTurno(); 
            }
        });

        // Observer
        modelo.adicionarOuvinte(this);
    }

    public void mostrar() {
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setLocationRelativeTo(Frame relative) {
        java.awt.Dimension scr = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int x = (scr.width - getWidth()) / 2;
        int y = (scr.height - getHeight()) / 2;
        setLocation(Math.max(0, x), Math.max(0, y));
    }

    @Override
    public void notificar(EventoJogo e, Object payload) {
    	switch (e) {
	    	case CARTA_SORTE_REVES_APLICADA: {
	            // Usa o payload (id da imagem); se vier nulo, pergunta ao modelo
	            String id = (payload instanceof String) ? (String) payload : modelo.obterIdUltimaCartaSorteReves();
	            canvas.registrarCartaSorte(id);
	            canvas.repaint();
	            break;
	        }
	        case ESTADO_ATUALIZADO: {
	            atualizarCartaEmTela(); // se ainda há id de sorte -> mostra; senão mostra propriedade
	            canvas.repaint();
	            break;
	        }
        }
        if (e == EventoJogo.ESTADO_ATUALIZADO) {
            canvas.repaint();
            return;
        }
        if (e == EventoJogo.DADOS_LANCADOS) {
            GameModelo.Lancamento l = (GameModelo.Lancamento) payload;
            canvas.definirDados(l.d1, l.d2);
            return;
        }
        if (e == EventoJogo.CARTA_SORTE_REVES_APLICADA && payload instanceof String) {
            canvas.registrarCarta((String) payload);
        }
        if (e == EventoJogo.ORDEM_SORTEADA) {
            System.out.println("[UI] Ordem sorteada recebida na View.");
            canvas.repaint();
            return;
        }
    }
    
    private void atualizarCartaEmTela() {
        // 1) Se houve carta de Sorte/Revés aplicada por último, priorize-a
        String idSorte = modelo.obterIdUltimaCartaSorteReves();
        if (idSorte != null) {
            canvas.registrarCartaSorte(idSorte);
            return;
        }

        // 2) Caso não haja carta de Sorte/Revés ativa, exiba carta da propriedade
        GameModelo.VisaoCasa v = modelo.obterCasaAtual();
        if (v != null && "PROPRIEDADE".equals(v.tipo) && v.nome != null) {
            canvas.registrarCartaPropriedade(v.nome);
        } else {
            canvas.limparCarta();
        }
    }

    public ControladorJogo getControlador() { return controlador; }
    public GameModelo getModelo() { return modelo; }
}
