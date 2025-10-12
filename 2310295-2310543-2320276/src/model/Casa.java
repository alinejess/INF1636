package model;

class Casa {
    protected String nome;

    public Casa(String nome) {
        this.nome = nome;
    }
    
    public void acao(Jogador jogador, Jogo jogo) { } // hook

    public boolean podeSerCompradaPor(Jogador j) { return false; } // por padrão, não é comprável
    public boolean comprar(Jogador j, Banco banco) { return false; } // por padrão, compra não se aplica
    public boolean construir(Jogador j, Banco banco) { return false; } // por padrão, construção não se aplica

    public String getNome() { return nome; } // retorna o nome da casa
   
}
