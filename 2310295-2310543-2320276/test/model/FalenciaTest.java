package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Arrays;

/**
 * Testes relacionados à falência do jogador.
 * Verifica se, ao ficar com saldo negativo e acionar a falência,
 * o jogador passa a ser marcado como inativo (falido).
 */
public class FalenciaTest {
  private Jogo jogo;
  private Jogador j;     // referência ao jogador atual (começa como "A")

  @Before
  public void setUp() {
    // Cria um jogo com dois jogadores ("A" e "B") e obtém o jogador atual.
    jogo = new Jogo(Arrays.asList("A", "B"));
    j = jogo.getJogadorAtual();
  }

  @Test(timeout = 3000) // falha o teste se levar mais de 3s
  public void jogadorComSaldoNegativo_falido() {
    // Arrange: força um estado de saldo negativo
    j.setSaldo(-100);

    // Act: aciona a rotina de falência passando o banco
    j.falir(jogo.getBanco());

    // Assert: um jogador falido deve ficar "inativo" no sistema
    assertTrue("Jogador com saldo negativo deveria estar falido", !j.isAtivo());
  }
}
