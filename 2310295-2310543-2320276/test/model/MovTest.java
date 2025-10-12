package model;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testes de MOVIMENTAÇÃO:
 * - deslocamento pela soma dos dados
 * - passagem pelo início (+$200)
 * - aritmética modular no tabuleiro circular
 * - efeitos de dupla (3 duplas seguidas -> prisão)
 * - interação com prisão (sair com dupla / permanecer preso sem dupla/carta)
 */
public class MovTest {

    private Jogo jogo;
    private Jogador atual;

    @Before
    public void setUp() {
        jogo = new Jogo(Arrays.asList("A", "B"));
        atual = jogo.getJogadorAtual();
    }

    @Test(timeout = 3000)
    public void mover_somaDosDadosAtualizaPosicao() {
        // Começa no índice 0 (Início). Andar (1,1) => 2 casas => índice 2.
        int posInicial = atual.getPosicao();
        assertEquals(0, posInicial);

        boolean ok = jogo.deslocarPiao(Arrays.asList(1, 1));
        assertTrue(ok);

        assertEquals("Deveria ter andado 2 casas", 2, atual.getPosicao());
    }

    @Test(timeout = 3000)
    public void mover_passarPeloInicio_recebe200() {
        // Põe o jogador no índice 4 e anda 3 casas (1,2) => 4 -> 1 (cruzou Início) => +200
        atual.setPosicao(4);
        int saldoAntes = atual.getSaldo();

        boolean ok = jogo.deslocarPiao(Arrays.asList(1, 2));
        assertTrue(ok);

        assertEquals("Posição final deveria ser 1 (4 + 3 % 6)", 1, atual.getPosicao());
        assertEquals("Ao cruzar o início, recebe +200", saldoAntes + 200, atual.getSaldo());
    }

    @Test(timeout = 3000)
    public void mover_modularNoFimDoTabuleiro() {
        // Tabuleiro tem 6 casas (0..5). De 5 andando 2 => (5+2)%6 = 1
        atual.setPosicao(5);
        boolean ok = jogo.deslocarPiao(Arrays.asList(1, 1));
        assertTrue(ok);
        assertEquals(1, atual.getPosicao());
    }

    @Test(timeout = 3000)
    public void prisao_tresDuplasSeguidas_enviaParaPrisao() {
        // 3 duplas seguidas prendem imediatamente
        int idxPrisao = jogo.getTabuleiro().getIndicePrisao();

        assertTrue(jogo.deslocarPiao(Arrays.asList(1, 1))); // 1ª dupla
        assertTrue(jogo.deslocarPiao(Arrays.asList(2, 2))); // 2ª dupla
        assertTrue(jogo.deslocarPiao(Arrays.asList(3, 3))); // 3ª dupla -> vai pra prisão

        assertTrue("Deveria estar preso após 3 duplas seguidas", atual.isPreso());
        assertEquals("Deveria ter sido movido para a prisão", idxPrisao, atual.getPosicao());
    }

    @Test(timeout = 3000)
    public void prisao_presoSemDuplaSemCarta_naoMove() {
        // Envia o jogador para a prisão
        jogo.enviarParaPrisao(atual);
        int idxPrisao = jogo.getTabuleiro().getIndicePrisao();
        assertEquals(idxPrisao, atual.getPosicao());
        assertTrue(atual.isPreso());

        // Lança dados sem dupla
        boolean ok = jogo.deslocarPiao(Arrays.asList(1, 2));
        assertTrue(ok);

        // Continua preso e não move
        assertTrue(atual.isPreso());
        assertEquals(idxPrisao, atual.getPosicao());
    }

    @Test(timeout = 3000)
    public void prisao_presoComDupla_saiEMoveNaMesmaJogada() {
        // Envia o jogador para a prisão
        jogo.enviarParaPrisao(atual);
        int idxPrisao = jogo.getTabuleiro().getIndicePrisao();
        assertEquals(idxPrisao, atual.getPosicao());
        assertTrue(atual.isPreso());

        // Tira dupla: deve liberar e mover a soma dos dados
        boolean ok = jogo.deslocarPiao(Arrays.asList(4, 4));
        assertTrue(ok);

        assertFalse("Deveria ter saído da prisão", atual.isPreso());
        assertNotEquals("Deveria ter se deslocado após sair", idxPrisao, atual.getPosicao());
    }
}
