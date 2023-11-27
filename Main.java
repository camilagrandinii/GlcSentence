/**
 * Codificação do Problema de Pertencimento de Sentença à Gramática
 * Fundamentos Teóricos da Computação
 * @author - Camila Lacerda Grandini & Milena Soares Barreira
 * 2023 - 2o. Semestre
*/

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Main {
    public static void main(String[] args) throws IOException {
        double seconds = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        GrammarExtractor grammarExtractor = new GrammarExtractor();
        CYK cykExecutor = new CYK();

        String nome_arquivo  = reader.readLine();
        nome_arquivo+=".txt";

        BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(nome_arquivo), StandardCharsets.UTF_8));

        Grammar grammar = new Grammar();

        String linha = bf.readLine();
        grammar = grammarExtractor.ExtractGrammar(linha, grammar);

        grammar.PrintGrammar();

        GrammarConversor grammarConversor = new GrammarConversor(grammar);

        System.out.println("\nQual algoritmo você deseja executar?");
        System.out.println("1) CYK padrão");
        System.out.println("2) CYK otimizado");

        int escolha = Integer.parseInt(reader.readLine());

        long startTime = System.nanoTime();

        String sufixoArquivo;
        if (escolha == 1) {
            grammar = grammarConversor.ToFncGrammar(grammar);
            grammar.PrintGrammar();
            sufixoArquivo = "-cyk-padrao";
        } else {
            grammar = grammarConversor.To2NfGrammar(grammar);
            grammar.PrintGrammar();
            sufixoArquivo = "-cyk-modificado";
        }

        while ((linha = bf.readLine()) != null) {
            boolean cykResult;

            if (escolha == 1) {
                cykResult = cykExecutor.CykCnf(grammar, linha);
            } else {
                List<String> nullableVariables = grammarConversor.findNullableVariables(grammar.rules);
                Map<String, Set<String>> unitRelation = grammar.GetUnitRelation(nullableVariables);
                Set<String> allSymbols = grammar.ComputeV(grammar);
                Map<String, Set<String>> inverseUnitGraph = grammar.GetInverseUnitGraph(unitRelation, allSymbols);
                Map<String, Set<String>> transitiveClosure = grammar.computeTransitiveClosure(inverseUnitGraph, allSymbols);
                
                cykResult = cykExecutor.Cyk2Nf(grammar, allSymbols, transitiveClosure, linha);
            }
            
            System.out.println("A string '" + linha + "' " + (cykResult ? "" : "nao ") + "pertence a gramatica.");

            // Criar o nome do arquivo de saída
            String nomeArquivoSaida = "output" + sufixoArquivo + ".txt";

            try (PrintStream out = new PrintStream(new FileOutputStream(nomeArquivoSaida, true))) {
                out.println("A string '" + linha + "' " + (cykResult ? "" : "nao ") + "pertence a gramatica.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            long endTime = System.nanoTime();

            // Calcular o tempo decorrido
            long timeElapsed = endTime - startTime;

            // Converter nanossegundos para segundos
            seconds = (double) timeElapsed / 1_000_000_000.0;

            // Imprimir o tempo decorrido
        }
        
        System.out.println("Tempo de execução: " + seconds + " segundos");
        
        bf.close();
    }
}
