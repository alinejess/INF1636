package model;

import java.util.*;

class Jogador { // representa um jogador (não público)
    private final String nome; // nome do jogador
    private int saldo; // saldo de dinheiro do jogador
    private int posicao; // posição atual no tabuleiro
    private boolean preso; // flag indicando se está preso
    private boolean temCartaSaidaPrisao; // flag da carta de saída da prisão
    private boolean ativo; // indica se o jogador ainda está no jogo (não faliu)
    private final List<Propriedade> propriedades; // lista de propriedades do jogador
    
    public Jogador(String nome) { // construtor recebendo o nome
        this.nome = nome; // armazena o nome
        this.saldo = 4000; // saldo inicial do jogador (pode ajustar conforme regra desejada)
        this.posicao = 0; // começa no início (índice 0)
        this.preso = false; // não está preso ao iniciar
        this.temCartaSaidaPrisao = false; // não tem carta ao iniciar
        this.ativo = true; // entra ativo no jogo
        this.propriedades = new ArrayList<>(); // cria a lista de propriedades
    }

    public void receber(int valor) { saldo += valor; } // adiciona valor ao saldo
    public void debitar(int valor) { saldo -= valor; } // subtrai valor do saldo

    public void adicionarPropriedade(Propriedade p) { // adiciona propriedade ao portfólio
        propriedades.add(p); // inclui na lista
    }

    public void pagar(int valor, Jogador credor, Banco banco) { // paga um valor a outro jogador
        if (!ativo) return; // se já está inativo, ignora
        debitar(valor); // debita o valor devido
        if (saldo >= 0) { // se ainda ficou com saldo não negativo
            credor.receber(valor); // transfere o valor integral ao credor
        } else { // se o saldo ficou negativo (não conseguiu pagar tudo)
            credor.receber(valor + saldo); // paga apenas a parte que tinha (valor + saldo negativo)
            falir(banco); // declara falência
        }
    }

    void falir(Banco banco) { // processa a falência do jogador
        ativo = false;  // marca jogador como inativo (fora do jogo)
        for (Propriedade p : new ArrayList<>(propriedades)) { // copia a lista para evitar modificação durante iteração
            int valorVenda = (int)Math.round(p.getValorTotal() * 0.9); // vende para o banco por 90% do valor total
            receber(valorVenda); // recebe o valor de liquidação
            p.transferirPara(banco); // transfere a propriedade ao banco e limpa construções
        }
        System.out.println(nome + " faliu e saiu do jogo."); // log simples
    }
    
    public void usarCartaSaidaPrisao() { // usa a carta de saída da prisão
        if (temCartaSaidaPrisao) { // se possui a carta
            temCartaSaidaPrisao = false; // se possui a carta
            this.preso = false; // e sai da prisão
        }
    }

    public boolean isPreso() { return preso; } // consulta se está preso
    public void setPreso(boolean preso) { this.preso = preso; }  // altera estado de prisão
    public void sairDaPrisao() { this.preso = false; }  // sai da prisão
    public boolean isTemCartaSaidaPrisao() { return temCartaSaidaPrisao; } // consulta a posse da carta
    public void setTemCartaSaidaPrisao(boolean b) { this.temCartaSaidaPrisao = b; } // define posse da carta
    
    public int getPosicao() { return posicao; } // devolve posição atual
    public void setPosicao(int posicao) { this.posicao = posicao; } // atualiza posição
    public int getSaldo() { return saldo; } // devolve saldo atual
    public String getNome() { return nome; }   // devolve nome do jogador
    public boolean isAtivo() { return ativo; } // indica se está ativo no jogo
} 
