package org.joshi.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private MessageHandler messageHandler;
    private final Socket socket;

    private final ObjectOutputStream out;

    public Client(int socketPort) throws IOException {
        socket = new Socket("localhost", socketPort);
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    public void start() {

        var clientThread = new Thread(() -> {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    Message msg = (Message) in.readObject();
                    if (msg != null) {
                        messageHandler.onMessage("SERVER_MSG", msg);
                    }
                }
            } catch (Exception ignore) {

            }
        });

        clientThread.start();
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void sendMsg(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void stop() throws IOException {
        socket.close();
    }
}
