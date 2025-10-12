package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.List;

/**
 * Testes do evento de passar pela Partida (GO).
 * Regra: ao cruzar o início, o jogador recebe +200 de salário.
 */
public class MovimentoGoTest {
  private Jogo jogo;
  private Jogador j;

  @Before
  public void setUp() {
    // Cria um jogo simples com dois jogadores
    jogo = new Jogo(java.util.Arrays.asList("A", "B"));
    j = jogo.getJogadorAtual(); // A
  }

  @Test(timeout = 3000) // falha se o teste demorar mais de 3s
  public void passarPelaPartida_recebeSalario() {
    int saldoAntes = j.getSaldo();

    // Faz uma jogada longa (6,6) que deve cruzar o início (GO) pelo menos 1x
    jogo.deslocarPiao(List.of(6, 6)); // soma 12

    // Confirma que recebeu salário ao cruzar GO
    assertTrue("Não recebeu salário ao passar pela partida",
        j.getSaldo() > saldoAntes);
  }
}
