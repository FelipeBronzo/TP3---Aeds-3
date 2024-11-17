package casamentodepadroes;

public class KMP {
    // Verifica se o padrão existe no texto
    public static boolean search(String text, String pattern) {
        int[] failureTable = buildFailureTable(pattern);
        int j = 0;

        for (int i = 0; i < text.length(); i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = failureTable[j - 1];
            }

            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }

            if (j == pattern.length()) {
                return true; // Padrão encontrado
            }
        }
        return false; // Padrão não encontrado
    }

    // Constrói a tabela de falhas para o padrão
    private static int[] buildFailureTable(String pattern) {
        int[] table = new int[pattern.length()];
        int j = 0;

        for (int i = 1; i < pattern.length(); i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = table[j - 1];
            }

            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }

            table[i] = j;
        }
        return table;
    }
}

