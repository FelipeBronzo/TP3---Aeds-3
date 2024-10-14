import java.io.*;


class BPlusTree implements Serializable {
    private BPlusTreeNode root;
    private int order;
    private final String indexFilePath = "dados/bplus_tree.idx"; // Caminho do arquivo de índice

    public BPlusTree(int order) {
        this.root = new BPlusTreeNode(true);
        this.order = order;
        carregar(); // Carregar o índice a partir do arquivo ao iniciar
    }

    public void insert(int id, long position) {
        BPlusTreeNode node = root;
        if (node.keys.size() == 2 * order - 1) {
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.children.add(root);
            splitChild(newRoot, 0, root);
            root = newRoot;
            insertNonFull(root, id, position);
        } else {
            insertNonFull(node, id, position);
        }
        salvar(); // Salva a árvore após cada inserção
    }

    private void splitChild(BPlusTreeNode parent, int index, BPlusTreeNode child) {
        BPlusTreeNode newChild = new BPlusTreeNode(child.isLeaf);
        int middle = order - 1;

        parent.keys.add(index, child.keys.get(middle));
        parent.children.add(index + 1, newChild);

        for (int i = middle + 1; i < child.keys.size(); i++) {
            newChild.keys.add(child.keys.get(i));
            if (child.isLeaf) {
                newChild.pointers.add(child.pointers.get(i));
            }
        }

        if (!child.isLeaf) {
            for (int i = middle + 1; i < child.children.size(); i++) {
                newChild.children.add(child.children.get(i));
            }
        }

        child.keys.subList(middle, child.keys.size()).clear();
        if (child.isLeaf) {
            child.pointers.subList(middle, child.pointers.size()).clear();
        } else {
            child.children.subList(middle + 1, child.children.size()).clear();
        }
    }

    private void insertNonFull(BPlusTreeNode node, int id, long position) {
        int i = node.keys.size() - 1;
        if (node.isLeaf) {
            while (i >= 0 && id < node.keys.get(i)) {
                i--;
            }
            node.keys.add(i + 1, id);
            node.pointers.add(i + 1, position);
        } else {
            while (i >= 0 && id < node.keys.get(i)) {
                i--;
            }
            i++;
            if (node.children.get(i).keys.size() == 2 * order - 1) {
                splitChild(node, i, node.children.get(i));
                if (id > node.keys.get(i)) {
                    i++;
                }
            }
            insertNonFull(node.children.get(i), id, position);
        }
    }

    public Long search(int id) {
        return search(root, id);
    }

    private Long search(BPlusTreeNode node, int id) {
        int i = 0;
        while (i < node.keys.size() && id > node.keys.get(i)) {
            i++;
        }
        if (i < node.keys.size() && id == node.keys.get(i)) {
            return node.isLeaf ? node.pointers.get(i) : null;
        } else if (node.isLeaf) {
            return null;
        } else {
            return search(node.children.get(i), id);
        }
    }

    // Método para remover um id da Árvore B+
    public void remove(int id) {
        remove(root, id);
        salvar(); // Salva a árvore após a remoção
    }

    private void remove(BPlusTreeNode node, int id) {
        int i = 0;
        while (i < node.keys.size() && id > node.keys.get(i)) {
            i++;
        }

        // Caso 1: O nó é folha e contém o id a ser removido
        if (node.isLeaf) {
            if (i < node.keys.size() && node.keys.get(i) == id) {
                node.keys.remove(i);
                node.pointers.remove(i);
            }
            return;
        }

        // Caso 2: O nó não é folha
        if (i < node.keys.size() && node.keys.get(i) == id) {
            if (node.isLeaf) {
                node.keys.remove(i);
                node.pointers.remove(i);
            } else {
                BPlusTreeNode child = node.children.get(i);
                remove(child, id);
            }
        } else {
            if (!node.isLeaf) {
                BPlusTreeNode child = node.children.get(i);
                remove(child, id);
            }
        }
    }

    // Método para salvar a árvore em um arquivo de índice
    public void salvar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFilePath))) {
            oos.writeObject(root);
        } catch (NotSerializableException e) {
            System.out.println("Erro: uma parte da Árvore B+ não é serializável: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro ao salvar o índice da Árvore B+: " + e.getMessage());
        }
    }

    // Método para carregar a árvore do arquivo de índice
    public void carregar() {
        File file = new File(indexFilePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFilePath))) {
                root = (BPlusTreeNode) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Erro ao carregar o índice da Árvore B+: " + e.getMessage());
            }
        }
    }
}
