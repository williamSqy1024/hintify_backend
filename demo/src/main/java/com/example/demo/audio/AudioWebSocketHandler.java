package com.example.demo.audio;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class AudioWebSocketHandler extends BinaryWebSocketHandler {

    private final ConcurrentHashMap<String, ByteArrayOutputStream> audioBuffers = new ConcurrentHashMap<>();

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
        System.out.println("WebSocket connected: " + session.getId());
        audioBuffers.put(session.getId(), new ByteArrayOutputStream());
        session.sendMessage(new TextMessage("Connected to audio server"));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        System.out.println("Received audio chunk: " + message.getPayloadLength() + " bytes");

        byte[] audioBytes = message.getPayload().array();
        audioBuffers.computeIfAbsent(session.getId(), k -> new ByteArrayOutputStream()).write(audioBytes, 0, audioBytes.length);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("WebSocket disconnected: " + session.getId());
        processAndSaveAudio(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("WebSocket error: " + exception.getMessage());
        processAndSaveAudio(session);
    }

    private void processAndSaveAudio(WebSocketSession session) {
        System.out.println("test");
        ByteArrayOutputStream audioBuffer = audioBuffers.remove(session.getId());
        System.out.println(audioBuffer == null);
        System.out.println(audioBuffer.size());

        if (audioBuffer != null && audioBuffer.size() > 0) {
            saveAudioToFile(audioBuffer.toByteArray(), "received_audio.wav");
        }
    }

    private void saveAudioToFile(byte[] audioBytes, String fileName) {
        try {
            System.out.println(fileName);
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, audioBytes.length / audioFormat.getFrameSize());

            File file = new File(fileName);
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);

            System.out.println("✅ Saved audio to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Error saving audio file: " + e.getMessage());
        }
    }
}
