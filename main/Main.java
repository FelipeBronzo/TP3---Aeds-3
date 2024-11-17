package main;

import filemanager.SerieFileManager;
import model.Serie;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);
        SerieFileManager fileManager = new SerieFileManager();
    
        try {
            fileManager.carregarArquivo();
        } catch (IOException e) {
            System.out.println("Erro ao carregar o arquivo: " + e.getMessage());
        }
    
        int operacao;
        do {
            System.out.println("\nEscolha a operação: ");
            System.out.println("1 - Carregar");
            System.out.println("2 - Ler");
            System.out.println("3 - Atualizar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Criar");
            System.out.println("6 - Compactar Arquivo (Huffman)");
            System.out.println("7 - Descompactar Arquivo (Huffman)");
            System.out.println("8 - Compactar Arquivo (LZW)");
            System.out.println("9 - Descompactar Arquivo (LZW)");
            System.out.println("10 - Buscar por padrão (KMP)");
            System.out.println("11 - Buscar por padrão (Boyer-Moore)");
            System.out.println("12 - Sair");


            System.out.print("Operação: ");
            operacao = entrada.nextInt();
            entrada.nextLine();
    
            try {
                switch (operacao) {
                    case 1 -> fileManager.carregarArquivo();
                    case 2 -> {
                        System.out.print("ID da série para ler: ");
                        int id = entrada.nextInt();
                        Serie serie = fileManager.lerSerie(id);
                        System.out.println(serie != null ? serie : "Série não encontrada.");
                    }
                    case 3 -> {
                        System.out.print("ID da série para atualizar: ");
                        int id = entrada.nextInt();
                        entrada.nextLine(); // Limpar o buffer
                        Serie novaSerie = obterDadosSerie(id, entrada);
                        fileManager.atualizarSerie(id, novaSerie);
                    }
                    case 4 -> {
                        System.out.print("ID da série para excluir: ");
                        int id = entrada.nextInt();
                        fileManager.excluirSerie(id);
                    }
                    case 5 -> {
                        System.out.print("ID da série para criar: ");
                        int id = entrada.nextInt();
                        entrada.nextLine(); // Limpar o buffer
                        Serie novaSerie = obterDadosSerie(id, entrada);
                        fileManager.adicionarSerie(novaSerie);
                    }
                    case 6 -> {
                        System.out.print("Versão para o arquivo comprimido: ");
                        int versao = entrada.nextInt();
                        fileManager.compactarArquivoHuffman(versao);
                    }
                    case 7 -> {
                        System.out.print("Versão do arquivo a ser descomprimido: ");
                        int versao = entrada.nextInt();
                        fileManager.descompactarArquivoHuffman(versao);
                    }
                    case 8 -> {
                        System.out.print("Versão para o arquivo comprimido (LZW): ");
                        int versao = entrada.nextInt();
                        fileManager.compactarArquivoLZW(versao);
                    }
                    case 9 -> {
                        System.out.print("Versão do arquivo a ser descomprimido (LZW): ");
                        int versao = entrada.nextInt();
                        fileManager.descompactarArquivoLZW(versao);
                    }
                    case 10 -> {
                        System.out.print("Campo para busca (name, language, companies): ");
                        String campo = entrada.nextLine();
                    
                        System.out.print("Padrão a buscar: ");
                        String padrao = entrada.nextLine();
                    
                        try {
                            List<Serie> resultados = fileManager.buscarPorPadrao(campo, padrao, "kmp");
                            System.out.println(resultados.isEmpty() ? "Nenhum resultado encontrado." : resultados);
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    
                    case 11 -> {
                        System.out.print("Campo para busca (name, language, companies): ");
                        String campo = entrada.nextLine();
                    
                        System.out.print("Padrão a buscar: ");
                        String padrao = entrada.nextLine();
                    
                        try {
                            List<Serie> resultados = fileManager.buscarPorPadrao(campo, padrao, "boyer-moore");
                            System.out.println(resultados.isEmpty() ? "Nenhum resultado encontrado." : resultados);
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    

                    case 12 -> {
                        System.out.println("Saindo...");
                    }

                }
            } catch (IOException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } while (operacao != 11);
    
        entrada.close();
    }
    
    private static Serie obterDadosSerie(int id, Scanner entrada) {
        System.out.print("Nome da série: ");
        String name = entrada.nextLine();

        System.out.print("Linguagem: ");
        String language = entrada.nextLine();

        Date firstAirDate = null;
        while (firstAirDate == null) {
            System.out.print("Data de estreia (dd/MM/yyyy): ");
            String dateStr = entrada.nextLine();
            try {
                firstAirDate = dateFormat.parse(dateStr);
            } catch (ParseException e) {
                System.out.println("Formato de data inválido. Tente novamente.");
            }
        }

        ArrayList<String> companies = new ArrayList<>();
        System.out.println("Insira os nomes das companhias (digite 'fim' para parar):");
        while (true) {
            System.out.print("Companhia: ");
            String company = entrada.nextLine();
            if (company.equalsIgnoreCase("fim")) break;
            companies.add(company);
        }

        return new Serie(id, name, language, firstAirDate, companies);
    }
}
