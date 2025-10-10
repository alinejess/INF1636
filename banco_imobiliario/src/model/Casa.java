package model;

class Casa {
    protected String nome;

    public Casa(String nome) {
        this.nome = nome;
    }

    public void acao(Jogador jogador, Jogo jogo) {
        // comportamento padrão: nada acontece
    }

    public String getNome() {
        return nome;
    }
}
