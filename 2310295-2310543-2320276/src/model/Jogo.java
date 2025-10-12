package model;

import java.util.*;

public class Jogo {
    private final List<Jogador> jogadores;
    private final Map<String, Jogador> jogadorPorNome;
    private final Tabuleiro tabuleiro;
    private final Banco banco;
    private int jogadorAtual;
    private int doublesSeguidos;
    private Random rng = new Random();

    public Jogo(List<String> nomesJogadores) {
        tabuleiro = new Tabuleiro();
        banco = new Banco();
        jogadores = new ArrayList<>();
        jogadorPorNome = new LinkedHashMap<>();
        for (String nome : nomesJogadores) {
            Jogador j = new Jogador(nome);
            jogadores.add(j);
            jogadorPorNome.put(nome, j);
        }
        jogadorAtual = 0;
        doublesSeguidos = 0;
    }

    public void setRandom(Random r) {
        this.rng = (r != null ? r : new Random());
    }
    
    public void enviarParaPrisao(Jogador j) { // torna público para ser chamado por VaParaPrisao
        j.setPreso(true);
        j.resetTentativasPrisao();
        j.setPosicao(tabuleiro.getIndicePrisao()); // marca o estado de preso
        // zera contador de tentativas enquanto preso
        try {
            java.lang.reflect.Field f = j.getClass().getDeclaredField("tentativasPrisao");
            f.setAccessible(true);
            f.setInt(j, 0);
        } catch (Exception ignored) {}
    }
    
    public List<Integer> lancarDados() {
        int d1 = 1 + rng.nextInt(6); // 1..6
        int d2 = 1 + rng.nextInt(6); // 1..6
        return Arrays.asList(d1, d2);
    }
    
    public boolean usarCartaSaidaPrisao() {
        Jogador j = getJogadorAtual();
        if (j.isTemCartaSaidaPrisao()) {
            j.usarCartaSaidaPrisao(); // já zera a flag e marca preso=false
            // zera o contador de tentativas
            try {
                java.lang.reflect.Field f = j.getClass().getDeclaredField("tentativasPrisao");
                f.setAccessible(true);
                f.setInt(j, 0);
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    public boolean deslocarPiao(List<Integer> dados) {
        if (dados == null || dados.size() != 2) return false;

        int d1 = dados.get(0);
        int d2 = dados.get(1);

        Jogador j = getJogadorAtual();

        // ========== PRISÃO ==========
        if (j.isPreso()) {
            boolean saiu = false;

            // 1) dupla solta
            if (d1 == d2) {
                j.sairDaPrisao();
                resetTentativasPrisao(j);
                saiu = true;
            }
            // 2) carta "saída livre"
            else if (j.isTemCartaSaidaPrisao()) {
                j.usarCartaSaidaPrisao(); // zera flag e sai
                resetTentativasPrisao(j);
                saiu = true;
            }
            // 3) não saiu: permanece preso e perde a vez
            else {
                incTentativasPrisao(j);
                return true;
            }
        }

        // 3 duplas seguidas => prisão
        if (d1 == d2) {
            doublesSeguidos++;
            if (doublesSeguidos == 3) {
                enviarParaPrisao(j);
                doublesSeguidos = 0;
                return true;
            }
        } else {
            doublesSeguidos = 0;
        }

        int passos = d1 + d2;
        int posInicial = j.getPosicao();
        int total = tabuleiro.getTotalCasas();
        int posFinal = (posInicial + passos) % total;

        // +$200 ao passar pelo início
        if (posInicial + passos >= total) j.receber(200);

        j.setPosicao(posFinal); // atualiza a posição do jogador
        Casa c = tabuleiro.getCasa(posFinal);
        c.acao(j, this); // executa a ação da casa

        // Se o jogador faliu durante a ação da casa, avance imediatamente para o próximo jogador ATIVO.
        if (!j.isAtivo()) {
            proximoJogador();
        }

        return true;
    }

    public boolean comprarPropriedadeAtual() {
        if (!getJogadorAtual().isAtivo()) {
            proximoJogador();
        }

        Jogador j = getJogadorAtual();
        Casa c = tabuleiro.getCasa(j.getPosicao());
        // comprar() já retorna false se não puder
        return c.comprar(j, banco);
    }

    public boolean construirNaPropriedadeAtual() {
        Jogador j = getJogadorAtual();
        Casa c = tabuleiro.getCasa(j.getPosicao());
        return c.construir(j, banco);
    }

    public void proximoJogador() {
        if (jogadores.isEmpty()) return; // proteção para caso a lista esteja vazia
        do { // pula jogadores falidos
            jogadorAtual = (jogadorAtual + 1) % jogadores.size(); // avança circularmente
        } while (!jogadores.get(jogadorAtual).isAtivo()); // repete até achar um jogador ativo
    }

    public void vendaForcada() {
        Jogador j = getJogadorAtual();
        if (j.getSaldo() >= 0) return; // nada a fazer
        j.vendaForcada(banco);         // delega para o próprio jogador (sem reflexão)
    }

    public Jogador getJogadorAtual() {
        if (jogadores.isEmpty()) throw new IllegalStateException("Sem jogadores");
        // Se o atual estiver inativo (faliu), avance até encontrar um ativo
        int guard = jogadores.size(); // evita loop infinito em casos extremos
        while (guard-- > 0 && !jogadores.get(jogadorAtual).isAtivo()) {
            proximoJogador();
        }
        return jogadores.get(jogadorAtual);
    }

    public Jogador getJogador(String nome) { // acesso via Map
        return jogadorPorNome.get(nome);
    }

    public List<Jogador> getJogadores() { return new ArrayList<>(jogadores); } // lista somente leitura
    public Banco getBanco() { return banco; } // leitura do banco para operações internas
    public Tabuleiro getTabuleiro() { return tabuleiro; } // leitura do tabuleiro

    private static void resetTentativasPrisao(Jogador j) {
        try {
            java.lang.reflect.Field f = j.getClass().getDeclaredField("tentativasPrisao");
            f.setAccessible(true);
            f.setInt(j, 0);
        } catch (Exception ignored) {}
    }

    private static void incTentativasPrisao(Jogador j) {
        try {
            java.lang.reflect.Field f = j.getClass().getDeclaredField("tentativasPrisao");
            f.setAccessible(true);
            int v = f.getInt(j);
            f.setInt(j, v + 1);
        } catch (Exception ignored) {}
    }

    private static int getTentativasPrisao(Jogador j) {
        try {
            java.lang.reflect.Field f = j.getClass().getDeclaredField("tentativasPrisao");
            f.setAccessible(true);
            return f.getInt(j);
        } catch (Exception e) {
            return 0;
        }
    }
}