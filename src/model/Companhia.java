package model;

/**
 * Companhia comprável (ex.: transporte) sem construção de casas/hotel.
 * O aluguel depende do resultado do dado (multiplicador específico por companhia).
 */
final class Companhia extends Casa {

    final int preco;
    final int aluguelBase;
    final int aluguelPorPonto;
    Jogador proprietario; // null = sem dono

    private Companhia(String nome, int preco) {
        super(nome);
        this.preco = preco;
        this.aluguelBase = Math.max(25, percent(preco, 25)); // base proporcional ao custo
        this.aluguelPorPonto = multiplicadorPorNome(nome);
        this.proprietario = null;
    }

    static Companhia criar(String nome, int preco) {
        return new Companhia(nome, preco);
    }

    int aluguelPorDados(int somaDados) {
        if (aluguelPorPonto > 0) {
            if (somaDados <= 0) {
                return aluguelBase;
            }
            return aluguelPorPonto * somaDados;
        }
        return aluguelBase;
    }

    private static int percent(int valor, int porcento) {
        return (int) Math.round(valor * (porcento / 100.0));
    }

    private static int multiplicadorPorNome(String nome) {
        String chave = normalizar(nome);
        if ("companhia ferroviária".equals(chave)) return 50;
        if ("companhia de viação".equals(chave)) return 50;
        if ("companhia de táxi".equals(chave)) return 40;
        if ("companhia de taxi".equals(chave)) return 40;
        if ("companhia de navegação".equals(chave)) return 40;
        if ("companhia de aviação".equals(chave)) return 50;
        if ("companhia de táxi aéreo".equals(chave)) return 50;
        if ("companhia de taxi aéreo".equals(chave)) return 50;
        return 0;
    }

    private static String normalizar(String nome) {
        return (nome == null ? "" : nome.trim().toLowerCase(java.util.Locale.ROOT));
    }
}
