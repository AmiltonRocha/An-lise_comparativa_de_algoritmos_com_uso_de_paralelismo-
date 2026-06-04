import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.*;

public class ParallelCPU {

    public static Resultado contar(String caminhoArquivo, String palavra, int numThreads) throws Exception {
        String nomeArquivo = Paths.get(caminhoArquivo).getFileName().toString();
        String texto = new String(Files.readAllBytes(Paths.get(caminhoArquivo)));
        String textoLimpo = texto.toLowerCase().replaceAll("[^a-zà-ÿ ]", " ").replaceAll("\\s+", " ").trim();
        String[] palavras = textoLimpo.split(" ");
        int total = palavras.length;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> futures = new ArrayList<>();
        int palavrasPorThread = (int) Math.ceil((double) total / numThreads);

        long inicio = System.nanoTime();

        for (int t = 0; t < numThreads; t++) {
            int inicioThread = t * palavrasPorThread;
            int fimThread = Math.min(inicioThread + palavrasPorThread, total);
            String[] parte = Arrays.copyOfRange(palavras, inicioThread, fimThread);

            futures.add(executor.submit(() -> {
                int c = 0;
                for (String p : parte) {
                    if (p.equals(palavra)) c++;
                }
                return c;
            }));
        }

        int count = 0;
        for (Future<Integer> f : futures) {
            count += f.get();
        }

        long fim = System.nanoTime();
        long tempoMs = (fim - inicio) / 1_000_000;
        executor.shutdown();

        return new Resultado("ParallelCPU", nomeArquivo, palavra, count, tempoMs, numThreads);
    }
}
