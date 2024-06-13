package JavaZap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;
import javax.swing.BorderFactory;

public class TelaJavaZap extends JFrame implements ActionListener {

    private JTextField inputTextField;
    private JTextArea chatArea;
    private JButton sendButton;
    private Socket clientSocket;
    private PrintStream outputStream; // Variável para a saída do socket
    private String clientId;
    private String userName; // Armazena o nome do usuário
    private String currentDateTime; // Armazena a data e hora atuais
    private String otherUserName; // Armazena o nome do outro cliente

    public TelaJavaZap() throws UnknownHostException, IOException {
        //Cabecalho
        super("WhatsApp 2");
        ImageIcon icon = new ImageIcon(getClass().getResource("/image/zap.png.png")); // Corrigido
        Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        setIconImage(image);
        setSize(400, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); //Controla a posição dos componentes

        JPanel contentPane = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Carregue a imagem
                ImageIcon icon = new ImageIcon(getClass().getResource("/image/RuaAzul.gif"));
                Image image = icon.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this); // Desenha a imagem
            }
        };


        // Mostra as Mensagens
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setForeground(Color.white); //Cor do texto da área de mensagens como branca
        chatArea.setOpaque(false); //Área de mensagens como transparente
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBounds(10, 10, 370, 450); // Define a posição e o tamanho da área de mensagens
        scrollPane.setOpaque(false); // Define o scrollPane como transparente
        scrollPane.getViewport().setOpaque(false); //viewport do scrollPane como transparente
        contentPane.add(scrollPane);

        // Caixa de texto de entrada + botão
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputTextField = new JTextField(25);
        inputTextField.setForeground(Color.black); // cor do texto do campo de entrada como preto
        sendButton = new JButton("Enviar");
        sendButton.setPreferredSize(new Dimension(100, 30));
        sendButton.setBackground(Color.GREEN);
        sendButton.setForeground(Color.white);
        sendButton.addActionListener(this);
        sendButton.setBorder(BorderFactory.createBevelBorder(10)); //Botao pra baixo do campo pesquisa
        inputPanel.add(inputTextField);
        inputPanel.add(sendButton);
        inputPanel.setOpaque(false);
        inputPanel.setBounds(10, 470, 370, 60); //posição e o tamanho dos componentes
        contentPane.add(inputPanel);
        setContentPane(contentPane);

        // Cria um diálogo para o usuário inserir o nome
        userName = JOptionPane.showInputDialog(this, "Digite seu nome:");

        // Se o usuário clicou em "Cancelar"
        if (userName == null) {
            System.err.println("O usuário cancelou a entrada do nome.");
            System.exit(0);
        }

        // Pega a data e hora atuais
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        currentDateTime = formatter.format(now);

        // Gera um identificador único para o cliente
        clientId = UUID.randomUUID().toString();

        // Thread para lidar com a conexão e a comunicação
        new Thread(() -> {
            try {
                // Conexão com o servidor
                clientSocket = new Socket(ConfigConexao.SERVER_ADDRESS, ConfigConexao.SERVER_PORT);
                System.out.println("Conectado ao servidor!");

                // Inicializar outputStream APÓS a conexão ser estabelecida
                outputStream = new PrintStream(clientSocket.getOutputStream());

                // Envie o identificador do cliente para o servidor
                outputStream.println(clientId);
                outputStream.println(userName);

                Scanner input = new Scanner(clientSocket.getInputStream());
                while (input.hasNextLine()) {
                    String message = input.nextLine();

                    // Processa a mensagem recebida
                    if (message.contains(":")) { // Verifica se a mensagem contém o separador ":"
                        String[] parts = message.split(":");
                        if (parts.length == 4) { // Verifica se a mensagem tem 4 partes
                            String senderId = parts[0];
                            otherUserName = parts[1]; // Extrai o nome do remetente
                            String currentDateTime = parts[2]; // Extrai a data/hora da mensagem
                            String messageContent = parts[3];

                            // **Aqui está a mudança: Incluindo os parênteses**
                            chatArea.append(String.format("%-30s", otherUserName + " (" + currentDateTime + "): " + messageContent) + "\n");
                        } else {
                            // Se a mensagem não tiver 3 partes, exibe-a como está
                            chatArea.append(message + "\n");
                        }
                    } else {
                        // Se a mensagem não contiver ":", exibe-a como está
                        chatArea.append(message + "\n");
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
            } finally {
                try {
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao fechar a conexão com o servidor: " + e.getMessage());
                }
            }
        }).start();

        setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = inputTextField.getText();
            if (!message.isEmpty()) {
                // Exibe a mensagem na área de chat
                chatArea.append(String.format("%-30s", userName + " (" + currentDateTime + "): " + message) + "\n");

                // Envie a mensagem para o servidor
                if (outputStream != null) { // Verifica se outputStream está inicializado
                    outputStream.println(message);
                }
                inputTextField.setText("");
            }
        }
    }

    // public static void main(String[] args) throws UnknownHostException, IOException {
    //     new TelaJavaZap("192.168.0.10", 12000);
    // }
}