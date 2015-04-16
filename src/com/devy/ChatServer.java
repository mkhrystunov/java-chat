package com.devy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    protected final static String CHATMASTER_ID = "ChatMaster";
    protected final static String SEP = ": ";
    protected ServerSocket serverSocket;
    protected final ArrayList<ChatHandler> clients;
    private static boolean DEBUG = false;

    public static void main(String[] args) {
        System.out.println("Devy Chat Server starting...");
        if (args.length == 1 && args[0].equals("-debug"))
            DEBUG = true;
        ChatServer server = new ChatServer();
        server.runServer();
        System.out.println("**ERROR* Chat Server quitting");
    }

    ChatServer() {
        clients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(Chat.PORTNUM);
            System.out.println("Devy Chat Server Listening on port " + Chat.PORTNUM);
        } catch (IOException e) {
            log("IOException in Chat Server.<init>" + e);
            System.exit(0);
        }
    }

    public void runServer() {
        try {
            while (true) {
                Socket us = serverSocket.accept();
                String hostName = us.getInetAddress().getHostName();
                log("Accepted from " + hostName);
                ChatHandler cl = new ChatHandler(us, hostName);
                synchronized (clients) {
                    clients.add(cl);
                    cl.start();
                    if (clients.size() == 1)
                        cl.send(CHATMASTER_ID, "Welcome! you're the first one here");
                    else
                        cl.send(CHATMASTER_ID, "Welcome! you're the latest of " + clients.size() + " users.");
                }
            }
        } catch (IOException e) {
            log("IOException in runServer: " + e);
            System.exit(0);
        }
    }

    protected void log(String s) {
        System.out.println(s);
    }

    protected class ChatHandler extends Thread {
        protected Socket clientSocket;
        protected BufferedReader is;
        protected PrintWriter pw;
        protected String clientIP;
        protected String login;

        public ChatHandler(Socket socket, String clnt) throws IOException {
            clientSocket = socket;
            clientIP = clnt;
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);
        }

        public void run() {
            String line;
            try {
                while ((line = is.readLine()) != null) {
                    char c = line.charAt(0);
                    line = line.substring(1);
                    switch (c) {
                        case Chat.CMD_LOGIN:
                            if (!Chat.isValidLoginName(line)) {
                                send(CHATMASTER_ID, "LOGIN " + line + " invalid");
                                log("LOGIN INVALID from " + clientIP);
                                continue;
                            }
                            login = line;
                            broadcast(CHATMASTER_ID, login + " joins us, for a total of " + clients.size() + " users.");
                            break;
                        case Chat.CMD_MSG:
                            if (login == null) {
                                send(CHATMASTER_ID, "please login first");
                                continue;
                            }
                            int where = line.indexOf(Chat.SEPARATOR);
                            if (where < 0 ){
                                psend(CHATMASTER_ID, "Please provide login and message in format: login\\message.");
                                break;
                            }
                            String recipient = line.substring(0, where);
                            String msg = line.substring(where + 1);
                            log("MSG: " + login + "-->" + recipient + ": " + msg);
                            ChatHandler cl = lookup(recipient);
                            if (cl == null)
                                psend(CHATMASTER_ID, recipient + " not logged in.");
                            else
                                cl.psend(login, msg);
                            break;
                        case Chat.CMD_QUIT:
                            broadcast(CHATMASTER_ID, "Goodbye to " + login + "@" + clientIP);
                            close();
                            return;
                        case Chat.CMD_BCAST:
                            if (login != null)
                                broadcast(login, line);
                            else
                                log("B<L FROM " + clientIP);
                            break;
                        default:
                            log("Unknown cmd " + c + " from " + login + "@" + clientIP);
                    }
                }
            } catch (IOException e) {
                log("IOException " + e);
            } finally {
                log(login + SEP + "All Done");
                synchronized (clients) {
                    clients.remove(this);
                    if (clients.size() == 0) {
                        log("I'm so lonely...");
                    } else if (clients.size() == 1) {
                        ChatHandler last = clients.get(0);
                        last.send(CHATMASTER_ID, "Hey, you're talking to yourself again.");
                    } else {
                        broadcast(CHATMASTER_ID, "There are now " + clients.size() + " users");
                    }
                }
            }
        }

        protected void close() {
            if (clientSocket == null) {
                log("close when not open");
                return;
            }
            try {
                clientSocket.close();
                clientSocket = null;
            } catch (IOException e) {
                log("Failure during close to " + clientIP);
            }
        }

        public void send(String sender, String msg) {
            pw.println(sender + SEP + msg);
        }

        protected void psend(String sender, String msg) {
            send("<*" + sender + "*>", msg);
        }

        public void broadcast(String sender, String msg) {
            log("Broadcasting " + sender + SEP + msg);
            for (Object client : clients) {
                ChatHandler sib = (ChatHandler) client;
                if (DEBUG)
                    log("Sending to " + sib);
                sib.send(sender, msg);
            }
            if (DEBUG)
                log("Done broadcast");
        }

        protected ChatHandler lookup(String nick) {
            synchronized (clients) {
                for (Object client : clients) {
                    ChatHandler cl = (ChatHandler) client;
                    if (cl.login.equals(nick))
                        return cl;
                }
            }
            return null;
        }

        public String toString() {
            return "ChatHandler[" + login + "]";
        }
    }
}
