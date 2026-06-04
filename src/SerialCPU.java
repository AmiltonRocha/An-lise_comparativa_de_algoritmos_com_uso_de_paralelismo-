import java.io.*;
import java.nio.file.*;

public class SerialCPU {

    public static Resultado contar(String caminhoArquivo, String palavra) throws Exception {
        String nomeArquivo = Paths.get(caminhoArquivo).getFileName().toString();
        String texto = new String(Files.readAllBytes(Paths.get(caminhoArquivo)));
        String textoLimpo = texto.toLowerCase().replaceAll("[^a-zà-ÿ ]", " ").replaceAll("\\s+", " ").trim();
        String[] palavras = textoLimpo.split(" ");

        long inicio = System.nanoTime();

        int count = 0;
        for (String p : palavras) {
            if (p.equals(palavra)) {
                count++;
            }
        }

        long fim = System.nanoTime();
        long tempoMs = (fim - inicio) / 1_000_000;

        return new Resultado("SerialCPU", nomeArquivo, palavra, count, tempoMs, 1);
    }
}
