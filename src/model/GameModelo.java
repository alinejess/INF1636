package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Carta;

/** API pública do Model (Iteração 1). */
public class GameModelo {
	
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
        Jogador j = jogadorAtual();
        ultimaPropriedadeAlcancada = null;

        // --- PRISÃO: só sai com dupla (ou carta em outro método) ---
        if (j.naPrisao) {
            if (d1 == d2) {
                // saiu com dupla
                j.naPrisao = false;
                j.turnosNaPrisao = 0;
                mover(j, d1 + d2);
                aplicarEfeitoDaCasa(j);
                
                notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                System.out.println("[Modelo] Saiu da prisão com dupla " + d1 + "+" + d2 + " e andou " + (d1 + d2));
                return true;
            } else {
                // continua preso, NÃO MOVE
                j.turnosNaPrisao++;
                notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                System.out.println("[Modelo] Continua preso. Dados: " + d1 + "+" + d2 + " (turnos na prisão=" + j.turnosNaPrisao + ")");
                return false;
            }
           
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

        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        return true;
    }

    public boolean construirCasa() {
        Jogador j = jogadorAtual();
        Casa c = tabuleiro.obter(j.posicao);
        if (!(c instanceof Propriedade)) return false;

        Propriedade p = (Propriedade) c;
        if (p.proprietario != j) return false;

        // Regra: 1 por vez, até 4 casas + 1 hotel
        if (p.hotel) return false;

        int custo;
        if (p.casas < 4) {
            custo = p.custoCasa;      // 50% do preço
            if (j.saldo < custo) return false;
            j.saldo -= custo;
            banco.receber(custo);
            p.casas++;
        } else { // p.casas == 4 -> ergue hotel
            custo = p.custoHotel;     // 100% do preço
            if (j.saldo < custo) return false;
            j.saldo -= custo;
            banco.receber(custo);
            p.hotel = true;
            // Mantemos p.casas = 4, pois a regra/planilha considera "4 casas e um hotel"
        }

        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
        return true;
    }


    /** Encerra o turno e seleciona o próximo jogador ativo. Se “virou a rodada”, paga salário. */
    public void encerrarTurno() {
        if (jogadores.isEmpty()) {
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }

        // Próximo jogador ativo a partir do atual
        int proximo = proximoJogadorAtivo(indiceJogadorAtual);
        if (proximo < 0) { // ninguém ativo -> fim de jogo
            // opcional: jogoEncerrado = true;
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }

        boolean virouRodada = (proximo == indiceInicioDaRodada);

        indiceJogadorAtual = proximo;
        ultimaPropriedadeAlcancada = null;

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
                indiceJogadorAtual
        );
    }
    
    public java.util.List<VisaoJogador> obterJogadores() {
        java.util.List<VisaoJogador> out = new java.util.ArrayList<VisaoJogador>();
        for (int i = 0; i < jogadores.size(); i++) {
            Jogador j = jogadores.get(i);
            out.add(new VisaoJogador(j.nome, j.saldo, j.posicao, j.naPrisao, j.ativo, temCartaSaidaPrisao(j), i));
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

        VisaoJogador(String nome, int saldo, int posicao, boolean naPrisao, boolean ativo,
        		boolean temCarta, int indice) {
            this.nome = nome; this.saldo = saldo; this.posicao = posicao;
            this.naPrisao = naPrisao; this.ativo = ativo; 
            this.temCartaSaidaDaPrisao = temCarta; this.indice = indice;
        }
    }

    public VisaoCasa obterCasaAtual() {
        Jogador p = jogadorAtual();
        Casa c = tabuleiro.obter(p.posicao);
        if (c instanceof Propriedade) {
            Propriedade pr = (Propriedade) c;
            String donoNome = (pr.proprietario == null ? null : pr.proprietario.nome);
            Integer donoIdx = (pr.proprietario == null ? null : Integer.valueOf(pr.proprietario.posicao));
            return VisaoCasa.propriedade(pr.nome, pr.preco, pr.custoCasa, pr.casas, pr.hotel, donoNome, donoIdx);
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

        private VisaoCasa(String nome, String tipo, Integer preco, Integer custoCasa,
                          Integer casas, Boolean hotel, String proprietario, Integer indiceProprietario) {
            this.nome = nome;
            this.tipo = tipo;
            this.preco = preco;
            this.custoCasa = custoCasa;
            this.casas = casas;
            this.hotel = hotel;
            this.proprietario = proprietario;
            this.indiceProprietario = indiceProprietario;
        }

        static VisaoCasa propriedade(String nome, int preco, int custoCasa, int casas,
                                     boolean hotel, String proprietario, Integer indiceProprietario) {
            return new VisaoCasa(nome, "PROPRIEDADE", preco, custoCasa, casas, hotel, proprietario, indiceProprietario);
        }

        static VisaoCasa especial(String nome, String tipoEspecial) {
            return new VisaoCasa(nome, tipoEspecial, null, null, null, null, null, null);
        }
    }

    // =========================
    //     LÓGICA INTERNA
    // =========================

    private Jogador jogadorAtual() { return jogadores.get(indiceJogadorAtual); }

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
            if (e.getTipo() == CasaEspecial.Tipo.VA_PARA_PRISAO) {
            	enviarParaPrisao(j);
                idUltimaCartaSorteReves = carta.getIdImagem(); // garanta getIdImagem() nas subclasses de Carta
                notificar(EventoJogo.ESTADO_ATUALIZADO, null);
                return;
            }
            
            if (e.getTipo() == CasaEspecial.Tipo.SORTE_REVES) {
                processarSorteReves(j);
                return;
            }
            
            // Prisão/Parada/Sorte-Revés etc.: nada aqui
            ultimaPropriedadeAlcancada = null;
            idUltimaCartaSorteReves = null;
            notificar(EventoJogo.ESTADO_ATUALIZADO, null);
            return;
        }

        // Propriedade
        if (c instanceof Propriedade) {
        	idUltimaCartaSorteReves = null;
        	
            Propriedade p = (Propriedade) c;
            ultimaPropriedadeAlcancada = p;

            if (p.proprietario == null) return;
            if (p.proprietario == j) return;
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
        log("[PRISÃO] %s foi para a prisão (pos=%d)", j.nome, j.posicao);
        notificar(EventoJogo.ESTADO_ATUALIZADO, null);
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
}
