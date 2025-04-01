import vosk
import wave
import json
import io
from pydub import AudioSegment

class VoskModel:
    def __init__(self, model_path):
        self.model = vosk.Model(model_path)
        self.sample_rate = 16000
        self.recognizer = vosk.KaldiRecognizer(self.model, self.sample_rate)

    def convert_audio_to_text(self, audio_bytes):
        audio_segment = AudioSegment.from_raw(io.BytesIO(audio_bytes), frame_rate=self.sample_rate, sample_width=2, channels=1)
        wav_data = io.BytesIO()
        audio_segment.export(wav_data, format="wav")
        wav_data.seek(0)
        wf = wave.open(wav_data, "rb")

        results = []
        while True:
            data = wf.readframes(4000)
            if len(data) == 0:
                break
            if self.recognizer.AcceptWaveform(data):
                result = json.loads(self.recognizer.Result())
                results.append(result)
            
        # Get the final result
        final_result = json.loads(self.recognizer.FinalResult())
        results.append(final_result)

        # Extract the recognized text
        recognized_text = " ".join([res["text"] for res in results if "text" in res])
        return recognized_text