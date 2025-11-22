package model;

/**
 * Companhia comprável (ex.: transporte) sem construção de casas/hotel.
 * O aluguel escala conforme a quantidade de companhias pertencentes ao mesmo dono.
 */
final class Companhia extends Casa {

    final int preco;
    final int aluguelBase;
    Jogador proprietario; // null = sem dono

    private Companhia(String nome, int preco) {
        super(nome);
        this.preco = preco;
        this.aluguelBase = Math.max(25, percent(preco, 25)); // base proporcional ao custo
        this.proprietario = null;
    }

    static Companhia criar(String nome, int preco) {
        return new Companhia(nome, preco);
    }

    /** Cálculo linear: cada companhia do mesmo jogador multiplica o aluguel base. */
    int aluguel(int quantidadeDoMesmoDono) {
        int multiplicador = Math.max(1, quantidadeDoMesmoDono);
        return aluguelBase * multiplicador;
    }

    private static int percent(int valor, int porcento) {
        return (int) Math.round(valor * (porcento / 100.0));
    }
}
