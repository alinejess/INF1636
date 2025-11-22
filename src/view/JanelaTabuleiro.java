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
    private final Button btnSalvar = new Button("Salvar jogo");
    private final Button btnCarregar = new Button("Carregar jogo");
    private final Button btnComprarProp = new Button("Comprar propriedade");
    private final Button btnConstruir = new Button("Construir casa/hotel");
    private final Button btnComprarCompanhia = new Button("Comprar companhia");
    private final Button btnDetalhesJogador = new Button("Detalhes do jogador");
    
    public JanelaTabuleiro(ControladorJogo controlador, GameModelo modelo) {
    	
        super("Banco Imobiliário — Tabuleiro");
        // this.canvas = new TabuleiroCanvas(modelo, controlador);
        this.controlador = controlador;
        this.modelo = modelo;

        setSize(new Dimension(1000, 700));
        setResizable(false);
        setLayout(new BorderLayout());
        
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                controlador.registrarEstadoFinal();
                dispose();
            }
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
        barra.add(btnComprarProp);
        barra.add(btnConstruir);
        barra.add(btnComprarCompanhia);
        barra.add(btnSalvar);
        barra.add(btnCarregar);
        barra.add(btnDetalhesJogador);
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
        btnComprarProp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.comprarPropriedade(JanelaTabuleiro.this);
            }
        });
        btnConstruir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.construirCasaOuHotel(JanelaTabuleiro.this);
            }
        });
        btnComprarCompanhia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.comprarCompanhia(JanelaTabuleiro.this);
            }
        });
        btnDetalhesJogador.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exibirDetalhesJogador();
            }
        });
        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.salvarPartida(JanelaTabuleiro.this);
            }
        });
        btnCarregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.carregarPartida(JanelaTabuleiro.this);
            }
        });

        // Observer
        modelo.adicionarOuvinte(this);
        atualizarEstadoInterface();
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
            atualizarEstadoInterface();
            return;
        }
        if (e == EventoJogo.DADOS_LANCADOS) {
            GameModelo.Lancamento l = (GameModelo.Lancamento) payload;
            canvas.definirDados(l.d1, l.d2);
            atualizarEstadoInterface();
            return;
        }
        if (e == EventoJogo.CARTA_SORTE_REVES_APLICADA && payload instanceof String) {
            canvas.registrarCarta((String) payload);
        }
        if (e == EventoJogo.ORDEM_SORTEADA) {
            System.out.println("[UI] Ordem sorteada recebida na View.");
            canvas.repaint();
            atualizarEstadoInterface();
            return;
        }
        atualizarEstadoInterface();
    }
    
    private void atualizarCartaEmTela() {
        // 1) Se houve carta de Sorte/Revés aplicada por último, priorize-a
        String idSorte = modelo.obterIdUltimaCartaSorteReves();
        if (idSorte != null) {
            canvas.registrarCartaSorte(idSorte);
            return;
        }

        // 2) Caso não haja carta de Sorte/Revés ativa, exiba carta da casa atual
        GameModelo.VisaoCasa v = modelo.obterCasaAtual();
        if (v != null && v.nome != null) {
            if ("PROPRIEDADE".equals(v.tipo)) {
                canvas.registrarCartaPropriedade(v.nome);
                return;
            }
            if ("COMPANHIA".equals(v.tipo)) {
                canvas.registrarCartaCompanhia(v.nome);
                return;
            }
        }
        canvas.limparCarta();
    }

    public ControladorJogo getControlador() { return controlador; }
    public GameModelo getModelo() { return modelo; }

    private void atualizarEstadoInterface() {
        if (btnSalvar != null) {
            btnSalvar.setEnabled(modelo != null && modelo.estaNoInicioDoTurno());
        }
        GameModelo.VisaoCasa casa = null;
        GameModelo.VisaoJogador jogador = null;
        try {
            casa = modelo.obterCasaAtual();
            jogador = modelo.obterJogadorDaVez();
        } catch (Throwable ignored) {}

        String nomeJogador = (jogador == null ? null : jogador.nome);
        boolean propriedadeDisponivel = casa != null && "PROPRIEDADE".equals(casa.tipo) && casa.proprietario == null;
        boolean propriedadeDoJogador = casa != null && "PROPRIEDADE".equals(casa.tipo) &&
                casa.proprietario != null && casa.proprietario.equals(nomeJogador);
        boolean companhiaDisponivel = casa != null && "COMPANHIA".equals(casa.tipo) && casa.proprietario == null;

        btnComprarProp.setEnabled(propriedadeDisponivel);
        btnConstruir.setEnabled(propriedadeDoJogador);
        btnComprarCompanhia.setEnabled(companhiaDisponivel);
        btnDetalhesJogador.setEnabled(modelo != null && jogador != null);
        canvas.atualizarInformacoesJogador(jogador, casa);
    }

    private void exibirDetalhesJogador() {
        if (modelo == null) return;
        GameModelo.VisaoJogador jogador;
        try {
            jogador = modelo.obterJogadorDaVez();
        } catch (Throwable t) {
            return;
        }
        if (jogador == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Saldo: $").append(jogador.saldo).append('\n');
        sb.append("Posição no tabuleiro: ").append(jogador.posicao).append('\n');
        sb.append("Na prisão: ").append(jogador.naPrisao ? "Sim" : "Não");
        if (jogador.temCartaSaidaDaPrisao) {
            sb.append(" (possui carta de saída)");
        }
        sb.append('\n');

        sb.append("Propriedades:\n");
        if (jogador.propriedades == null || jogador.propriedades.isEmpty()) {
            sb.append("  — nenhuma\n");
        } else {
            for (String prop : jogador.propriedades) {
                sb.append("  • ").append(prop).append('\n');
            }
        }

        sb.append("Companhias:\n");
        if (jogador.companhias == null || jogador.companhias.isEmpty()) {
            sb.append("  — nenhuma\n");
        } else {
            for (String comp : jogador.companhias) {
                sb.append("  • ").append(comp).append('\n');
            }
        }

        javax.swing.JOptionPane.showMessageDialog(this,
                sb.toString(),
                "Jogador " + jogador.nome,
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}
