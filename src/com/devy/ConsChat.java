package com.devy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsChat {
    public static void main(String[] args) throws IOException {
        new ConsChat().chat();
    }

    protected Socket socket;
    protected BufferedReader is;
    protected PrintWriter pw;
    protected BufferedReader cons;

    protected ConsChat() throws IOException {
        socket = new Socket("localhost", Chat.PORTNUM);
        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
        cons = new BufferedReader(new InputStreamReader(System.in));

        new Thread() {
            @Override
            public void run() {
                setName("socket reader thread");
                System.out.println("Starting " + getName());
                System.out.flush();
                String line;
                try {
                    while ((line = is.readLine()) != null) {
                        System.out.println(line);
                        System.out.flush();
                    }
                } catch (IOException ex) {
                    System.err.println("Read error on socket: " + ex);
                }
            }
        }.start();
    }

    protected void chat() throws IOException {
        String text;

        System.out.println("Login name: ");
        System.out.flush();
        text = cons.readLine();
        send(Chat.CMD_LOGIN + text);

        while((text = cons.readLine()) != null) {
            if (text.length() == 0 || text.charAt(0) == '#')
                continue; // ignore null lines and comments
            if (text.charAt(0) == '/') {
                send(text.substring(1));
                if (text.charAt(1) == Chat.CMD_QUIT)
                    System.exit(0);
            }
            else
                send("B"+text);
        }
    }

    protected void send(String s) {
        pw.println(s);
        pw.flush();
    }
}
