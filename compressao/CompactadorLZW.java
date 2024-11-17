package compressao;

import java.io.*;
import java.util.*;

public class CompactadorLZW {
    private static final int TAMANHO_DICIONARIO_INICIAL = 256;

    // Compactação
    public byte[] compactar(String texto) throws IOException {
        if (texto == null || texto.isEmpty()) {
            throw new IllegalArgumentException("Texto a ser compactado está vazio ou nulo.");
        }
    
        Map<String, Integer> dicionario = inicializarDicionarioCompactacao();
        List<Integer> codigos = new ArrayList<>();
        String w = "";
    
        for (char c : texto.toCharArray()) {
            String wc = w + c;
            if (dicionario.containsKey(wc)) {
                w = wc;
            } else {
                codigos.add(dicionario.get(w));
                dicionario.put(wc, dicionario.size());
                w = "" + c;
            }
        }
    
        if (!w.isEmpty()) {
            codigos.add(dicionario.get(w));
        }
    
        return codigosParaBytes(codigos);
    }
    
    

    // Descompactação
    public String descompactar(byte[] dadosCompactados) throws IOException {
        List<Integer> codigos = bytesParaCodigos(dadosCompactados);
        Map<Integer, String> dicionario = inicializarDicionarioDescompactacao();
    
        StringBuilder resultado = new StringBuilder();
        String anterior = "" + (char) codigos.remove(0).intValue();
        resultado.append(anterior);
    
        for (Integer codigo : codigos) {
            
            if (codigo == null) continue;
        
            String entrada;
            if (dicionario.containsKey(codigo)) {
                entrada = dicionario.get(codigo);
            } else if (codigo == dicionario.size()) {
                entrada = anterior + anterior.charAt(0);
            } else {
                System.out.println("Código inválido encontrado: " + codigo);
                throw new IllegalArgumentException("Erro durante a descompactação: Código inválido.");
            }
        
            resultado.append(entrada);
            dicionario.put(dicionario.size(), anterior + entrada.charAt(0));
            anterior = entrada;
        }
    
        return resultado.toString();
    }
    

    // Inicializa o dicionário de compactação
    private Map<String, Integer> inicializarDicionarioCompactacao() {
        Map<String, Integer> dicionario = new HashMap<>();
        for (int i = 0; i < TAMANHO_DICIONARIO_INICIAL; i++) {
            dicionario.put("" + (char) i, i);
        }
        return dicionario;
    }

    // Inicializa o dicionário de descompactação
    private Map<Integer, String> inicializarDicionarioDescompactacao() {
        Map<Integer, String> dicionario = new HashMap<>();
        for (int i = 0; i < TAMANHO_DICIONARIO_INICIAL; i++) {
            dicionario.put(i, "" + (char) i);
        }
        return dicionario;
    }

    // Converte uma lista de códigos inteiros para bytes
    private byte[] codigosParaBytes(List<Integer> codigos) throws IOException {
        if (codigos == null || codigos.isEmpty()) {
            throw new IllegalArgumentException("Lista de códigos vazia ou nula.");
        }
    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Integer codigo : codigos) {
            if (codigo == null) continue; // Ignorar valores nulos
            baos.write((codigo >> 8) & 0xFF); // Byte alto
            baos.write(codigo & 0xFF);       // Byte baixo
        }
        return baos.toByteArray();
    }
    
    

    // Converte bytes para uma lista de códigos inteiros
    private List<Integer> bytesParaCodigos(byte[] dadosCompactados) {
        if (dadosCompactados == null || dadosCompactados.length % 2 != 0) {
            throw new IllegalArgumentException("Dados compactados inválidos.");
        }
    
        List<Integer> codigos = new ArrayList<>();
        for (int i = 0; i < dadosCompactados.length; i += 2) {
            int codigo = ((dadosCompactados[i] & 0xFF) << 8) | (dadosCompactados[i + 1] & 0xFF);
            codigos.add(codigo);
        }
    
    
        return codigos;
    }
    
    
    
}
