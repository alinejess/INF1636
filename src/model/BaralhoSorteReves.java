package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class BaralhoSorteReves {
    private final List<Carta> cartas = new ArrayList<Carta>();
    private final Random rnd = new Random();
    private boolean cartaSairDisponivel = true; // controle da “única”

    static final class CartaEstado {
        enum Tipo { BANCO_PAGA_JOGADOR, JOGADOR_PAGA_BANCO, RECEBE_DE_CADA_JOGADOR, SAIR_DA_PRISAO, VA_PARA_PRISAO }

        final String idImagem;
        final Tipo tipo;
        final int valor;

        CartaEstado(String idImagem, Tipo tipo, int valor) {
            this.idImagem = idImagem;
            this.tipo = tipo;
            this.valor = valor;
        }
    }

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
        if (b.cartaSairDisponivel) b.cartas.add(new CartaSairDaPrisao("chance9.png"));
        b.cartas.add(new CartaIrParaInicio("chance10.png", 200));
        b.cartas.add(new CartaRecebeDeCadaJogador("chance11.png", 50));
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
        b.cartaSairDisponivel = true;
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

    List<CartaEstado> exportarEstado() {
        List<CartaEstado> estado = new ArrayList<CartaEstado>(cartas.size());
        for (Carta c : cartas) {
            estado.add(criarEstadoDaCarta(c));
        }
        return estado;
    }

    void restaurarEstado(List<CartaEstado> estado, boolean cartaSairDisponivel) {
        cartas.clear();
        if (estado != null) {
            for (CartaEstado info : estado) {
                cartas.add(criarCartaAPartirDoEstado(info));
            }
        }
        this.cartaSairDisponivel = cartaSairDisponivel;
    }

    boolean isCartaSairDisponivel() {
        return cartaSairDisponivel;
    }

    void devolverCartaSairDaPrisao() {
        if (!cartaSairDisponivel) {
            cartas.add(new CartaSairDaPrisao("chance9.png"));
            cartaSairDisponivel = true;
            embaralhar();
        }
    }

    private static CartaEstado criarEstadoDaCarta(Carta carta) {
        if (carta instanceof CartaBancoPagaJogador) {
            CartaBancoPagaJogador c = (CartaBancoPagaJogador) carta;
            return new CartaEstado(carta.getIdImagem(), CartaEstado.Tipo.BANCO_PAGA_JOGADOR, c.getValor());
        }
        if (carta instanceof CartaJogadorPagaBanco) {
            CartaJogadorPagaBanco c = (CartaJogadorPagaBanco) carta;
            return new CartaEstado(carta.getIdImagem(), CartaEstado.Tipo.JOGADOR_PAGA_BANCO, c.getValor());
        }
        if (carta instanceof CartaRecebeDeCadaJogador) {
            CartaRecebeDeCadaJogador c = (CartaRecebeDeCadaJogador) carta;
            return new CartaEstado(carta.getIdImagem(), CartaEstado.Tipo.RECEBE_DE_CADA_JOGADOR, c.getPorJogador());
        }
        if (carta instanceof CartaSairDaPrisao) {
            return new CartaEstado(carta.getIdImagem(), CartaEstado.Tipo.SAIR_DA_PRISAO, 0);
        }
        return new CartaEstado(carta.getIdImagem(), CartaEstado.Tipo.VA_PARA_PRISAO, 0);
    }

    private static Carta criarCartaAPartirDoEstado(CartaEstado estado) {
        if (estado == null || estado.tipo == null) return null;
        switch (estado.tipo) {
            case BANCO_PAGA_JOGADOR:
                return new CartaBancoPagaJogador(estado.idImagem, estado.valor);
            case JOGADOR_PAGA_BANCO:
                return new CartaJogadorPagaBanco(estado.idImagem, estado.valor);
            case RECEBE_DE_CADA_JOGADOR:
                return new CartaRecebeDeCadaJogador(estado.idImagem, estado.valor);
            case SAIR_DA_PRISAO:
                return new CartaSairDaPrisao(estado.idImagem);
            case VA_PARA_PRISAO:
                return new CartaVaParaPrisao(estado.idImagem);
            default:
                return null;
        }
    }

}
