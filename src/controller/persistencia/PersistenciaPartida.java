package controller.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import model.GameModelo;

public final class PersistenciaPartida {

    private static final String CABECALHO = "INF1636-BANCO-IMOBILIARIO";
    private static final int VERSAO_ATUAL = 3;

    private PersistenciaPartida() {}

    public static void salvar(GameModelo.Snapshot snapshot, File arquivo) throws IOException {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(arquivo, "arquivo");

        File parent = arquivo.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(arquivo.toPath(), StandardCharsets.UTF_8)) {
            writer.write("# " + CABECALHO);
            writer.newLine();
            writer.write("versao=" + snapshot.versao);
            writer.newLine();
            writer.write("indiceAtual=" + snapshot.indiceJogadorAtual);
            writer.newLine();
            writer.write("indiceInicio=" + snapshot.indiceInicioDaRodada);
            writer.newLine();
            writer.write("numeroRodada=" + snapshot.numeroDaRodada);
            writer.newLine();
            writer.write("salarioPorRodada=" + snapshot.salarioPorRodada);
            writer.newLine();
            writer.write("saldoBanco=" + snapshot.saldoBanco);
            writer.newLine();
            writer.write("inicioDeTurno=" + snapshot.inicioDeTurno);
            writer.newLine();
            writer.write("podeLancarDados=" + snapshot.podeLancarDados);
            writer.newLine();
            writer.write("idUltimaCarta=" + encodeTexto(snapshot.idUltimaCartaSorte));
            writer.newLine();
            writer.write("cartaSairDisponivel=" + snapshot.cartaSairDisponivel);
            writer.newLine();

            writer.write("jogadores.total=" + snapshot.jogadores.size());
            writer.newLine();
            for (int i = 0; i < snapshot.jogadores.size(); i++) {
                writer.write("jogador." + i + "=" + formatarJogador(snapshot.jogadores.get(i)));
                writer.newLine();
            }

            writer.write("propriedades.total=" + snapshot.propriedades.size());
            writer.newLine();
            for (int i = 0; i < snapshot.propriedades.size(); i++) {
                writer.write("propriedade." + i + "=" + formatarPropriedade(snapshot.propriedades.get(i)));
                writer.newLine();
            }

            writer.write("cartas.total=" + snapshot.baralhoCartas.size());
            writer.newLine();
            for (int i = 0; i < snapshot.baralhoCartas.size(); i++) {
                writer.write("carta." + i + "=" + formatarCarta(snapshot.baralhoCartas.get(i)));
                writer.newLine();
            }
        }
    }

    public static GameModelo.Snapshot carregar(File arquivo) throws IOException {
        Objects.requireNonNull(arquivo, "arquivo");
        if (!arquivo.exists()) {
            throw new IOException("Arquivo não encontrado: " + arquivo.getAbsolutePath());
        }

        Map<String, String> valores = new TreeMap<String, String>();
        TreeMap<Integer, String> jogadores = new TreeMap<Integer, String>();
        TreeMap<Integer, String> propriedades = new TreeMap<Integer, String>();
        TreeMap<Integer, String> cartas = new TreeMap<Integer, String>();

        try (BufferedReader reader = Files.newBufferedReader(arquivo.toPath(), StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;
                int idx = linha.indexOf('=');
                if (idx < 0) continue;
                String chave = linha.substring(0, idx).trim();
                String valor = linha.substring(idx + 1).trim();
                if (chave.startsWith("jogador.")) {
                    jogadores.put(parseIndice(chave.substring("jogador.".length())), valor);
                } else if (chave.startsWith("propriedade.")) {
                    propriedades.put(parseIndice(chave.substring("propriedade.".length())), valor);
                } else if (chave.startsWith("carta.")) {
                    cartas.put(parseIndice(chave.substring("carta.".length())), valor);
                } else {
                    valores.put(chave, valor);
                }
            }
        }

        int versao = parseInteiro(valores.getOrDefault("versao", Integer.toString(VERSAO_ATUAL)), VERSAO_ATUAL);
        int indiceAtual = parseInteiro(valores.getOrDefault("indiceAtual", "0"), 0);
        int indiceInicio = parseInteiro(valores.getOrDefault("indiceInicio", "0"), 0);
        int numeroRodada = parseInteiro(valores.getOrDefault("numeroRodada", "1"), 1);
        int salario = parseInteiro(valores.getOrDefault("salarioPorRodada", "0"), 0);
        int saldoBanco = parseInteiro(valores.getOrDefault("saldoBanco", "0"), 0);
        boolean inicioDeTurno = Boolean.parseBoolean(valores.getOrDefault("inicioDeTurno", "true"));
        boolean cartaSairDisponivel = Boolean.parseBoolean(valores.getOrDefault("cartaSairDisponivel", "true"));
        String ultimaCarta = decodeTexto(valores.getOrDefault("idUltimaCarta", ""));
        boolean podeLancarDados = Boolean.parseBoolean(valores.getOrDefault("podeLancarDados", "true"));

        List<GameModelo.Snapshot.JogadorEstado> listaJogadores = new ArrayList<GameModelo.Snapshot.JogadorEstado>();
        int totalJogadores = parseInteiro(valores.getOrDefault("jogadores.total", Integer.toString(jogadores.size())), jogadores.size());
        for (int i = 0; i < totalJogadores; i++) {
            String dado = jogadores.get(i);
            if (dado == null) throw new IOException("Entrada de jogador ausente no índice " + i);
            String[] partes = dado.split("\\|", -1);
            if (partes.length < 7) throw new IOException("Entrada de jogador inválida: " + dado);
            listaJogadores.add(new GameModelo.Snapshot.JogadorEstado(
                    decodeTexto(partes[0]),
                    parseInteiro(partes[1], 0),
                    parseInteiro(partes[2], 0),
                    Boolean.parseBoolean(partes[3]),
                    parseInteiro(partes[4], 0),
                    parseInteiro(partes[5], 0),
                    Boolean.parseBoolean(partes[6])
            ));
        }

        List<GameModelo.Snapshot.PropriedadeEstado> listaPropriedades = new ArrayList<GameModelo.Snapshot.PropriedadeEstado>();
        int totalProps = parseInteiro(valores.getOrDefault("propriedades.total", Integer.toString(propriedades.size())), propriedades.size());
        for (int i = 0; i < totalProps; i++) {
            String dado = propriedades.get(i);
            if (dado == null) throw new IOException("Entrada de propriedade ausente no índice " + i);
            String[] partes = dado.split("\\|", -1);
            if (partes.length < 5) throw new IOException("Entrada de propriedade inválida: " + dado);
            Integer proprietario = parseInteiro(partes[4], -1);
            if (proprietario != null && proprietario < 0) proprietario = null;
            boolean construcaoLiberada = true;
            if (partes.length >= 6) {
                construcaoLiberada = Boolean.parseBoolean(partes[5]);
            }
            listaPropriedades.add(new GameModelo.Snapshot.PropriedadeEstado(
                    parseInteiro(partes[0], 0),
                    decodeTexto(partes[1]),
                    parseInteiro(partes[2], 0),
                    Boolean.parseBoolean(partes[3]),
                    proprietario,
                    construcaoLiberada
            ));
        }

        List<GameModelo.Snapshot.CartaEstado> listaCartas = new ArrayList<GameModelo.Snapshot.CartaEstado>();
        int totalCartas = parseInteiro(valores.getOrDefault("cartas.total", Integer.toString(cartas.size())), cartas.size());
        for (int i = 0; i < totalCartas; i++) {
            String dado = cartas.get(i);
            if (dado == null) throw new IOException("Entrada de carta ausente no índice " + i);
            String[] partes = dado.split("\\|", -1);
            if (partes.length < 3) throw new IOException("Entrada de carta inválida: " + dado);
            listaCartas.add(new GameModelo.Snapshot.CartaEstado(
                    decodeTexto(partes[0]),
                    partes[1],
                    parseInteiro(partes[2], 0)
            ));
        }

        return new GameModelo.Snapshot(
                versao,
                listaJogadores,
                listaPropriedades,
                listaCartas,
                cartaSairDisponivel,
                indiceAtual,
                indiceInicio,
                numeroRodada,
                salario,
                saldoBanco,
                ultimaCarta,
                inicioDeTurno,
                podeLancarDados
        );
    }

    private static String formatarJogador(GameModelo.Snapshot.JogadorEstado jogador) {
        return join("|",
                encodeTexto(jogador.nome),
                Integer.toString(jogador.saldo),
                Integer.toString(jogador.posicao),
                Boolean.toString(jogador.naPrisao),
                Integer.toString(jogador.turnosNaPrisao),
                Integer.toString(jogador.cartasSaidaDaPrisao),
                Boolean.toString(jogador.ativo));
    }

    private static String formatarPropriedade(GameModelo.Snapshot.PropriedadeEstado prop) {
        int proprietario = (prop.proprietario == null ? -1 : prop.proprietario.intValue());
        return join("|",
                Integer.toString(prop.indice),
                encodeTexto(prop.nome),
                Integer.toString(prop.casas),
                Boolean.toString(prop.hotel),
                Integer.toString(proprietario),
                Boolean.toString(prop.construcaoLiberada));
    }

    private static String formatarCarta(GameModelo.Snapshot.CartaEstado carta) {
        return join("|",
                encodeTexto(carta.idImagem),
                carta.tipo,
                Integer.toString(carta.valor));
    }

    private static String join(String separador, String... valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) sb.append(separador);
            sb.append(valores[i]);
        }
        return sb.toString();
    }

    private static int parseIndice(String num) {
        return Integer.parseInt(num.trim());
    }

    private static Integer parseInteiro(String valor, int padrao) {
        if (valor == null) return padrao;
        try {
            return Integer.valueOf(valor.trim());
        } catch (Exception e) {
            return padrao;
        }
    }

    private static String encodeTexto(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeTexto(String token) throws IOException {
        if (token == null || token.isEmpty()) return "";
        try {
            return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Valor inválido no arquivo de salvamento.", ex);
        }
    }
}
