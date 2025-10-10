package model;

class Propriedade extends Casa {
    private Jogador dono;
    private int preco;
    private int aluguel;
    private int casas;
    private boolean hotel;

    public Propriedade(String nome, int preco, int aluguel) {
        super(nome);
        this.preco = preco;
        this.aluguel = aluguel;
        this.dono = null;
        this.casas = 0;
        this.hotel = false;
    }

    @Override
    public void acao(Jogador jogador, Jogo jogo) {
        if (dono == null) {
            if (jogador.getSaldo() >= preco) {
                jogador.comprarPropriedade(this);
                System.out.println(jogador.getNome() + " comprou " + nome);
            }
        } else if (dono != jogador) {
        	  construir(jogador);
        } else {
            jogador.pagarAluguel(dono, calcularAluguel());
        }
    }

    private void construir(Jogador jogador) {
        if (!hotel && casas < 4) {
            casas++;
            System.out.println(jogador.getNome() + " construiu uma casa em " + nome);
        } else if (casas >= 1 && !hotel) {
            hotel = true;
            casas = 0;
            System.out.println(jogador.getNome() + " construiu um hotel em " + nome);
        }
    }

    private int calcularAluguel() {
        int base = aluguel;
        if (hotel) base *= 5;
        else base *= (casas + 1);
        return base;
    }

    public int getValorTotal() {
        int valorCasas = casas * (preco / 2);
        int valorHotel = hotel ? preco : 0;
        return preco + valorCasas + valorHotel;
    }

    public int getPreco() { return preco; }
    public String getNome() { return nome; }
    
    public Jogador getDono() { return dono; }
    public void setDono(Jogador dono) { this.dono = dono; }

}
