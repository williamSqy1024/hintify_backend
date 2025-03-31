package com.example.demo.audio;

import org.springframework.web.socket.*;

public class PythonWebSocketHandler implements WebSocketHandler {

    private WebSocketSession session;
    private AudioWebSocketHandler audioWebSocketHandler; // Reference to AudioWebSocketHandler

    public PythonWebSocketHandler(AudioWebSocketHandler audioHandler) {
        this.audioWebSocketHandler = audioHandler; // Inject reference
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        System.out.println("Connected to Python WebSocket server");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming messages from Python (if needed)
        System.out.println("Received message from Python: " + message.getPayload());
        audioWebSocketHandler.broadcastToFrontend(message.getPayload().toString());

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("Connection to Python WebSocket server closed.");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public WebSocketSession getSession() {
        return session;
    }
}
