package model;

import java.util.*;

public class Jogo {
    private List<Jogador> jogadores;
    private Tabuleiro tabuleiro;
    private int jogadorAtual;

    public Jogo(List<String> nomesJogadores) {
        tabuleiro = new Tabuleiro();
        jogadores = new ArrayList<>();
        for (String nome : nomesJogadores) {
            jogadores.add(new Jogador(nome));
        }
        jogadorAtual = 0;
    }

    public int[] lancarDados() {
        Random r = new Random();
        return new int[] { r.nextInt(6) + 1, r.nextInt(6) + 1 };
    }

    public boolean deslocarPiao(int[] dados) {
        int soma = dados[0] + dados[1];
        Jogador j = jogadores.get(jogadorAtual);

        if (j.isPreso()) {
            if (dados[0] == dados[1]) {
                j.setPreso(false);
                System.out.println(j.getNome() + " saiu da prisão com dados iguais!");
            } else if (j.temCartaSaidaPrisao()) {
                j.setPreso(false);
                j.setTemCartaSaidaPrisao(false);
                System.out.println(j.getNome() + " usou carta de saída da prisão!");
            } else {
                System.out.println(j.getNome() + " continua preso.");
                return false;
            }
        }

        j.mover(soma, tabuleiro.getTotalCasas());
        Casa casa = tabuleiro.getCasa(j.getPosicao());
        casa.acao(j, this);

        // remover jogador falido
        if (j.getSaldo() < 0) {
            System.out.println(j.getNome() + " faliu e saiu do jogo!");
            jogadores.remove(j);
        }

        return true;
    }


    public void proximoJogador() {
        jogadorAtual = (jogadorAtual + 1) % jogadores.size();
    }

    public Jogador getJogadorAtual() {
        return jogadores.get(jogadorAtual);
    }
}
