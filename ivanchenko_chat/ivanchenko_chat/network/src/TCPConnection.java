import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    private final  TCPConnectionUpServer eventUpServer;
    private final BufferedReader in;
    private final BufferedWriter out;

    // Конструктор, який приймає IP-адресу та порт і встановлює з'єднання
    public TCPConnection(TCPConnectionUpServer eventUpServer, String ipAdr, int port) throws IOException{
        this(eventUpServer, new Socket(ipAdr, port));
    }

    // Конструктор, який приймає готовий об'єкт Socket і встановлює з'єднання
    public TCPConnection(TCPConnectionUpServer eventUpServer, Socket socket) throws IOException {
        this.eventUpServer = eventUpServer;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventUpServer.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()){
                        eventUpServer.onReceiveString(TCPConnection.this, in.readLine());
                    }
                    String message = in.readLine();
                } catch (IOException e){
                    eventUpServer.onExepction(TCPConnection.this, e);
                } finally {
                    eventUpServer.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    // Метод для відправлення рядка через з'єднання
    public synchronized void sendMessage(String value){
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventUpServer.onExepction(TCPConnection.this, e);
            disconnect();
        }
    }

    // Метод для відключення з'єднання
    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventUpServer.onExepction(TCPConnection.this, e );
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }

}
