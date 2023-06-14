import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ChatClient extends JFrame implements ActionListener, TCPConnectionUpServer {

    private static final String IP_ADR = "192.168.10.10";
    private static final int PORT= 8010;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }

    private final JTextArea login = new JTextArea();
    private final JTextField fieldNickname = new JTextField("Anna");
    private final JTextField fieldInput = new JTextField();

    private TCPConnection connection;
    private ChatClient(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        login.setEditable(false);
        login.setLineWrap(true);
        add(login, BorderLayout.CENTER);
        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADR, PORT);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Обробка події введення тексту та надсилання повідомлення
        String message = fieldInput.getText();
        if (message.equals("")) return; // Якщо введений текст пустий, то не виконувати подальші дії
        fieldInput.setText(null); // Очистити поле вводу тексту
        connection.sendMessage(fieldNickname.getText() + ": " + message); // Надіслати повідомлення через TCP з'єднання
    }


    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        // Обробка події підключення TCP з'єднання
        printMessage("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        // Обробка події отримання повідомлення по TCP з'єднанню
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        // Обробка події відключення TCP з'єднання
        printMessage("Connection close");
    }

    @Override
    public void onExepction(TCPConnection tcpConnection, Exception e) {
        // Обробка події виникнення виключення при роботі з TCP з'єднанням
        printMessage("Connection Exception");
    }

    private synchronized void printMessage(String message){
        // Метод для виведення повідомлення в JTextArea
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               login.append(message + "\n");
               login.setCaretPosition(login.getDocument().getLength());
            }
        });
    }
}
