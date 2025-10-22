package ui;

import controller.GameFacade;
import controller.GameListener;
import model.Propriedade;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame implements GameListener {
    private final GameFacade facade;
    private final BoardPanel boardPanel;
    private final InfoPanel infoPanel;

    public MainWindow(GameFacade facade) {
        super("Jogo — P1 UI (Java2D)");
        this.facade = facade;

        this.boardPanel = new BoardPanel(facade);
        this.infoPanel  = new InfoPanel(facade);

        // Conecta o combo do InfoPanel à inspeção de cartas no BoardPanel
        this.infoPanel.setOnInspectProperty(p -> {
            boardPanel.showPropertyOverlay(p); // aceita null para limpar
            repaintAll();
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1024, 650));
        setMaximumSize(new Dimension(1280, 800));
        setPreferredSize(new Dimension(1200, 760));

        add(boardPanel, BorderLayout.CENTER);
        add(infoPanel,  BorderLayout.EAST);

        setJMenuBar(createMenuBar());

        facade.addListener(this);
        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu jogo = new JMenu("Jogo");
        JMenuItem sair = new JMenuItem("Sair");
        sair.addActionListener(e -> System.exit(0));
        jogo.add(sair);
        mb.add(jogo);
        return mb;
    }

    // ---------- GameListener ----------
    @Override public void onStateChanged()                  { repaintAll(); }
    @Override public void onPlayerChanged()                  { repaintAll(); }
    @Override public void onDiceRolled(int d1, int d2)      { boardPanel.setDice(d1, d2); repaintAll(); }
    @Override public void onLandedOnProperty(Propriedade p) { boardPanel.showPropertyOverlay(p); repaintAll(); }

    private void repaintAll() {
        infoPanel.refresh();
        boardPanel.repaint();
    }
}
