/**
 * Codificação do Problema de Pertencimento de Sentença à Gramática
 * Fundamentos Teóricos da Computação
 * @author - Camila Lacerda Grandini & Milena Soares Barreira
 * 2023 - 2o. Semestre
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Main {
    public static void main(String[] args) throws IOException {
        GrammarExtractor grammarExtractor = new GrammarExtractor();
        CYK cykExecutor = new CYK();

        String nome_arquivo = "gramatica.txt"; // Substitua pelo nome do arquivo que deseja ler
        BufferedReader bf = new BufferedReader(new FileReader(nome_arquivo));

        Grammar grammar = new Grammar();

        String linha = bf.readLine();
        grammar = grammarExtractor.ExtractGrammar(linha, grammar);

        GrammarConversor grammarConversor = new GrammarConversor(grammar);
        
        grammar.PrintGrammar();
        
        grammar = grammarConversor.To2NfGrammar(grammar);

        grammar.PrintGrammar();

        //grammar = grammarConversor.ToFncGrammar(grammar);

        while ((linha = bf.readLine()) != null) {
            //boolean cykResult = cykExecutor.CykCnf(grammar, linha);

            // System.out.println("Resultado CYK based on Chomsky Normal Form");
            // System.out.println("A string '" + linha + "' " + (cykResult ? "" : "nao ") + "pertence a gramatica.");

            boolean cyk2NfResult = cykExecutor.Cyk2Nf(grammar, linha);
            System.out.println("Resultado CYK based on the 2NF form");

            System.out.println("A string '" + linha + "' " + (cyk2NfResult ? "" : "nao ") + "pertence a gramatica.");
        }

        bf.close();
    }
}
// se passa so vazio ta quebrando 
// nao fizemos tambem dois seguidos tipo aBB b-> lambda ele identifica aB duas vezes
