package model;

final class Jogador {
    final String nome;
    int saldo;
    int posicao; // índice no tabuleiro
    boolean naPrisao;
    int turnosNaPrisao;
    int cartasSaidaDaPrisao;
    boolean ativo;

    Jogador(String nome, int saldoInicial, int posicaoInicial) {
        this.nome = nome;
        this.saldo = saldoInicial;
        this.posicao = posicaoInicial;
        this.naPrisao = false;
        this.turnosNaPrisao = 0;
        this.cartasSaidaDaPrisao = 0;
        this.ativo = true;
    }
    
    boolean temCartaSaidaPrisao(Jogador j) {
        return j.cartasSaidaDaPrisao > 0;
    }
}
