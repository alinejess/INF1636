package model;

class Prisao extends Casa {
    public Prisao(String nome) {
        super(nome);
    }

    @Override
    public void acao(Jogador jogador, Jogo jogo) {
        jogador.setPreso(true);
        System.out.println(jogador.getNome() + " foi preso!");
    }
}
