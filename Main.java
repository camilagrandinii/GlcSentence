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
    }
}
