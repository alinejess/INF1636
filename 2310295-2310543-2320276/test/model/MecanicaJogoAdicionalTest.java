package model;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

/**
 * Grupo 1 — Testes adicionais de mecânica do jogo.
 * Requer JUnit 4.
 *
 * Testes:
 *  - new Jogo(Arrays.asList("A","B"))
 *  - jogo.deslocarPiao(List.of(d1, d2)) controla os dados para o teste   // <— atualizado para List
 *  - jogo.getJogadorAtual(), Jogador.setPosicao(), getPosicao(), isPreso()/setPreso()
 *  - jogo.getTabuleiro().getCasa(i) e Propriedade para comprar
 *  - jogo.comprarPropriedadeAtual()
 *  - Falência: j.falir(jogo.getBanco())
 *  - Salário ao passar pela Partida (GO): +200 por volta
 */
public class MecanicaJogoAdicionalTest {

  private Jogo jogo;
  private Jogador a, b;

  @Before
  public void setUp() {
    jogo = new Jogo(Arrays.asList("A", "B"));
    a = jogo.getJogadorAtual();
  }

  // 1) Três duplas seguidas → prisão
  @Test(timeout = 3000)
  public void tresDuplasSeguidas_enviaParaPrisao() {
    assertFalse("Não deveria iniciar preso", a.isPreso());

    jogo.deslocarPiao(List.of(1, 1));
    assertFalse("Após 1ª dupla não deveria estar preso", a.isPreso());

    jogo.deslocarPiao(List.of(2, 2));
    assertFalse("Após 2ª dupla não deveria estar preso", a.isPreso());

    jogo.deslocarPiao(List.of(3, 3));
    assertTrue("Após 3ª dupla deveria estar preso", a.isPreso());
  }

  // 2) Jogador preso não anda
  @Test(timeout = 3000)
  public void jogadorPreso_naoAnda() {
    int posInicial = a.getPosicao();
    a.setPreso(true);

    jogo.deslocarPiao(List.of(4, 2)); // qualquer rolagem
    assertTrue("Ainda deveria estar preso", a.isPreso());
    assertEquals("Preso não deveria se mover", posInicial, a.getPosicao());
  }

  // 3) Carta “Saída Livre da Prisão” é consumida ao usar
  // Valida pelo menos que o "sinalizador" do jogador é limpo após uso.
  @Test(timeout = 3000)
  public void cartaSaidaLivre_consumoDaCartaEAsaida() {
    a.setPreso(true);

    // quando expuser:
    a.setTemCartaSaidaPrisao(true);
    jogo.usarCartaSaidaPrisao();

    assertFalse("Após usar a carta, deveria sair da prisão", a.isPreso());
    assertFalse("A carta deve ser devolvida/consumida", a.isTemCartaSaidaPrisao());
  }

  // 4) Passar pelo GO várias vezes soma R$200 cada
  @Test(timeout = 3000)
  public void passarPeloGo_variasVezes_recebeSalarioTodasAsVezes() {
    int saldoAntes = a.getSaldo();

    // Passa pelo GO pelo menos duas vezes numa jogada longa
    jogo.deslocarPiao(List.of(6, 6)); // soma 12 (deve cruzar GO >= 1 vez)
    jogo.deslocarPiao(List.of(6, 6)); // cruza de novo

    // verificamos que recebeu pelo menos +400 de salário no total.
    assertTrue("Saldo não refletiu múltiplas passagens pela Partida (>= +400)",
        a.getSaldo() >= saldoAntes + 400);
  }

  // 5) Propriedade de jogador falido volta ao Banco
  @Test(timeout = 4000)
  public void propriedadeDeFalido_voltaAoBanco_semDono() {
	  
    // Levar A a uma propriedade “segura”
    a.setPosicao(1);
    jogo.deslocarPiao(List.of(1, 2));
    assertTrue("Não caiu em Propriedade",
        jogo.getTabuleiro().getCasa(a.getPosicao()) instanceof Propriedade);

    assertTrue("Deveria conseguir comprar", jogo.comprarPropriedadeAtual());
    int idxProp = a.getPosicao();

    // Falir o jogador A
    a.setSaldo(-100);
    a.falir(jogo.getBanco());
    assertTrue("Após falência, A deveria estar inativo", !a.isAtivo());

    Jogador atual = jogo.getJogadorAtual();
    atual.setPosicao(idxProp - 3);
    jogo.deslocarPiao(List.of(1, 2)); // cai na propriedade de índice idxProp

    assertTrue("Após falência do antigo dono, a propriedade deveria estar sem dono e vendável",
        jogo.comprarPropriedadeAtual());
  }
}
