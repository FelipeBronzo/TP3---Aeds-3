import java.io.*;


public class SerieFileManager {
    private final String dbPath = "dados/series.db";
    private final String csvPath = "tvs.csv/tvs.csv";
    private BPlusTree indexTree;

    public SerieFileManager() {
        this.indexTree = new BPlusTree(8); // Ordem 8 para a Árvore B+
    }

    public void carregarArquivo() throws IOException {
        File dbFile = new File(dbPath);
        
        // Verificar se o arquivo de dados já existe para evitar carregar os dados do CSV mais de uma vez
        if (!dbFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvPath));
                 RandomAccessFile arq = new RandomAccessFile(dbFile, "rw")) {

                // Ignorar o cabeçalho do CSV
                bufferedReader.readLine();

                String linha;
                while ((linha = bufferedReader.readLine()) != null) {
                    Serie serie = new Serie();
                    serie.ler(linha);  // Preenche o objeto Serie com os dados do CSV

                    long posicao = arq.getFilePointer();  // Salva a posição atual no arquivo
                    byte[] ba = serie.toByteArray();      // Converte a série para bytes

                    arq.writeBoolean(true);  // Marca o registro como ativo
                    arq.writeInt(ba.length);
                    arq.write(ba);           // Escreve os dados da série

                    // Insere no índice B+ a posição da série no arquivo
                    indexTree.insert(serie.getId(), posicao);
                }

                // Após carregar o CSV, salva a árvore B+ no arquivo de índice
                indexTree.salvar();
                System.out.println("Arquivo de dados e índice carregados com sucesso.");
            } catch (FileNotFoundException e) {
                System.out.println("Arquivo CSV não encontrado: " + e.getMessage());
            }
        } else {
            // Se o arquivo já existe, apenas carrega o índice B+
            indexTree.carregar();
            System.out.println("Arquivo de índice carregado a partir do arquivo existente.");
        }
    }

    public Serie lerSerie(int id) throws IOException {
        Long posicao = indexTree.search(id);
        if (posicao == null) {
            return null;
        }

        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "r")) {
            arq.seek(posicao);
            boolean lapide = arq.readBoolean();
            int tamanhoRegistro = arq.readInt();
            byte[] ba = new byte[tamanhoRegistro];
            arq.readFully(ba);

            if (lapide) {
                Serie serie = new Serie();
                serie.fromByteArray(ba);
                return serie;
            }
        }
        return null;
    }

    public void adicionarSerie(Serie serie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            arq.seek(arq.length());
            long posicao = arq.getFilePointer();
            byte[] ba = serie.toByteArray();
            arq.writeBoolean(true);
            arq.writeInt(ba.length);
            arq.write(ba);

            indexTree.insert(serie.getId(), posicao); // Atualiza o índice B+ com o novo registro
            indexTree.salvar(); // Salva a árvore de índice após a adição
        }
    }

    public void atualizarSerie(int id, Serie novaSerie) throws IOException {
        Long posicaoAntiga = indexTree.search(id);
        if (posicaoAntiga != null) {
            try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
                // Marcar o registro antigo como excluído
                arq.seek(posicaoAntiga);
                arq.writeBoolean(false); // Marca o registro antigo como excluído
    
                // Gravar o novo registro no final do arquivo
                arq.seek(arq.length());
                long novaPosicao = arq.getFilePointer();
                byte[] ba = novaSerie.toByteArray();
                arq.writeBoolean(true);
                arq.writeInt(ba.length);
                arq.write(ba);
    
                // Atualizar o índice: remover o antigo e adicionar o novo
                indexTree.remove(id); // Remove a posição antiga do índice
                indexTree.insert(novaSerie.getId(), novaPosicao); // Insere a nova posição no índice
                indexTree.salvar(); // Salva o índice após a atualização
            }
        } else {
            System.out.println("ID não encontrado para atualização.");
        }
    }
    

    public void excluirSerie(int id) throws IOException {
        Long posicao = indexTree.search(id);
        if (posicao != null) {
            try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
                arq.seek(posicao);
                arq.writeBoolean(false); // Marca o registro como excluído

                indexTree.salvar(); // Salva a árvore de índice após a exclusão
            }
        }
    }
}
