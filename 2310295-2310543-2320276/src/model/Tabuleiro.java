package model; // pacote do Model

import java.util.*; // importa List e ArrayList

class Tabuleiro { // representa a coleção ordenada de casas do jogo
    private final List<Casa> casas; // lista de casas na ordem em que são percorridas
    private int indicePrisao;  // guarda a posição da casa de prisão no tabuleiro

    public Tabuleiro() { // construtor padrão
        casas = new ArrayList<>(); // instancia a lista
        inicializarCasas(); // popula o tabuleiro com as casas
    }

    private void inicializarCasas() { // cria as casas e define seus tipos/posições
        casas.add(new Casa("Início"));                         // 0 casa de início (ponto de partida)
        casas.add(new Propriedade("Av. Paulista", 200, 50));   // 1 propriedade com preço e aluguel base
        casas.add(new Propriedade("Av. Faria Lima", 220, 55)); // 2 outra propriedade
        casas.add(new VaParaPrisao("Vá para a prisão"));       // 3 envia direto para prisão
        casas.add(new Propriedade("Av. Brasil", 240, 60));     // 4 outra propriedade
        casas.add(new Prisao("Prisão"));                       // 5 (prisão)
        indicePrisao = 5;
    }

    public Casa getCasa(int posicao) {  // devolve a casa de uma posição 
    	return casas.get(posicao % casas.size());  // usa módulo para garantir índice válido circular
    }
    public int getTotalCasas() { return casas.size(); } // total de casas no tabuleiro
    public int getIndicePrisao() { return indicePrisao; } // retorna a posição da prisão

}
