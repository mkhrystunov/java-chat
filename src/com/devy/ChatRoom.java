package com.devy;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatRoom extends Applet {
    protected boolean inAnApplet = true;
    protected boolean loggedIn;
    protected Frame cp;
    protected static int PORTNUM = Chat.PORTNUM;
    protected int port;
    protected Socket socket;
    protected BufferedReader is;
    protected PrintWriter pw;
    protected TextField textField;
    protected TextArea textArea;
    protected Button lib;
    protected Button lob;
    final static String TITLE = "Chat: Toy Chat Room Client";
    protected String paintMessage;

    public void init() {
        paintMessage = "Creating window for chat";
        repaint();
        cp = new Frame(TITLE);
        cp.setLayout(new BorderLayout());
        String portNum = null;
        if (inAnApplet)
            portNum = getParameter("port");
        port = PORTNUM;
        if (portNum != null)
            port = Integer.parseInt(portNum);

        // GUI
        textArea = new TextArea(14, 80);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cp.add(BorderLayout.NORTH, textArea);

        Panel p = new Panel();
        Button b;

        p.add(lib = new Button("Login"));
        lib.setEnabled(true);
        lib.requestFocus();
        lib.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                login();
                lib.setEnabled(false);
                lob.setEnabled(true);
                textField.requestFocus();
            }
        });

        p.add(lob = new Button("Logout"));
        lob.setEnabled(false);
        lob.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                logout();
                lib.setEnabled(true);
                lob.setEnabled(false);
                lib.requestFocus();
            }
        });

        p.add(new Label("Message here:"));
        textField = new TextField(40);
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (loggedIn) {
                    pw.println(Chat.CMD_BCAST + textField.getText());
                    textField.setText("");
                }
            }
        });
        p.add(textField);

        cp.add(BorderLayout.SOUTH, p);

        cp.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                ChatRoom.this.cp.setVisible(false);
                ChatRoom.this.cp.dispose();
                logout();
            }
        });
        cp.pack();
        Dimension us = cp.getSize(),
                them = Toolkit.getDefaultToolkit().getScreenSize();
        int newX = (them.width - us.width) / 2;
        int newY = (them.height - us.height) / 2;
        cp.setLocation(newX, newY);
        cp.setVisible(true);
        paintMessage = "Window should now be visible";
        repaint();
    }

    protected String serverHost = "localhost";

    public void login() {
        showStatus("In login!");
        if (loggedIn)
            return;
        if (inAnApplet)
            serverHost = getCodeBase().getHost();
        try {
            socket = new Socket(serverHost, port);
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            showStatus("Can't get socket to " + serverHost + "/" + port + ":" + e);
            cp.add(new Label("Can't get socket: " + e));
            return;
        }
        showStatus("Got socket");

        new Thread(new Runnable() {
            @Override
            public void run() {
                String line;
                try {
                    while (loggedIn && ((line = is.readLine()) != null))
                        textArea.append(line + "\n");
                } catch (IOException e) {
                    showStatus("LOST THE LINK!");
                    return;
                }
            }
        }).start();

        pw.println(Chat.CMD_LOGIN + "AppletUser");
        loggedIn = true;
    }

    public void logout() {
        if (!loggedIn)
            return;
        loggedIn = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {

        }
    }

    public void paint(Graphics g) {
        Dimension d = getSize();
        int h = d.height;
        int w = d.width;
        g.fillRect(0, 0, w, 0);
        g.setColor(Color.BLACK);
        g.drawString(paintMessage, 10, (h / 2) - 5);
    }

    public void showStatus(String status) {
        if (inAnApplet)
            super.showStatus(status);
        System.out.println(status);
    }

    public static void main(String[] args) {
        ChatRoom room = new ChatRoom();
        room.inAnApplet = false;
        room.init();
        room.start();
    }
}
