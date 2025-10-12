package model;

class Prisao extends Casa {
    public Prisao(String nome) {
        super(nome); // define o nome da casa
    }

    @Override
    public void acao(Jogador jogador, Jogo jogo) { // ação ao cair na casa de prisão (visitante)
    }
}