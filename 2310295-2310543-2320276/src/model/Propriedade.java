package model;

class Propriedade extends Casa { // uma casa que pode ter dono, construções e cobrar aluguel
    private Jogador dono; // referência ao proprietário (null se sem dono)
    private final int preco; // preço de compra do terreno
    private final int aluguelBase; // aluguel base usado no cálculo do aluguel
    private int casas; // quantidade de casas 
    private boolean hotel; // indica se há hotel (true/false)
    private boolean podeConstruirNestaJogada = false; // limita a construção por queda e se comprou nao constrói na rodada

    public Propriedade(String nome, int preco, int aluguelBase) { // construtor completo
        super(nome);  // chama construtor da classe base com o nome
        this.preco = preco; // armazena o preço
        this.aluguelBase = aluguelBase; // armazena o aluguel base
        this.dono = null; // começa sem dono
        this.casas = 0; // começa sem casas
        this.hotel = false; // começa sem hotel
    }

    // quando o jogador (que é dono) cai na propriedade:
    @Override
    public void acao(Jogador jogador, Jogo jogo) { // executa quando o peão cai nesta propriedade
        if (temDono() && dono != jogador && (casas > 0 || hotel)) { // se tem dono, é de outro, e tem construções
            int aluguel = calcularAluguel(); // calcula aluguel conforme casas/hotel
            jogador.pagar(aluguel, dono, jogo.getBanco()); // jogador paga aluguel ao dono (pode falir)
        } else if (dono == jogador) { // se o dono caiu na própria propriedade
        	podeConstruirNestaJogada = true; // habilita UMA construção nesta jogada 
    	} else {
    		podeConstruirNestaJogada = false; // sem dono e quem caiu não é dono => não habilita construir
    	}
    }

    @Override
    public boolean podeSerCompradaPor(Jogador j) { // indica se pode ser comprada pelo jogador atual
        return dono == null && j.getSaldo() >= preco; // sem dono e saldo suficiente
    }

    @Override
    public boolean comprar(Jogador j, Banco banco) { // tenta comprar esta propriedade
        if (!podeSerCompradaPor(j)) return false; // se não puder, retorna false
        j.debitar(preco); // debita o preço do comprador
        dono = j; // define o novo dono
        banco.venderPropriedade(this);	// banco registra a venda (recebe o valor)
        j.adicionarPropriedade(this);  // adiciona ao portfólio do jogador
        podeConstruirNestaJogada = false; // não construir na mesma jogada de compra
        return true; // compra realizada
    }

    @Override
    public boolean construir(Jogador j, Banco banco) { // tenta construir (1 imóvel por queda)
        if (dono != j || !podeConstruirNestaJogada) return false; // só o dono pode construir, mas se não puder construir nessa jogada retorna

        boolean ok = false; // flag de sucesso
        if (podeConstruirHotel(j)) { // tenta hotel primeiro (regras de upgrade)
            int precoHotel = preco; // custo do hotel (preço do terreno)
            if (j.getSaldo() >= precoHotel) {  // verifica saldo
            	j.debitar(precoHotel);  // debita valor do hotel
            	ok = construirHotel(banco);  // aplica construção e repassa ao banco
            }
        } else if (podeConstruir(j)) { // caso não possa hotel, tenta casa
            int precoCasa = preco / 2; // custo de uma casa (metade do terreno)
            if (j.getSaldo() >= precoCasa) { // verifica saldo 
            	j.debitar(precoCasa);  // debita valor da casa
            	ok = construirCasa(banco); // aplica construção e repassa ao banco
            }
        }
        
        if (ok) podeConstruirNestaJogada = false; // permite apenas 1 imóvel por queda
        return ok; // retorna se construiu ou não
    }

    private int calcularAluguel() { // calcula o aluguel baseado nas construções
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
		if (casas >= 4) return false; // limite de 4 casas
		casas++; // incrementa quantidade de casas
		banco.receber(preco / 2); // banco recebe o custo da casa
		return true; // sucesso
	}
    
	boolean construirHotel(Banco banco) { // aplica a construção de hotel
		if (hotel || casas < 1) return false; // não pode ter hotel e precisa de ≥ 1 casa
		hotel = true; // marca que tem hotel
		banco.receber(preco); // banco recebe o custo do hotel
		return true; // sucesso
	}

	boolean temDono() { return dono != null; } // indica se existe proprietário
    
	void transferirPara(Banco banco) { // transfere a propriedade de volta ao banco (falência)
		dono = null; // remove o dono
		casas = 0; // zera as casas (volta ao estado original)
		hotel = false; // remove hotel
		podeConstruirNestaJogada = false; // reseta flag de construção
		banco.adicionarPropriedade(this); // devolve ao estoque do banco
	}

	public int getValorTotal() { // valor do terreno + construções (para liquidação)
		int valorCasas = casas * (preco / 2); // cada casa custa metade do terreno
		int valorHotel = hotel ? preco : 0; // hotel custa o preço do terreno
		return preco + valorCasas + valorHotel; // soma total
	}

	public int getPreco() { return preco; } // expõe o preço (usado pelo Banco)
	public Jogador getDono() { return dono; } // expõe o dono (leitura)
}
