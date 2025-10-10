package model; // pacote do model

class Casa { // classe base para casa
    protected String nome; // nome da casa

    public Casa(String nome) { // constructor recebe o nome da casa
        this.nome = nome; // armazena o nome
    }
    
    /** Ação ao cair na casa (padrão: nada). */
    public void acao(Jogador jogador, Jogo jogo) { } // ação ao cair numa casa (aind não impementado)

    /* ==== MÉTODOS POLIMÓRFICOS (ganchos) ==== */
    public boolean podeSerCompradaPor(Jogador j) { return false; } // por padrão, não é comprável
    public boolean comprar(Jogador j, Banco banco) { return false; } // por padrão, compra não se aplica
    public boolean construir(Jogador j, Banco banco) { return false; } // por padrão, construção não se aplica

    public String getNome() { return nome; } // retorna o nome da casa
   
}
