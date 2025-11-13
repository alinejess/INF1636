package model;

abstract class Carta {
    private final String idImagem; // ex.: "chance17.png"

    Carta(String idImagem) {
        this.idImagem = idImagem;
    }

    public String getIdImagem() { return idImagem; }

    /** Aplica o efeito da carta ao jogador J no modelo M. */
    abstract void aplicar(GameModelo m, Jogador j);

    /** Por padrão, cartas voltam ao fundo do baralho após aplicadas. */
    boolean deveVoltarAoFundo() { return true; }
}

/** +$X vindo do Banco */
final class CartaBancoPagaJogador extends Carta {
    private final int valor;
    CartaBancoPagaJogador(String idImagem, int valor) { super(idImagem); this.valor = valor; }
    @Override void aplicar(GameModelo jogo, Jogador j) { jogo.transferirBancoParaJogador(j, valor); }
}

/** -$X para o Banco */
final class CartaJogadorPagaBanco extends Carta {
    private final int valor;
    CartaJogadorPagaBanco(String idImagem, int valor) { super(idImagem); this.valor = valor; }
    @Override void aplicar(GameModelo jogo, Jogador j) { jogo.transferirJogadorParaBanco(j, valor); }
}

/** +$Y de cada jogador ativo */
final class CartaRecebeDeCadaJogador extends Carta {
    private final int porJogador;
    CartaRecebeDeCadaJogador(String idImagem, int porJogador) { super(idImagem); this.porJogador = porJogador; }
    @Override void aplicar(GameModelo jogo, Jogador j) { jogo.receberDeCadaJogador(j, porJogador); }
}

/** Ganha a carta “Sair da Prisão” (única) */
final class CartaSairDaPrisao extends Carta {
    CartaSairDaPrisao(String idImagem) { super(idImagem); }
    @Override void aplicar(GameModelo jogo, Jogador j) { jogo.concederCartaSaidaDaPrisaoAoJogadorDaVez(j); }
}

/** Vai imediatamente para a prisão (permanece no baralho; não é única) */
final class CartaVaParaPrisao extends Carta {
    CartaVaParaPrisao(String idImagem) { super(idImagem); }
    @Override void aplicar(GameModelo jogo, Jogador j) { jogo.enviarParaPrisao(j); }
}
