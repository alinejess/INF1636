package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import model.Carta;

/** API pública do Model (Iteração 1). */
public class GameModelo {

    private static final int SNAPSHOT_VERSAO = 4;
	
	// --- Observer ---
	private final List<OuvinteJogo> ouvintes = new ArrayList<OuvinteJogo>();

	public static final class Lancamento {
	    public final int d1, d2;
	    public Lancamento(int d1, int d2) { this.d1 = d1; this.d2 = d2; }
	}

	public void adicionarOuvinte(OuvinteJogo o) { if (o != null) ouvintes.add(o); }
	public void removerOuvinte(OuvinteJogo o) { ouvintes.remove(o); }
	private void notificar(EventoJogo ev, Object payload) {
	    for (OuvinteJogo o : ouvintes) try { o.notificar(ev, payload); } catch (Throwable ignore) {}
	}
	// --- final observer --- 
	
	/** Emite o último lançamento para a View (faces dos dados). */
	public void registrarLancamento(int d1, int d2) {
	    notificar(EventoJogo.DADOS_LANCADOS, new Lancamento(d1, d2));
	}

    private Tabuleiro tabuleiro;
    private List<Jogador> jogadores;
    private Banco banco;
    private Random sorteio;
    private int indiceJogadorAtual;
    private Propriedade ultimaPropriedadeAlcancada;
    private Carta carta;
    
    final BaralhoSorteReves baralho;

    // ---- Regras de rodada/salário ----
    private int salarioPorRodada = 200;
    private int numeroDaRodada = 1;
    /** Quem “abre” a rodada atual; quando o turno volta para ele, nova rodada começa. */
    private int indiceInicioDaRodada = 0;
    
    private  String idUltimaCartaSorteReves = null;
    private boolean inicioDeTurno = true;
    private boolean podeLancarDados = true;
    private int duplasConsecutivas = 0;

    public GameModelo() {
        this.tabuleiro = Tabuleiro.criarPadrao();
        this.jogadores = new ArrayList<Jogador>();
        this.banco = new Banco(200_000);
        this.sorteio = new Random();
        this.indiceJogadorAtual = 0;
        this.ultimaPropriedadeAlcancada = null;
        this.indiceInicioDaRodada = 0;
        this.numeroDaRodada = 1;
        this.salarioPorRodada = 0; 
        this.baralho = BaralhoSorteReves.criarPadrao();
        this.inicioDeTurno = true;
        this.podeLancarDados = true;
        this.duplasConsecutivas = 0;
    }

    // =========================
    //     GESTÃO JOGADORES
    // ========================= 

    public void adicionarJogador(String nome) {
        jogadores.add(new Jogador(nome, 4000, 0));
        // Se for o primeiro jogador, ele passa a ser o início da rodada.
        if (jogadores.size() == 1) {
            indiceInicioDaRodada = 0;
            indiceJogadorAtual = 0;
        }
    }

    public int getQuantidadeDeJogadoresAtivos() {
        int c = 0;
        for (Jogador j : jogadores) if (j.ativo) c++;
        return c;
    }
   
    public int obterIndiceJogadorDaVez() {
        return indiceJogadorAtual; // expõe somente o índice da vez (0..n-1)
    }

    // =========================
    //     ETAPAS DA JOGADA
    // =========================

    public boolean deslocarPiao(int d1, int d2) {
        if (!podeLancarDados) {
            log("[DADOS] Lançamento ignorado: aguardando próximo turno.");
            return false;
        }
        Jogador j = jogadorAtual();
        boolean dupla = (d1 == d2);
        podeLancarDados = false;
        ultimaPropriedadeAlcancada = null;
        inicioDeTurno = false;

        // --- PRISÃO: só sai com dupla (ou carta em outro método) ---
        if (j.naPrisao) {
            duplasConsecutivas = 0;
            if (dupla) {
                // saiu com dupla
                j.naPrisao = false;
                j.turnosNaPrisao = 0;
                mover(j, d1 + d2);
                aplicarEfeitoDaCasa(j);
                
                duplasConsecutivas = 1;
                podeLancarDados = j.ativo; // continua jogando se ainda ativo
                notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                System.out.println("[Modelo] Saiu da prisão com dupla " + d1 + "+" + d2 + " e andou " + (d1 + d2));
                return true;
            } else {
                // continua preso, NÃO MOVE
                j.turnosNaPrisao++;
                podeLancarDados = false;
                notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                System.out.println("[Modelo] Continua preso. Dados: " + d1 + "+" + d2 + " (turnos na prisão=" + j.turnosNaPrisao + ")");
                return false;
            }
           
        }

        if (dupla) {
            duplasConsecutivas++;
            if (duplasConsecutivas >= 3) {
                duplasConsecutivas = 0;
                enviarParaPrisao(j);
                podeLancarDados = false;
                return true;
            }
        } else {
            duplasConsecutivas = 0;
        }
        
        Casa c = tabuleiro.obter(j.posicao);
        boolean caiuEmSorte = (c instanceof CasaEspecial) &&
                              ((CasaEspecial) c).getTipo() == CasaEspecial.Tipo.SORTE_REVES;

        if (!caiuEmSorte) {
            idUltimaCartaSorteReves = null; // libera para a View poder exibir propriedade
        }

        // --- Jogo normal ---
        mover(j, d1 + d2);
        aplicarEfeitoDaCasa(j);
        podeLancarDados = dupla && j.ativo;
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        System.out.println("[Modelo] Moveu " + (d1 + d2) + " casas. Nova posição=" + j.posicao);
        return true;
    }

    public boolean comprarPropriedade() {
        Jogador j = jogadorAtual();
        Casa c = tabuleiro.obter(j.posicao);
        if (!(c instanceof Propriedade)) return false;

        Propriedade p = (Propriedade) c;
        if (p.proprietario != null) return false;
        if (j.saldo < p.preco) return false;

        j.saldo -= p.preco;
        banco.receber(p.preco);
        p.proprietario = j;
        p.construcaoLiberada = false;
        log("[COMPRA] %s adquiriu a propriedade %s por %d (saldo=%d)",
                j.nome, p.nome, p.preco, j.saldo);

        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        return true;
    }

    public boolean comprarCompanhia() {
        Jogador j = jogadorAtual();
        Casa c = tabuleiro.obter(j.posicao);
        if (!(c instanceof Companhia)) return false;

        Companhia companhia = (Companhia) c;
        if (companhia.proprietario != null) return false;
        if (j.saldo < companhia.preco) return false;

        j.saldo -= companhia.preco;
        banco.receber(companhia.preco);
        companhia.proprietario = j;
        log("[COMPRA] %s adquiriu a companhia %s por %d (saldo=%d)",
                j.nome, companhia.nome, companhia.preco, j.saldo);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        return true;
    }

    public boolean construirCasa() {
        Jogador j = jogadorAtual();
        Casa c = tabuleiro.obter(j.posicao);
        if (!(c instanceof Propriedade)) return false;

        Propriedade p = (Propriedade) c;
        if (p.proprietario != j) return false;
        if (!p.construcaoLiberada) return false;

        // Regra: 1 por vez, até 4 casas + 1 hotel
        if (p.hotel) return false;

        int custo;
        if (p.casas < 4) {
            custo = p.custoCasa;      // 50% do preço
            if (j.saldo < custo) return false;
            j.saldo -= custo;
            banco.receber(custo);
            p.casas++;
            log("[CONSTRUÇÃO] %s construiu uma casa em %s (casas=%d, saldo=%d)",
                    j.nome, p.nome, p.casas, j.saldo);
        } else { // p.casas == 4 -> ergue hotel
            custo = p.custoHotel;     // 100% do preço
            if (j.saldo < custo) return false;
            j.saldo -= custo;
            banco.receber(custo);
            p.hotel = true;
            log("[CONSTRUÇÃO] %s construiu um HOTEL em %s (saldo=%d)",
                    j.nome, p.nome, j.saldo);
            // Mantemos p.casas = 4, pois a regra/planilha considera "4 casas e um hotel"
        }

        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        return true;
    }


    /** Encerra o turno e seleciona o próximo jogador ativo. Se “virou a rodada”, paga salário. */
    public void encerrarTurno() {
        if (jogadores.isEmpty()) {
            inicioDeTurno = true;
            podeLancarDados = true;
            duplasConsecutivas = 0;
            imprimirEstadoCompleto();
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }

        // Próximo jogador ativo a partir do atual
        int proximo = proximoJogadorAtivo(indiceJogadorAtual);
        if (proximo < 0) { // ninguém ativo -> fim de jogo
            // opcional: jogoEncerrado = true;
            inicioDeTurno = true;
            podeLancarDados = true;
            duplasConsecutivas = 0;
            imprimirEstadoCompleto();
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }

        boolean virouRodada = (proximo == indiceInicioDaRodada);

        indiceJogadorAtual = proximo;
        ultimaPropriedadeAlcancada = null;
        inicioDeTurno = true;
        podeLancarDados = true;
        duplasConsecutivas = 0;

        if (virouRodada) {
            numeroDaRodada++;
            pagarSalarioDeRodadaParaTodos();

            // se quem “abria” a rodada foi eliminado, avance o ponteiro de início
            if (!jogadores.get(indiceInicioDaRodada).ativo) {
                indiceInicioDaRodada = proximoJogadorAtivo(indiceInicioDaRodada);
            }
            // (alternativa válida: fixar explicitamente o novo início)
            // indiceInicioDaRodada = indiceJogadorAtual;
        }

        // IMPORTANTÍSSIMO: avisa a View para trocar cor/nome, etc.
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }
    
    public String obterIdUltimaCartaSorteReves() {
    	return idUltimaCartaSorteReves;
    }


    // =========================
    //          PRISÃO
    // =========================

    /** Concede 1 carta de "Sair da Prisão" ao jogador (baralho devolve a única depois que for usada). */
    public void concederCartaSaidaDaPrisaoAoJogadorDaVez(Jogador j) {
        j.cartasSaidaDaPrisao++;
    	log("Carta de saída prisão concedida ao jogador %s", j.nome);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }
    
    
 // Atalho público para testes: concede carta ao jogador da vez (sem expor Jogador)
    public void concederCartaSaidaPrisao() {
    	concederCartaSaidaDaPrisaoAoJogadorDaVez(jogadorAtual()); // chama o método já existente no Model (package-private)
    }
    
    public boolean usarCartaSaidaPrisao() {
        Jogador j = jogadorAtual();
        if (!j.naPrisao || j.cartasSaidaDaPrisao <= 0) return false;
        j.cartasSaidaDaPrisao--;
        j.naPrisao = false;
        j.turnosNaPrisao = 0;
        baralho.devolverCartaSairDaPrisao(); // volta a “única” ao baralho
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        return true;
    }

    // =========================
    //      CONSULTA / CONFIG
    // =========================

    public int getSaldoBanco() { return banco.getSaldo(); }

    public int getNumeroDaRodada() { return numeroDaRodada; }

    /** Permite alterar o valor do salário por rodada (default 200). */
    public void configurarSalarioPorRodada(int valor) {
        if (valor >= 0) this.salarioPorRodada = valor;
    }

    public boolean estaNoInicioDoTurno() {
        return inicioDeTurno;
    }

    public boolean podeLancarDados() {
        return podeLancarDados;
    }
    
    // --- util interno ---
    private boolean temCartaSaidaPrisao(Jogador j) {
        return j != null && j.cartasSaidaDaPrisao > 0;
    }

    // =========================
    //     TIPOS “VISÃO”
    // =========================
     
    public VisaoJogador obterJogadorDaVez() {
        Jogador j = jogadores.get(indiceJogadorAtual);
        return new VisaoJogador(
                j.nome,
                j.saldo,
                j.posicao,
                j.naPrisao,
                j.ativo,
                temCartaSaidaPrisao(j),
                indiceJogadorAtual,
                propriedadesDoJogador(j),
                companhiasDoJogador(j)
        );
    }
    
    public java.util.List<VisaoJogador> obterJogadores() {
        java.util.List<VisaoJogador> out = new java.util.ArrayList<VisaoJogador>();
        for (int i = 0; i < jogadores.size(); i++) {
            Jogador j = jogadores.get(i);
            out.add(new VisaoJogador(j.nome, j.saldo, j.posicao, j.naPrisao, j.ativo, temCartaSaidaPrisao(j), i,
                    propriedadesDoJogador(j), companhiasDoJogador(j)));
        }
        return out;
    }

    public static final class VisaoJogador {
        public final String nome;
        public final int saldo;
        public final int posicao;
        public final boolean naPrisao;
        public final boolean ativo;
        public final boolean temCartaSaidaDaPrisao;
        public final int indice;
        public final java.util.List<String> propriedades;
        public final java.util.List<String> companhias;

        VisaoJogador(String nome, int saldo, int posicao, boolean naPrisao, boolean ativo,
                boolean temCarta, int indice, java.util.List<String> propriedades, java.util.List<String> companhias) {
            this.nome = nome;
            this.saldo = saldo;
            this.posicao = posicao;
            this.naPrisao = naPrisao;
            this.ativo = ativo;
            this.temCartaSaidaDaPrisao = temCarta;
            this.indice = indice;
            this.propriedades = propriedades;
            this.companhias = companhias;
        }
    }

    public VisaoCasa obterCasaAtual() {
        Jogador p = jogadorAtual();
        Casa c = tabuleiro.obter(p.posicao);
        if (c instanceof Propriedade) {
            Propriedade pr = (Propriedade) c;
            String donoNome = (pr.proprietario == null ? null : pr.proprietario.nome);
            Integer donoIdx = (pr.proprietario == null ? null : Integer.valueOf(pr.proprietario.posicao));
            return VisaoCasa.propriedade(pr.nome, pr.preco, pr.custoCasa, pr.casas,
                    pr.hotel, donoNome, donoIdx, pr.construcaoLiberada);
        } else if (c instanceof Companhia) {
            Companhia comp = (Companhia) c;
            String donoNome = (comp.proprietario == null ? null : comp.proprietario.nome);
            Integer donoIdx = (comp.proprietario == null ? null : Integer.valueOf(comp.proprietario.posicao));
            return VisaoCasa.companhia(comp.nome, comp.preco, donoNome, donoIdx);
        } else if (c instanceof CasaEspecial) {
            CasaEspecial e = (CasaEspecial) c;
            return VisaoCasa.especial(e.getNome(), e.getTipo().name());
        }
        return null;
    }

    
    public static final class VisaoCasa {
        public final String nome;
        public final String tipo; // "PROPRIEDADE" ou tipo especial
        public final Integer preco;
        public final Integer custoCasa;
        public final Integer casas;
        public final Boolean hotel;
        public final String proprietario;
        public final Integer indiceProprietario; // NOVO
        public final Boolean construcaoLiberada;

        private VisaoCasa(String nome, String tipo, Integer preco, Integer custoCasa,
                          Integer casas, Boolean hotel, String proprietario,
                          Integer indiceProprietario, Boolean construcaoLiberada) {
            this.nome = nome;
            this.tipo = tipo;
            this.preco = preco;
            this.custoCasa = custoCasa;
            this.casas = casas;
            this.hotel = hotel;
            this.proprietario = proprietario;
            this.indiceProprietario = indiceProprietario;
            this.construcaoLiberada = construcaoLiberada;
        }

        static VisaoCasa propriedade(String nome, int preco, int custoCasa, int casas,
                                     boolean hotel, String proprietario, Integer indiceProprietario,
                                     boolean construcaoLiberada) {
            return new VisaoCasa(nome, "PROPRIEDADE", preco, custoCasa, casas, hotel,
                    proprietario, indiceProprietario, construcaoLiberada);
        }

        static VisaoCasa companhia(String nome, int preco, String proprietario, Integer indiceProprietario) {
            return new VisaoCasa(nome, "COMPANHIA", preco, null, null, null,
                    proprietario, indiceProprietario, null);
        }

        static VisaoCasa especial(String nome, String tipoEspecial) {
            return new VisaoCasa(nome, tipoEspecial, null, null, null, null, null, null, null);
        }
    }

    // =========================
    //     LÓGICA INTERNA
    // =========================

    private Jogador jogadorAtual() { return jogadores.get(indiceJogadorAtual); }

    private int contarCompanhiasDoJogador(Jogador proprietario) {
        if (proprietario == null) return 0;
        int total = 0;
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Companhia) {
                Companhia comp = (Companhia) casa;
                if (comp.proprietario == proprietario) total++;
            }
        }
        return total;
    }

    /** Próximo índice de jogador ativo em sentido horário. */
    private int proximoJogadorAtivo(int aPartirDe) {
        int n = jogadores.size();
        if (n == 0) return -1;
        int i = aPartirDe;
        for (int passo = 0; passo < n; passo++) {
            i = (i + 1) % n;
            if (jogadores.get(i).ativo) return i;
        }
        return -1; // nenhum ativo
    }
    
    private void mover(Jogador j, int passos) {
        int novo = (j.posicao + passos) % tabuleiro.tamanho();
        j.posicao = novo;
    }
    
    /** Embaralha a ordem de turno SEM mexer na cor/pino de cada jogador.
     * Todos recomeçam na casa 0 e fora da prisão.
     */
    public void sortearOrdemJogadores() {
        java.util.Collections.shuffle(jogadores, sorteio);

        for (Jogador j : jogadores) {
            j.posicao = 0;
            j.naPrisao = false;
            j.turnosNaPrisao = 0;
            // saldo permanece (em nova partida já é 4000)
        }

        indiceJogadorAtual   = 0;
        indiceInicioDaRodada = 0;
        numeroDaRodada       = 1;
        inicioDeTurno        = true;
        podeLancarDados      = true;
        duplasConsecutivas   = 0;

        // debug opcional:
        StringBuilder sb = new StringBuilder();
        for (Jogador j : jogadores) sb.append(j.nome).append(" ");
        System.out.println("[SORTEIO] ordem: " + sb.toString());

        notificar(EventoJogo.ORDEM_SORTEADA, jogadores);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }



    /** Snapshot imutável para a View exibir a carta aplicada. */
    public static final class CartaSorteRevesInfo {
        public final String idImagem;     // ex.: "chance17"

        public CartaSorteRevesInfo(String idImagem) {
            this.idImagem = idImagem;
        }
    }
    
    public void configurarSementeSorteio(long semente) {
        this.sorteio.setSeed(semente);
    }
    
    
    void aplicarEfeitoDaCasa(Jogador j) {
        Casa c = tabuleiro.obter(j.posicao);

        // Vá para a prisão
        if (c instanceof CasaEspecial) {
            CasaEspecial e = (CasaEspecial) c;
            switch (e.getTipo()) {
                case VA_PARA_PRISAO:
                    enviarParaPrisao(j);
                    idUltimaCartaSorteReves = (carta != null ? carta.getIdImagem() : null);
                    notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                    return;
                case SORTE_REVES:
                    processarSorteReves(j);
                    return;
                case LUCROS:
                    transferirBancoParaJogador(j, 200);
                    log("[CASA] %s recebeu 200 em lucros", j.nome);
                    notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                    return;
                case DIVIDENDOS:
                    transferirJogadorParaBanco(j, 200);
                    log("[CASA] %s pagou 200 de imposto de renda", j.nome);
                    notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                    return;
                default:
                    ultimaPropriedadeAlcancada = null;
                    idUltimaCartaSorteReves = null;
                    notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                    return;
            }
        }

        // Companhia
        if (c instanceof Companhia) {
            idUltimaCartaSorteReves = null;
            ultimaPropriedadeAlcancada = null;

            Companhia comp = (Companhia) c;
            if (comp.proprietario == null) return;
            if (comp.proprietario == j) return;

            int quantidade = contarCompanhiasDoJogador(comp.proprietario);
            int aluguel = comp.aluguel(quantidade);
            transferirJogadorParaJogador(j, comp.proprietario, aluguel);
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }

        // Propriedade
        if (c instanceof Propriedade) {
        	idUltimaCartaSorteReves = null;
        	
            Propriedade p = (Propriedade) c;
            ultimaPropriedadeAlcancada = p;

            if (p.proprietario == null) return;
            if (p.proprietario == j) {
                p.construcaoLiberada = true;
                return;
            }
            if (p.casas == 0 && !p.hotel) return; // regra da iteração 1

            int aluguel = p.aluguel();
            transferirJogadorParaJogador(j, p.proprietario, aluguel);
            
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }
    }
    
    public void aplicarSorteRevesNoJogadorDaVez() {
        processarSorteReves(jogadorAtual()); // jogadorAtual() pode continuar package-private
    }
    
    void transferirJogadorParaJogador(Jogador pagador, Jogador recebedor, int valor) {
        int v = Math.max(0, valor);
        pagador.saldo -= v;
        recebedor.saldo += v;
        if (pagador.saldo < 0) {
            falir(pagador); // inativa e devolve propriedades
        }
        
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }

    
    /** Compra uma carta, aplica o efeito e devolve um snapshot para a View exibir. */
 // Em GameModelo.java
    void processarSorteReves(Jogador j) {
        if (baralho == null) {
            System.out.println("[SORTE/REVES] ERRO: baralho nulo.");
            idUltimaCartaSorteReves = null;
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }

        // Compra segura (com fallback de embaralhar se esvaziou)
        Carta carta = baralho.comprar();
        if (carta == null) {
            baralho.embaralhar();
            carta = baralho.comprar();
            if (carta == null) {
                System.out.println("[SORTE/REVES] Baralho vazio após embaralhar.");
                idUltimaCartaSorteReves = null;
                notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                return;
            }
        }

        // Guarda o id da imagem da carta e avisa a View
        idUltimaCartaSorteReves = carta.getIdImagem();
        System.out.println("[SORTE/REVES] " + idUltimaCartaSorteReves);
        notificar(EventoJogo.CARTA_SORTE_REVES_APLICADA, idUltimaCartaSorteReves);

        // Aplica o efeito da carta
        carta.aplicar(this, j);

        // Regra: "Sair da Prisão" não retorna automaticamente ao baralho
        if (!(carta instanceof CartaSairDaPrisao)) {
            baralho.devolverAoFundo(carta);
        }

        // Atualiza a tela após aplicar os efeitos (saldo, posição etc.)
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }



    // ---- Pagamentos centralizados ----

   private void pagarSalarioDeRodadaParaTodos() {
        for (Jogador j : jogadores) {
            if (j.ativo) {
                transferirBancoParaJogador(j, salarioPorRodada);
            }
        }
    }
    
    void falir(Jogador j) {
		    // devolve propriedades ao banco (sem casas/hotel)
		    for (int i = 0; i < tabuleiro.tamanho(); i++) {
		        Casa casa = tabuleiro.obter(i);
		        if (casa instanceof Propriedade) {
		            Propriedade pr = (Propriedade) casa;
                    if (pr.proprietario == j) {
                        pr.proprietario = null;
                        pr.casas = 0;
                        pr.hotel = false;
                        pr.construcaoLiberada = true;
                    }
                } else if (casa instanceof Companhia) {
                    Companhia cp = (Companhia) casa;
                    if (cp.proprietario == j) {
                        cp.proprietario = null;
                    }
                }
            }
            j.ativo = false;
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        }
    
 // --- API para Cartas (usada por Carta.java) ---

    /** Jogador recebe de cada outro jogador ativo. Pode causar falências. */
    public void receberDeCadaJogador(Jogador recebedor, int porJogador) {
        for (Jogador o : jogadores) if (o != recebedor && o.ativo) {
            o.saldo -= porJogador;
            recebedor.saldo += porJogador;
            if (o.saldo < 0) falir(o);
            log("[$] %s pagou %d a %s (saldo pagador=%d, saldo recebedor=%d)",
                    o.nome, porJogador, recebedor.nome, o.saldo, recebedor.saldo);
        }
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }
    
    /** Jogador paga ao banco (débito). Pode falir. */
    void transferirJogadorParaBanco(Jogador j, int valor) {
    	if (valor < 0) valor = -valor;
        j.saldo -= valor;
        banco.receber(valor);
        if (j.saldo < 0) falir(j);
        log("[$] %s -> Banco: -%d (saldo=%d)", j.nome, valor, j.saldo);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }

    void transferirBancoParaJogador(Jogador j, int valor) {
        if (valor < 0) valor = -valor;
        banco.pagar(valor);
        j.saldo += valor;
        log("[$] Banco -> %s: +%d (saldo=%d)", j.nome, valor, j.saldo);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }


    /** Envia o jogador diretamente para a prisão. */
    public void enviarParaPrisao(Jogador j) {
        j.posicao = tabuleiro.indicePrisao();
        j.naPrisao = true;
        j.turnosNaPrisao = 0;
        ultimaPropriedadeAlcancada = null;
        duplasConsecutivas = 0;
        log("[PRISÃO] %s foi para a prisão (pos=%d)", j.nome, j.posicao);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }

    public void enviarJogadorParaInicio(Jogador j, int bonus) {
        j.posicao = 0;
        j.naPrisao = false;
        j.turnosNaPrisao = 0;
        ultimaPropriedadeAlcancada = null;
        idUltimaCartaSorteReves = null;
        log("[SORTE/REVES] %s foi enviado ao ponto de partida", j.nome);
        if (bonus > 0) {
            banco.pagar(bonus);
            j.saldo += bonus;
            log("[$] Banco -> %s: +%d (saldo=%d)", j.nome, bonus, j.saldo);
        }
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }

    public Snapshot criarSnapshot() {
        List<Snapshot.JogadorEstado> jogadoresEstado = new ArrayList<Snapshot.JogadorEstado>();
        IdentityHashMap<Jogador, Integer> mapaIndices = new IdentityHashMap<Jogador, Integer>();
        for (int i = 0; i < jogadores.size(); i++) {
            Jogador jog = jogadores.get(i);
            mapaIndices.put(jog, Integer.valueOf(i));
            jogadoresEstado.add(new Snapshot.JogadorEstado(
                    jog.nome,
                    jog.saldo,
                    jog.posicao,
                    jog.naPrisao,
                    jog.turnosNaPrisao,
                    jog.cartasSaidaDaPrisao,
                    jog.ativo));
        }

        List<Snapshot.PropriedadeEstado> propriedadesEstado = new ArrayList<Snapshot.PropriedadeEstado>();
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Propriedade) {
                Propriedade prop = (Propriedade) casa;
                Integer proprietario = (prop.proprietario == null ? null : mapaIndices.get(prop.proprietario));
                propriedadesEstado.add(new Snapshot.PropriedadeEstado(
                        i,
                        prop.nome,
                        prop.casas,
                        prop.hotel,
                        proprietario,
                        prop.construcaoLiberada));
            }
        }

        List<Snapshot.CartaEstado> cartasEstado = new ArrayList<Snapshot.CartaEstado>();
        for (BaralhoSorteReves.CartaEstado carta : baralho.exportarEstado()) {
            if (carta != null && carta.tipo != null) {
                cartasEstado.add(new Snapshot.CartaEstado(carta.idImagem, carta.tipo.name(), carta.valor));
            }
        }

        return new Snapshot(
                SNAPSHOT_VERSAO,
                jogadoresEstado,
                propriedadesEstado,
                cartasEstado,
                baralho.isCartaSairDisponivel(),
                indiceJogadorAtual,
                indiceInicioDaRodada,
                numeroDaRodada,
                salarioPorRodada,
                banco.getSaldo(),
                idUltimaCartaSorteReves,
                inicioDeTurno,
                podeLancarDados,
                duplasConsecutivas
        );
    }

    public void restaurar(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (snapshot.jogadores == null || snapshot.jogadores.isEmpty()) {
            throw new IllegalArgumentException("Snapshot sem jogadores.");
        }

        List<Jogador> novosJogadores = new ArrayList<Jogador>(snapshot.jogadores.size());
        for (Snapshot.JogadorEstado estado : snapshot.jogadores) {
            if (estado == null) continue;
            Jogador j = new Jogador(estado.nome, estado.saldo, estado.posicao);
            j.naPrisao = estado.naPrisao;
            j.turnosNaPrisao = estado.turnosNaPrisao;
            j.cartasSaidaDaPrisao = estado.cartasSaidaDaPrisao;
            j.ativo = estado.ativo;
            novosJogadores.add(j);
        }
        if (novosJogadores.isEmpty()) {
            throw new IllegalArgumentException("Snapshot inválido: nenhum jogador reconstruído.");
        }

        this.jogadores = novosJogadores;
        this.banco = new Banco(snapshot.saldoBanco);
        this.indiceJogadorAtual = indiceValido(snapshot.indiceJogadorAtual, jogadores.size());
        this.indiceInicioDaRodada = indiceValido(snapshot.indiceInicioDaRodada, jogadores.size());
        this.numeroDaRodada = snapshot.numeroDaRodada <= 0 ? 1 : snapshot.numeroDaRodada;
        this.salarioPorRodada = Math.max(0, snapshot.salarioPorRodada);
        this.idUltimaCartaSorteReves = snapshot.idUltimaCartaSorte;
        this.inicioDeTurno = snapshot.inicioDeTurno;
        this.podeLancarDados = snapshot.podeLancarDados;
        this.duplasConsecutivas = Math.max(0, snapshot.duplasConsecutivas);
        this.ultimaPropriedadeAlcancada = null;
        this.carta = null;

        restaurarPropriedades(snapshot.propriedades, this.jogadores);
        restaurarBaralho(snapshot.baralhoCartas, snapshot.cartaSairDisponivel);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }

    private void restaurarPropriedades(List<Snapshot.PropriedadeEstado> estado, List<Jogador> jogadoresDestino) {
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Propriedade) {
                Propriedade prop = (Propriedade) casa;
                prop.proprietario = null;
                prop.casas = 0;
                prop.hotel = false;
                prop.construcaoLiberada = true;
            }
        }
        if (estado == null) return;
        for (Snapshot.PropriedadeEstado propEstado : estado) {
            if (propEstado == null) continue;
            if (propEstado.indice < 0 || propEstado.indice >= tabuleiro.tamanho()) continue;
            Casa casa = tabuleiro.obter(propEstado.indice);
            if (!(casa instanceof Propriedade)) continue;
            Propriedade prop = (Propriedade) casa;
            prop.casas = Math.max(0, propEstado.casas);
            prop.hotel = propEstado.hotel;
            prop.construcaoLiberada = propEstado.construcaoLiberada;
            if (propEstado.proprietario != null &&
                    propEstado.proprietario >= 0 &&
                    propEstado.proprietario < jogadoresDestino.size()) {
                prop.proprietario = jogadoresDestino.get(propEstado.proprietario);
            } else {
                prop.proprietario = null;
            }
        }
    }

    private void restaurarBaralho(List<Snapshot.CartaEstado> cartas, boolean cartaSairDisponivel) {
        List<BaralhoSorteReves.CartaEstado> estado = new ArrayList<BaralhoSorteReves.CartaEstado>();
        if (cartas != null) {
            for (Snapshot.CartaEstado c : cartas) {
                if (c == null || c.tipo == null) continue;
                try {
                    BaralhoSorteReves.CartaEstado.Tipo tipo = BaralhoSorteReves.CartaEstado.Tipo.valueOf(c.tipo);
                    estado.add(new BaralhoSorteReves.CartaEstado(c.idImagem, tipo, c.valor));
                } catch (IllegalArgumentException ex) {
                    // ignora carta inválida
                }
            }
        }
        baralho.restaurarEstado(estado, cartaSairDisponivel);
    }

    private static int indiceValido(int indice, int limite) {
        if (limite <= 0) return 0;
        if (indice < 0 || indice >= limite) return 0;
        return indice;
    }

    public static final class Snapshot {
        public final int versao;
        public final List<JogadorEstado> jogadores;
        public final List<PropriedadeEstado> propriedades;
        public final List<CartaEstado> baralhoCartas;
        public final boolean cartaSairDisponivel;
        public final int indiceJogadorAtual;
        public final int indiceInicioDaRodada;
        public final int numeroDaRodada;
        public final int salarioPorRodada;
        public final int saldoBanco;
        public final String idUltimaCartaSorte;
        public final boolean inicioDeTurno;
        public final boolean podeLancarDados;
        public final int duplasConsecutivas;

        public Snapshot(int versao,
                        List<JogadorEstado> jogadores,
                        List<PropriedadeEstado> propriedades,
                        List<CartaEstado> baralhoCartas,
                        boolean cartaSairDisponivel,
                        int indiceJogadorAtual,
                        int indiceInicioDaRodada,
                        int numeroDaRodada,
                        int salarioPorRodada,
                        int saldoBanco,
                        String idUltimaCartaSorte,
                        boolean inicioDeTurno,
                        boolean podeLancarDados,
                        int duplasConsecutivas) {
            this.versao = versao;
            this.jogadores = copiaImutavel(jogadores);
            this.propriedades = copiaImutavel(propriedades);
            this.baralhoCartas = copiaImutavel(baralhoCartas);
            this.cartaSairDisponivel = cartaSairDisponivel;
            this.indiceJogadorAtual = indiceJogadorAtual;
            this.indiceInicioDaRodada = indiceInicioDaRodada;
            this.numeroDaRodada = numeroDaRodada;
            this.salarioPorRodada = salarioPorRodada;
            this.saldoBanco = saldoBanco;
            this.idUltimaCartaSorte = idUltimaCartaSorte;
            this.inicioDeTurno = inicioDeTurno;
            this.podeLancarDados = podeLancarDados;
            this.duplasConsecutivas = duplasConsecutivas;
        }

        private static <T> List<T> copiaImutavel(List<T> origem) {
            if (origem == null || origem.isEmpty()) return Collections.emptyList();
            return Collections.unmodifiableList(new ArrayList<T>(origem));
        }

        public static final class JogadorEstado {
            public final String nome;
            public final int saldo;
            public final int posicao;
            public final boolean naPrisao;
            public final int turnosNaPrisao;
            public final int cartasSaidaDaPrisao;
            public final boolean ativo;

            public JogadorEstado(String nome,
                                 int saldo,
                                 int posicao,
                                 boolean naPrisao,
                                 int turnosNaPrisao,
                                 int cartasSaidaDaPrisao,
                                 boolean ativo) {
                this.nome = nome;
                this.saldo = saldo;
                this.posicao = posicao;
                this.naPrisao = naPrisao;
                this.turnosNaPrisao = turnosNaPrisao;
                this.cartasSaidaDaPrisao = cartasSaidaDaPrisao;
                this.ativo = ativo;
            }
        }

        public static final class PropriedadeEstado {
            public final int indice;
            public final String nome;
            public final int casas;
            public final boolean hotel;
            public final Integer proprietario;
            public final boolean construcaoLiberada;

            public PropriedadeEstado(int indice,
                                     String nome,
                                     int casas,
                                     boolean hotel,
                                     Integer proprietario,
                                     boolean construcaoLiberada) {
                this.indice = indice;
                this.nome = nome;
                this.casas = casas;
                this.hotel = hotel;
                this.proprietario = proprietario;
                this.construcaoLiberada = construcaoLiberada;
            }
        }

        public static final class CartaEstado {
            public final String idImagem;
            public final String tipo;
            public final int valor;

            public CartaEstado(String idImagem, String tipo, int valor) {
                this.idImagem = idImagem;
                this.tipo = tipo;
                this.valor = valor;
            }
        }
    }

    public void imprimirEstadoCompleto() {
        Jogador atual = null;
        try {
            atual = jogadorAtual();
        } catch (Throwable ignored) {}

        log("===== ESTADO ATUAL DO JOGO =====");
        log("Rodada: %d | Jogador atual: %s | Saldo do banco: %d",
                numeroDaRodada,
                (atual == null ? "<desconhecido>" : atual.nome),
                banco.getSaldo());
        for (int i = 0; i < jogadores.size(); i++) {
            logJogadorCompleto(jogadores.get(i), i);
        }
        log("===== FIM DO ESTADO =====");
    }

    private void logJogadorCompleto(Jogador jogador, int indice) {
        log("[Jogador %d] %s | saldo=%d | pos=%d | ativo=%s | prisão=%s | cartas-saida=%d",
                indice + 1,
                jogador.nome,
                jogador.saldo,
                jogador.posicao,
                jogador.ativo,
                jogador.naPrisao,
                jogador.cartasSaidaDaPrisao);
        log("   Propriedades: %s", formatarLista(propriedadesDoJogador(jogador)));
        log("   Companhias: %s", formatarLista(companhiasDoJogador(jogador)));
    }

    private java.util.List<String> propriedadesDoJogador(Jogador jogador) {
        java.util.List<String> lista = new java.util.ArrayList<String>();
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Propriedade) {
                Propriedade prop = (Propriedade) casa;
                if (prop.proprietario == jogador) {
                    String status = prop.hotel ? "HOTEL" : (prop.casas + " casa(s)");
                    lista.add(prop.nome + " (" + status + ")");
                }
            }
        }
        return lista;
    }

    private java.util.List<String> companhiasDoJogador(Jogador jogador) {
        java.util.List<String> lista = new java.util.ArrayList<String>();
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Companhia) {
                Companhia comp = (Companhia) casa;
                if (comp.proprietario == jogador) {
                    lista.add(comp.nome);
                }
            }
        }
        return lista;
    }

    private String formatarLista(java.util.List<String> itens) {
        if (itens.isEmpty()) return "nenhum";
        return String.join(", ", itens);
    }

    public java.util.List<String> obterPropriedadesJogadorDaVez() {
        return new java.util.ArrayList<String>(propriedadesDoJogador(jogadorAtual()));
    }

    public int venderPropriedadeDaVez(String nomePropriedade) {
        Jogador jogador = jogadorAtual();
        Propriedade prop = encontrarPropriedadePorNome(nomePropriedade);
        if (prop == null || prop.proprietario != jogador) return 0;

        int valorBase = prop.preco + prop.casas * prop.custoCasa + (prop.hotel ? prop.custoHotel : 0);
        int valorVenda = (int) Math.round(valorBase * 0.9);
        banco.pagar(valorVenda);
        jogador.saldo += valorVenda;
        prop.proprietario = null;
        prop.casas = 0;
        prop.hotel = false;
        prop.construcaoLiberada = true;
        log("[VENDA] %s vendeu %s para o banco por %d", jogador.nome, prop.nome, valorVenda);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        return valorVenda;
    }

    private Propriedade encontrarPropriedadePorNome(String nome) {
        if (nome == null) return null;
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Propriedade) {
                Propriedade p = (Propriedade) casa;
                if (nome.equalsIgnoreCase(p.nome)) return p;
            }
        }
        return null;
    }

    public java.util.List<RankingJogador> calcularRankingFinal() {
        java.util.List<RankingJogador> ranking = new java.util.ArrayList<RankingJogador>();
        for (int i = 0; i < jogadores.size(); i++) {
            Jogador j = jogadores.get(i);
            ranking.add(new RankingJogador(j.nome, calcularCapital(j)));
        }
        ranking.sort((a, b) -> Integer.compare(b.capital, a.capital));
        return ranking;
    }

    private int calcularCapital(Jogador j) {
        int capital = j.saldo;
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Propriedade) {
                Propriedade p = (Propriedade) casa;
                if (p.proprietario == j) {
                    capital += (int) Math.round(p.preco * 0.9);
                }
            } else if (casa instanceof Companhia) {
                Companhia c = (Companhia) casa;
                if (c.proprietario == j) {
                    capital += (int) Math.round(c.preco * 0.9);
                }
            }
        }
        return capital;
    }

    public static final class RankingJogador {
        public final String nome;
        public final int capital;

        RankingJogador(String nome, int capital) {
            this.nome = nome;
            this.capital = capital;
        }
    }

    private static void log(String fmt, Object... args) {
        System.out.println(String.format(fmt, args));
    }
    
    // --- Apoio a testes (públicos) ---
    public void depurarMoverPara(int indiceCasa) {
        int tam = tabuleiro.tamanho();
        int normal = ((indiceCasa % tam) + tam) % tam; // normaliza negativo/overflow
        jogadorAtual().posicao = normal;
    }

    /** Ajusta o saldo do jogador da vez (apenas para testes). */
    public void depurarDefinirSaldoJogadorDaVez(int novoSaldo) {
        jogadorAtual().saldo = novoSaldo;
    }

    /** Libera um novo lançamento (apoio a testes). */
    public void depurarLiberarLancamento() {
        this.podeLancarDados = true;
        this.inicioDeTurno = true;
        this.duplasConsecutivas = 0;
    }

    public void liquidarPatrimonio() {
        for (int i = 0; i < tabuleiro.tamanho(); i++) {
            Casa casa = tabuleiro.obter(i);
            if (casa instanceof Propriedade) {
                Propriedade prop = (Propriedade) casa;
                if (prop.proprietario != null) {
                    liquidarPropriedade(prop);
                }
            } else if (casa instanceof Companhia) {
                Companhia comp = (Companhia) casa;
                if (comp.proprietario != null) {
                    liquidarCompanhia(comp);
                }
            }
        }
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
    }

    private void liquidarPropriedade(Propriedade prop) {
        int valorBase = prop.preco + prop.casas * prop.custoCasa + (prop.hotel ? prop.custoHotel : 0);
        int valorVenda = (int) Math.round(valorBase * 0.9);
        banco.pagar(valorVenda);
        prop.proprietario.saldo += valorVenda;
        prop.proprietario = null;
        prop.casas = 0;
        prop.hotel = false;
        prop.construcaoLiberada = true;
    }

    private void liquidarCompanhia(Companhia comp) {
        int valorVenda = (int) Math.round(comp.preco * 0.9);
        banco.pagar(valorVenda);
        comp.proprietario.saldo += valorVenda;
        comp.proprietario = null;
    }
}
