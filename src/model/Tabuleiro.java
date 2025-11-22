package model;

import java.util.ArrayList;
import java.util.List;

// Representa o conjunto de casas do jogo.
class Tabuleiro {
    private List<Casa> casas;

    private Tabuleiro(List<Casa> casas) {
        this.casas = casas;
    }

    static Tabuleiro criarPadrao() {
    	
    	// struct <nome, vector<posicoes>, tipo, valor
    	
        List<Casa> s = new ArrayList<Casa>();

        s.add(new CasaEspecial("Ponto de Partida", CasaEspecial.Tipo.INICIO));            // 0
        s.add(Propriedade.criarBasica("Leblon", 100));                                    // 1
        s.add(new CasaEspecial("Sorte/Revés", CasaEspecial.Tipo.SORTE_REVES));            // 2
        s.add(Propriedade.criarBasica("Av. Presidente Vargas", 60));                      // 3
        s.add(Propriedade.criarBasica("Av. Nossa S. de Copacabana", 60));                 // 4
        s.add(Companhia.criar("Companhia Ferroviária", 200));                             // 5
        s.add(Propriedade.criarBasica("Av. Brigadero Faria Lima", 240));                  // 6
        s.add(Companhia.criar("Companhia de Viação", 200));                               // 7
        s.add(Propriedade.criarBasica("Av. Rebouças", 220));                              // 8
        s.add(Propriedade.criarBasica("Av. 9 de Julho", 220));                            // 9
        s.add(new CasaEspecial("Prisão", CasaEspecial.Tipo.PRISAO));                      // 10 	
        s.add(Propriedade.criarBasica("Av. Europa", 200));                                // 11
        s.add(new CasaEspecial("Sorte/Revés", CasaEspecial.Tipo.SORTE_REVES));            // 12
        s.add(Propriedade.criarBasica("Rua Augusta", 180));                               // 13
        s.add(Propriedade.criarBasica("Av. Pacaembú", 180));                              // 14
        s.add(Companhia.criar("Companhia de Táxi", 150));                                 // 15
        s.add(new CasaEspecial("Sorte/Revés", CasaEspecial.Tipo.SORTE_REVES));            // 16
        s.add(Propriedade.criarBasica("Interlagos", 350));                                // 17
        s.add(new CasaEspecial("Lucros", CasaEspecial.Tipo.LUCROS));                      // 18
        s.add(Propriedade.criarBasica("Morumbi", 400));                                   // 19
        s.add(new CasaEspecial("Parada Livre", CasaEspecial.Tipo.PARADA_LIVRE));          // 20
        s.add(Propriedade.criarBasica("Flamengo", 120));                                  // 21
        s.add(new CasaEspecial("Sorte/Revés", CasaEspecial.Tipo.SORTE_REVES));            // 22
        s.add(Propriedade.criarBasica("Botafogo", 100));                                  // 23
        s.add(new CasaEspecial("Dividendos", CasaEspecial.Tipo.DIVIDENDOS));              // 24
        s.add(Companhia.criar("Companhia de Navegação", 150));                            // 25
        s.add(Propriedade.criarBasica("Av. Brasil", 160));                                // 26
        s.add(new CasaEspecial("Sorte/Revés", CasaEspecial.Tipo.SORTE_REVES));            // 27
        s.add(Propriedade.criarBasica("Av. Paulista", 140));                              // 28
        s.add(Propriedade.criarBasica("Jardim Europa", 140));                             // 29
        s.add(new CasaEspecial("Vá para a Prisão", CasaEspecial.Tipo.VA_PARA_PRISAO));    // 30
        s.add(Propriedade.criarBasica("Copacabana", 260));                                // 31
        s.add(Companhia.criar("Companhia de Aviação", 200));                              // 32
        s.add(Propriedade.criarBasica("Av. Vieira Souto", 320));                          // 33
        s.add(Propriedade.criarBasica("Av. Atlântica", 300));                             // 34
        s.add(Companhia.criar("Companhia de Táxi Aéreo", 200));                           // 35
        s.add(Propriedade.criarBasica("Ipanema", 300));                                   // 36
        s.add(new CasaEspecial("Sorte/Revés", CasaEspecial.Tipo.SORTE_REVES));            // 37
        s.add(Propriedade.criarBasica("Jardim Paulista", 280));                           // 38
        s.add(Propriedade.criarBasica("Brooklin", 260));                                  // 39

        return new Tabuleiro(s);
    }

    int tamanho() { return casas.size(); }

    public Casa obter(int indice) { return casas.get(indice); }

    int indicePrisao() {
        for (int i = 0; i < casas.size(); i++) {
            Casa c = casas.get(i);
            if (c instanceof CasaEspecial) {
                CasaEspecial e = (CasaEspecial) c;
                if (e.getTipo() == CasaEspecial.Tipo.PRISAO) return i;
            }
        }
        return 10; // fallback
    }
}
