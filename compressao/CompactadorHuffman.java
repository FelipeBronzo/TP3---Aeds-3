package compressao;

import java.io.Serializable;
import java.util.*;

public class CompactadorHuffman {
    private Map<Character, String> tabelaDeCodigos; // Mapa para armazenar os códigos
    private Nodo raiz; // Raiz da árvore de Huffman

    // Classe para representar os nós da árvore
    public static class Nodo implements Comparable<Nodo>, Serializable {
    private static final long serialVersionUID = 1L; // Adicione um ID para evitar problemas de versão
    char caractere;
    int frequencia;
    Nodo esquerdo, direito;

    Nodo(char caractere, int frequencia) {
        this.caractere = caractere;
        this.frequencia = frequencia;
    }

    Nodo(Nodo esquerdo, Nodo direito) {
        this.caractere = '\0'; // Nó intermediário
        this.frequencia = esquerdo.frequencia + direito.frequencia;
        this.esquerdo = esquerdo;
        this.direito = direito;
    }

    @Override
    public int compareTo(Nodo outro) {
        return Integer.compare(this.frequencia, outro.frequencia);
    }
}

    

    public void construirArvore(String texto) {
        // Calcula as frequências dos caracteres
        Map<Character, Integer> frequencias = new HashMap<>();
        for (char c : texto.toCharArray()) {
            frequencias.put(c, frequencias.getOrDefault(c, 0) + 1);
        }
    
        // Cria uma fila de prioridade para construir a árvore
        PriorityQueue<Nodo> fila = new PriorityQueue<>();
        for (var entrada : frequencias.entrySet()) {
            fila.add(new Nodo(entrada.getKey(), entrada.getValue()));
        }
    
        // Constrói a árvore de Huffman
        while (fila.size() > 1) {
            Nodo esquerdo = fila.poll();
            Nodo direito = fila.poll();
            fila.add(new Nodo(esquerdo, direito));
        }
    
        raiz = fila.poll(); // Define a raiz da árvore
        tabelaDeCodigos = new HashMap<>();
        construirTabelaDeCodigos(raiz, "");
    }
    
    // Método recursivo para construir os códigos binários
    private void construirTabelaDeCodigos(Nodo nodo, String codigo) {
        if (nodo.esquerdo == null && nodo.direito == null) {
            tabelaDeCodigos.put(nodo.caractere, codigo);
            return;
        }
        construirTabelaDeCodigos(nodo.esquerdo, codigo + "0");
        construirTabelaDeCodigos(nodo.direito, codigo + "1");
    }

    public byte[] compactar(String texto) {
        construirArvore(texto);
    
        StringBuilder textoCodificado = new StringBuilder();
        for (char c : texto.toCharArray()) {
            textoCodificado.append(tabelaDeCodigos.get(c));
        }
    
        // Converte o texto codificado para bytes
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < textoCodificado.length(); i += 8) {
            String byteString = textoCodificado.substring(i, Math.min(i + 8, textoCodificado.length()));
            bytes.add((byte) Integer.parseInt(byteString, 2));
        }
    
        byte[] resultado = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            resultado[i] = bytes.get(i);
        }
        return resultado;
    }
    

    public String descompactar(byte[] dadosCompactados, Nodo raiz) {
        StringBuilder textoDecodificado = new StringBuilder();
    
        // Converte os bytes de volta para uma string binária
        StringBuilder codigoBinario = new StringBuilder();
        for (byte b : dadosCompactados) {
            codigoBinario.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
    
        // Decodifica o texto usando a árvore de Huffman
        Nodo atual = raiz;
        for (char bit : codigoBinario.toString().toCharArray()) {
            atual = (bit == '0') ? atual.esquerdo : atual.direito;
            if (atual.esquerdo == null && atual.direito == null) {
                textoDecodificado.append(atual.caractere);
                atual = raiz;
            }
        }
    
        return textoDecodificado.toString();
    }
    
    

    public Map<Character, String> getTabelaDeCodigos() {
        return tabelaDeCodigos;
    }
    
    public void setTabelaDeCodigos(Map<Character, String> tabelaDeCodigos) {
        this.tabelaDeCodigos = tabelaDeCodigos;
    }

    public Nodo reconstruirArvore() {
        if (tabelaDeCodigos == null || tabelaDeCodigos.isEmpty()) {
            throw new IllegalStateException("Tabela de códigos está vazia ou não foi carregada.");
        }
    
        Nodo raiz = new Nodo(null, null);
        for (var entrada : tabelaDeCodigos.entrySet()) {
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
    
    public Nodo getRaiz() {
        return raiz;
    }
    
    
}
