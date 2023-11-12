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

        String nome_arquivo = "gramatica.txt"; // Substitua pelo nome do arquivo que deseja ler
        BufferedReader bf = new BufferedReader(new FileReader(nome_arquivo));

        Grammar grammar = new Grammar();

        String linha = bf.readLine();
        grammar = grammarExtractor.ExtractGrammar(linha, grammar);

        while ((linha = bf.readLine()) != null) {
            // Adicionar tratativas para os testes
        }

        bf.close();

        grammar.PrintGrammar();
        grammarConversor.ToFncGrammar(grammar);
        // String sentenceToCheck = "a";

        // // Chame o método para verificar se a sentença pertence à linguagem
        // boolean belongsToLanguage = grammarConversor.checkSentenceBelongsLanguage(grammar, sentenceToCheck);

        // // Exiba o resultado
        // if (belongsToLanguage) {
        //     System.out.println("A sentença pertence à linguagem.");
        // } else {
        //     System.out.println("A sentença não pertence à linguagem.");
        // }
    }
}
// se passa so vazio ta quebrando 
// nao fizemos o tratamento de bb duas regras lower case seguidas uma da outra
// nao fizemos tambem dois seguidos tipo aBB b-> lambda ele identifica aB duas vezes
