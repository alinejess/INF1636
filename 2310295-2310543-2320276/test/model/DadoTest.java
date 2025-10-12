package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.List;

/**
 * Teste simples para a API de dados do jogo.
 * Garante que o método lancarDados() retorna DOIS valores entre 1 e 6.
 */

public class DadoTest {
  private Jogo jogo;

  @Before
  public void setUp() {
    // Cria um jogo mínimo com dois jogadores (A e B).
    jogo = new Jogo(java.util.Arrays.asList("A", "B"));
  }

  @Test(timeout = 2000) // falha se o teste demorar mais de 2s
  public void lancarDados_retornaValoresEntre1e6() {
    // Act: lança os dados pela API do jogo (retorna List<Integer> com 2 elementos)
    List<Integer> d = jogo.lancarDados();

    // Sanidade: deve retornar exatamente 2 valores
    assertEquals("Deveria retornar exatamente 2 valores", 2, d.size());

    // Assert: cada valor deve estar no intervalo [1..6]
    assertTrue("d1 fora de [1..6]", d.get(0) >= 1 && d.get(0) <= 6);
    assertTrue("d2 fora de [1..6]", d.get(1) >= 1 && d.get(1) <= 6);
  }
}