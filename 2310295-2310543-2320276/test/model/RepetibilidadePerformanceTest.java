package model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Testes de repetibilidade (RNG) e performance/sanidade do fluxo de turnos.
 *
 * Atualização importante:
 *  - A API do jogo agora usa List<Integer> em deslocarPiao, então este teste
 *    passou a usar List.of(d1, d2) em vez de new int[]{d1, d2}.
 */
public class RepetibilidadePerformanceTest {

  private Jogo jogo;

  @Before
  public void setUp() {
    // Simula uma partida com três jogadores para exercitar mais interações
    jogo = new Jogo(Arrays.asList("A","B","C"));
  }

  @Test(timeout = 10_000) // 10s para uma simulação longa
  public void simular1000Turnos_semExcecao() {
    Random r = new Random(123); // seed fixa para tornar a simulação reprodutível

    for (int i = 0; i < 1000; i++) {
      // Rola dois dados externos à lógica do jogo
      int d1 = 1 + r.nextInt(6);
      int d2 = 1 + r.nextInt(6);

      jogo.deslocarPiao(List.of(d1, d2));

      // Normaliza eventuais saldos negativos com venda forçada.
      for (Jogador j : jogo.getJogadores()) {
        if (j.getSaldo() < 0) {
          jogo.vendaForcada();
        }
      }

      // Avança a vez
      jogo.proximoJogador();
    }
  }

  @Test(timeout = 3000)
  public void nenhumSaldoNegativoIndevido_depoisDeVendasForcadas() {
    Random r = new Random(321); // outra semente para variar a trajetória

    for (int i = 0; i < 200; i++) {
      int d1 = 1 + r.nextInt(6);
      int d2 = 1 + r.nextInt(6);

      jogo.deslocarPiao(List.of(d1, d2));

      // Se algum jogador ficou negativo, chama a venda forçada
      for (Jogador j : jogo.getJogadores()) {
        if (j.getSaldo() < 0) {
          jogo.vendaForcada();
        }
      }

      // Próximo turno
      jogo.proximoJogador();
    }

    // No final, jogadores ainda ativos não devem permanecer negativos
    for (Jogador j : jogo.getJogadores()) {
      if (j.isAtivo()) {
        assertTrue("Ativo não deve ficar negativo após liquidações", j.getSaldo() >= 0);
      }
    }
  }
}
