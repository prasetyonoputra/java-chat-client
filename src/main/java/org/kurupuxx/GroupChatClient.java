package org.kurupuxx;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GroupChatClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, PORT)) {
            System.out.println("Connected to the server");

            new Thread(new MessageReceiver(socket)).start();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Enter message (or 'FILE:<filename>' to send a file):");
                String input = scanner.nextLine();

                if (input.startsWith("FILE:")) {
                    sendFile(socket, input.substring(5).trim());
                } else {
                    out.println(input);
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static void sendFile(Socket socket, String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("File does not exist!");
                return;
            }

            byte[] buffer = new byte[4096];
            FileInputStream fileIn = new FileInputStream(file);
            OutputStream socketOut = socket.getOutputStream();

            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) > 0) {
                socketOut.write(buffer, 0, bytesRead);
            }
            fileIn.close();
            System.out.println("File sent successfully.");
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
        }
    }

    private static class MessageReceiver implements Runnable {
        private Socket socket;

        public MessageReceiver(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                }
            } catch (IOException e) {
                System.err.println("Connection closed: " + e.getMessage());
            }
        }
    }
}