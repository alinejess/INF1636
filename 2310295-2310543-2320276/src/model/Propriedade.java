package model;

class Propriedade extends Casa {
    private Jogador dono;
    private final int preco;
    private final int aluguelBase;
    private int casas;
    private boolean hotel;
    private boolean podeConstruirNestaJogada = false;

    public Propriedade(String nome, int preco, int aluguelBase) {
        super(nome);
        this.preco = preco;
        this.aluguelBase = aluguelBase;
        this.dono = null;
        this.casas = 0;
        this.hotel = false;
    }

    // quando o jogador (que é dono) cai na propriedade:
    @Override
    public void acao(Jogador jogador, Jogo jogo) {
        // se o “dono” registrado faliu, volta ao banco e zera dono/construções
        if (dono != null && !dono.isAtivo()) {
            transferirPara(jogo.getBanco());
        }

        if (temDono() && dono != jogador && (casas > 0 || hotel)) {
            int aluguel = calcularAluguel();
            jogador.pagar(aluguel, dono, jogo.getBanco()); // pode disparar falência do visitante
            if (jogador.getSaldo() < 0) {
                jogo.vendaForcada(); // tenta vender a 90% até regularizar
            }
            // quem pagou aluguel não habilita construção
            podeConstruirNestaJogada = false;
        } else if (dono == jogador) {
            // dono caiu na própria: habilita UMA construção nesta jogada
            podeConstruirNestaJogada = true;
        } else {
            // sem dono: ninguém pode construir
            podeConstruirNestaJogada = false;
        }
    }


    @Override
    public boolean podeSerCompradaPor(Jogador j) { // indica se pode ser comprada pelo jogador atual
        return dono == null && j.getSaldo() >= preco; // sem dono e saldo suficiente
    }

    @Override
    public boolean comprar(Jogador j, Banco banco) {
        if (dono != null && !dono.isAtivo()) {
            transferirPara(banco);
        }
        if (!podeSerCompradaPor(j)) return false;
        j.debitar(preco);
        dono = j;
        banco.venderPropriedade(this);
        j.adicionarPropriedade(this);
        podeConstruirNestaJogada = false;
        return true;
    }
    
    

    @Override
    public boolean construir(Jogador j, Banco banco) { // tenta construir (1 imóvel por queda)
        if (dono != j || !podeConstruirNestaJogada) return false; // só o dono pode construir, mas se não puder construir nessa jogada, retorna

        boolean ok = false;
        if (podeConstruirHotel(j)) {
            int precoHotel = preco;
            if (j.getSaldo() >= precoHotel) {
                j.debitar(precoHotel);
                ok = construirHotel(banco);
            }
        } else if (podeConstruir(j)) {
            int precoCasa = preco / 2; // custo de uma casa (metade do terreno)
            if (j.getSaldo() >= precoCasa) {
                j.debitar(precoCasa);
                ok = construirCasa(banco);
            }
        }
        
        if (ok) podeConstruirNestaJogada = false; // permite apenas 1 imóvel por queda
        return ok; // retorna se construiu ou não
    }

    private int calcularAluguel() {
        int mult = 1 + casas + (hotel ? 2 : 0); // multiplicador simples: base + casas + bônus de hotel
        return aluguelBase * mult; // aluguel final
    }
    
    boolean podeConstruir(Jogador j) { // pré-condições para construir uma casa
        return dono == j && !hotel && casas < 4; // precisa ser dono, não ter hotel e ter < 4 casas
    }
    
    boolean podeConstruirHotel(Jogador j) { // pré-condições para construir hotel
        return dono == j && !hotel && casas >= 1; // precisa ser dono, sem hotel e ter ao menos 1 casa
    }

    boolean construirCasa(Banco banco) { // aplica a construção de casa
        if (casas >= 4) return false; 
        casas++;
        banco.receber(preco / 2); // banco recebe o custo da casa
        return true;
    }
    
    boolean construirHotel(Banco banco) { // aplica a construção de hotel
        if (hotel || casas < 1) return false; // não pode ter hotel e precisa de ≥ 1 casa
        hotel = true;
        banco.receber(preco); // banco recebe o custo do hotel
        return true;
    }

    boolean temDono() { return dono != null; } // indica se existe proprietário
    
    void transferirPara(Banco banco) { // transfere a propriedade de volta ao banco (falência/venda forçada)
        if (dono != null) {
            dono.removerPropriedade(this); // remove do portfólio do dono
        }

        dono = null;
        casas = 0;
        hotel = false;
        podeConstruirNestaJogada = false;

        banco.adicionarPropriedade(this); // devolve ao estoque do banco
    }

    public int getValorTotal() { // valor do terreno + construções
        int valorCasas = casas * (preco / 2); // cada casa custa metade do terreno
        int valorHotel = hotel ? preco : 0; // hotel custa o preço do terreno
        return preco + valorCasas + valorHotel; // soma total
    }

    public int getPreco() { return preco; } // expõe o preço (usado pelo Banco)
    public Jogador getDono() { return dono; } // expõe o dono (leitura)
}
