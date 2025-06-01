package FabricaChocolate;

import com.mongodb.client.*;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class NemTaoFantasticaFabricaDeChocolates extends JFrame {

    private MongoCollection<Document> collection;
    private JTextField nomeField, valorField, marcaField, codigoField;
    private JTable tabela;
    private DefaultTableModel model;

    private static final int ALTURA_CABECALHO_DESEJADA = 200;

    // Classe interna para o painel do cabeçalho com a imagem
    private static class ImageHeaderPanel extends JPanel {
        private Image image;
        private final int desiredHeight;
        private String errorMessage = null;

        public ImageHeaderPanel(String imagePath, int desiredHeight) {
            this.desiredHeight = desiredHeight;
            try {
                URL imgUrl = getClass().getResource(imagePath);
                if (imgUrl != null) {
                    this.image = new ImageIcon(imgUrl).getImage();
                    // Validação da imagem (getWidth/getHeight retornam -1 se a imagem não estiver carregada)
                    if (this.image.getWidth(null) == -1 || this.image.getHeight(null) == -1) {
                        throw new Exception("Dimensões da imagem inválidas após carregamento.");
                    }
                } else {
                    this.errorMessage = "Imagem do cabeçalho não encontrada: " + imagePath;
                    System.err.println(this.errorMessage);
                }
            } catch (Exception e) {
                this.image = null; // Garante que a imagem é nula se houver erro
                this.errorMessage = "Erro ao carregar imagem: " + e.getMessage();
                System.err.println(this.errorMessage);
                e.printStackTrace();
            }
            // Define a altura preferida. A largura será gerenciada pelo BorderLayout.
            setPreferredSize(new Dimension(10, desiredHeight)); // Largura inicial pequena, altura é a chave
        }

        @Override
        public Dimension getPreferredSize() {
            // Garante que a altura preferida seja sempre a desejada.
            // A largura será a largura atual do painel se já definida, ou uma largura base.
            int width = (getParent() != null && getParent().getWidth() > 0) ? getParent().getWidth() : super.getPreferredSize().width;
            return new Dimension(width, desiredHeight);
        }
        
        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                int panelWidth = getWidth();
                int panelHeight = getHeight(); // Deve ser igual a desiredHeight

                int imgOriginalWidth = image.getWidth(null);
                int imgOriginalHeight = image.getHeight(null);

                if (imgOriginalWidth <= 0 || imgOriginalHeight <= 0) {
                    drawErrorMessage(g, "Dimensões da imagem originais inválidas.");
                    return;
                }

                // Lógica de redimensionamento para caber no painel mantendo a proporção
                double imgAspectRatio = (double) imgOriginalWidth / imgOriginalHeight;
                double panelAspectRatio = (double) panelWidth / panelHeight;

                int scaledImgWidth;
                int scaledImgHeight;

                if (imgAspectRatio > panelAspectRatio) { // Imagem mais larga que o painel (relativamente)
                    scaledImgWidth = panelWidth;
                    scaledImgHeight = (int) (panelWidth / imgAspectRatio);
                } else { // Imagem mais alta que o painel (relativamente) ou proporção igual
                    scaledImgHeight = panelHeight;
                    scaledImgWidth = (int) (panelHeight * imgAspectRatio);
                }
                
                // Centraliza a imagem
                int x = (panelWidth - scaledImgWidth) / 2;
                int y = (panelHeight - scaledImgHeight) / 2;

                g.drawImage(image, x, y, scaledImgWidth, scaledImgHeight, this);

            } else if (errorMessage != null) {
                drawErrorMessage(g, errorMessage);
            } else {
                drawErrorMessage(g, "Imagem não disponível.");
            }
        }

        private void drawErrorMessage(Graphics g, String message) {
            // Garante que o fundo seja pintado se não houver imagem
            g.setColor(getBackground() != null ? getBackground() : Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            FontMetrics fm = g.getFontMetrics();
            int stringWidth = fm.stringWidth(message);
            int stringAscent = fm.getAscent();
            int xPos = (getWidth() - stringWidth) / 2;
            int yPos = (getHeight() + stringAscent) / 2 - fm.getDescent();
            g.drawString(message, Math.max(0, xPos), Math.max(fm.getHeight(), yPos)); // Evita x,y < 0
        }
    }


    public NemTaoFantasticaFabricaDeChocolates() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("loja");
        collection = database.getCollection("chocolates");

        setTitle("Loja de Chocolates");
        setSize(700, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            URL iconUrl = getClass().getResource("/FabricaChocolate/chocolate.png");
            if (iconUrl != null) setIconImage(new ImageIcon(iconUrl).getImage());
            else System.err.println("Recurso do ícone não encontrado: /FabricaChocolate/chocolate.png");
        } catch (Exception e) { e.printStackTrace(); }

        Color begeClaro = new Color(255, 248, 220);
        Font fonteCampos = new Font("SansSerif", Font.PLAIN, 14);

        // --- Painel do Cabeçalho ---
        ImageHeaderPanel painelCabecalho = new ImageHeaderPanel("/FabricaChocolate/fundo_chocolate.jpeg", ALTURA_CABECALHO_DESEJADA);
        painelCabecalho.setBackground(new Color(253, 237, 197));

        // --- Painel de Conteúdo Principal ---
        JPanel painelConteudoPrincipal = new JPanel(new BorderLayout());
        painelConteudoPrincipal.setBackground(begeClaro);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        nomeField = new JTextField(); valorField = new JTextField();
        marcaField = new JTextField(); codigoField = new JTextField();
        nomeField.setFont(fonteCampos); valorField.setFont(fonteCampos);
        marcaField.setFont(fonteCampos); codigoField.setFont(fonteCampos);
        form.add(new JLabel("Nome:")); form.add(nomeField);
        form.add(new JLabel("Valor:")); form.add(valorField);
        form.add(new JLabel("Marca:")); form.add(marcaField);
        form.add(new JLabel("Código de Barras:")); form.add(codigoField);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botoes.setOpaque(false);
        botoes.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        JButton adicionarBtn = new JButton("Adicionar"); JButton atualizarBtn = new JButton("Atualizar");
        JButton excluirBtn = new JButton("Excluir"); JButton mostrarBtn = new JButton("Mostrar");
        botoes.add(adicionarBtn); botoes.add(atualizarBtn);
        botoes.add(excluirBtn); botoes.add(mostrarBtn);

        JPanel painelFormularioBotoes = new JPanel(new BorderLayout());
        painelFormularioBotoes.setOpaque(false);
        painelFormularioBotoes.add(form, BorderLayout.NORTH);
        painelFormularioBotoes.add(botoes, BorderLayout.SOUTH);

        model = new DefaultTableModel(new String[]{"Nome", "Valor", "Marca", "Código"}, 0);
        tabela = new JTable(model);
        tabela.setBackground(begeClaro);
        tabela.setFont(fonteCampos);
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(25);
        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.getViewport().setBackground(begeClaro);
        scrollTabela.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        
        scrollTabela.setPreferredSize(new Dimension(600, 300));

        painelConteudoPrincipal.add(painelFormularioBotoes, BorderLayout.NORTH);
        painelConteudoPrincipal.add(scrollTabela, BorderLayout.CENTER);

        JPanel painelMestre = new JPanel(new BorderLayout());
        painelMestre.add(painelCabecalho, BorderLayout.NORTH);
        painelMestre.add(painelConteudoPrincipal, BorderLayout.CENTER);

        JScrollPane scrollPaneJanela = new JScrollPane(painelMestre);
        scrollPaneJanela.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        scrollPaneJanela.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        setContentPane(scrollPaneJanela);

        adicionarBtn.addActionListener(e -> adicionarProduto());
        atualizarBtn.addActionListener(e -> atualizarProduto());
        excluirBtn.addActionListener(e -> excluirProduto());
        mostrarBtn.addActionListener(e -> mostrarProdutos());

        tabela.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tabela.getSelectedRow();
                if (row != -1) {
                    nomeField.setText(model.getValueAt(row, 0).toString());
                    valorField.setText(model.getValueAt(row, 1).toString().replace("R$ ", "").replace(",", "."));
                    marcaField.setText(model.getValueAt(row, 2).toString());
                    codigoField.setText(model.getValueAt(row, 3).toString());
                }
            }
        });
        
        // Carrega os produtos ao iniciar
        mostrarProdutos();
    }

    
    private void adicionarProduto() {
        String nome = nomeField.getText();
        if (nome.isEmpty() || valorField.getText().isEmpty() || marcaField.getText().isEmpty() || codigoField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos devem ser preenchidos!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            double valor = Double.parseDouble(valorField.getText().replace(",", "."));
            String marca = marcaField.getText();
            String codigo = codigoField.getText();

            if (collection.find(eq("codigoBarras", codigo)).first() != null) {
                JOptionPane.showMessageDialog(this, "Já existe um produto com este código de barras!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Document doc = new Document("nome", nome)
                    .append("valor", valor)
                    .append("marca", marca)
                    .append("codigoBarras", codigo);

            collection.insertOne(doc);
            JOptionPane.showMessageDialog(this, "Produto adicionado!");
            limparCampos();
            mostrarProdutos();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O valor do produto é inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarProduto() {
        String codigoOriginal = "";
        int selectedRow = tabela.getSelectedRow();

        if (selectedRow == -1 && codigoField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela ou informe o código de barras para atualizar.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (selectedRow != -1) {
             codigoOriginal = model.getValueAt(selectedRow, 3).toString();
        } else {
            codigoOriginal = codigoField.getText();
        }

        if (nomeField.getText().isEmpty() || valorField.getText().isEmpty() || marcaField.getText().isEmpty() || codigoField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos devem ser preenchidos para atualização!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String novoCodigo = codigoField.getText();

        try {
            Document docAtualizado = new Document("nome", nomeField.getText())
                    .append("valor", Double.parseDouble(valorField.getText().replace(",", ".")))
                    .append("marca", marcaField.getText())
                    .append("codigoBarras", novoCodigo);

            if (!novoCodigo.equals(codigoOriginal)) {
                if (collection.find(eq("codigoBarras", novoCodigo)).first() != null) {
                    JOptionPane.showMessageDialog(this, "O novo código de barras já está em uso por outro produto!", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            var result = collection.replaceOne(eq("codigoBarras", codigoOriginal), docAtualizado);
            
            if (result.getModifiedCount() > 0) {
                JOptionPane.showMessageDialog(this, "Produto atualizado!");
            } else {
                 JOptionPane.showMessageDialog(this, "Nenhum produto encontrado com o código original para atualizar ou os dados são os mesmos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            limparCampos();
            mostrarProdutos(); 
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O valor do produto é inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirProduto() {
        String codigo = "";
        int selectedRow = tabela.getSelectedRow();

        if (selectedRow != -1) {
            codigo = model.getValueAt(selectedRow, 3).toString();
            // codigoField.setText(codigo); // Opcional: preencher o campo
        } else {
            codigo = codigoField.getText();
        }
        
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela ou informe o código de barras para excluir.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o produto com código: " + codigo + "?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            var result = collection.deleteOne(eq("codigoBarras", codigo));
            if (result.getDeletedCount() > 0) {
                JOptionPane.showMessageDialog(this, "Produto excluído!");
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum produto encontrado com este código para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            limparCampos();
            mostrarProdutos();
        }
    }

    private void mostrarProdutos() {
        model.setRowCount(0);
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                String nome = d.getString("nome");
                Double valorDouble = d.getDouble("valor");
                String valorFormatado = "N/A";
                if (valorDouble != null) {
                    valorFormatado = String.format("R$ %.2f", valorDouble);
                }
                String marca = d.getString("marca");
                String codigo = d.getString("codigoBarras");
                model.addRow(new Object[]{nome, valorFormatado, marca, codigo});
            }
        }
    }

    private void limparCampos() {
        nomeField.setText("");
        valorField.setText("");
        marcaField.setText("");
        codigoField.setText("");
        tabela.clearSelection();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new NemTaoFantasticaFabricaDeChocolates().setVisible(true);
        });
    }
}