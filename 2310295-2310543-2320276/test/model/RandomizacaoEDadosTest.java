package model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;

/**
 * Testes de randomização e dados.
 *
 * Atualizações:
 *  - A API do jogo agora retorna os dados como List<Integer> (não mais int[]).
 *  - Os testes foram ajustados para essa nova interface.
 */
public class RandomizacaoEDadosTest {

  private Jogo jogo;

  @Before
  public void setUp() {
    // Cria um jogo básico com dois jogadores
    jogo = new Jogo(Arrays.asList("A","B"));
  }

  @Test(timeout = 2000)
  public void faixaValoresDosDados_entre1e6() {
    // Lança várias vezes e valida o intervalo [1..6] para cada dado
    for (int i = 0; i < 200; i++) {
      List<Integer> r = jogo.lancarDados();
      assertEquals("Deveria retornar 2 valores", 2, r.size());
      assertTrue("d1 fora de [1..6]", r.get(0) >= 1 && r.get(0) <= 6);
      assertTrue("d2 fora de [1..6]", r.get(1) >= 1 && r.get(1) <= 6);
    }
  }

  @Test(timeout = 3000)
  public void distribuicaoRazoavel_naoDegenerada() {
    // Histograma simples de aparições por face (1..6)
	  Map<Integer,Integer> hist = new HashMap<>();
	  for (int v = 1; v <= 6; v++) hist.put(v, 0);

	  for (int i = 0; i < 2000; i++) {
	    List<Integer> r = jogo.lancarDados();
	    hist.put(r.get(0), hist.get(r.get(0)) + 1);
	    hist.put(r.get(1), hist.get(r.get(1)) + 1);
	  }
	  for (int v = 1; v <= 6; v++) {
	    assertTrue("face " + v + " nunca saiu", hist.get(v) > 50);
	  }
  }

  @Test
  public void setRandom_tornaResultadosRepetiveis() {
    // Testa reprodutibilidade: mesma semente => mesma sequência
    Random seed = new Random(42);

    // Primeira sequência com a mesma semente
    jogo.setRandom(seed);
    List<List<Integer>> seq1 = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      seq1.add(jogo.lancarDados());
    }

    // Reinicia o RNG com a mesma semente e compara
    jogo.setRandom(new Random(42));
    for (int i = 0; i < 100; i++) {
      assertEquals("Sequências com mesma semente devem coincidir no lançamento " + i,
          seq1.get(i), jogo.lancarDados());
    }
  }
}
