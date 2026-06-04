public class Resultado {
    String algoritmo;
    String arquivo;
    String palavra;
    int contagem;
    long tempoMs;
    int numThreads;

    Resultado(String algoritmo, String arquivo, String palavra, int contagem, long tempoMs, int numThreads) {
        this.algoritmo = algoritmo;
        this.arquivo = arquivo;
        this.palavra = palavra;
        this.contagem = contagem;
        this.tempoMs = tempoMs;
        this.numThreads = numThreads;
    }
}