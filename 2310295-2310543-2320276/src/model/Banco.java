package model;

import java.util.*;

class Banco {
    private int saldo;
    private final List<Propriedade> propriedades; // propriedades sob posse do banco

    public Banco() {
        saldo = 200000;// saldo inicial arbitrário para o banco
        propriedades = new ArrayList<>(); // lista de propriedades do banco
    }

    public void venderPropriedade(Propriedade p) {
        propriedades.remove(p); // remove do estoque do banco
        saldo += p.getPreco(); // o banco recebe o valor da venda
    }

    public void adicionarPropriedade(Propriedade p) { // adiciona propriedade ao estoque do banco
        if (!propriedades.contains(p)) propriedades.add(p); // evita duplicidade
    }

    public void receber(int valor) { saldo += valor; } // método para o banco receber valores 
    public int getSaldo() { return saldo; } // expõe o saldo do banco (leitura)
}
