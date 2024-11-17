package compressao;

import java.io.*;
import java.util.*;

public class CompactadorHuffman {
    private Map<Character, String> charParaCodigo;
    private Map<String, Character> codigoParaChar;

    public CompactadorHuffman() {
        charParaCodigo = new HashMap<>();
        codigoParaChar = new HashMap<>();
    }

    // Compactar um arquivo
    public void compactarArquivo(String caminhoEntrada, String caminhoSaida) throws IOException {
        StringBuilder conteudo = new StringBuilder();

        // Lê o conteúdo do arquivo sequencial
        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoEntrada))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                conteudo.append(linha).append("\n");
            }
        }

        // Compactação
        byte[] dadosCompactados = compactar(conteudo.toString());

        // Escreve os dados compactados no arquivo de saída
        try (FileOutputStream fos = new FileOutputStream(caminhoSaida)) {
            fos.write(dadosCompactados);
        }

        System.out.println("Compactação concluída. Arquivo salvo em: " + caminhoSaida);
    }

    // Descompactar um arquivo
    public void descompactarArquivo(String caminhoEntrada, String caminhoSaida) throws IOException {
        byte[] dadosCompactados;

        // Lê os dados compactados do arquivo
        try (FileInputStream fis = new FileInputStream(caminhoEntrada)) {
            dadosCompactados = fis.readAllBytes();
        }

        // Descompactação
        String dadosDescompactados = descompactar(dadosCompactados);

        // Escreve os dados descompactados no arquivo de saída
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(caminhoSaida))) {
            escritor.write(dadosDescompactados);
        }

        System.out.println("Descompactação concluída. Arquivo salvo em: " + caminhoSaida);
    }

    // Método principal de compactação
    public byte[] compactar(String texto) {
        // Passo 1: Construir o mapa de frequências
        Map<Character, Integer> mapaFrequencias = new HashMap<>();
        for (char ch : texto.toCharArray()) {
            mapaFrequencias.put(ch, mapaFrequencias.getOrDefault(ch, 0) + 1);
        }

        // Passo 2: Construir a árvore de Huffman
        PriorityQueue<Nodo> filaPrioridade = new PriorityQueue<>();
        for (var entrada : mapaFrequencias.entrySet()) {
            filaPrioridade.add(new Nodo(entrada.getKey(), entrada.getValue()));
        }

        while (filaPrioridade.size() > 1) {
            Nodo esquerdo = filaPrioridade.poll();
            Nodo direito = filaPrioridade.poll();
            filaPrioridade.add(new Nodo(esquerdo, direito));
        }

        Nodo raiz = filaPrioridade.poll();
        construirCodigo(raiz, "");

        // Passo 3: Converter o texto para o código binário
        StringBuilder textoCodificado = new StringBuilder();
        for (char ch : texto.toCharArray()) {
            textoCodificado.append(charParaCodigo.get(ch));
        }

        // Retornar os dados compactados como bytes
        return textoCodificado.toString().getBytes();
    }

    // Método principal de descompactação
    public String descompactar(byte[] dadosCompactados) {
        StringBuilder textoDecodificado = new StringBuilder();
        Nodo raiz = construirArvoreAPartirDoCodigo();
        Nodo atual = raiz;

        String bits = new String(dadosCompactados);
        for (char bit : bits.toCharArray()) {
            atual = (bit == '0') ? atual.esquerdo : atual.direito;

            if (atual.esquerdo == null && atual.direito == null) {
                textoDecodificado.append(atual.caractere);
                atual = raiz;
            }
        }

        return textoDecodificado.toString();
    }

    // Métodos auxiliares
    private void construirCodigo(Nodo nodo, String s) {
        if (nodo.esquerdo == null && nodo.direito == null) {
            charParaCodigo.put(nodo.caractere, s);
            codigoParaChar.put(s, nodo.caractere);
            return;
        }
        construirCodigo(nodo.esquerdo, s + "0");
        construirCodigo(nodo.direito, s + "1");
    }

    private Nodo construirArvoreAPartirDoCodigo() {
        Nodo raiz = new Nodo(null, null);
        for (var entrada : charParaCodigo.entrySet()) {
            Nodo atual = raiz;
            for (char bit : entrada.getValue().toCharArray()) {
                if (bit == '0') {
                    if (atual.esquerdo == null) atual.esquerdo = new Nodo(null, null);
                    atual = atual.esquerdo;
                } else {
                    if (atual.direito == null) atual.direito = new Nodo(null, null);
                    atual = atual.direito;
                }
            }
            atual.caractere = entrada.getKey();
        }
        return raiz;
    }

    // Classe auxiliar para a árvore de Huffman
    private static class Nodo implements Comparable<Nodo> {
        char caractere;
        int frequencia;
        Nodo esquerdo, direito;

        Nodo(char caractere, int frequencia) {
            this.caractere = caractere;
            this.frequencia = frequencia;
        }

        Nodo(Nodo esquerdo, Nodo direito) {
            this.esquerdo = esquerdo;
            this.direito = direito;
            this.frequencia = esquerdo.frequencia + direito.frequencia;
        }

        @Override
        public int compareTo(Nodo outro) {
            return Integer.compare(this.frequencia, outro.frequencia);
        }
    }
}

