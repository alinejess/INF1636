package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Arrays;
import java.util.List;

public class ConstrucaoTest {
  private Jogo jogo;
  private Jogador dono;
  private int idxProp;

  @Before
  public void setUp() {
    jogo = new Jogo(Arrays.asList("A", "B"));
    dono = jogo.getJogadorAtual();

    // Ir direto para a propriedade índice 4 (segura: não é prisão e não passa pelo início)
    dono.setPosicao(1);
    jogo.deslocarPiao(List.of(1, 2)); // 1→4
    assertTrue(jogo.getTabuleiro().getCasa(dono.getPosicao()) instanceof Propriedade);

    // Compra na queda atual (nesta mesma queda NÃO pode construir)
    assertTrue("Deveria conseguir comprar", jogo.comprarPropriedadeAtual());
    idxProp = dono.getPosicao();

    // Força nova queda do dono na própria propriedade para habilitar construir
    dono.setPosicao(idxProp - 3);        // 1
    jogo.deslocarPiao(List.of(1, 2));    // 1→4 (cai na própria) → agora pode construir
  }

  @Test(timeout = 3000)
  public void construirEmPropriedadePropria_permitido() {
    boolean construiu = jogo.construirNaPropriedadeAtual();
    assertTrue("Deveria permitir construção na própria propriedade", construiu);
  }
}
