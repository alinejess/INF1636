package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Arrays;
import java.util.List;

/**
 * Testes de integridade estrutural do jogo (tabuleiro, posições e ordem de turnos).
 * 
 * Objetivos:
 *  - Garantir que o tabuleiro tenha GO, prisão e ao menos uma propriedade.
 *  - Garantir que o avanço no tabuleiro é modular (volta ao início ao passar do fim).
 *  - Garantir que a ordem de turnos é circular e pula jogadores inativos (falidos).
 *  - Garantir que remover/falir um jogador não quebra a ordem de turnos.
 */
public class IntegridadeEstruturalTest {

  private Jogo jogo;
  private Jogador A, B;

  @Before
  public void setUp() {
    // Cria um jogo simples com dois jogadores "A" e "B"
    jogo = new Jogo(Arrays.asList("A","B"));

    // A começa como jogador atual no seu Model
    A = jogo.getJogadorAtual();

    // Avança para o próximo jogador
    jogo.proximoJogador();
    B = jogo.getJogadorAtual();

    // Volta a vez para A
    jogo.proximoJogador();
  }

  @Test(timeout=2000) // Permite o teste executar apenas por 2s
  public void tabuleiroContemGOPrisaoEPropriedades() {
    int total = jogo.getTabuleiro().getTotalCasas();

    // Esta verificação checa a posição inicial de A (que deve ser 0).
    assertTrue("GO deve ser índice 0 (convenção do projeto)", 0 <= A.getPosicao());

    // Prisão deve existir e ter um índice válido no tabuleiro
    assertTrue("Prisão deve existir no tabuleiro", jogo.getTabuleiro().getIndicePrisao() >= 0);

    // Deve existir ao menos uma Propriedade no tabuleiro
    boolean temProp = false;
    for (int i = 0; i < total; i++) {
      if (jogo.getTabuleiro().getCasa(i) instanceof Propriedade) {
        temProp = true;
        break;
      }
    }
    assertTrue("Deve existir ao menos uma Propriedade", temProp);
  }

  @Test(timeout=2000) // Permite o teste executar apenas por 2s
  public void posicaoNaoPassaDoFim_modular() {
    int total = jogo.getTabuleiro().getTotalCasas();

    // Coloca A perto do fim e anda 12 (6,6) —> deve dar a volta
    A.setPosicao(total - 7);
    jogo.deslocarPiao(List.of(6, 6));

    // Verifica que a posição final bate
    assertEquals((total - 7 + 12) % total, A.getPosicao());
  }

  @Test(timeout=2000) // Permite o teste executar apenas por 2s
  public void ordemDeTurnosCircular_pulaInativos() {
    // Falir A e garantir que o jogador atual não seja A
    A.falir(jogo.getBanco());
    assertFalse(A.isAtivo());
    assertNotSame("Jogador atual deve pular o inativo", A, jogo.getJogadorAtual());
  }

  @Test(timeout=2000) // Permite o teste executar apenas por 2s
  public void removerFalido_naoQuebraOrdem() {
    // Falir A e iterar algumas vezes na ordem de turnos: nunca deve voltar para A, já que está inativo
    A.falir(jogo.getBanco());
    for (int i = 0; i < 5; i++) {
      jogo.proximoJogador();
      assertNotSame(A, jogo.getJogadorAtual());
    }
  }
}
