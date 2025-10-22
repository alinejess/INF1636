package controller;


import model.Propriedade;


public interface GameListener {
void onStateChanged(); // Redesenhar tela geral
void onPlayerChanged(); // Destaque de cor do jogador da vez
void onDiceRolled(int d1, int d2); // Atualizar imagens dos dados
void onLandedOnProperty(Propriedade p);// Exibir cartão de propriedade
}