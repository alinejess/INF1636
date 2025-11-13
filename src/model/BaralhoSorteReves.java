package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class BaralhoSorteReves {
    private final List<Carta> cartas = new ArrayList<Carta>();
    private final Random rnd = new Random();
    private boolean cartaSairDisponivel = true; // controle da “única”

    static BaralhoSorteReves criarPadrao() {
        BaralhoSorteReves b = new BaralhoSorteReves();

        // sorte
        b.cartas.add(new CartaBancoPagaJogador("chance1.png", 25));
        b.cartas.add(new CartaBancoPagaJogador("chance2.png", 150));
        b.cartas.add(new CartaBancoPagaJogador("chance3.png", 80));
        b.cartas.add(new CartaBancoPagaJogador("chance4.png", 200));
        b.cartas.add(new CartaBancoPagaJogador("chance5.png", 50));
        b.cartas.add(new CartaBancoPagaJogador("chance6.png", 50));
        b.cartas.add(new CartaBancoPagaJogador("chance7.png", 100));
        b.cartas.add(new CartaBancoPagaJogador("chance8.png", 100));
        if (b.cartaSairDisponivel) b.cartas.add(new CartaSairDaPrisao("chance9.png")); // “Sair da Prisão” é única: entra só se disponível
        b.cartas.add(new CartaBancoPagaJogador("chance10.png", 200));
        b.cartas.add(new CartaRecebeDeCadaJogador("chance11.png", 50)); // recebe 50 de cada jogador
        b.cartas.add(new CartaBancoPagaJogador("chance12.png", 45));
        b.cartas.add(new CartaBancoPagaJogador("chance13.png", 100));
        b.cartas.add(new CartaBancoPagaJogador("chance14.png", 100));
        b.cartas.add(new CartaBancoPagaJogador("chance15.png", 20));
        
        // revés
        b.cartas.add(new CartaJogadorPagaBanco("chance16.png", 15));
        b.cartas.add(new CartaJogadorPagaBanco("chance17.png", 25));
        b.cartas.add(new CartaJogadorPagaBanco("chance18.png", 45));
        b.cartas.add(new CartaJogadorPagaBanco("chance19.png", 30));
        b.cartas.add(new CartaJogadorPagaBanco("chance20.png", 100));
        b.cartas.add(new CartaJogadorPagaBanco("chance21.png", 100));
        b.cartas.add(new CartaJogadorPagaBanco("chance22.png", 40));
        b.cartas.add(new CartaVaParaPrisao("chance23.png")); // NOVA (permanece no baralho)
        b.cartas.add(new CartaJogadorPagaBanco("chance24.png", 30));
        b.cartas.add(new CartaJogadorPagaBanco("chance25.png", 50));
        b.cartas.add(new CartaJogadorPagaBanco("chance26.png", 25));
        b.cartas.add(new CartaJogadorPagaBanco("chance27.png", 30));
        b.cartas.add(new CartaJogadorPagaBanco("chance28.png", 45));
        b.cartas.add(new CartaJogadorPagaBanco("chance29.png", 50));
        b.cartas.add(new CartaJogadorPagaBanco("chance30.png", 50));
        
        b.embaralhar();
        return b;
    }


    void embaralhar() { Collections.shuffle(cartas, rnd); }

    Carta comprar() {
        if (cartas.isEmpty()) return null;
        Carta c = cartas.remove(0);
        if (c instanceof CartaSairDaPrisao) cartaSairDisponivel = false; // saiu do baralho
        return c;
    }

    void devolverAoFundo(Carta c) {
        if (c == null) return;
        if (c instanceof CartaSairDaPrisao) return; // não volta automaticamente
        cartas.add(c);
    }

    void devolverCartaSairDaPrisao() {
        if (!cartaSairDisponivel) {
            cartas.add(new CartaSairDaPrisao("chance4.png"));
            cartaSairDisponivel = true;
            embaralhar();
        }
    }
}
