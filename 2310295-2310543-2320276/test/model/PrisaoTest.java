package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Arrays;
import java.util.List;

/**
 * Testes básicos das regras de prisão.
 *
 * Caso coberto aqui:
 *  - Jogador preso sai da prisão ao tirar "dupla" nos dados.
 */
public class PrisaoTest {
  private Jogo jogo;
  private Jogador j;

  @Before
  public void setUp() {
    jogo = new Jogo(Arrays.asList("A", "B"));
    j = jogo.getJogadorAtual();
  }

  @Test(timeout = 3000) // falha o teste se demorar mais de 3s
  public void sairDaPrisao_comDupla() {
    // Arrange: prende manualmente o jogador
    j.setPreso(true);
    assertTrue("Deveria estar preso inicialmente", j.isPreso());

    // Act: rola uma dupla (5,5) — pela regra, dupla solta da prisão
    jogo.deslocarPiao(List.of(5, 5));

    // Assert: após tirar dupla, deve sair da prisão
    assertFalse("Deveria sair da prisão ao tirar dupla", j.isPreso());
  }
}
