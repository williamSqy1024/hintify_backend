import asyncio
import websockets
import wave
import io

# WebSocket server that handles messages from Java
async def audio_server(websocket):  # The second argument can be ignored, hence using '_'
    try:
        print(f"Connection established with {websocket.remote_address}")
        
        # Loop to handle incoming messages
        async for message in websocket:
            if isinstance(message, bytes):  # Check if the message is binary (audio)
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
        # Convert the byte data into an in-memory file stream
        audio_stream = io.BytesIO(audio_data)
        
        # Open the audio stream as a WAV file
        with wave.open(audio_stream, 'rb') as audio_file:
            with wave.open("received_audio.wav", 'wb') as output_file:
                output_file.setparams(audio_file.getparams())  # Set parameters for output file
                output_file.writeframes(audio_file.readframes(audio_file.getnframes()))  # Write frames to file

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
