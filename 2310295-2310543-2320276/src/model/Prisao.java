package model; // pacote do model	

class Prisao extends Casa { // casa de prisão (não pública)
    public Prisao(String nome) { // construtor padrão
        super(nome); // define o nome da casa
    }

    @Override
    public void acao(Jogador jogador, Jogo jogo) { // ação ao cair na casa de prisão (visitante)
        // visitante não tem nenhuma ação automática
    }
}