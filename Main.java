import java.util.Scanner;
import java.io.IOException;


public class Main {
        public static void main(String[] args) {
            Scanner entrada = new Scanner(System.in);
            SerieFileManager fileManager = new SerieFileManager();
    
            int operacao;
            System.out.println("Escolha a operação: 1-Carregar, 2-Ler, 3-Atualizar, 4-Excluir, 5-Criar, 6-Ordenar, 7-Sair");
            operacao = entrada.nextInt();
    
            while (operacao >= 1 && operacao <= 6) {
                try {
                    switch (operacao) {
                        case 1 -> fileManager.carregarArquivo();
                        case 2 -> {
                            System.out.print("ID da série para ler: ");
                            Serie serie = fileManager.lerSerie(entrada.nextInt());
                            System.out.println(serie != null ? serie : "Série não encontrada.");
                        }
                        case 3 -> {
                            System.out.print("ID da série para atualizar: ");
                            Serie novaSerie = new Serie(); // preencha os atributos de acordo com entrada
                            fileManager.atualizarSerie(entrada.nextInt(), novaSerie);
                        }
                        case 4 -> {
                            System.out.print("ID da série para excluir: ");
                            fileManager.excluirSerie(entrada.nextInt());
                        }
                        case 5 -> {
                            Serie novaSerie = new Serie(); // preencha os atributos de acordo com entrada
                            fileManager.adicionarSerie(novaSerie);
                        }
                        case 6 -> System.out.println("Ordenação em desenvolvimento...");
                    }
                } catch (IOException e) {
                    System.out.println("Erro: " + e.getMessage());
                }
    
                System.out.println("Escolha a operação: 1-Carregar, 2-Ler, 3-Atualizar, 4-Excluir, 5-Criar, 6-Ordenar, 7-Sair");
                operacao = entrada.nextInt();
            }
            entrada.close();
        }
}
