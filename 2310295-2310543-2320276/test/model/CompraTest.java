package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Arrays;
import java.util.List;

public class CompraTest {
  private Jogo jogo;
  private Jogador j;

  @Before
  public void setUp() {
    jogo = new Jogo(Arrays.asList("A", "B"));
    j = jogo.getJogadorAtual();

    // Ir até uma propriedade segura (índice 4) e comprar
    j.setPosicao(1);
    jogo.deslocarPiao(List.of(1, 2)); // 1→4
    assertTrue(jogo.comprarPropriedadeAtual()); // agora j tem um ativo para vender
  }

  @Test(timeout = 3000)
  public void vendaForcada_quandoSaldoNegativo_regularizaSaldo() {
    j.setSaldo(-50);     // força saldo negativo
    jogo.vendaForcada(); // deve vender propriedade(s) a 90% até >= 0
    assertTrue("Venda forçada não regularizou saldo", j.getSaldo() >= 0);
  }
}
