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
        GrammarConversor grammarConversor = new GrammarConversor();
        CYK cykExecutor = new CYK();

        String nome_arquivo = "gramatica3.txt"; // Substitua pelo nome do arquivo que deseja ler
        BufferedReader bf = new BufferedReader(new FileReader(nome_arquivo));

        Grammar grammar = new Grammar();

        String linha = bf.readLine();
        grammar = grammarExtractor.ExtractGrammar(linha, grammar);
        
        grammar.PrintGrammar();
        
        grammar = grammarConversor.ToFncGrammar(grammar);

        while ((linha = bf.readLine()) != null) {
            //boolean cykResult = cykExecutor.cyk(ruleArrayGrammar.rules, linha);

            //System.out.println("A string '" + linha + "' " + (cykResult ? "faz" : "não faz") + " parte da gramática.");
        }

        bf.close();
    }
}
// se passa so vazio ta quebrando 
// nao fizemos tambem dois seguidos tipo aBB b-> lambda ele identifica aB duas vezes
