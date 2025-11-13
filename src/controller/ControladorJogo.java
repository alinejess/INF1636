package controller;

import model.GameModelo;
import view.JanelaTabuleiro;
import view.CoresJogador.CorPino;

public class ControladorJogo {

    private GameModelo modelo;
    private JanelaTabuleiro janelaTabuleiro;    

    public void iniciarNovaPartida(int quantidadeJogadores) {
        if (quantidadeJogadores < 3) quantidadeJogadores = 3;
        if (quantidadeJogadores > 6) quantidadeJogadores = 6;

        // criando jogadores
        modelo = new GameModelo();
        for (int i = 0; i < quantidadeJogadores; i++) {
            CorPino cor = CorPino.porIndice(i);
            String nome = "Jogador " + cor.getRotulo();
            modelo.adicionarJogador(nome); // mantém API do Model sem acoplar na View
        }
        
        modelo.sortearOrdemJogadores();
        
        janelaTabuleiro = new JanelaTabuleiro(this, modelo);
        janelaTabuleiro.mostrar();
    }

    public GameModelo getModelo() { return modelo; }
    
    public void encerrarTurno() {
        modelo.encerrarTurno();
    }
    
    public void lancarAleatorio() {
        int d1 = 1 + new java.util.Random().nextInt(6);
        int d2 = 1 + new java.util.Random().nextInt(6);
        modelo.registrarLancamento(d1, d2);
        modelo.deslocarPiao(d1, d2);
    }

    public void lancarForcado(int d1, int d2) {
        if (d1 < 1 || d1 > 6 || d2 < 1 || d2 > 6) return;
        modelo.registrarLancamento(d1, d2);
        modelo.deslocarPiao(d1, d2);
    }
    
    public void aplicarProximaCartaParaJogadorDaVez() {
        modelo.aplicarSorteRevesNoJogadorDaVez();
    }
}
