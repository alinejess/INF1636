package model;

import org.junit.Test;
import static org.junit.Assert.*;

public class Testes {

    // 1) Lançamento virtual dos dados
	@Test
	public void deveLancarDoisDadosEntre1e6() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.deslocarPiao(2, 5);
	}

    // 2) Deslocar peão conforme os dados
    @Test
    public void deveDeslocarPeloValorDosDadosEFazerWrap() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A");

        boolean moveu = m.deslocarPiao(1, 2);
        assertTrue(moveu);
        assertEquals(3, m.obterJogadorDaVez().posicao);

        m.depurarMoverPara(38);
        m.depurarLiberarLancamento();
        m.deslocarPiao(1, 2);
        assertEquals(1, m.obterJogadorDaVez().posicao);
    }

    // 6) Prisão: entrada e saída
    @Test
    public void deveEntrarNaPrisaoESairComDuplaOuCarta() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A");

        // Ir para "Vá para a Prisão" (índice 30)
        m.depurarMoverPara(27);
        m.deslocarPiao(2, 1);
        assertTrue(m.obterJogadorDaVez().naPrisao);

        // Sem dupla: permanece preso e não move
        m.depurarLiberarLancamento();
        boolean moveu = m.deslocarPiao(1, 2);
        assertFalse(moveu);
        assertTrue(m.obterJogadorDaVez().naPrisao);

        // Com dupla: sai e move
        m.depurarLiberarLancamento();
        boolean moveu2 = m.deslocarPiao(1, 1);
        assertTrue(moveu2);
        assertFalse(m.obterJogadorDaVez().naPrisao);

        // Testar carta de saída
        m.concederCartaSaidaPrisao();
        // voltar a prender para testar carta
        m.depurarMoverPara(26);
        m.depurarLiberarLancamento();
        m.deslocarPiao(3, 1);
        assertTrue(m.obterJogadorDaVez().naPrisao);
        assertTrue(m.usarCartaSaidaPrisao());
        assertFalse(m.obterJogadorDaVez().naPrisao);
    }

    @Test
    public void apenasDuplasPermitemNovoLancamento() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A");

        assertTrue(m.deslocarPiao(1, 2));
        assertFalse("Sem dupla, não deve permitir novo lançamento no mesmo turno", m.deslocarPiao(1, 2));

        m.depurarLiberarLancamento();
        assertTrue(m.deslocarPiao(3, 3));
        assertTrue("Dupla libera novo lançamento", m.deslocarPiao(1, 4));
        assertFalse("Após resultado diferente, bloqueia novamente", m.deslocarPiao(2, 3));
    }

    @Test
    public void terceiraDuplaEnvioAutomatizadoParaPrisao() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A");

        assertTrue(m.deslocarPiao(1, 1));
        assertTrue("Primeira dupla deve liberar novo lançamento", m.podeLancarDados());
        assertTrue(m.deslocarPiao(2, 2));
        assertTrue(m.deslocarPiao(3, 3));
        assertTrue("Jogador deve estar na prisão após três duplas seguidas", m.obterJogadorDaVez().naPrisao);
        assertFalse("Após terceira dupla não deve ser permitido novo lançamento", m.podeLancarDados());
    }

    // 3) Comprar propriedade sem dono
    @Test
    public void deveComprarPropriedadeSemDono() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A");

        // Propriedade índice 3 (Av. Presidente Vargas)
        m.deslocarPiao(1, 2);
        GameModelo.VisaoCasa casaAntes = m.obterCasaAtual();
        assertEquals("PROPRIEDADE", casaAntes.tipo);

        int saldoJogAntes = m.obterJogadorDaVez().saldo;
        int saldoBancoAntes = m.getSaldoBanco();

        assertTrue(m.comprarPropriedade());

        GameModelo.VisaoCasa casaDepois = m.obterCasaAtual();
        assertNotNull(casaDepois.proprietario);
        assertTrue(m.obterJogadorDaVez().saldo < saldoJogAntes);
        assertTrue(m.getSaldoBanco() > saldoBancoAntes);
    }

    // 4) Construir casa na própria propriedade
    @Test
    public void deveConstruirCasasEAposQuatroViraHotel() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A");

        // Compra a casa 3
        m.deslocarPiao(1, 2);
        assertTrue(m.comprarPropriedade());

        assertFalse(m.construirCasa());
        m.encerrarTurno();
        moverAtualParaCasa(m, 3);

        int saldoAntes = m.obterJogadorDaVez().saldo;
        int bancoAntes = m.getSaldoBanco();

        // Constrói 1 casa
        assertTrue(m.construirCasa());
        assertEquals(Integer.valueOf(1), m.obterCasaAtual().casas);
        assertTrue(m.obterJogadorDaVez().saldo < saldoAntes);
        assertTrue(m.getSaldoBanco() > bancoAntes);

        // Constrói até 4 casas
        m.construirCasa(); m.construirCasa(); m.construirCasa();
        assertEquals(Integer.valueOf(4), m.obterCasaAtual().casas);

        // 5ª construção vira hotel
        assertTrue(m.construirCasa());
        assertTrue(m.obterCasaAtual().hotel.booleanValue());
    }

    // 5) Aluguel automático (≥1 casa) ao cair na casa de outro
    @Test
    public void deveCobrarAluguelAutomaticamenteComPeloMenosUmaCasa() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A"); // dono
        m.adicionarJogador("B"); // pagador

        // A compra e constrói 1 casa na 3
        m.deslocarPiao(1, 2);
        assertTrue(m.comprarPropriedade());
        assertFalse(m.construirCasa());
        m.encerrarTurno(); // -> B
        m.encerrarTurno(); // -> A novamente
        moverAtualParaCasa(m, 3);
        assertTrue(m.construirCasa());
        int saldoA_antes = m.obterJogadorDaVez().saldo;

        // Turno do B
        m.encerrarTurno();
        int saldoB_antes = m.obterJogadorDaVez().saldo;

        // B cai na 3
        m.deslocarPiao(1, 2);

        int saldoB_depois = m.obterJogadorDaVez().saldo; // ainda B
        m.encerrarTurno(); // volta para A
        int saldoA_depois = m.obterJogadorDaVez().saldo;

        assertTrue(saldoB_depois < saldoB_antes);
        assertTrue(saldoA_depois > saldoA_antes);
    }

    // 7) Aluguel -> falência -> sair do jogo (perde propriedades)
    @Test
    public void deveFalirAoFicarNegativoEPerderPropriedades() {
        GameModelo m = new GameModelo();
        m.adicionarJogador("A"); // dono da 6
        m.adicionarJogador("B"); // vai falir

        // --- A prepara a casa 6 com hotel (aluguel alto) ---
        m.depurarMoverPara(6);
        assertTrue("A deve conseguir comprar a 6", m.comprarPropriedade());
        m.depurarDefinirSaldoJogadorDaVez(100_000);
        assertFalse(m.construirCasa());
        m.encerrarTurno(); // -> B
        m.encerrarTurno(); // -> A
        moverAtualParaCasa(m, 6);
        m.construirCasa(); m.construirCasa(); m.construirCasa(); m.construirCasa();
        m.construirCasa(); // hotel

        // Confere que a 6 é PROPRIEDADE de A e tem hotel
        GameModelo.VisaoCasa v6 = m.obterCasaAtual();
        assertEquals("PROPRIEDADE", v6.tipo);
        assertEquals("A", v6.proprietario);
        assertTrue("Deveria haver hotel na 6", v6.hotel.booleanValue());

        // --- B compra a casa 3 e constrói 1 casa ---
        m.encerrarTurno();                // -> B
        m.depurarMoverPara(3);
        assertTrue("B deve conseguir comprar a 3", m.comprarPropriedade());
        m.depurarDefinirSaldoJogadorDaVez(100_000);
        assertFalse(m.construirCasa());
        m.encerrarTurno(); // -> A
        m.encerrarTurno(); // -> B
        moverAtualParaCasa(m, 3);
        assertTrue("B deve conseguir construir 1 casa na 3", m.construirCasa());

        // --- B fica com pouco dinheiro e cai na 6 de A -> falência ---
        m.depurarDefinirSaldoJogadorDaVez(100);
        m.depurarMoverPara(0);
        m.deslocarPiao(3, 3);             // 0 -> 6
        assertFalse("B deve ter falido ao pagar aluguel da 6", m.obterJogadorDaVez().ativo);

        // --- Verifica que a casa 3 (que era de B) voltou ao banco, sem casas/hotel ---
        m.encerrarTurno();                // volta para A (apenas para inspecionar)
        m.depurarMoverPara(3);
        GameModelo.VisaoCasa v = m.obterCasaAtual();
        assertEquals("PROPRIEDADE", v.tipo);
        assertNull("Propriedade do falido deveria voltar ao banco", v.proprietario);
        assertEquals("Número de casas deveria ser 0 após falência", Integer.valueOf(0), v.casas);
        assertFalse("Hotel não deve existir após falência", v.hotel.booleanValue());
    }


    // Extra: salário por rodada ao virar a rodada
	@Test
	public void devePagarSalarioParaTodosQuandoViraRodada() {
	    GameModelo m = new GameModelo();
	    m.configurarSalarioPorRodada(200);
	    m.adicionarJogador("A");
		m.adicionarJogador("B");
		m.adicionarJogador("C");
		
		int a0 = m.obterJogadorDaVez().saldo; // A
		m.encerrarTurno();                    // -> B
		int b0 = m.obterJogadorDaVez().saldo; // B
		m.encerrarTurno();                    // -> C
		int c0 = m.obterJogadorDaVez().saldo; // C
		
		// Virou a rodada ao voltar para A: todos recebem +200
		m.encerrarTurno();                    // -> A
		assertEquals(a0 + 200, m.obterJogadorDaVez().saldo);
		
		m.encerrarTurno();                    // -> B
		assertEquals(b0 + 200, m.obterJogadorDaVez().saldo);
		
		m.encerrarTurno();                    // -> C
	    assertEquals(c0 + 200, m.obterJogadorDaVez().saldo);
	}

	
	@Test
	public void naoCompraSeJaTemDono() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	m.adicionarJogador("B");
	
	// A compra a 3
	m.deslocarPiao(1,2);
	org.junit.Assert.assertTrue(m.comprarPropriedade());
	
	 // B tenta comprar a mesma
	    m.encerrarTurno();
	    m.deslocarPiao(1,2);
	    org.junit.Assert.assertFalse(m.comprarPropriedade());
	}
	
	@Test
	public void naoCompraSemSaldoSuficiente() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.depurarDefinirSaldoJogadorDaVez(10);
	    m.deslocarPiao(1,2);
	    org.junit.Assert.assertFalse(m.comprarPropriedade());
	}
	
	@Test
	public void naoConstroiSeNaoForDonoOuNaoEstiverNaCasa() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.adicionarJogador("B");
	
	    // A compra a 3
	    m.deslocarPiao(1,2);
	    org.junit.Assert.assertTrue(m.comprarPropriedade());
	
	    // B tenta construir na 3 (não é dono)
	    m.encerrarTurno();
	    m.deslocarPiao(1,2);
	    org.junit.Assert.assertFalse(m.construirCasa());
	
	    // Volta pra A mas sai da casa: não pode construir fora dela
	    m.encerrarTurno();
	    m.depurarMoverPara(0); // não é propriedade nem a mesma casa
	    org.junit.Assert.assertFalse(m.construirCasa());
	}
	
	@Test
	public void naoConstroiDepoisDeHotel() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.deslocarPiao(1,2);
	    org.junit.Assert.assertTrue(m.comprarPropriedade());
	    org.junit.Assert.assertFalse(m.construirCasa());
	    m.encerrarTurno();
	    moverAtualParaCasa(m, 3);
	    m.depurarDefinirSaldoJogadorDaVez(100000);
	    m.construirCasa(); m.construirCasa(); m.construirCasa(); m.construirCasa();
	    org.junit.Assert.assertTrue(m.construirCasa());  // vira hotel
	    org.junit.Assert.assertFalse(m.construirCasa()); // não passa de hotel
	}
	
	@Test
	public void cobraAluguelMesmoSemCasas() {
		GameModelo m = new GameModelo();
		m.configurarSalarioPorRodada(0); // evita que a virada altere saldos
		m.adicionarJogador("A");
		m.adicionarJogador("B");

		// A compra sem construir
		m.deslocarPiao(1,2);
		assertTrue(m.comprarPropriedade());
		int saldoA0 = m.obterJogadorDaVez().saldo;
		
		// B cai lá e deve pagar aluguel mesmo sem casas
		m.encerrarTurno();
		int saldoB0 = m.obterJogadorDaVez().saldo;
		m.deslocarPiao(1,2);
		int saldoB1 = m.obterJogadorDaVez().saldo;
		
		assertTrue("Pagador deveria perder dinheiro", saldoB1 < saldoB0);
		m.encerrarTurno(); // volta para A
        int saldoA1 = m.obterJogadorDaVez().saldo;
        assertTrue("Proprietário deveria receber dinheiro", saldoA1 > saldoA0);
	}

	@Test
	public void naoPermiteVenderNoMesmoTurnoDaCompra() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.adicionarJogador("B");

	    m.deslocarPiao(1,2);
	    assertTrue(m.comprarPropriedade());
	    String nome = m.obterPropriedadesJogadorDaVez().get(0);
	    assertEquals(0, m.venderPropriedadeDaVez(nome));

	    m.encerrarTurno(); // -> B
	    m.encerrarTurno(); // -> A novamente
	    int valor = m.venderPropriedadeDaVez(m.obterPropriedadesJogadorDaVez().get(0));
	    assertTrue(valor > 0);
	}

	@Test
	public void ganhaBonusAoPassarInicio() {
	    GameModelo m = new GameModelo();
	    m.configurarSalarioPorRodada(0);
	    m.adicionarJogador("A");
	    int saldo0 = m.obterJogadorDaVez().saldo;
	    m.depurarMoverPara(38);
	    m.deslocarPiao(1, 2);
	    int saldo1 = m.obterJogadorDaVez().saldo;
	    assertEquals(saldo0 + 200, saldo1);
	}
	
	@Test
	public void vaParaPrisaoLevaAoIndiceDaPrisao() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.depurarMoverPara(27);
	    m.deslocarPiao(1,2); // cai em "Vá para a Prisão" (30)
	    org.junit.Assert.assertTrue(m.obterJogadorDaVez().naPrisao);
	    // depois de aplicar, a posição precisa ser a da prisão (tabuleiro padrão: 11)
	    org.junit.Assert.assertEquals(10, m.obterJogadorDaVez().posicao);
	}
	
	@Test
	public void encerrarTurnoPulaJogadorInativo() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.adicionarJogador("B");
	    m.adicionarJogador("C");
	
	    // A monta a armadilha na 7 (hotel)
	    m.depurarMoverPara(6);
	    m.comprarPropriedade();
        org.junit.Assert.assertFalse(m.construirCasa());
        m.encerrarTurno(); // -> B
        m.encerrarTurno(); // -> C
        m.encerrarTurno(); // -> A novamente
        moverAtualParaCasa(m, 6);
	    m.depurarDefinirSaldoJogadorDaVez(100000);
	    m.construirCasa(); m.construirCasa(); m.construirCasa(); m.construirCasa();
	    m.construirCasa();
	
	    // B vai falir
	    m.encerrarTurno();              // B
	    m.depurarDefinirSaldoJogadorDaVez(10);
	    m.depurarMoverPara(0);
	    m.deslocarPiao(2,4);       
	    org.junit.Assert.assertFalse(m.obterJogadorDaVez().ativo);
	    // encerrarTurno deve ir direto para C (pulando B inativo)
	    m.encerrarTurno();
	    
	    org.junit.Assert.assertEquals("C", m.obterJogadorDaVez().nome);
	}

	@Test
	public void snapshotDeveRestaurarEstadoBasico() {
	    GameModelo original = new GameModelo();
	    original.adicionarJogador("Jogador Vermelho");
	    original.adicionarJogador("Jogador Azul");
	    original.sortearOrdemJogadores();

	    original.deslocarPiao(1, 2); // cai na 3 e pode comprar
	    org.junit.Assert.assertTrue(original.comprarPropriedade());
	    org.junit.Assert.assertFalse(original.construirCasa());
	    original.encerrarTurno(); // -> jogador 2
	    original.encerrarTurno(); // -> jogador inicial novamente
	    moverAtualParaCasa(original, 3);
	    org.junit.Assert.assertTrue(original.construirCasa());
	    original.encerrarTurno(); // garante início de turno para salvar

	    GameModelo.Snapshot snapshot = original.criarSnapshot();
	    GameModelo restaurado = new GameModelo();
	    restaurado.restaurar(snapshot);

	    org.junit.Assert.assertEquals(original.obterJogadores().size(), restaurado.obterJogadores().size());
	    restaurado.depurarMoverPara(3);
	    GameModelo.VisaoCasa casa = restaurado.obterCasaAtual();
	    org.junit.Assert.assertEquals("PROPRIEDADE", casa.tipo);
	    org.junit.Assert.assertEquals(Integer.valueOf(1), casa.casas);
	    org.junit.Assert.assertNotNull("Propriedade deveria ter proprietário após restauração", casa.proprietario);
	    org.junit.Assert.assertTrue("Flag de turno deve indicar que é possível salvar após restauração",
	            restaurado.estaNoInicioDoTurno());
	    org.junit.Assert.assertEquals(original.getSaldoBanco(), restaurado.getSaldoBanco());
	}

	@Test
	public void indicadorDeInicioDeTurnoDeveAlternar() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.adicionarJogador("B");
	    m.sortearOrdemJogadores();
	    org.junit.Assert.assertTrue(m.estaNoInicioDoTurno());
	    m.deslocarPiao(1, 1);
	    org.junit.Assert.assertFalse(m.estaNoInicioDoTurno());
	    m.encerrarTurno();
	    org.junit.Assert.assertTrue(m.estaNoInicioDoTurno());
	}

	@Test
	public void deveComprarCompanhia() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.depurarMoverPara(5); // Companhia Ferroviária
	    int saldoBancoAntes = m.getSaldoBanco();
	    int saldoJogAntes = m.obterJogadorDaVez().saldo;
	    org.junit.Assert.assertTrue(m.comprarCompanhia());
	    org.junit.Assert.assertTrue(m.getSaldoBanco() > saldoBancoAntes);
	    org.junit.Assert.assertTrue(m.obterJogadorDaVez().saldo < saldoJogAntes);
	}

	@Test
	public void aluguelDeCompanhiaEscalaComQuantidade() {
	    GameModelo m = new GameModelo();
	    m.adicionarJogador("A");
	    m.adicionarJogador("B");
	    // Jogador A compra duas companhias
	    m.depurarMoverPara(5);
	    org.junit.Assert.assertTrue(m.comprarCompanhia());
	    m.encerrarTurno();
	    m.encerrarTurno(); // volta para A
	    m.depurarMoverPara(7);
	    org.junit.Assert.assertTrue(m.comprarCompanhia());

	    // Jogador B cai em uma das companhias e paga aluguel proporcional
	    m.encerrarTurno(); // -> B
	    int saldoAntes = m.obterJogadorDaVez().saldo;
	    m.depurarMoverPara(3);
	    m.deslocarPiao(1, 1); // cai na posição 5 (Companhia Ferroviária)
	    int saldoDepois = m.obterJogadorDaVez().saldo;
	    org.junit.Assert.assertTrue("Saldo deveria diminuir ao pagar aluguel da companhia", saldoDepois < saldoAntes);
	}

	private void moverAtualParaCasa(GameModelo jogo, int indiceCasa) {
	    int destino = ((indiceCasa % 40) + 40) % 40;
	    int origem = destino - 2;
	    jogo.depurarMoverPara(origem);
	    jogo.deslocarPiao(1, 1);
	}
}
