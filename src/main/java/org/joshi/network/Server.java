package org.joshi.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP server that allows sending and receiving messages.
 */
public class Server {

    /**
     * A map containing the uuid and associated client handler.
     */
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    private final MessageHandler messageHandler;
    private final ServerSocket socket;

    public Server(int socketPort, MessageHandler messageHandler) throws IOException {
        socket = new ServerSocket(socketPort);
        this.messageHandler = messageHandler;
    }

    /**
     * Start server.
     *
     * @param numClients number of clients to wait for
     */
    public void start(int numClients) throws IOException {
        for (int i = 0; i < numClients; i++) {
            String id = UUID.randomUUID().toString();
            ClientHandler handler = new ClientHandler(socket.accept(), id, messageHandler);
            clients.put(id, handler);
            handler.start();
        }
    }

    public void sendMsg(String id, Message msg) throws IOException {
        clients.get(id).sendMsg(msg);
    }

    /**
     * Stop server.
     */
    public void stop() throws IOException {
        socket.close();
    }

    /**
     * Class that handles each client.
     */
    private static class ClientHandler extends Thread {
        private final Socket socket;
        private ObjectOutputStream out;

        /**
         * The id of this client.
         */
        private final String id;

        private final MessageHandler messageHandler;

        public ClientHandler(Socket socket, String id, MessageHandler messageHandler) {
            this.socket = socket;
            this.id = id;
            this.messageHandler = messageHandler;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Message msg = (Message) in.readObject();
                if (msg != null) {
                    messageHandler.onMessage(id, msg);
                }
            } catch (Exception ex) {
                System.out.println("Socket closed due to exception.");
            }
        }

        /**
         * Send message to this client.
         *
         * @param msg message
         */
        void sendMsg(Message msg) throws IOException {
            out.writeObject(msg);
            out.flush();
        }
    }
}