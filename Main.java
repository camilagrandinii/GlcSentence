/**
 * Codificação do Problema de Pertencimento de Sentença à Gramática
 * Fundamentos Teóricos da Computação
 * @author - Camila Lacerda Grandini & Milena Soares Barreira
 * 2023 - 2o. Semestre
*/

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Main {
    public static void main(String[] args) throws IOException {
        GrammarExtractor grammarExtractor = new GrammarExtractor();
        CYK cykExecutor = new CYK();

        String nome_arquivo = "gramatica.txt"; // Substitua pelo nome do arquivo que deseja ler
        BufferedReader bf = new BufferedReader(new FileReader(nome_arquivo));

        Grammar grammar = new Grammar();

        String linha = bf.readLine();
        grammar = grammarExtractor.ExtractGrammar(linha, grammar);

        grammar.PrintGrammar();

        GrammarConversor grammarConversor = new GrammarConversor(grammar);

        System.out.println("\nQual algoritmo você deseja executar?");
        System.out.println("1) CYK padrão");
        System.out.println("2) CYK otimizado");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int escolha = Integer.parseInt(reader.readLine());

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
            String algoritmo;

            if (escolha == 1) {
                cykResult = cykExecutor.CykCnf(grammar, linha);
                algoritmo = "CYK padrão";
            } else {
                List<String> nullableVariables = grammarConversor.findNullableVariables(grammar.rules);
                Map<String, Set<String>> unitRelation = grammar.GetUnitRleation(nullableVariables);
                Set<String> allSymbols = grammar.ComputeV();
                Map<String, Set<String>> inverseUnitGraph = grammar.GetInverseUnitGraph(allSymbols, unitRelation);

                cykResult = cykExecutor.Cyk2Nf(grammar, inverseUnitGraph, linha);
                algoritmo = "CYK otimizado";
            }
            
            System.out.println("A string '" + linha + "' " + (cykResult ? "" : "nao ") + "pertence a gramatica.");

            // Criar o nome do arquivo de saída
            String nomeArquivoSaida = "output" + sufixoArquivo + ".txt";

            try (PrintStream out = new PrintStream(new FileOutputStream(nomeArquivoSaida, true))) {
                out.println("A string '" + linha + "' " + (cykResult ? "" : "nao ") + "pertence a gramatica.");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        bf.close();
    }
}

// se passa so vazio ta quebrando 
// nao fizemos tambem dois seguidos tipo aBB b-> lambda ele identifica aB duas vezes
