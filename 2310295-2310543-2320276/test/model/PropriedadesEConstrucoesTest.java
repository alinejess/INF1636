package model;

import static org.junit.Assert.*;
import org.junit.*;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Grupo 2 — Testes de Propriedades e Construções (JUnit 4).
 * Cenários cobertos:
 *  - Compra bloqueada se saldo insuficiente.
 *  - Construção bloqueada se não for dono.
 *  - Construção bloqueada na mesma jogada da compra.
 *  - Hotel só permitido após >= 1 casa.
 *  - “5ª casa” (após ter hotel) -> retorno false.
 *  - Preço correto de construção: casa = 1/2 terreno; hotel = 1x terreno.
 *  - Aluguel aumenta conforme nº de casas/hotel.
 *  - Aluguel zerado se não houver casas.
 */
public class PropriedadesEConstrucoesTest {

  private Jogo jogo;
  private Jogador A, B;
  private int idxProp;

  @Before
  public void setUp() {
    jogo = new Jogo(Arrays.asList("A", "B"));
    A = jogo.getJogadorAtual();
    jogo.proximoJogador();
    B = jogo.getJogadorAtual();
    // volta para A como atual para o início dos testes
    jogo.proximoJogador();

    idxProp = firstPropertyIndex();
    assertTrue("Precisa haver ao menos 1 propriedade no tabuleiro", idxProp >= 0);
  }

  /* ========== TESTES ========== */

  @Test(timeout = 3000)
  public void compraBloqueada_seSaldoInsuficiente() {
    // A sem saldo, cai na propriedade e tenta comprar
    A.setSaldo(0);
    landCurrentOn(idxProp); // A cai na propriedade
    boolean ok = jogo.comprarPropriedadeAtual();
    assertFalse("Compra deveria ser bloqueada por saldo insuficiente", ok);
  }

  @Test(timeout = 3000)
  public void construcaoBloqueada_seNaoForDono() {
    // A compra a propriedade
    A.setSaldo(10_000);
    landCurrentOn(idxProp);
    assertTrue("Pré-condição: compra deveria ocorrer", jogo.comprarPropriedadeAtual());

    // B cai na mesma propriedade e tenta construir
    toPlayerB();
    landCurrentOn(idxProp);
    boolean ok = jogo.construirNaPropriedadeAtual();
    assertFalse("Não-dono não pode construir", ok);
  }

  @Test(timeout = 3000)
  public void construcaoBloqueada_naMesmaJogadaDaCompra() {
    // A compra a propriedade
    A.setSaldo(10_000);
    landCurrentOn(idxProp);
    assertTrue(jogo.comprarPropriedadeAtual());

    // mesma jogada: tentar construir deve falhar
    boolean ok = jogo.construirNaPropriedadeAtual();
    assertFalse("Construção na mesma jogada da compra deve ser bloqueada", ok);
  }

  @Test(timeout = 3000)
  public void hotelSomenteAposUmaCasa() throws Exception {
    Propriedade prop = propAt(idxProp);

    // A compra
    A.setSaldo(10_000);
    landCurrentOn(idxProp);
    assertTrue(jogo.comprarPropriedadeAtual());

    // 1ª queda do dono: construir -> deve virar 1 CASA (hotel ainda false)
    landCurrentOn(idxProp); // habilita construção
    assertTrue("Deveria construir com 0 casas (uma casa)", jogo.construirNaPropriedadeAtual());
    assertEquals(1, getCasas(prop));
    assertFalse("Hotel não deve existir após a 1ª construção", isHotel(prop));

    // 2ª queda do dono: agora construir deve virar HOTEL
    landCurrentOn(idxProp);
    assertTrue("Deveria ser possível construir hotel após >=1 casa", jogo.construirNaPropriedadeAtual());
    assertTrue("Hotel deveria existir após segunda construção", isHotel(prop));
  }

  @Test(timeout = 3000)
  public void construirQuintaCasa_retornaFalse() throws Exception {
    Propriedade prop = propAt(idxProp);

    // A compra e constrói 1 casa, depois hotel; tentar “mais uma” construção deve falhar
    A.setSaldo(10_000);
    landCurrentOn(idxProp);
    assertTrue(jogo.comprarPropriedadeAtual());

    landCurrentOn(idxProp); // 1ª construção -> casa
    assertTrue(jogo.construirNaPropriedadeAtual());
    assertEquals(1, getCasas(prop));
    assertFalse(isHotel(prop));

    landCurrentOn(idxProp); // 2ª construção -> hotel
    assertTrue(jogo.construirNaPropriedadeAtual());
    assertTrue(isHotel(prop));

    // 3ª tentativa: já com hotel, não deve mais permitir (equivalente a "5ª casa" proibida)
    landCurrentOn(idxProp);
    assertFalse("Após ter hotel, não deve permitir mais construções", jogo.construirNaPropriedadeAtual());
  }

  @Test(timeout = 3000)
  public void precoConstrucao_casaMeioTerreno_hotelPrecoCheio() {
    Propriedade prop = propAt(idxProp);
    int precoTerreno = prop.getPreco();

    // A compra
    A.setSaldo(10_000);
    landCurrentOn(idxProp);
    assertTrue(jogo.comprarPropriedadeAtual());

    // 1ª construção (casa): custo = 1/2 do terreno
    landCurrentOn(idxProp);
    int saldoAntes = A.getSaldo();
    assertTrue(jogo.construirNaPropriedadeAtual());
    int custoCasa = saldoAntes - A.getSaldo();
    assertEquals("Custo da casa deve ser 1/2 do terreno", precoTerreno / 2, custoCasa);

    // 2ª construção (hotel): custo = 1x do terreno
    landCurrentOn(idxProp);
    saldoAntes = A.getSaldo();
    assertTrue(jogo.construirNaPropriedadeAtual());
    int custoHotel = saldoAntes - A.getSaldo();
    assertEquals("Custo do hotel deve ser 1x o preço do terreno", precoTerreno, custoHotel);
  }

  @Test(timeout = 4000)
  public void aluguelAumentaComCasasEHotel_eZeradoSemCasas() {
    Propriedade prop = propAt(idxProp);

    // A compra; B cai sem casas -> aluguel 0
    A.setSaldo(10_000);
    landCurrentOn(idxProp);
    assertTrue(jogo.comprarPropriedadeAtual());

    toPlayerB();
    int saldoB0 = B.getSaldo();
    landCurrentOn(idxProp); // B cai na prop sem casas
    assertEquals("Sem casas, aluguel deve ser 0", saldoB0, B.getSaldo());

    // 1 casa -> aluguel1 > 0
    toPlayerA();
    landCurrentOn(idxProp);
    assertTrue(jogo.construirNaPropriedadeAtual());

    toPlayerB();
    int saldoB1 = B.getSaldo();
    landCurrentOn(idxProp); // B cai na prop com 1 casa
    int aluguel1 = saldoB1 - B.getSaldo();
    assertTrue("Com 1 casa, aluguel deve ser > 0", aluguel1 > 0);

    // hotel -> aluguelH > aluguel1
    toPlayerA();
    landCurrentOn(idxProp);
    assertTrue(jogo.construirNaPropriedadeAtual()); // vira hotel

    toPlayerB();
    int saldoB2 = B.getSaldo();
    landCurrentOn(idxProp);
    int aluguelH = saldoB2 - B.getSaldo();
    assertTrue("Com hotel, aluguel deve ser maior que com 1 casa", aluguelH > aluguel1);
  }

  private int firstPropertyIndex() {
    int n = jogo.getTabuleiro().getTotalCasas();
    for (int i = 0; i < n; i++) {
      if (jogo.getTabuleiro().getCasa(i) instanceof Propriedade) return i;
    }
    return -1;
  }

  private Propriedade propAt(int idx) {
    return (Propriedade) jogo.getTabuleiro().getCasa(idx);
  }

  private void landCurrentOn(int idx) {
    Jogador j = jogo.getJogadorAtual();
    j.setPosicao(idx);
    jogo.getTabuleiro().getCasa(idx).acao(j, jogo);
  }

  private void toPlayerA() {
    // avança até o jogador A ser o atual
    while (jogo.getJogadorAtual() != A) jogo.proximoJogador();
  }

  private void toPlayerB() {
    while (jogo.getJogadorAtual() != B) jogo.proximoJogador();
  }

  private static int getCasas(Propriedade p) throws Exception {
    Field f = Propriedade.class.getDeclaredField("casas");
    f.setAccessible(true);
    return f.getInt(p);
    // (apenas para inspeção no teste; Model pode manter os campos package-private)
  }

  private static boolean isHotel(Propriedade p) throws Exception {
    Field f = Propriedade.class.getDeclaredField("hotel");
    f.setAccessible(true);
    return f.getBoolean(p);
  }
}
