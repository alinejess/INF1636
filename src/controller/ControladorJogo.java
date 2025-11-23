package controller;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.persistencia.PersistenciaPartida;
import model.GameModelo;
import view.JanelaInicial;
import view.JanelaTabuleiro;
import view.CoresJogador.CorPino;

public class ControladorJogo {

    private GameModelo modelo;
    private JanelaTabuleiro janelaTabuleiro;    

    public void iniciarNovaPartida(int quantidadeJogadores) {
        if (quantidadeJogadores < 3) quantidadeJogadores = 3;
        if (quantidadeJogadores > 6) quantidadeJogadores = 6;

        GameModelo novoModelo = new GameModelo();
        for (int i = 0; i < quantidadeJogadores; i++) {
            CorPino cor = CorPino.porIndice(i);
            String nome = "Jogador " + cor.getRotulo();
            novoModelo.adicionarJogador(nome);
        }
        novoModelo.sortearOrdemJogadores();
        abrirJanelaTabuleiro(novoModelo);
    }

    public GameModelo getModelo() { return modelo; }
    
    public void encerrarTurno() {
        if (modelo != null) modelo.encerrarTurno();
    }
    
    public void lancarAleatorio() {
        if (modelo == null) return;
        if (!modelo.podeLancarDados()) return;
        int d1 = 1 + new Random().nextInt(6);
        int d2 = 1 + new Random().nextInt(6);
        modelo.registrarLancamento(d1, d2);
        modelo.deslocarPiao(d1, d2);
    }

    public void lancarForcado(int d1, int d2) {
        if (modelo == null) return;
        if (d1 < 1 || d1 > 6 || d2 < 1 || d2 > 6) return;
        if (!modelo.podeLancarDados()) return;
        modelo.registrarLancamento(d1, d2);
        modelo.deslocarPiao(d1, d2);
    }
    
    public void aplicarProximaCartaParaJogadorDaVez() {
        if (modelo == null) return;
        modelo.aplicarSorteRevesNoJogadorDaVez();
    }

    public void comprarPropriedade(Component parent) {
        if (!validarModelo(parent)) return;
        if (!modelo.comprarPropriedade()) {
            JOptionPane.showMessageDialog(parent,
                    "Não é possível comprar esta propriedade (já tem dono, não é uma propriedade ou falta dinheiro).",
                    "Compra não realizada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void comprarCompanhia(Component parent) {
        if (!validarModelo(parent)) return;
        if (!modelo.comprarCompanhia()) {
            JOptionPane.showMessageDialog(parent,
                    "Não é possível comprar esta companhia agora.",
                    "Compra não realizada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void construirCasaOuHotel(Component parent) {
        if (!validarModelo(parent)) return;
        if (!modelo.construirCasa()) {
            JOptionPane.showMessageDialog(parent,
                    "Para construir é necessário estar em uma propriedade própria, ter saldo, ter visitado a propriedade após a compra e respeitar o limite de casas/hotel.",
                    "Construção não realizada",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void venderPropriedade(Component parent) {
        if (!validarModelo(parent)) return;
        java.util.List<String> propriedades = modelo.obterPropriedadesJogadorDaVez();
        if (propriedades.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Você não possui propriedades para vender.",
                    "Venda não realizada",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object escolha = JOptionPane.showInputDialog(parent,
                "Selecione a propriedade a vender (90% do valor):",
                "Vender propriedade",
                JOptionPane.QUESTION_MESSAGE,
                null,
                propriedades.toArray(),
                propriedades.get(0));
        if (!(escolha instanceof String)) return;
        int valor = modelo.venderPropriedadeDaVez((String) escolha);
        if (valor <= 0) {
            JOptionPane.showMessageDialog(parent,
                    "Não foi possível vender a propriedade selecionada. Aguarde o início do seu próximo turno e confirme se ela pertence a você.",
                    "Venda não realizada",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parent,
                    "Propriedade vendida. Você recebeu $" + valor + ".",
                    "Venda concluída",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void salvarPartida(Component parent) {
        if (modelo == null) {
            JOptionPane.showMessageDialog(parent, "Não há partida em andamento para salvar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!modelo.estaNoInicioDoTurno()) {
            JOptionPane.showMessageDialog(parent,
                    "O salvamento só é permitido antes do jogador iniciar a jogada atual.",
                    "Regra de Salvamento",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = criarFileChooser("Salvar partida");
        int resultado = chooser.showSaveDialog(parent);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        File arquivo = ajustarExtensaoTxt(chooser.getSelectedFile());
        try {
            PersistenciaPartida.salvar(modelo.criarSnapshot(), arquivo);
            JOptionPane.showMessageDialog(parent, "Partida salva em:\n" + arquivo.getAbsolutePath(),
                    "Salvamento concluído", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Falha ao salvar partida:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void encerrarJogo(Component parent) {
        if (modelo == null) {
            JOptionPane.showMessageDialog(parent, "Não há partida em andamento.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modelo.liquidarPatrimonio();
        registrarEstadoFinal();
        mostrarResultadoFinal(parent);
        if (janelaTabuleiro != null) {
            janelaTabuleiro.setVisible(false);
            janelaTabuleiro.dispose();
            janelaTabuleiro = null;
        }
        modelo = null;
    }

    private void mostrarResultadoFinal(Component parent) {
        if (modelo == null) return;
        java.util.List<GameModelo.RankingJogador> ranking = modelo.calcularRankingFinal();
        StringBuilder sb = new StringBuilder();
        if (!ranking.isEmpty()) {
            int topo = ranking.get(0).capital;
            java.util.List<GameModelo.RankingJogador> lideres = new java.util.ArrayList<GameModelo.RankingJogador>();
            for (GameModelo.RankingJogador r : ranking) {
                if (r.capital == topo) lideres.add(r);
            }
            if (lideres.size() == 1) {
                sb.append("Parabéns! O ").append(lideres.get(0).nome).append(" é o vencedor!\n\n");
            } else {
                sb.append("Empate! Os seguintes jogadores dividiram o primeiro lugar:\n");
                for (GameModelo.RankingJogador r : lideres) {
                    sb.append(" - ").append(r.nome).append("\n");
                }
                sb.append("\n");
            }
        }
        for (int i = 0; i < ranking.size(); i++) {
            GameModelo.RankingJogador r = ranking.get(i);
            sb.append(i + 1).append("º - ").append(r.nome)
              .append(" | Capital: $").append(r.capital)
              .append("\n");
        }
        JOptionPane.showMessageDialog(parent, sb.toString(), "Resultado Final", JOptionPane.INFORMATION_MESSAGE);
    }
    public boolean carregarPartida(Component parent) {
        JFileChooser chooser = criarFileChooser("Carregar partida");
        int resultado = chooser.showOpenDialog(parent);
        if (resultado != JFileChooser.APPROVE_OPTION) return false;

        File arquivo = chooser.getSelectedFile();
        try {
            GameModelo novoModelo = new GameModelo();
            novoModelo.restaurar(PersistenciaPartida.carregar(arquivo));
            abrirJanelaTabuleiro(novoModelo);
            JOptionPane.showMessageDialog(parent, "Partida carregada com sucesso.",
                    "Carregamento concluído", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Falha ao carregar partida:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private JFileChooser criarFileChooser(String titulo) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(titulo);
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de save (*.txt)", "txt"));
        return chooser;
    }

    private File ajustarExtensaoTxt(File arquivo) {
        if (arquivo == null) return null;
        String nome = arquivo.getName().toLowerCase();
        if (!nome.endsWith(".txt")) {
            return new File(arquivo.getParentFile(), arquivo.getName() + ".txt");
        }
        return arquivo;
    }

    private boolean validarModelo(Component parent) {
        if (modelo != null) return true;
        JOptionPane.showMessageDialog(parent,
                "Nenhuma partida ativa.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
        return false;
    }

    private void abrirJanelaTabuleiro(GameModelo novoModelo) {
        if (novoModelo == null) return;
        if (janelaTabuleiro != null) {
            janelaTabuleiro.setVisible(false);
            janelaTabuleiro.dispose();
        }
        this.modelo = novoModelo;
        janelaTabuleiro = new JanelaTabuleiro(this, this.modelo);
        janelaTabuleiro.setVisible(true);
    }

    public void registrarEstadoFinal() {
        if (modelo != null) {
            modelo.imprimirEstadoCompleto();
        }
    }
}
