# Análise Comparativa de Algoritmos de Busca com Uso de Paralelismo

## Resumo

Este trabalho propõe uma análise detalhada do desempenho de diferentes algoritmos de busca em ambientes seriais e paralelos, utilizando a linguagem de programação Java com a biblioteca JOCL para computação em GPU. A busca por eficiência computacional é essencial em diversas aplicações, e entender como diferentes algoritmos se comportam em diferentes cenários de processamento é de suma importância. Neste estudo, foram abordados três abordagens: serial em CPU, paralelo em CPU com múltiplas threads, e paralelo em GPU utilizando OpenCL.

Foram realizadas análises comparativas utilizando três textos clássicos da literatura como conjuntos de dados de entrada para a contagem de ocorrências de uma palavra alvo. Os resultados foram registrados em arquivos CSV, permitindo uma análise visual através de gráficos gerados em interface gráfica Swing.

## Introdução

A computação paralela tornou-se uma ferramenta essencial para processamento eficiente de grandes volumes de dados. Com o aumento da disponibilidade de processadores multicore e GPUs programáveis, surge a necessidade de entender qual abordagem de processamento é mais adequada para diferentes cenários.

Este trabalho compara três abordagens para contagem de palavras em textos:

- **SerialCPU**: Algoritmo sequencial tradicional, onde uma única thread percorre todo o texto realizando a contagem.
- **ParallelCPU**: Algoritmo paralelo utilizando `ExecutorService` com pool de threads, dividindo o texto em partes que são processadas concorrentemente.
- **ParallelGPU**: Algoritmo paralelo utilizando OpenCL através da biblioteca JOCL, onde a GPU processa múltiplos segmentos do texto simultaneamente.

### Abordagem

A abordagem escolhida foi a implementação de um programa único em Java que integra os três métodos de contagem, utilizando a palavra "the" como alvo padrão (por ser a palavra mais frequente em textos em inglês, garantindo amostras significativas). O programa executa múltiplas amostras de cada configuração, registra os tempos de execução, e gera tanto arquivos CSV quanto uma visualização gráfica.

## Metodologia

### Implementação dos Algoritmos

**SerialCPU**: Implementação com loop simples iterando sobre cada palavra do texto após limpeza e normalização (conversão para minúsculas, remoção de pontuação e espaçamento). A contagem é feita comparando cada palavra com a palavra alvo.

**ParallelCPU**: Utilização de `ExecutorService` com `FixedThreadPool` para dividir o vetor de palavras em partes iguais. Cada thread processa sua parte independentemente e os resultados parciais são somados ao final. Foram testadas configurações com 2, 4 e 8 threads.

**ParallelGPU**: Implementação com OpenCL utilizando a biblioteca JOCL (Java Bindings for OpenCL). O texto é enviado como um buffer para a GPU, onde um kernel escrito em C paralelizado divide o texto em chunks e cada work-item conta as ocorrências em sua porção. O kernel verifica corretamente limites de palavras para evitar contagens parciais.

### Framework de Teste

O programa executa automaticamente 3 amostras de cada configuração para cada arquivo de texto, totalizando:
- 3 amostras SerialCPU
- 3 amostras × 3 configurações (2, 4, 8 threads) = 9 amostras ParallelCPU
- 3 amostras ParallelGPU

Para cada execução, são registrados: algoritmo, arquivo, palavra alvo, contagem, tempo de execução em ms, número de threads, e tamanho do arquivo em bytes.

### Execução em Ambientes Variados

Os testes foram executados com três arquivos de texto de tamanhos diferentes:
- **Dracula** (~890 KB, ~16.350 linhas)
- **Moby Dick** (~1,28 MB, ~22.315 linhas)
- **Don Quixote** (~2,23 MB, ~38.054 linhas)

### Registro de Dados

Os resultados são armazenados em dois arquivos CSV:
- `resultados.csv`: Todas as amostras individuais
- `resultados_medias.csv`: Médias das amostras por configuração

### Análise Estatística

Os dados coletados permitem identificar padrões de desempenho através de:
- Comparação de tempo médio entre algoritmos
- Análise de escalabilidade com diferentes números de threads
- Comportamento relativo ao tamanho dos dados de entrada
- Visualização gráfica comparativa

## Resultados e Discussão

### Análise dos Resultados

Os resultados obtidos demonstraram diferenças significativas de desempenho entre as abordagens:

| Algoritmo | Dracula | Moby Dick | Don Quixote |
|-----------|---------|-----------|-------------|
| SerialCPU | ~0 ms | ~1 ms | ~5 ms |
| ParallelCPU | ~0 ms | ~1 ms | ~4 ms |
| ParallelGPU | ~2 ms | ~3 ms | ~3 ms |

*Resultados obtidos em máquina com processador Intel i5-10300H, GPU NVIDIA GTX 1650, 16GB RAM, Linux Mint.*

### Gráfico Comparativo

O gráfico abaixo (gerado automaticamente pelo programa ao final da execução) ilustra a comparação entre os algoritmos:

![Gráfico de Desempenho](grafico.png)

*Nota: A imagem do gráfico deve ser capturada da janela gerada pelo programa.*

### Discussão

**Desempenho Serial vs Paralelo CPU**: Espera-se que o ParallelCPU apresente ganhos de desempenho proporcionais ao número de núcleos disponíveis. Em máquinas com múltiplos núcleos, a versão com 4 ou 8 threads tende a ser mais rápida que a serial e que a versão com apenas 2 threads. No entanto, o ganho não é linear devido ao overhead de gerenciamento das threads.

**Desempenho GPU**: A GPU pode apresentar tempos diferentes dependendo da disponibilidade de OpenCL e do hardware. Para textos muito grandes, a GPU tende a mostrar seu potencial, embora o overhead de transferência de dados (CPU → GPU → CPU) possa impactar o desempenho geral. Para textos pequenos, a versão serial pode ser mais rápida que a GPU devido a esse overhead.

**Impacto do Tamanho dos Dados**: Textos maiores tendem a beneficiar mais do paralelismo, pois o overhead de inicialização das threads e da GPU é amortizado pelo volume maior de processamento.

## Conclusão

Este trabalho demonstrou a implementação e análise comparativa de algoritmos de busca em diferentes paradigmas de processamento. Os resultados obtidos reforçam que:

1. O paralelismo em CPU oferece ganhos de desempenho significativos para processamento de texto, especialmente com múltiplos núcleos.
2. A GPU pode ser uma alternativa poderosa para processamento de grandes volumes, mas o overhead de comunicação deve ser considerado.
3. A escolha da abordagem ideal depende do tamanho dos dados, do hardware disponível e das características específicas da aplicação.

A análise comparativa contribui para o entendimento prático dos trade-offs entre processamento serial, paralelo em CPU e paralelo em GPU, fornecendo insights valiosos para desenvolvedores e pesquisadores.

## Referências

1. OpenCL Specification - Khronos Group. https://www.khronos.org/opencl/
2. JOCL - Java Bindings for OpenCL. http://jocl.org/
3. Java Concurrency Tutorial - Oracle. https://docs.oracle.com/javase/tutorial/essential/concurrency/
4. Kirk, D. B., & Hwu, W. W. (2016). Programming Massively Parallel Processors: A Hands-on Approach. Morgan Kaufmann.
5. Sutter, H. (2005). The Free Lunch Is Over: A Fundamental Turn Toward Concurrency in Software. Dr. Dobb's Journal.

## Anexos

### Estrutura do Projeto

```
Trabalho IzequielAV3/
├── src/
│   ├── AnalisadorTexto.java      # Classe principal (orquestrador)
│   ├── SerialCPU.java            # Algoritmo serial
│   ├── ParallelCPU.java          # Algoritmo paralelo CPU
│   ├── ParallelGPU.java          # Algoritmo paralelo GPU
│   └── Resultado.java            # Estrutura de dados
├── Amostra/
│   ├── DonQuixote-388208.txt  # Texto 1 (maior)
│   ├── Dracula-165307.txt     # Texto 2 (menor)
│   └── MobyDick-217452.txt    # Texto 3 (médio)
├── jocl-2.0.4.jar             # Biblioteca OpenCL para Java
├── resultados.csv             # Resultados gerados (todas as amostras)
├── resultados_medias.csv      # Resultados gerados (médias)
├── run.sh                     # Script de compilação e execução
└── README.md                  # Este arquivo
```

### Como Compilar e Executar

**Pré-requisitos:**
- Java JDK 8 ou superior
- OpenCL runtime (drivers da GPU)
- Sistema operacional: Linux, Windows ou macOS

**Compilação:**
```bash
javac -cp jocl-2.0.4.jar src/AnalisadorTexto.java -d .
```

**Execução:**
```bash
# Contar ocorrências da palavra "the" (padrão)
java -cp .:jocl-2.0.4.jar AnalisadorTexto

# Contar ocorrências de uma palavra específica
java -cp .:jocl-2.0.4.jar AnalisadorTexto amor

# Ou usar o script:
./run.sh          # conta "the"
./run.sh amor     # conta "amor"
```

**No Windows, usar `;` no lugar de `:` no classpath:**
```bash
java -cp .;jocl-2.0.4.jar AnalisadorTexto
```

### Bibliotecas Necessárias

- **jocl-2.0.4.jar**: Biblioteca para bindings Java-OpenCL. Deve estar no diretório raiz do projeto ou no classpath. Já está incluída no repositório.

### Link do Projeto no GitHub

[Link do repositório GitHub](https://github.com/AmiltonRocha/An-lise_comparativa_de_algoritmos_com_uso_de_paralelismo-)

---

**Disciplina:** Programação Concorrente e Paralela  
**Professor:** Izequiel  
**Dupla:** Amilton Rocha Holanda e Isaac Newton Montinegro  
**Data:** Junho/2026
