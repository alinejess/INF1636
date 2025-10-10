package model; // declara que esta classe pertence ao pacote 'model'

import java.util.*; // importa utilitários (List, ArrayList, Collections, Random)

public class Jogo { // classe pública: ponto de entrada/API do Model
    private final List<Jogador> jogadores; // lista de jogadores da partida (imutável na referência)
    private final Tabuleiro tabuleiro; // referência ao tabuleiro do jogo (imutável na referência)
    private final Banco banco; // banco do jogo (armazena dinheiro e propriedades sem dono)
    private int jogadorAtual; // índice do jogador da vez dentro da lista 'jogadores'
    private int doublesSeguidos; // contador de duplas seguidas 

    public Jogo(List<String> nomesJogadores) { // construtor recebe nomes e inicializa a partida
        tabuleiro = new Tabuleiro(); // cria o tabuleiro com as casas definidas
        banco = new Banco(); // cria o banco
        jogadores = new ArrayList<>(); // instancia a lista de jogadores
        for (String nome : nomesJogadores) { // percorre os nomes recebidos
            jogadores.add(new Jogador(nome)); // cria um jogador para cada nome e adiciona à lista
        }
        jogadorAtual = 0; // primeiro jogador inicia (índice 0)
        doublesSeguidos = 0; // zera contador de duplas
    }

    /** Lança 2 dados e retorna seus valores (1..6). */
    public int[] lancarDados() { // método da API para lançar os dados
        Random r = new Random(); // usa gerador pseudo-aleatório da JDK
        int d1 = 1 + r.nextInt(6); // sorteia primeiro dado (1 a 6)
        int d2 = 1 + r.nextInt(6); // sorteia segundo dado (1 a 6)
        return new int[]{d1, d2}; // retorna os dois valores em um array
    }
    
    public void enviarParaPrisao(Jogador j) { // torna público para ser chamado por VaParaPrisao
        j.setPosicao(tabuleiro.getIndicePrisao()); // move o jogador para o índice da prisão
        j.setPreso(true); // marca o estado de preso
        System.out.println(j.getNome() + ", você foi preso!"); // log de aviso para jogador
    }

    /** Desloca o pião do jogador da vez e executa a ação da casa. */
    public boolean deslocarPiao(int[] dados) { // recebe os valores dos dados lançados
        Jogador j = getJogadorAtual(); // obtém referência do jogador atual

        // Tratamento de prisão (saída por dupla ou cartão)
        if (j.isPreso()) { // se o jogador estiver preso
            boolean saiu = false; // marca se o jogador saiu da prisão
            if (dados[0] == dados[1]) { j.sairDaPrisao(); saiu = true; } // se tirar uma dupla nos dados sai da prisão
            else if (j.isTemCartaSaidaPrisao()) { j.usarCartaSaidaPrisao(); saiu = true; } // se tiver carta sai da prisão
            if (!saiu) return true; // se não saiu, segue preso e perde a vez
        }

        // 3 duplas seguidas => prisão (opcional de acordo com as regras)
        if (dados[0] == dados[1]) { // se tirou dupla na jogada atual
            doublesSeguidos++; // incrmenta +1 no contador de duplas seguidas
            if (doublesSeguidos == 3) { // se o contador chegou a 3
                enviarParaPrisao(j); // manda o jogador para prisão
                doublesSeguidos = 0; // reseta o contador
                return true; // finaliza a jogada
            }
        } else { // se não foi dupla
            doublesSeguidos = 0; // reseta o contador de duplas
        }

        int passos = dados[0] + dados[1]; // soma dos dados define os passos a andar
        int posInicial = j.getPosicao(); // posição antes do movimento
        int total = tabuleiro.getTotalCasas(); // número total de casas do tabuleiro
        int posFinal = (posInicial + passos) % total; // posição final usando aritmética modular

        // +$200 ao passar pelo início
        if (posInicial + passos >= total) j.receber(200);

        j.setPosicao(posFinal); // atualiza a posição do jogador de acorrdo com a jogada
        Casa c = tabuleiro.getCasa(posFinal); // obtém a casa atingida
        c.acao(j, this); // executa a ação da casa (polimórfico)
        return true; // indica que a jogada pôde ser completada
    }

    /** Tenta comprar a propriedade da casa atual (polimórfico, sem instanceof). */
    public boolean comprarPropriedadeAtual() { // API para o Controller acionar compra (ainda não foi implementado)
        Jogador j = getJogadorAtual(); // pega o jogador da vez
        Casa c = tabuleiro.getCasa(j.getPosicao()); // casa atual do jogador
        // tanto faz checar podeSerCompradaPor() antes; comprar() retorna false se não puder
        return c.comprar(j, banco);
    }

    /** Tenta construir (1 imóvel por vez) na casa atual (polimórfico). */
    public boolean construirNaPropriedadeAtual() { //  chama a API para construir a casa se puder
        Jogador j = getJogadorAtual(); // jogador da vez
        Casa c = tabuleiro.getCasa(j.getPosicao()); // casa atual do jogador
        return c.construir(j, banco); // manda para a casa
    }

    public void proximoJogador() { // avanaça para o próximo jogador
        if (jogadores.isEmpty()) return; // proteção para caso a lista esteja vazia
        do { // pula jogadores falids
            jogadorAtual = (jogadorAtual + 1) % jogadores.size(); // avança circularmente
        } while (!jogadores.get(jogadorAtual).isAtivo()); // repete até achar um jogador ativo
    }

    public Jogador getJogadorAtual() { return jogadores.get(jogadorAtual); } // mostra o jogador da vez
    public List<Jogador> getJogadores() { return Collections.unmodifiableList(jogadores); } // lista somente leitura
    public Banco getBanco() { return banco; } // leitura do banco para operações internas
    public Tabuleiro getTabuleiro() { return tabuleiro; } // leitura do tabuleiro
}
