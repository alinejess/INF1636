package model;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

/**
 * Testes de robustez e tratamento de exceções/entradas inválidas.
 *
 * Atualização: a API do jogo agora usa List<Integer> em deslocarPiao.
 */
public class RobustezExcecoesTest {

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
  public void deslocarPiao_null_geraExcecaoOuFalse() {
    // Para a API atual, deslocarPiao(null) deve retornar false (ou lançar exceção).
    try {
      boolean ok = jogo.deslocarPiao(null); // List nula
      assertFalse("Se não lançar exceção, deve retornar false", ok);
    } catch (NullPointerException | IllegalArgumentException expected) {
      // Também aceitável: lançar exceção para entrada inválida
    }
  }

  @Test(timeout = 2000)
  public void construirForaDePropriedade_retornaFalse() {
    // Índice 0 (GO) não é propriedade; construir deve retornar false.
    A.setPosicao(0);
    boolean ok = jogo.construirNaPropriedadeAtual();
    assertFalse("Construir fora de Propriedade deve retornar false", ok);
  }

  @Test(timeout = 2000)
  public void falidoTentarJogar_ehIgnorado() {
    int posA = A.getPosicao();

    // Falir A e garantir que está inativo
    A.falir(jogo.getBanco());
    assertFalse(A.isAtivo());

    // Ao tentar deslocar, o jogo deve operar com o próximo ativo (B), não com A
    jogo.deslocarPiao(List.of(2, 3));

    // A posição do falido não deve mudar, e o jogador atual não pode ser A
    assertEquals("Posição do falido não deve mudar", posA, A.getPosicao());
    assertNotSame("Jogador atual não pode ser o falido", A, jogo.getJogadorAtual());
  }
}
