package com.example.demo.audio;

import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class AudioWebSocketHandler extends BinaryWebSocketHandler {
    public static AudioWebSocketHandler instance;
    private final ConcurrentHashMap<String, WebSocketSession> frontendSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ByteArrayOutputStream> audioBuffers = new ConcurrentHashMap<>();
    private WebSocketClient pythonWebSocketClient;
    private PythonWebSocketHandler pythonHandler; // Store the WebSocket handler for Python

    public AudioWebSocketHandler() {
        this.pythonHandler = new PythonWebSocketHandler(this);
    }

    private final AudioFormat audioFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        16000, // Sample Rate: 16kHz
        16, // Bit Depth: 16-bit
        1, // Mono
        2, // Frame Size (16-bit PCM = 2 bytes)
        16000, // Frame Rate (same as Sample Rate)
        false // Little-endian (LSB first)
    );

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        System.out.println("WebSocket connected: " + session.getId());
        frontendSessions.put(sessionId, session);
        audioBuffers.put(session.getId(), new ByteArrayOutputStream());
        initializePythonWebSocket();
        System.out.println("New connection: " + sessionId);
        System.out.println("Total connections: " + frontendSessions.size());
        // session.sendMessage(new TextMessage("Connected to audio server"));
    }

    public void broadcastToFrontend(String message) {
        System.out.println("Attempting to broadcast to " + frontendSessions.size() + " clients");
        
        frontendSessions.forEach((id, session) -> {
            try {
                if (session.isOpen()) {
                    System.out.println("broadcastToFrontend test: " + message);
                    session.sendMessage(new TextMessage(message));
                    System.out.println("Sent to " + id);
                } else {
                    System.out.println("Session " + id + " is closed");
                    frontendSessions.remove(id); // Clean up closed sessions
                }
            } catch (IOException e) {
                System.err.println("Error sending to " + id + ": " + e.getMessage());
            }
        });
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        System.out.println("Received audio chunk: " + message.getPayloadLength() + " bytes");

        byte[] audioBytes = message.getPayload().array();
        audioBuffers.computeIfAbsent(session.getId(), k -> new ByteArrayOutputStream()).write(audioBytes, 0, audioBytes.length);
        sendDataToPython(pythonHandler.getSession(), audioBytes);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        System.out.println("WebSocket disconnected: " + sessionId);
        frontendSessions.remove(session.getId());
        processAndSaveAudio(session);
        System.out.println("Connection closed: " + sessionId);
        System.out.println("Remaining connections: " + frontendSessions.size());
        if (pythonHandler.getSession() != null && pythonHandler.getSession().isOpen()) {
            try {
                pythonHandler.getSession().close();  // Close the WebSocket session
                System.out.println("Closed WebSocket connection to Python server");
            } catch (IOException e) {
                System.err.println("Error closing WebSocket session: " + e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("WebSocket error: " + exception.getMessage());
        processAndSaveAudio(session);
    }

    private void processAndSaveAudio(WebSocketSession session) {
        System.out.println("Send the audio bytes to Python!");
        ByteArrayOutputStream audioBuffer = audioBuffers.remove(session.getId());

        if (audioBuffer != null && audioBuffer.size() > 0) {
            saveAudioToFile(audioBuffer.toByteArray(), "uploads/received_audio_java.wav");
            sendDataToPython(pythonHandler.getSession(), audioBuffer.toByteArray());
        }
    }

    private void saveAudioToFile(byte[] audioBytes, String fileName) {
        try 
            {
                ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, audioBytes.length / audioFormat.getFrameSize());

                File file = new File(fileName);
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);

                System.out.println("✅ Saved audio to: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("❌ Error saving audio file: " + e.getMessage());
            }
    }

    private void initializePythonWebSocket() {
        pythonWebSocketClient = new StandardWebSocketClient();
        try {
            // Create WebSocketConnectionManager and start the connection
            WebSocketConnectionManager connectionManager = new WebSocketConnectionManager(pythonWebSocketClient, pythonHandler, "ws://127.0.0.1:50000");
            connectionManager.start();  // Start the connection to Python WebSocket server
            System.out.println("Trying to connect to Python WebSocket...");
            int attempts = 0;
            System.out.println("python session: " + pythonHandler.getSession());
            while (pythonHandler.getSession() == null && attempts < 10) {
                try {
                    Thread.sleep(500);
                    attempts++;
                    System.out.println("Attempts: " + attempts);

                } catch (InterruptedException e) {
                    System.out.println("Error: " + e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println("python session 2: " + pythonHandler.getSession());
            // Once connected, you can safely send data
            // sendDataToPython(pythonHandler.getSession());
    
        } catch (Exception e) {
            System.err.println("Error during WebSocket connection or sending data: " + e.getMessage());
        }
    }
    private void sendDataToPython(WebSocketSession session, byte[] audioBytes) {
        // Ensure pythonSession is not null before attempting to send data
        if (session!= null && session.isOpen()) {
            try {
                String message = "Hello from Java";  // Example data
                session.sendMessage(new BinaryMessage(audioBytes)); // Send data to Python
                System.out.println("Sent data to Python: " + message);
            } catch (IOException e) {
                System.err.println("Error sending data to Python: " + e.getMessage());
            }
        } else {
            System.err.println("Error: Python WebSocket session is not yet established or is closed.");
        }
    }

    public ConcurrentHashMap<String, WebSocketSession> getFrontendSessions() {
        return frontendSessions;
    }
    
}
