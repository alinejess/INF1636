package model;

import java.util.*;

class Jogador {
    private String nome;
    private int saldo;
    private int posicao;
    private boolean preso;
    private boolean temCartaSaidaPrisao;
    private List<Propriedade> propriedades;

    public Jogador(String nome) {
        this.nome = nome;
        this.saldo = 4000;
        this.posicao = 0;
        this.preso = false;
        this.temCartaSaidaPrisao = false;
        this.propriedades = new ArrayList<>();
    }
    
    public void venderPropriedadeAoBanco(Propriedade p, Banco banco) {
        int valorVenda = (int)(0.9 * p.getValorTotal());
        saldo += valorVenda;
        banco.comprarPropriedade(p);
        propriedades.remove(p);
        System.out.println(nome + " vendeu " + p.getNome() + " ao banco por $" + valorVenda);
    }

    public void setTemCartaSaidaPrisao(boolean valor) {
        temCartaSaidaPrisao = valor;
    }

    public boolean temCartaSaidaPrisao() {
        return temCartaSaidaPrisao;
    }
    
    public void mover(int casas, int totalCasas) {
        if (!preso) {
            posicao = (posicao + casas) % totalCasas;
        }
    }

    public void setPreso(boolean preso) {
        this.preso = preso;
    }
    
    public void comprarPropriedade(Propriedade p) {
        if (saldo >= p.getPreco()) {
            saldo -= p.getPreco();
            p.setDono(this);
            propriedades.add(p);
        }
    }

    public boolean isPreso() {
        return preso;
    }
    
    public void pagarAluguel(Jogador dono, int valor) {
        saldo -= valor;
        dono.receber(valor);
        if (saldo < 0) {
            System.out.println(nome + " faliu!");
        }
    }

    public void receber(int valor) {
        saldo += valor;
    }

    public int getPosicao() { return posicao; }
    public int getSaldo() { return saldo; }
    public String getNome() { return nome; }
}
