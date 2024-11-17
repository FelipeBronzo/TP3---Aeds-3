package filemanager;
import java.io.*;


import compressao.CompactadorHuffman;

import model.Serie;

public class SerieFileManager {
    private final String dbPath = "dados/series.db";
    private final String csvPath = "tvs.csv/tvs.csv";
    private final CompactadorHuffman compactadorHuffman;

    public SerieFileManager() {
        this.compactadorHuffman = new CompactadorHuffman(); // Inicializa o compactador
    }

    // Carrega os dados do CSV para o arquivo sequencial .db
    public void carregarArquivo() throws IOException {
        File dbFile = new File(dbPath);

        // Verificar se o arquivo de dados já existe
        if (!dbFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
                 RandomAccessFile arq = new RandomAccessFile(dbFile, "rw")) {

                // Ignorar o cabeçalho do CSV
                bufferedReader.readLine();

                String linha;
                while ((linha = bufferedReader.readLine()) != null) {
                    Serie serie = new Serie();
                    serie.ler(linha);  // Preenche o objeto Serie com os dados do CSV

                    // Converte a série para bytes e escreve no arquivo sequencial
                    byte[] ba = serie.toByteArray();
                    arq.writeBoolean(true);  // Registro ativo
                    arq.writeInt(ba.length);
                    arq.write(ba);
                }

                System.out.println("Dados do CSV carregados no arquivo sequencial.");
            } catch (FileNotFoundException e) {
                System.out.println("Arquivo CSV não encontrado: " + e.getMessage());
            }
        } else {
            System.out.println("Arquivo sequencial já existe. Nenhuma ação necessária.");
        }
    }

    // Lê uma série pelo ID no arquivo sequencial
    public Serie lerSerie(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "r")) {
            while (arq.getFilePointer() < arq.length()) {
                boolean ativo = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (ativo) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);
                    if (serie.getId() == id) {
                        return serie;
                    }
                }
            }
        }
        return null; // Série não encontrada
    }

    // Adiciona uma nova série ao final do arquivo
    public void adicionarSerie(Serie serie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            arq.seek(arq.length()); // Move para o final do arquivo
            byte[] ba = serie.toByteArray();
            arq.writeBoolean(true); // Registro ativo
            arq.writeInt(ba.length);
            arq.write(ba);
        }
    }

    // Atualiza uma série existente, marcando a antiga como excluída e adicionando uma nova no final
    public boolean atualizarSerie(int id, Serie novaSerie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            while (arq.getFilePointer() < arq.length()) {
                long posicaoAtual = arq.getFilePointer();
                boolean ativo = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (ativo) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);
                    if (serie.getId() == id) {
                        arq.seek(posicaoAtual);
                        arq.writeBoolean(false); // Marca o registro antigo como inativo

                        arq.seek(arq.length()); // Grava o novo registro no final do arquivo
                        byte[] novoBa = novaSerie.toByteArray();
                        arq.writeBoolean(true); // Registro ativo
                        arq.writeInt(novoBa.length);
                        arq.write(novoBa);
                        return true;
                    }
                }
            }
        }
        return false; // ID não encontrado
    }

    // Exclui uma série marcando-a como inativa
    public boolean excluirSerie(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            while (arq.getFilePointer() < arq.length()) {
                long posicaoAtual = arq.getFilePointer();
                boolean ativo = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (ativo) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);
                    if (serie.getId() == id) {
                        arq.seek(posicaoAtual);
                        arq.writeBoolean(false); // Marca o registro como inativo
                        return true;
                    }
                }
            }
        }
        return false; // ID não encontrado
    }

    public void compactarArquivoHuffman(int versao) throws IOException {
        String arquivoComprimido = "dados/seriesHuffmanCompressao" + versao + ".huf";
    
        // Lê o conteúdo binário do arquivo sequencial
        StringBuilder conteudo = new StringBuilder();
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "r")) {
            while (arq.getFilePointer() < arq.length()) {
                boolean ativo = arq.readBoolean(); // Lê a lápide
                int tamanhoRegistro = arq.readInt(); // Lê o tamanho do registro
                byte[] registro = new byte[tamanhoRegistro];
                arq.readFully(registro); // Lê o registro completo
    
                // Apenas registros ativos são considerados para compactação
                if (ativo) {
                    conteudo.append(new String(registro, "UTF-8")).append("\n");
                }
            }
        }
    
        // Compacta o conteúdo usando Huffman
        compactadorHuffman.construirArvore(conteudo.toString());
        byte[] dadosCompactados = compactadorHuffman.compactar(conteudo.toString());
    
        // Salva a árvore de Huffman e os dados compactados no arquivo de saída
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivoComprimido))) {
            oos.writeObject(compactadorHuffman.getRaiz()); // Salva a raiz da árvore
            oos.write(dadosCompactados); // Salva os dados compactados
        }
    
        System.out.println("Arquivo compactado e salvo como: " + arquivoComprimido);
    }
    
    public void descompactarArquivoHuffman(int versao) throws IOException {
        String arquivoComprimido = "dados/seriesHuffmanCompressao" + versao + ".huf";
        String arquivoDescompactado = dbPath;
    
        CompactadorHuffman.Nodo raiz;
        byte[] dadosCompactados;
    
        // Lê a árvore de Huffman e os dados compactados
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivoComprimido))) {
            raiz = (CompactadorHuffman.Nodo) ois.readObject(); // Lê a raiz da árvore
            dadosCompactados = ois.readAllBytes(); // Lê os dados compactados
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao carregar a árvore de Huffman: " + e.getMessage());
        }
    
        // Descompacta os dados
        String dadosDescompactados = compactadorHuffman.descompactar(dadosCompactados, raiz);
    
        // Recria o arquivo sequencial no formato original
        try (RandomAccessFile arq = new RandomAccessFile(arquivoDescompactado, "rw")) {
            arq.setLength(0); // Limpa o arquivo existente
    
            // Processa os registros descompactados
            String[] registros = dadosDescompactados.split("\n");
            for (String registro : registros) {
                byte[] registroBytes = registro.getBytes("UTF-8");
    
                arq.writeBoolean(true); // Lápide (marcar como ativo)
                arq.writeInt(registroBytes.length); // Tamanho do registro
                arq.write(registroBytes); // Dados do registro
            }
        }
    
        System.out.println("Arquivo descompactado e recriado como: " + arquivoDescompactado);
    }
    
    
}
