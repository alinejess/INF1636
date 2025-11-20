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
        int d1 = 1 + new Random().nextInt(6);
        int d2 = 1 + new Random().nextInt(6);
        modelo.registrarLancamento(d1, d2);
        modelo.deslocarPiao(d1, d2);
    }

    public void lancarForcado(int d1, int d2) {
        if (modelo == null) return;
        if (d1 < 1 || d1 > 6 || d2 < 1 || d2 > 6) return;
        modelo.registrarLancamento(d1, d2);
        modelo.deslocarPiao(d1, d2);
    }
    
    public void aplicarProximaCartaParaJogadorDaVez() {
        if (modelo == null) return;
        modelo.aplicarSorteRevesNoJogadorDaVez();
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

    private void abrirJanelaTabuleiro(GameModelo novoModelo) {
        if (novoModelo == null) return;
        if (janelaTabuleiro != null) {
            janelaTabuleiro.setVisible(false);
            janelaTabuleiro.dispose();
        }
        this.modelo = novoModelo;
        janelaTabuleiro = new JanelaTabuleiro(this, this.modelo);
        janelaTabuleiro.mostrar();
    }
}
