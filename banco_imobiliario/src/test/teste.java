package test;

import java.util.Arrays;
import model.Jogo;

public class teste {
    public static void main(String[] args) {
        Jogo jogo = new Jogo(Arrays.asList("Luiza", "Carlos"));
        int[] dados = jogo.lancarDados();
        System.out.println("Dados: " + dados[0] + ", " + dados[1]);
        jogo.deslocarPiao(dados);
    }
}
