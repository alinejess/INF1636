package model;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Arrays;
import java.util.List;

public class AluguelTest {
  private Jogo jogo;
  private Jogador dono, visitante;
  private Propriedade p;
  private int idxProp; // índice da propriedade escolhida no tabuleiro

  @Before
  public void setUp() {
    // Inicia jogo
    jogo = new Jogo(Arrays.asList("A", "B"));
    dono = jogo.getJogadorAtual();

    // Ir direto para uma propriedade segura (índice 4: "Av. Brasil"), sem prisão e sem passar pelo início:
    // coloca o dono no índice 1 e anda (1,2) ⇒ 1→4 (não é dupla, não soma 12, não passa pelo início)
    dono.setPosicao(1);
    jogo.deslocarPiao(List.of(1, 2)); // cai no índice 4 (Propriedade)
    assertTrue("Pré-condição: deveria estar em uma Propriedade",
        jogo.getTabuleiro().getCasa(dono.getPosicao()) instanceof Propriedade);

    // Compra a propriedade
    boolean comprou = jogo.comprarPropriedadeAtual();
    assertTrue("Deveria conseguir comprar a propriedade", comprou);

    // Guarda a referência e o índice desta propriedade
    idxProp = dono.getPosicao();            // deve ser 4
    p = (Propriedade) jogo.getTabuleiro().getCasa(idxProp);

    // Na mesma queda da compra NÃO pode construir; força nova queda do dono na própria casa:
    // posiciona 3 casas antes e anda (1,2) -> cai na própria propriedade
    dono.setPosicao(idxProp - 3);
    jogo.deslocarPiao(List.of(1, 2));

    // Constrói 1 casa (agora pode, pois caiu na própria nesta jogada)
    boolean construiu = jogo.construirNaPropriedadeAtual();
    assertTrue("Deveria permitir construir 1 casa na nova queda do dono", construiu);

    // Passa a vez para o visitante
    jogo.proximoJogador();
    visitante = jogo.getJogadorAtual();
  }

  @Test(timeout = 3000)
  public void cairNaPropriedadeAlheia_debitaVisitanteECreditaDono() {
    int saldoDonoAntes = dono.getSaldo();
    int saldoVisitanteAntes = visitante.getSaldo();

    // Posiciona o visitante 3 casas antes e anda (1,2) ⇒ cai exatamente na propriedade do dono
    visitante.setPosicao(idxProp - 3);      // 1
    jogo.deslocarPiao(List.of(1, 2));       // 1→4 (propriedade do dono com 1 casa)

    // Verificações: dono recebe, visitante paga
    assertTrue("Dono não recebeu aluguel", dono.getSaldo() > saldoDonoAntes);
    assertTrue("Visitante não pagou aluguel", visitante.getSaldo() < saldoVisitanteAntes);
  }
}
