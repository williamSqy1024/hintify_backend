import asyncio
import websockets
import wave

# WebSocket server that handles messages from Java
async def audio_server(websocket):  # The second argument can be ignored, hence using '_'
    try:
        print(f"Connection established with {websocket.remote_address}")
        
        # Loop to handle incoming messages
        async for message in websocket:
            if isinstance(message, bytes):  # Check if the message is binary (audio)
                response = "Hintify received your aduio!!!"
                await websocket.send(response)
                print(f"Received audio chunk of size: {len(message)} bytes")
                save_audio(message)  # Save the received audio data
            else:
                response = "This is the message from Python Server, Welcome to use Hintify"
                await websocket.send(response)
                print(f"Sent response: {response}")
                print(f"Received message from Java: {message}")  # Handle any other type of message

    except websockets.exceptions.ConnectionClosed as e:
        print(f"Connection closed: {e}")
    finally:
        print(f"Closing connection with {websocket.remote_address}")
        await websocket.close()

# Function to save received audio to a file
def save_audio(audio_data):
    try:
        sample_width = 2  # 16-bit audio (modify based on Java settings)
        frame_rate = 16000  # Sample rate (modify based on Java settings)
        num_channels = 1  # Mono or Stereo
        
        with wave.open("uploads/received_audio_python.wav", 'wb') as output_file:
            output_file.setnchannels(num_channels)
            output_file.setsampwidth(sample_width)
            output_file.setframerate(frame_rate)
            output_file.writeframes(audio_data)

        print("✅ Audio saved successfully to 'received_audio.wav'")
    
    except Exception as e:
        print(f"❌ Error while saving audio: {e}")

# Start the WebSocket server
async def start_server():
    # Start the WebSocket server on localhost (127.0.0.1) and port 50000
    server = await websockets.serve(audio_server, "127.0.0.1", 50000)
    print("WebSocket server running on ws://127.0.0.1:50000")
    await server.wait_closed()

# Run the server
if __name__ == "__main__":
    asyncio.get_event_loop().run_until_complete(start_server())
