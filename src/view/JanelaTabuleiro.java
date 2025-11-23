package view;

import controller.ControladorJogo;

import model.EventoJogo;
import model.GameModelo;
import model.OuvinteJogo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class JanelaTabuleiro extends JFrame implements OuvinteJogo {

    private static final long serialVersionUID = 1L;

    private final ControladorJogo controlador;
    private final GameModelo modelo;
    private final TabuleiroCanvas canvas;

    private final JComboBox<String> escolhaD1 = new JComboBox<String>();
    private final JComboBox<String> escolhaD2 = new JComboBox<String>();
    private final JButton btnForcar = new JButton("Lançar (forçar)");
    private final JButton btnAleatorio = new JButton("Lançar (aleatório)");
    private final JButton btnEncerrarTurno = new JButton("Encerrar turno");
    private final JButton btnSalvar = new JButton("Salvar");
    private final JButton btnCarregar = new JButton("Carregar");
    private final JButton btnComprarProp = new JButton("Comprar propriedade");
    private final JButton btnConstruir = new JButton("Construir casa/hotel");
    private final JButton btnComprarCompanhia = new JButton("Comprar companhia");
    private final JButton btnVenderPropriedade = new JButton("Vender propriedade");
    private final JButton btnEncerrarJogo = new JButton("Encerrar jogo");
    private final JToggleButton btnTelaCheia = new JToggleButton("Tela cheia");

    private boolean telaCheia = false;

    public JanelaTabuleiro(ControladorJogo controlador, GameModelo modelo) {
        super("Banco Imobiliário — Tabuleiro");
        this.controlador = controlador;
        this.modelo = modelo;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 720));
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (controlador.getModelo() != null) {
                    controlador.encerrarJogo(JanelaTabuleiro.this);
                } else {
                    dispose();
                }
            }
        });

        canvas = new TabuleiroCanvas(modelo, controlador);
        add(canvas, BorderLayout.CENTER);

        JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        for (int i = 1; i <= 6; i++) {
            escolhaD1.addItem(Integer.toString(i));
            escolhaD2.addItem(Integer.toString(i));
        }
        barra.add(escolhaD1);
        barra.add(escolhaD2);
        barra.add(btnForcar);
        barra.add(btnAleatorio);
        barra.add(btnEncerrarTurno);
        barra.add(btnComprarProp);
        barra.add(btnConstruir);
        barra.add(btnComprarCompanhia);
        barra.add(btnVenderPropriedade);
        barra.add(btnSalvar);
        barra.add(btnCarregar);
        barra.add(btnTelaCheia);
        barra.add(btnEncerrarJogo);
        add(barra, BorderLayout.SOUTH);

        configurarAcoes();

        modelo.adicionarOuvinte(this);
        atualizarEstadoInterface();

        pack();
        setVisible(true);
    }

    private void configurarAcoes() {
        btnForcar.addActionListener((ActionEvent e) -> {
            int d1 = Integer.parseInt((String) escolhaD1.getSelectedItem());
            int d2 = Integer.parseInt((String) escolhaD2.getSelectedItem());
            controlador.lancarForcado(d1, d2);
        });
        btnAleatorio.addActionListener(e -> controlador.lancarAleatorio());
        btnEncerrarTurno.addActionListener(e -> controlador.encerrarTurno());
        btnSalvar.addActionListener(e -> controlador.salvarPartida(this));
        btnCarregar.addActionListener(e -> controlador.carregarPartida(this));
        btnComprarProp.addActionListener(e -> controlador.comprarPropriedade(this));
        btnConstruir.addActionListener(e -> controlador.construirCasaOuHotel(this));
        btnComprarCompanhia.addActionListener(e -> controlador.comprarCompanhia(this));
        btnVenderPropriedade.addActionListener(e -> controlador.venderPropriedade(this));
        btnEncerrarJogo.addActionListener(e -> controlador.encerrarJogo(this));
        btnTelaCheia.addActionListener(e -> alternarTelaCheia());
    }

    private void alternarTelaCheia() {
        telaCheia = btnTelaCheia.isSelected();
        if (telaCheia) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setExtendedState(JFrame.NORMAL);
        }
    }

    @Override
    public void notificar(EventoJogo e, Object payload) {
        switch (e) {
            case CARTA_SORTE_REVES_APLICADA:
                String id = (payload instanceof String) ? (String) payload : modelo.obterIdUltimaCartaSorteReves();
                canvas.registrarCartaSorte(id);
                break;
            case ESTADO_ATUALIZADO:
                atualizarCartaEmTela();
                break;
            default:
                break;
        }
        if (e == EventoJogo.DADOS_LANCADOS && payload instanceof GameModelo.Lancamento) {
            canvas.limparCarta();
            GameModelo.Lancamento l = (GameModelo.Lancamento) payload;
            canvas.definirDados(l.d1, l.d2);
        }
        atualizarEstadoInterface();
        canvas.repaint();
    }

    private void atualizarCartaEmTela() {
        if (modelo.estaNoInicioDoTurno()) {
            canvas.limparCarta();
            return;
        }
        String idSorte = modelo.obterIdUltimaCartaSorteReves();
        if (idSorte != null) {
            canvas.registrarCartaSorte(idSorte);
            return;
        }
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

    private void atualizarEstadoInterface() {
        GameModelo.VisaoCasa casa = null;
        GameModelo.VisaoJogador jogador = null;
        boolean podeLancar = true;
        try {
            casa = modelo.obterCasaAtual();
            jogador = modelo.obterJogadorDaVez();
            podeLancar = modelo.podeLancarDados();
        } catch (Throwable ignored) {}

        String nomeJogador = (jogador == null ? null : jogador.nome);
        boolean propriedadeDisponivel = casa != null && "PROPRIEDADE".equals(casa.tipo) && casa.proprietario == null;
        boolean propriedadeDoJogador = casa != null && "PROPRIEDADE".equals(casa.tipo) &&
                casa.proprietario != null && casa.proprietario.equals(nomeJogador) &&
                Boolean.TRUE.equals(casa.construcaoLiberada);
        boolean companhiaDisponivel = casa != null && "COMPANHIA".equals(casa.tipo) && casa.proprietario == null;

        btnSalvar.setEnabled(modelo.estaNoInicioDoTurno());
        btnForcar.setEnabled(podeLancar);
        btnAleatorio.setEnabled(podeLancar);
        boolean possuiPropriedades = jogador != null && jogador.propriedades != null && !jogador.propriedades.isEmpty();
        btnComprarProp.setEnabled(propriedadeDisponivel);
        btnConstruir.setEnabled(propriedadeDoJogador);
        btnComprarCompanhia.setEnabled(companhiaDisponivel);
        btnVenderPropriedade.setEnabled(possuiPropriedades);
        canvas.atualizarInformacoesJogador(jogador, casa);
    }
}
