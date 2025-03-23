package com.example.demo.audio;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class AudioWebSocketHandler extends TextWebSocketHandler {

    private boolean isStreaming = false;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        if ("start".equals(payload)) {
            isStreaming = true;
            startStreaming(session);
        } else if ("stop".equals(payload)) {
            isStreaming = false;
            stopStreaming(session);
        }
    }

    private void startStreaming(WebSocketSession session) throws IOException {
        // Simulate audio streaming (replace with actual audio capture logic)
        new Thread(() -> {
            try {
                while (isStreaming) {
                    // Simulate sending audio chunks (e.g., random bytes)
                    byte[] audioChunk = new byte[1024];
                    // Fill with random data (replace with real audio data)
                    new java.util.Random().nextBytes(audioChunk);
                    session.sendMessage(new TextMessage(new String(audioChunk)));
                    System.out.println("Sent audio chunk of size: " + audioChunk.length + " bytes");

                    Thread.sleep(100); // Simulate delay
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void stopStreaming(WebSocketSession session) throws IOException {
        // Stop streaming logic (if needed)
        session.sendMessage(new TextMessage("Stream stopped"));
    }
}