package model;

import static org.junit.Assert.*;
import org.junit.*;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Testes sobre Banco e processos de liquidação (venda forçada).
 *
 * O foco aqui é:
 *  - saldo inicial do Banco e dos jogadores;
 *  - cálculo de venda forçada (90% do valor total da propriedade);
 *  - recebimento de taxas/impostos pelo Banco;
 *  - marcação de jogador como inativo quando não cobre a dívida.
 */
public class BancoELiquidacaoTest {

  private Jogo jogo;
  private Jogador A, B;

  @Before
  public void setUp() {
    // Cria um jogo com dois jogadores e referencia A e B
    jogo = new Jogo(Arrays.asList("A","B"));
    A = jogo.getJogadorAtual();
    jogo.proximoJogador();
    B = jogo.getJogadorAtual();
    jogo.proximoJogador(); // volta a vez para A
  }

  @Test(timeout = 2000)
  public void saldoInicialBanco_200k() throws Exception {
    // O Banco deve iniciar com 200.000 (de acordo com a convenção do projeto)
    assertEquals(200_000, getBancoSaldo(jogo.getBanco()));
  }

  @Test(timeout = 2000)
  public void saldoInicialJogadores_4000() {
    // Todos os jogadores começam com 4000 de saldo
    for (Jogador j : jogo.getJogadores()) {
      assertEquals(4000, j.getSaldo());
    }
  }

  @Test(timeout = 4000)
  public void vendaForcada_calcula90pctDoValorTotal() {
    // 1) Descobre a primeira propriedade do tabuleiro
    int idx = firstPropertyIndex();
    Propriedade p = (Propriedade) jogo.getTabuleiro().getCasa(idx);

    // 2) Garante saldo suficiente para comprar e construir 1 casa
    A.setSaldo(10_000);

    // 3) A "cai" na propriedade, compra, "cai" de novo e constrói 1 casa
    landOn(idx);
    assertTrue("Deveria conseguir comprar", jogo.comprarPropriedadeAtual());
    landOn(idx);
    assertTrue("Deveria conseguir construir 1 casa", jogo.construirNaPropriedadeAtual());

    // 4) Valor total do ativo (terreno + casas + hotel, quando houver)
    int valorTotal = p.getValorTotal();
    int esperado = (int) Math.round(valorTotal * 0.90); // regra: vende a 90%

    // 5) Força saldo negativo e dispara venda forçada
    A.setSaldo(-10); // dívida pequena para vender exatamente essa propriedade
    jogo.vendaForcada();

    // 6) Verificações: saldo ajusta +90%, e a propriedade volta ao Banco (sem dono)
    assertEquals("Após venda forçada, saldo deve somar 90% do valor total",
        -10 + esperado, A.getSaldo());
    assertNull("Propriedade volta a ficar sem dono", p.getDono());
  }

  @Test(timeout = 3000)
  public void bancoRecebeQuandoPagaTaxaOuImposto() throws Exception {
    int antes = getBancoSaldo(jogo.getBanco());

    // Simula uma taxa/imposto: pagar ao Banco (credor null indica Banco)
    A.pagar(150, null, jogo.getBanco());

    // Banco deve receber o valor pago
    assertEquals("Banco deve receber a taxa", antes + 150, getBancoSaldo(jogo.getBanco()));
  }

  @Test(timeout = 2000)
  public void liquidacaoTotal_semCobrirDivida_marcaInativo() {
    // Sem propriedades e com grande dívida, venda forçada não resolve → jogador inativo
    A.setSaldo(-5_000);
    jogo.vendaForcada(); // não há o que vender; deve marcar inativo
    assertFalse("Jogador deve ser marcado como inativo se não cobre a dívida", A.isAtivo());
  }

  private int firstPropertyIndex() { //Retorna o índice da primeira casa do tabuleiro que é uma Propriedade.
    int n = jogo.getTabuleiro().getTotalCasas();
    for (int i = 0; i < n; i++) {
      if (jogo.getTabuleiro().getCasa(i) instanceof Propriedade) return i;
    }
    throw new AssertionError("Tabuleiro não tem Propriedade");
  }

  private void landOn(int idx) { //Simula "cair" diretamente em uma casa do tabuleiro, acionando sua ação.
    Jogador j = jogo.getJogadorAtual();
    j.setPosicao(idx);
    jogo.getTabuleiro().getCasa(idx).acao(j, jogo);
  }

  private static int getBancoSaldo(Banco b) throws Exception { //Lê o saldo interno do Banco.
    Field f = Banco.class.getDeclaredField("saldo");
    f.setAccessible(true);
    return f.getInt(b);
  }
}
