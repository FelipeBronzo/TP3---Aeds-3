import java.io.Serializable;
import java.util.ArrayList;

class BPlusTreeNode implements Serializable {
    private static final long serialVersionUID = 1L; // Adiciona um serialVersionUID para controle

    boolean isLeaf;
    ArrayList<Integer> keys;
    ArrayList<Long> pointers; // Posições no arquivo de dados
    ArrayList<BPlusTreeNode> children;

    BPlusTreeNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.pointers = new ArrayList<>();
        this.children = new ArrayList<>();
    }
}
