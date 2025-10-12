package model;

import java.util.*;

class Tabuleiro {
    private final List<Casa> casas;
    private int indicePrisao;

    public Tabuleiro() {
        casas = new ArrayList<>();
        inicializarCasas();
    }

    private void inicializarCasas() {
        casas.add(new Casa("Início")); // 0 casa de início (ponto de partida)
        casas.add(new Propriedade("Av. Paulista", 200, 50)); // 1 propriedade com preço e aluguel base
        casas.add(new Propriedade("Av. Faria Lima", 220, 55)); // 2 outra propriedade
        casas.add(new VaParaPrisao("Vá para a prisão")); // 3 envia direto para prisão
        casas.add(new Propriedade("Av. Brasil", 240, 60)); // 4 outra propriedade
        casas.add(new Prisao("Prisão")); // 5 (prisão)
        indicePrisao = 5;
    }

    public Casa getCasa(int posicao) { // devolve a casa de uma posição 
    	return casas.get(posicao % casas.size()); // usa módulo para garantir índice válido circular
    }
    public int getTotalCasas() { return casas.size(); } // total de casas no tabuleiro
    public int getIndicePrisao() { return indicePrisao; } // retorna a posição da prisão

}
