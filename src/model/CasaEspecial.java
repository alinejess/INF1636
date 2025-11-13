package model;

/** Casas especiais: início, prisão, vá-para-prisão, etc. */
class CasaEspecial extends Casa {

	enum Tipo {
	    INICIO, 
	    PROPRIEDADE,
	    PRISAO, 
	    VA_PARA_PRISAO, 
	    PARADA_LIVRE, 
	    SORTE_REVES, 
	    METRO, 
	    ONIBUS, 
	    TAXI, 
	    BARCA, 
	    AVIAO, 
	    HELICOPTERO, 
	    VAZIA, 
	    LUCROS,
	    DIVIDENDOS
	}

    private final Tipo tipo;

    CasaEspecial(String nome, Tipo tipo) {
        super(nome);
        this.tipo = tipo;
    }

    public Tipo getTipo() { return tipo; }
}
