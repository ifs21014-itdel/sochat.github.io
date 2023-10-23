package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ServerFrame extends JFrame implements Runnable {

    JButton btStart, btStop;
    JTextArea taInfo;
    ServerSocket serverSocket;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    ServerThread serverThread;

    public ServerFrame() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel lbStateServer = new JLabel("Server's status");
        lbStateServer.setFont(new Font("Comic Sans MS", Font.BOLD, 18));

        taInfo = new JTextArea();
        taInfo.setEditable(false);
        taInfo.setFont(new Font("Serif", Font.PLAIN, 16));
        taInfo.setBackground(Color.BLACK);
        taInfo.setForeground(Color.CYAN);
        JScrollPane scroll = new JScrollPane(taInfo);
        scroll.setPreferredSize(new Dimension(400, 400));

        btStart = new JButton("Start Server");
        btStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btStartEvent(ae);
            }
        });

        btStop = new JButton("Stop Server");
        btStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btStopEvent(ae);
            }
        });
        btStart.setEnabled(true);
        btStop.setEnabled(false);

        JPanel panelBtn = new JPanel();
        panelBtn.add(btStart);
        panelBtn.add(btStop);

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(lbStateServer, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelBtn, BorderLayout.SOUTH);

        this.add(panel);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Membuat frame muncul di tengah layar
    }

    public void appendMessage(String message) {
        taInfo.append(message + "\n");
        taInfo.setCaretPosition(taInfo.getDocument().getLength());
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(9999);
            appendMessage("[" + sdf.format(new Date()) + "] Server is running and ready to serve any client...");
            appendMessage("[" + sdf.format(new Date()) + "] Now there's no one connected to the server.");

            while (true) {
                Socket socketOfServer = serverSocket.accept();
                serverThread = new ServerThread(socketOfServer);
                serverThread.taServer = this.taInfo;
                serverThread.start();
            }

        } catch (java.net.SocketException e) {
            System.out.println("Server is closed");
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("An error occurred. It may be due to: Another server is occupying this port!");
            System.out.println("Or the server has already been closed.");
            JOptionPane.showMessageDialog(this, "Another server is occupying this port.", "Error", JOptionPane.ERROR_MESSAGE);
            this.setVisible(false);
            System.exit(0);
        }
    }

    private void btStartEvent(ActionEvent ae) {
        Connection conn = new UserDatabase().connect();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Please open MySQL first.", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new Thread(this).start();
        this.btStart.setEnabled(false);
        this.btStop.setEnabled(true);
    }

    private void btStopEvent(ActionEvent ae) {
        int kq = JOptionPane.showConfirmDialog(this, "Are you sure you want to close the server?", "Notice", JOptionPane.YES_NO_OPTION);
        if (kq == JOptionPane.YES_OPTION) {
            try {
                serverThread.notifyToAllUsers("Warning: The server has been closed!");

                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);
    }

    @Override
    public void run() {
        this.startServer();
    }
}
