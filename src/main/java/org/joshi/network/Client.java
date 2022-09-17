package org.joshi.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private final MessageHandler messageHandler;
    private final Socket socket;

    private final ObjectOutputStream out;

    public Client(int socketPort, MessageHandler messageHandler) throws IOException {
        socket = new Socket("localhost", socketPort);
        out = new ObjectOutputStream(socket.getOutputStream());
        this.messageHandler = messageHandler;
    }

    public void start() {

        var clientThread = new Thread(() -> {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Message msg = (Message) in.readObject();
                if (msg != null) {
                    messageHandler.onMessage("SERVER_MSG", msg);
                }
            } catch (Exception ex) {
                System.out.println("Socket closed due to exception.");
            }
        });

        clientThread.start();
    }

    public void sendMsg(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public void stop() throws IOException {
        socket.close();
    }
}
