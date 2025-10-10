package model; // pacote do Model

class VaParaPrisao extends Casa { // casa que envia o jogador diretamente para a prisão
    public VaParaPrisao(String nome) { super(nome); } // construtor que define o nome

    @Override
    public void acao(Jogador jogador, Jogo jogo) { // ação ao cair nesta casa
        jogo.enviarParaPrisao(jogador); // chama a API do Jogo para prender o jogador
    }
}
