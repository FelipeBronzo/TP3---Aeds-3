package filemanager;
import java.io.*;

import model.Serie;

public class SerieFileManager {
    private final String dbPath = "dados/series.db";
    private final String csvPath = "tvs.csv/tvs.csv";

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
}
