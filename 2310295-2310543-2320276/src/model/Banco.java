package model; // pacote do Model

import java.util.*; // importa List e ArrayList

class Banco { // representa o banco do jogo
    private int saldo; // saldo do banco (controle interno)
    private final List<Propriedade> propriedades; // propriedades sob posse do banco

    public Banco() { // construtor padrão
        saldo = 200000;// saldo inicial arbitrário para o banco
        propriedades = new ArrayList<>(); // lista de propriedades do banco
    }

    public void venderPropriedade(Propriedade p) { // registra a venda de uma propriedade no jogo
        propriedades.remove(p); // remove do estoque do banco, caso esteja 
        saldo += p.getPreco(); // o banco recebe o valor da venda a partir do preço da propriedade
    }

    public void adicionarPropriedade(Propriedade p) { // adiciona propriedade ao stoque do banco
        if (!propriedades.contains(p)) propriedades.add(p); // evita duplicidade
    }

    public void receber(int valor) { saldo += valor; } // método para o banco receber valores 
    public int getSaldo() { return saldo; } // expõe o saldo do banco (leitura)
}
