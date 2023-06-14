import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionUpServer {

    public static void main(String[] args){
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private ChatServer(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(8010)) {
            while (true){
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e){
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        // Обробка події підключення TCP з'єднання
        connections.add(tcpConnection);
        sendToAllConnections("Clint connected: " + tcpConnection);
    }

    // Обробка події отримання повідомлення по TCP з'єднанню
    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        // Обробка події відключення TCP з'єднання
        connections.remove(tcpConnection);
        sendToAllConnections("Clint disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onExepction(TCPConnection tcpConnection, Exception e) {
        // Обробка події виникнення виключення при роботі з TCP з'єднанням
        System.out.println("TCPConnection exception: " + e);
    }
    
    private void sendToAllConnections(String value){
        // Надсилання повідомлення всім підключеним клієнтам
        System.out.println(value);
        final int conSize = connections.size();
        for (int i = 0; i < connections.size(); i++) {
            connections.get(i).sendMessage(value);
        }
    }
}
