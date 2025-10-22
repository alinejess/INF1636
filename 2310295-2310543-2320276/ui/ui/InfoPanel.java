package ui;

import controller.GameFacade;
import model.Jogador;
import model.Propriedade;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class InfoPanel extends JPanel {
    private final GameFacade facade;

    private final JLabel lblNome  = new JLabel();
    private final JLabel lblSaldo = new JLabel();
    private final JComboBox<String> comboProps = new JComboBox<>();
    private final JButton btnRolar      = new JButton("Rolar dados / Simular");
    private final JButton btnConstruir  = new JButton("Construir");
    private final JButton btnVender     = new JButton("Vender (forçado)");
    private final JButton btnEncerrar   = new JButton("Encerrar turno");

    // ---- NOVO: suporte a inspeção da propriedade via combo ----
    private final List<Propriedade> propsView = new ArrayList<>();
    private Consumer<Propriedade> onInspect;       // callback configurado pelo MainWindow
    private boolean updatingCombo = false;         // evita disparar evento durante refresh

    public InfoPanel(GameFacade facade) {
        this.facade = facade;

        setPreferredSize(new Dimension(260, 10));
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 4, 4, 4);

        // Cabeçalho
        JLabel titulo = new JLabel("Informações");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridy = 0; add(titulo, c);

        // Linhas de info
        c.gridy = 1; add(lblNome, c);
        c.gridy = 2; add(lblSaldo, c);

        // Propriedades
        c.gridy = 3; add(new JLabel("Propriedades:"), c);
        c.gridy = 4; add(comboProps, c);

        // Botões
        c.gridy = 5; add(btnRolar, c);
        c.gridy = 6; add(btnConstruir, c);
        c.gridy = 7; add(btnVender, c);
        c.gridy = 8; add(btnEncerrar, c);

        // Handlers (ações do turno)
        btnRolar.addActionListener(e -> facade.rolarDados());
        btnConstruir.addActionListener(e -> facade.construirNaAtual());
        btnVender.addActionListener(e -> facade.venderForcado());
        btnEncerrar.addActionListener(e -> facade.encerrarTurno());

        // Handler do combo: inspeciona a propriedade escolhida
        comboProps.addActionListener(e -> {
            if (updatingCombo) return; // não dispara durante refresh()
            int idx = comboProps.getSelectedIndex();
            Propriedade p = (idx >= 0 && idx < propsView.size()) ? propsView.get(idx) : null;
            if (onInspect != null) onInspect.accept(p);
        });

        refresh();
    }

    /** MainWindow chama isto para receber o "evento de inspeção" do combo. */
    public void setOnInspectProperty(Consumer<Propriedade> callback) {
        this.onInspect = callback;
    }

    public void refresh() {
        Jogador j = facade.getJogadorAtual();
        if (j == null) return;

        Color col = facade.getColorFor(j.getNome());
        lblNome.setText("Jogador: " + j.getNome());
        lblNome.setForeground(col.darker());
        lblSaldo.setText("Dinheiro: $ " + j.getSaldo());

        updatingCombo = true;
        try {
            comboProps.removeAllItems();
            propsView.clear();

            List<Propriedade> props = j.getPropriedades();
            if (props != null && !props.isEmpty()) {
                for (Propriedade p : props) {
                    propsView.add(p);
                    comboProps.addItem(p.getNome());
                }
                comboProps.setSelectedIndex(0);
            } else {
                comboProps.addItem("(sem propriedades)");
                comboProps.setSelectedIndex(0);
            }
        } finally {
            updatingCombo = false;
        }
    }
}
