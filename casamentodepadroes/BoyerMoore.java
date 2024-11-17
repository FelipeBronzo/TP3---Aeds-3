package casamentodepadroes;

public class BoyerMoore {
    // Realiza a busca do padrão no texto
    public static boolean search(String text, String pattern) {
        int[] badCharTable = buildBadCharTable(pattern);
        int m = pattern.length();
        int n = text.length();
        int shift = 0;

        while (shift <= n - m) {
            int j = m - 1;

            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }

            if (j < 0) {
                return true; // Padrão encontrado
            }

            // Atualiza o deslocamento usando a tabela de caracteres ruins
            shift += Math.max(1, j - badCharTable[text.charAt(shift + j)]);
        }

        return false; // Padrão não encontrado
    }

    // Constrói a tabela de caracteres ruins
    private static int[] buildBadCharTable(String pattern) {
        final int ALPHABET_SIZE = 256; // Tamanho da tabela ASCII
        int[] table = new int[ALPHABET_SIZE];

        // Inicializa com -1
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            table[i] = -1;
        }

        // Preenche as posições do padrão
        for (int i = 0; i < pattern.length(); i++) {
            table[pattern.charAt(i)] = i;
        }

        return table;
    }
}
