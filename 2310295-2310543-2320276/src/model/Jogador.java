package model;

import java.util.*;

class Jogador {
    private final String nome;
    private int saldo;
    private int posicao; // posição atual no tabuleiro
    private boolean preso;
    private boolean temCartaSaidaPrisao; // flag da carta de saída da prisão
    private boolean ativo; // indica se o jogador ainda está no jogo (não faliu)
    private final List<Propriedade> propriedades; // lista de propriedades do jogador
    private int tentativasPrisao = 0;
    int getTentativasPrisao() { return tentativasPrisao; }
    void incTentativasPrisao() { tentativasPrisao++; }
    void resetTentativasPrisao() { tentativasPrisao = 0; }
    
    public Jogador(String nome) {
        this.nome = nome;
        this.saldo = 4000;
        this.posicao = 0;
        this.preso = false;
        this.temCartaSaidaPrisao = false;
        this.ativo = true;
        this.propriedades = new ArrayList<>();
    }

    public void receber(int valor) { saldo += valor; }
    public void debitar(int valor) { saldo -= valor; }

    public void adicionarPropriedade(Propriedade p) { // adiciona propriedade
        propriedades.add(p);
    }

    public void pagar(int valor, Jogador credor, Banco banco) {
        if (!ativo || valor <= 0) return;

        // 1) Debita o valor devido
        debitar(valor);

        // 2) Se ficou negativo, tenta liquidar propriedades (90%) antes de falir
        if (saldo < 0) {
            // copia e ordena as propriedades da mais barata para a mais cara
            List<Propriedade> ordenadas = new ArrayList<>(propriedades);
            Collections.sort(ordenadas, new Comparator<Propriedade>() {
                @Override
                public int compare(Propriedade a, Propriedade b) {
                    // evita overflow
                    return a.getValorTotal() - b.getValorTotal();
                }
            });

            for (Propriedade p : ordenadas) {
                if (saldo >= 0) break;
                int valorVenda = (int) Math.round(p.getValorTotal() * 0.9);
                receber(valorVenda);
                // transfere para o banco
                p.transferirPara(banco);
            }
        }

        // 3) Calcula quanto efetivamente será creditado ao recebedor
        int pago;
        if (saldo >= 0) {
            // regularizou -> pagamento integral
            pago = valor;
        } else {
            // ainda negativo -> pagamento parcial possível (não deixar < 0)
            pago = valor + saldo; // saldo é negativo
            if (pago < 0) pago = 0;
        }

        // 4) Credita ao recebedor (Jogador ou Banco)
        if (credor != null) {
            credor.receber(pago);
        } else if (banco != null) {
            banco.receber(pago);
        }

        // 5) Se ainda ficou negativo após tentar vender, declara falência
        if (saldo < 0) {
            falir(banco);
        }
    }

    void removerPropriedade(Propriedade p) {
        propriedades.remove(p);
    }
    
    void vendaForcada(Banco banco) {
        if (saldo >= 0) return;

        // copia e ordena as propriedades da mais barata para a mais cara
        List<Propriedade> ordenadas = new ArrayList<>(propriedades);
        Collections.sort(ordenadas, new Comparator<Propriedade>() {
            @Override
            public int compare(Propriedade a, Propriedade b) {
                return a.getValorTotal() - b.getValorTotal();
            }
        });

        for (Propriedade p : ordenadas) {
            if (saldo >= 0) break;
            int valorVenda = (int) Math.round(p.getValorTotal() * 0.9);
            receber(valorVenda);
            p.transferirPara(banco); // este método já remove do dono e zera construções
        }

        if (saldo < 0) {
            falir(banco); // ainda negativo => falência
        }
    }

    void falir(Banco banco) {
        ativo = false;  // marca jogador como inativo (fora do jogo)
        // Soma de 90% do valor total de todas as propriedades
        int totalRecebido = 0;
        for (Propriedade p : new ArrayList<>(propriedades)) { // evitar modificação durante iteração
            int valorVenda = (int) Math.round(p.getValorTotal() * 0.9);
            if (valorVenda > 0) totalRecebido += valorVenda;
            p.transferirPara(banco); // transfere a propriedade ao banco e limpa construções/dono
        }

        propriedades.clear(); // garante que a lista local não retenha referências
        receber(totalRecebido); // credita o valor líquido da liquidação
    }
    
    public void usarCartaSaidaPrisao() {
        if (temCartaSaidaPrisao) {
            temCartaSaidaPrisao = false;
            this.preso = false;
            resetTentativasPrisao();
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
    public void setSaldo(int novoSaldo) {
        this.saldo = novoSaldo;
    }
}
