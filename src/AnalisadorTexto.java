import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;
import java.awt.*;

public class AnalisadorTexto {

    static class PainelGrafico extends JPanel {
        java.util.List<Resultado> resultados;
        Color[] cores = {new Color(0x4472C4), new Color(0xED7D31), new Color(0x70AD47)};

        PainelGrafico(java.util.List<Resultado> resultados) {
            this.resultados = resultados;
            setPreferredSize(new Dimension(1200, 700));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (resultados.isEmpty()) return;

            String[] arquivos = resultados.stream().map(r -> r.arquivo).distinct().toArray(String[]::new);
            String[] algos = {"SerialCPU", "ParallelCPU", "ParallelGPU"};
            int nArquivos = arquivos.length;
            int nAlgos = algos.length;
            int margemEsq = 120, margemSup = 80, margemDir = 60, margemInf = 100;
            int larg = getWidth() - margemEsq - margemDir;
            int alt = getHeight() - margemSup - margemInf;
            long maxTempo = Math.max(resultados.stream().mapToLong(r -> r.tempoMs).max().orElse(1), 1);
            int grupoLarg = larg / nArquivos;
            int barraLarg = Math.min(grupoLarg / (nAlgos + 1), 60);
            int espaco = Math.max((grupoLarg - barraLarg * nAlgos) / (nAlgos + 1), 5);

            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("Comparacao de Desempenho", margemEsq, 40);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("Tempo (ms)", 15, margemSup + alt / 2);
            g2.drawLine(margemEsq, margemSup, margemEsq, margemSup + alt);
            g2.drawLine(margemEsq, margemSup + alt, margemEsq + larg, margemSup + alt);

            for (int i = 0; i <= 10; i++) {
                long val = maxTempo * i / 10;
                int y = margemSup + alt - (int)(alt * i / 10.0);
                g2.drawLine(margemEsq - 5, y, margemEsq, y);
                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(String.format("%d", val), margemEsq - 8 - fm.stringWidth(String.format("%d", val)), y + 4);
            }

            for (int fi = 0; fi < nArquivos; fi++) {
                String arquivo = arquivos[fi];
                int grupoX = margemEsq + fi * grupoLarg + espaco;
                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                String nomeCurto = arquivo.replace(".txt", "").replaceAll("-\\d+", "");
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(nomeCurto);
                g2.drawString(nomeCurto, grupoX + (nAlgos * barraLarg + (nAlgos - 1) * espaco) / 2 - textW / 2, margemSup + alt + 20);

                for (int ai = 0; ai < nAlgos; ai++) {
                    String algo = algos[ai];
                    Optional<Resultado> optRes = resultados.stream()
                        .filter(r -> r.arquivo.equals(arquivo) && r.algoritmo.equals(algo)).findFirst();
                    if (!optRes.isPresent()) continue;

                    Resultado res = optRes.get();
                    int barX = grupoX + ai * (barraLarg + espaco);
                    int barH = Math.max((int)(alt * res.tempoMs / (double)maxTempo), 5);
                    int barY = margemSup + alt - barH;
                    g2.setColor(cores[ai % cores.length]);
                    g2.fillRect(barX, barY, barraLarg, barH);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(barX, barY, barraLarg, barH);
                    g2.setFont(new Font("Arial", Font.BOLD, 11));
                    String labelTempo = res.tempoMs + "ms";
                    fm = g2.getFontMetrics();
                    textW = fm.stringWidth(labelTempo);
                    g2.setColor(Color.BLACK);
                    g2.drawString(labelTempo, barX + barraLarg / 2 - textW / 2, barY - 5);
                    if (res.contagem >= 0) {
                        g2.setFont(new Font("Arial", Font.PLAIN, 10));
                        String labelCont = res.contagem + "";
                        fm = g2.getFontMetrics();
                        textW = fm.stringWidth(labelCont);
                        g2.setColor(Color.WHITE);
                        g2.drawString(labelCont, barX + barraLarg / 2 - textW / 2, barY + 15);
                        g2.setColor(Color.BLACK);
                    }
                }
            }

            int legX = margemEsq;
            int legY = margemSup - 30;
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int ai = 0; ai < nAlgos; ai++) {
                int lx = legX + ai * 100;
                g2.setColor(cores[ai % cores.length]);
                g2.fillRect(lx, legY, 15, 15);
                g2.setColor(Color.BLACK);
                g2.drawRect(lx, legY, 15, 15);
                g2.drawString(algos[ai], lx + 20, legY + 13);
            }
        }
    }

    static java.util.List<Resultado> calcularMedias(java.util.List<Resultado> resultados) {
        Map<String, java.util.List<Resultado>> grupos = resultados.stream()
            .collect(Collectors.groupingBy(r -> r.algoritmo + "|" + r.arquivo + "|" + r.numThreads));
        java.util.List<Resultado> medias = new ArrayList<>();
        for (java.util.List<Resultado> grupo : grupos.values()) {
            Resultado r0 = grupo.get(0);
            long tempoMedio = (long) grupo.stream().mapToLong(r -> r.tempoMs).average().orElse(0);
            medias.add(new Resultado(r0.algoritmo, r0.arquivo, r0.palavra, r0.contagem, tempoMedio, r0.numThreads));
        }
        return medias;
    }

    public static void main(String[] args) throws Exception {
        String pastaAmostras = "Amostra";
        String palavraAlvo = args.length > 0 ? args[0].toLowerCase() : "the";
        int NUM_AMOSTRAS = 3;
        java.util.List<Resultado> todosResultados = new ArrayList<>();

        java.util.List<String> arquivos = new ArrayList<>();
        Files.list(Paths.get(pastaAmostras))
            .filter(p -> p.toString().endsWith(".txt"))
            .sorted().forEach(p -> arquivos.add(p.toString()));

        System.out.println("=== ANALISE DE DESEMPENHO ===");
        System.out.println("Palavra: \"" + palavraAlvo + "\"\n");

        for (String caminho : arquivos) {
            String nomeArquivo = Paths.get(caminho).getFileName().toString();
            System.out.println("--- " + nomeArquivo + " ---");

            for (int s = 0; s < NUM_AMOSTRAS; s++) {
                Resultado r = SerialCPU.contar(caminho, palavraAlvo);
                todosResultados.add(r);
                System.out.println("  SerialCPU [" + (s+1) + "]: " + r.contagem + " ocorr. em " + r.tempoMs + " ms");
            }

            for (int s = 0; s < NUM_AMOSTRAS; s++) {
                Resultado r = ParallelCPU.contar(caminho, palavraAlvo, 2);
                todosResultados.add(r);
                System.out.println("  ParallelCPU[2] [" + (s+1) + "]: " + r.contagem + " ocorr. em " + r.tempoMs + " ms");
            }

            for (int s = 0; s < NUM_AMOSTRAS; s++) {
                Resultado r = ParallelCPU.contar(caminho, palavraAlvo, 4);
                todosResultados.add(r);
                System.out.println("  ParallelCPU[4] [" + (s+1) + "]: " + r.contagem + " ocorr. em " + r.tempoMs + " ms");
            }

            for (int s = 0; s < NUM_AMOSTRAS; s++) {
                Resultado r = ParallelCPU.contar(caminho, palavraAlvo, 8);
                todosResultados.add(r);
                System.out.println("  ParallelCPU[8] [" + (s+1) + "]: " + r.contagem + " ocorr. em " + r.tempoMs + " ms");
            }

            for (int s = 0; s < NUM_AMOSTRAS; s++) {
                Resultado r = null;
                try {
                    r = ParallelGPU.contar(caminho, palavraAlvo);
                } catch (Throwable e) {
                    System.err.println("  Erro GPU: " + e.getMessage());
                    r = new Resultado("ParallelGPU(ERRO)", nomeArquivo, palavraAlvo, -1, 0, 0);
                }
                todosResultados.add(r);
                String msg = (r.contagem >= 0) ?
                    r.contagem + " ocorr. em " + r.tempoMs + " ms" :
                    "ERRO - OpenCL indisponivel";
                System.out.println("  ParallelGPU [" + (s+1) + "]: " + msg);
            }
            System.out.println();
        }

        // Salvar CSV
        try (PrintWriter pw = new PrintWriter(new FileWriter("resultados.csv"))) {
            pw.println("Algoritmo,Arquivo,Palavra,Contagem,TempoMs,NumThreads");
            for (Resultado r : todosResultados) {
                pw.println(r.algoritmo + "," + r.arquivo + "," + r.palavra + "," +
                    r.contagem + "," + r.tempoMs + "," + r.numThreads);
            }
        }
        System.out.println("CSV salvo em: resultados.csv");

        // Salvar CSV com medias
        java.util.List<Resultado> medias = calcularMedias(todosResultados);
        try (PrintWriter pw = new PrintWriter(new FileWriter("resultados_medias.csv"))) {
            pw.println("Algoritmo,Arquivo,Palavra,Contagem,TempoMedioMs,NumThreads");
            for (Resultado r : medias) {
                pw.println(r.algoritmo + "," + r.arquivo + "," + r.palavra + "," +
                    r.contagem + "," + r.tempoMs + "," + r.numThreads);
            }
        }
        System.out.println("CSV de medias salvo em: resultados_medias.csv");

        // Mostrar grafico com as medias
        String[] algosGrafico = {"SerialCPU", "ParallelCPU", "ParallelGPU"};
        java.util.List<Resultado> mediasGrafico = new ArrayList<>();
        for (String a : algosGrafico) {
            for (String arq : arquivos.stream().map(p -> Paths.get(p).getFileName().toString()).toArray(String[]::new)) {
                String finalArq = arq;
                medias.stream().filter(r -> r.arquivo.equals(finalArq) && r.algoritmo.equals(a))
                    .findFirst().ifPresent(mediasGrafico::add);
            }
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Analise de Desempenho");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new PainelGrafico(mediasGrafico));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
