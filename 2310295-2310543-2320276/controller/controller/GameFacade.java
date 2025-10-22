package controller;

import model.*;  // << usa o pacote do seu modelo (src.model)

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Façade para a View. Encapsula o acesso ao modelo e emite eventos para a UI.
 * A View NÃO acessa o modelo diretamente.
 */
public class GameFacade {
    private final Jogo jogo;
    private final Map<String, Color> playerColors = new LinkedHashMap<>();
    private final List<GameListener> listeners = new CopyOnWriteArrayList<>();

    private int lastD1 = 0, lastD2 = 0;
    private Propriedade lastLandedProperty = null;

    private static final Color[] PALETTE = new Color[]{
            new Color(220, 20, 60),   // crimson
            new Color(30, 144, 255),  // dodgerBlue
            new Color(50, 205, 50),   // limeGreen
            new Color(255, 140, 0),   // darkOrange
            new Color(186, 85, 211),  // mediumOrchid
            new Color(0, 206, 209),   // darkTurquoise
            new Color(255, 105, 180), // hotPink
            new Color(112, 128, 144)  // slateGray
    };

    public GameFacade(List<String> nomesJogadores) {
        this.jogo = new Jogo(nomesJogadores);
        int i = 0;
        for (Jogador j : jogo.getJogadores()) {
            playerColors.put(j.getNome(), PALETTE[i % PALETTE.length]);
            i++;
        }
    }

    // ---------------------------- Observers ----------------------------
    public void addListener(GameListener l) { listeners.add(l); }
    public void removeListener(GameListener l) { listeners.remove(l); }
    private void fireState() { listeners.forEach(GameListener::onStateChanged); }
    private void firePlayerChanged() { listeners.forEach(GameListener::onPlayerChanged); }
    private void fireDiceRolled() { listeners.forEach(l -> l.onDiceRolled(lastD1, lastD2)); }
    private void fireLandedProperty() {
        if (lastLandedProperty != null) {
            for (GameListener l : listeners) l.onLandedOnProperty(lastLandedProperty);
        } else {
            fireState();
        }
    }

    // ----------------------------- Queries -----------------------------
    public List<Jogador> getJogadores() { return jogo.getJogadores(); }
    public Jogador getJogadorAtual() { return jogo.getJogadorAtual(); }
    public Tabuleiro getTabuleiro() { return jogo.getTabuleiro(); }

    public int getTotalCasas() { return jogo.getTabuleiro().getTotalCasas(); }
    public Casa getCasa(int idx) { return jogo.getTabuleiro().getCasa(idx); }
    public int getIndicePrisao() { return jogo.getTabuleiro().getIndicePrisao(); }

    public int getPosicao(Jogador j) { return j.getPosicao(); }
    public boolean isPreso(Jogador j) { return j.isPreso(); }
    public int getSaldo(Jogador j) { return j.getSaldo(); }

    public Color getColorFor(String nomeJogador) {
        return playerColors.getOrDefault(nomeJogador, Color.GRAY);
    }

    public int getLastD1() { return lastD1; }
    public int getLastD2() { return lastD2; }
    public Optional<Propriedade> getLastLandedProperty() { return Optional.ofNullable(lastLandedProperty); }

    public boolean hasOwner(Propriedade p) { return p.getDono() != null; }
    public Optional<Jogador> getOwner(Propriedade p) { return Optional.ofNullable(p.getDono()); }

    // ----------------------------- Commands -----------------------------
    public void rolarDados() {
        List<Integer> dados = jogo.lancarDados();
        lastD1 = dados.get(0);
        lastD2 = dados.get(1);
        fireDiceRolled();

        boolean ok = jogo.deslocarPiao(dados);
        lastLandedProperty = null;

        if (ok) {
            Casa casa = jogo.getTabuleiro().getCasa(jogo.getJogadorAtual().getPosicao());
            if (casa instanceof Propriedade) {
                lastLandedProperty = (Propriedade) casa;
                fireLandedProperty();
            } else {
                fireState();
            }
        } else {
            fireState();
        }
    }

    public void construirNaAtual() {
        try {
            jogo.construirNaPropriedadeAtual();
        } catch (Exception ignored) {}
        fireState();
    }

    public void venderForcado() {
        try {
            jogo.vendaForcada();
        } catch (Exception ignored) {}
        fireState();
    }

    public void encerrarTurno() {
        jogo.proximoJogador();
        lastLandedProperty = null;
        firePlayerChanged();
        fireState();
    }
}
