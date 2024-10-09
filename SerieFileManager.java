import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class SerieFileManager {
    private final String dbPath = "dados/series.db";
    private final String csvPath = "tvs.csv/tvs.csv";
    private ArrayList<Serie> listadeseries = new ArrayList<>();
    private boolean isLoaded = false;

    public void carregarArquivo() throws IOException {
        if (!isLoaded) {
            try (BufferedReader bufferedreader = new BufferedReader(new FileReader(csvPath))) {
                bufferedreader.readLine(); // Ignorar cabeçalho

                String linha;
                while ((linha = bufferedreader.readLine()) != null) {
                    Serie serie = new Serie();
                    serie.ler(linha);
                    listadeseries.add(serie);
                    adicionarSerieNoArquivo(serie);
                }
                isLoaded = true;
            }
        }
    }

    public Serie lerSerie(int id) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(dbPath))) {
            while (dis.available() > 0) {
                boolean lapide = dis.readBoolean();
                int tamanhoRegistro = dis.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                dis.readFully(ba);

                if (lapide) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);
                    if (serie.getId() == id) {
                        return serie;
                    }
                }
            }
        }
        return null;
    }

    public void atualizarSerie(int id, Serie novaSerie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            while (arq.getFilePointer() < arq.length()) {
                long posicaoInicio = arq.getFilePointer();
                boolean lapide = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (lapide) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);

                    if (serie.getId() == id) {
                        byte[] novoBa = novaSerie.toByteArray();
                        arq.seek(posicaoInicio);
                        arq.writeBoolean(true);
                        arq.writeInt(novoBa.length);
                        arq.write(novoBa);
                        return;
                    }
                }
            }
        }
    }

    public void excluirSerie(int id) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            while (arq.getFilePointer() < arq.length()) {
                long posicaoInicio = arq.getFilePointer();
                boolean lapide = arq.readBoolean();
                int tamanhoRegistro = arq.readInt();
                byte[] ba = new byte[tamanhoRegistro];
                arq.readFully(ba);

                if (lapide) {
                    Serie serie = new Serie();
                    serie.fromByteArray(ba);

                    if (serie.getId() == id) {
                        arq.seek(posicaoInicio);
                        arq.writeBoolean(false); // Marca como excluído
                        return;
                    }
                }
            }
        }
    }

    public void adicionarSerie(Serie serie) throws IOException {
        try (RandomAccessFile arq = new RandomAccessFile(dbPath, "rw")) {
            arq.seek(arq.length());
            byte[] ba = serie.toByteArray();
            arq.writeBoolean(true);
            arq.writeInt(ba.length);
            arq.write(ba);
        }
    }

    private void adicionarSerieNoArquivo(Serie serie) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(dbPath, true))) {
            byte[] ba = serie.toByteArray();
            dos.writeBoolean(true);
            dos.writeInt(ba.length);
            dos.write(ba);
        }
    }

    public void ordenarSeries() throws IOException {
        int tamanhoMemoria = 100; // Tamanho do bloco de memória
        distribuirSeriesParaArquivos(tamanhoMemoria);

        String nomeArquivoOrdenado = "dados/series_ordenadas.db";
        int numeroDeArquivos = contarArquivosTemporarios();

        intercalarArquivos(numeroDeArquivos, nomeArquivoOrdenado);
        System.out.println("Arquivo ordenado com sucesso!");
    }

    private void distribuirSeriesParaArquivos(int tamanhoMemoria) throws IOException {
        try (RandomAccessFile entrada = new RandomAccessFile(dbPath, "r")) {
            int contadorArquivo = 0;

            while (entrada.getFilePointer() < entrada.length()) {
                ArrayList<Serie> listaSeries = new ArrayList<>();

                for (int i = 0; i < tamanhoMemoria && entrada.getFilePointer() < entrada.length(); i++) {
                    boolean lapide = entrada.readBoolean();
                    int tamanhoRegistro = entrada.readInt();
                    byte[] ba = new byte[tamanhoRegistro];
                    entrada.readFully(ba);

                    if (lapide) {
                        Serie serie = new Serie();
                        serie.fromByteArray(ba);
                        listaSeries.add(serie);
                    }
                }

                listaSeries.sort(Comparator.comparingInt(Serie::getId));

                String nomeArquivoTemporario = "bloco" + contadorArquivo + ".tmp";
                try (RandomAccessFile tempFile = new RandomAccessFile(nomeArquivoTemporario, "rw")) {
                    for (Serie serie : listaSeries) {
                        byte[] ba = serie.toByteArray();
                        tempFile.writeBoolean(true);
                        tempFile.writeInt(ba.length);
                        tempFile.write(ba);
                    }
                }
                contadorArquivo++;
            }
        }
    }

    private int contarArquivosTemporarios() {
        int contador = 0;
        while (true) {
            File arquivo = new File("bloco" + contador + ".tmp");
            if (arquivo.exists()) {
                contador++;
            } else {
                break;
            }
        }
        return contador;
    }

    private void intercalarArquivos(int numeroDeArquivos, String arquivoSaida) throws IOException {
        RandomAccessFile[] arquivos = new RandomAccessFile[numeroDeArquivos];
        for (int i = 0; i < numeroDeArquivos; i++) {
            arquivos[i] = new RandomAccessFile("bloco" + i + ".tmp", "r");
        }

        try (RandomAccessFile saida = new RandomAccessFile(arquivoSaida, "rw")) {
            Serie[] seriesCorrentes = new Serie[numeroDeArquivos];
            boolean[] lapides = new boolean[numeroDeArquivos];
            byte[][] buffers = new byte[numeroDeArquivos][];

            for (int i = 0; i < numeroDeArquivos; i++) {
                if (arquivos[i].getFilePointer() < arquivos[i].length()) {
                    lapides[i] = arquivos[i].readBoolean();
                    int tamanhoRegistro = arquivos[i].readInt();
                    buffers[i] = new byte[tamanhoRegistro];
                    arquivos[i].readFully(buffers[i]);

                    if (lapides[i]) {
                        seriesCorrentes[i] = new Serie();
                        seriesCorrentes[i].fromByteArray(buffers[i]);
                    }
                }
            }

            while (true) {
                int indiceMenor = -1;
                Serie menorSerie = null;

                for (int i = 0; i < numeroDeArquivos; i++) {
                    if (seriesCorrentes[i] != null && (menorSerie == null || seriesCorrentes[i].getId() < menorSerie.getId())) {
                        menorSerie = seriesCorrentes[i];
                        indiceMenor = i;
                    }
                }

                if (indiceMenor == -1) {
                    break;
                }

                byte[] ba = menorSerie.toByteArray();
                saida.writeBoolean(true);
                saida.writeInt(ba.length);
                saida.write(ba);

                if (arquivos[indiceMenor].getFilePointer() < arquivos[indiceMenor].length()) {
                    lapides[indiceMenor] = arquivos[indiceMenor].readBoolean();
                    int tamanhoRegistro = arquivos[indiceMenor].readInt();
                    buffers[indiceMenor] = new byte[tamanhoRegistro];
                    arquivos[indiceMenor].readFully(buffers[indiceMenor]);

                    if (lapides[indiceMenor]) {
                        seriesCorrentes[indiceMenor] = new Serie();
                        seriesCorrentes[indiceMenor].fromByteArray(buffers[indiceMenor]);
                    } else {
                        seriesCorrentes[indiceMenor] = null;
                    }
                } else {
                    seriesCorrentes[indiceMenor] = null;
                }
            }
        }

        for (RandomAccessFile arquivo : arquivos) {
            arquivo.close();
        }
    }
}
