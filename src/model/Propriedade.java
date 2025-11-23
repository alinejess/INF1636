package model;

import java.util.Objects;

/** Propriedade comprável e que pode receber casas/hotel. */
class Propriedade extends Casa {

    int preco;          // preço do território
    int custoCasa;      // 50% do preço
    int custoHotel;     // 100% do preço
    Jogador proprietario;  // null = sem dono
    int casas;             // 0..4
    boolean hotel;         // true se houver hotel
    boolean construcaoLiberada; // libera construção após nova visita

    private Propriedade(String nome, int preco) {
        super(Objects.requireNonNull(nome));
        this.preco = preco;
        this.custoCasa = percent(preco, 50);   // 50% do preço
        this.custoHotel = percent(preco, 100); // 100% do preço
        this.proprietario = null;
        this.casas = 0;
        this.hotel = false;
        this.construcaoLiberada = true;
    }

    /** Fábrica moderna: calcula custos via fórmula. */
    static Propriedade criarBasica(String nome, int preco) {
        return new Propriedade(nome, preco);
    }

    /** Valor do aluguel conforme fórmula: Va = Vb + Vc*n + Vh. */
    int aluguel() {
        int vb = percent(preco, 10);                 // 10%
        int vc = percent(preco, 15) * casas;         // 15% por casa
        int vh = hotel ? percent(preco, 30) : 0;     // 30% se houver hotel
        return vb + vc + vh;
    }

    // --- Utilitário de porcentagem com arredondamento padrão ---
    private static int percent(int valor, int porcento) {
        // arredonda para o inteiro mais próximo para evitar perdas por truncamento
        return (int) Math.round(valor * (porcento / 100.0));
    }
}
