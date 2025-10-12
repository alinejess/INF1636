package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Arrays;
import java.util.List;
/**
 * Testes detalhados sobre as regras de prisão.
 * 
 * Coberturas:
 *  - Prisão após 3 duplas seguidas.
 *  - Uso da carta "Saída Livre" para sair antes das 3 tentativas.
 */
public class PrisaoDetalhadoTest {

  private Jogo jogo;
  private Jogador A;

  @Before
  public void setUp() {
    jogo = new Jogo(Arrays.asList("A","B"));
    A = jogo.getJogadorAtual(); // A
  }

  @Test(timeout = 2000) // falha se o teste demorar mais de 2s
  public void tresDuplasSeguidas_enviamParaPrisao() {
    assertFalse(A.isPreso()); // pré-condição: começa livre

    jogo.deslocarPiao(List.of(1, 1));
    assertFalse(A.isPreso());

    jogo.deslocarPiao(List.of(2, 2));
    assertFalse(A.isPreso());

    jogo.deslocarPiao(List.of(3, 3));
    assertTrue(A.isPreso());
  }

  @Test(timeout = 2000) // falha se o teste demorar mais de 2s
  public void usarCartaSaidaLivre_liberaAntesDas3Tentativas() {
    // Arrange: jogador está preso e possui a carta de saída
    A.setPreso(true);
    A.setTemCartaSaidaPrisao(true);

    // Act: rolagem qualquer NÃO-DUPLA; o jogo deve consumir a carta e liberar o jogador
    jogo.deslocarPiao(List.of(2, 3));

    // Assert: saiu da prisão e a carta foi consumida
    assertFalse("Deveria sair usando a carta", A.isPreso());
    assertFalse("Carta deve ser consumida", A.isTemCartaSaidaPrisao());
  }
}
