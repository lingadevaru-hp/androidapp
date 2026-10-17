"""
Thoshan Chat Android — Python STT/TTS Module
=============================================
Standalone Python module providing:
  • Speech-to-Text (STT) via Vosk offline model (Indian English, no API key)
  • Text-to-Speech (TTS) via ElevenLabs API (API key required)

This module is the reference implementation for the Android app's voice
pipeline. Android cannot run Python natively, so the Android app uses the
native SpeechRecognizer (STT) and TextToSpeech (TTS) APIs instead. This
Python module can be deployed as a backend service (e.g. on Render) if the
Android app ever needs a server-side voice pipeline.

Usage (standalone):
  export ELEVENLABS_API_KEY="sk_..."
  python stt_tts_module.py

Requirements:
  pip install vosk requests numpy
"""

import os
import io
import json
import tempfile
import zipfile
import subprocess
import logging
from pathlib import Path

import vosk
import numpy as np
import requests

log = logging.getLogger("stt_tts_module")
logging.basicConfig(level=os.environ.get("LOG_LEVEL", "INFO"))

# ── Configuration ───────────────────────────────────────────────────────
VOSK_MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-en-in-0.5.zip"
VOSK_MODEL_DIR = Path(tempfile.gettempdir()) / "vosk-model-en-in-0.5"
VOSK_MODEL_PATH = VOSK_MODEL_DIR / "model"

ELEVENLABS_TTS_URL = "https://api.elevenlabs.io/v1/text-to-speech/{voice_id}"
ELEVENLABS_API_KEY = os.environ.get("ELEVENLABS_API_KEY", "")
ELEVENLABS_VOICE_ID = os.environ.get("ELEVENLABS_VOICE_ID", "21m00Tcm4TlvDq8ikWAM")  # Rachel
ELEVENLABS_MODEL_ID = os.environ.get("ELEVENLABS_MODEL_ID", "eleven_multilingual_v2")

_vosk_model = None


# ============================================================================
# Vosk Offline Speech-to-Text
# ============================================================================

def _ensure_vosk():
    """Download and load the Vosk Indian English model on first use."""
    global _vosk_model
    if _vosk_model is not None:
        return
    if not VOSK_MODEL_PATH.exists():
        VOSK_MODEL_DIR.mkdir(parents=True, exist_ok=True)
        log.info("Downloading Vosk Indian English model (~54MB) — first run only...")
        zip_path = VOSK_MODEL_DIR / "vosk-model.zip"
        try:
            import urllib.request
            urllib.request.urlretrieve(VOSK_MODEL_URL, str(zip_path))
        except Exception as exc:
            log.error("Vosk model download failed: %s", exc)
            raise RuntimeError("Vosk model unavailable")
        with zipfile.ZipFile(str(zip_path)) as zf:
            zf.extractall(str(VOSK_MODEL_DIR))
        zip_path.unlink()
    _vosk_model = vosk.Model(str(VOSK_MODEL_DIR))
    log.info("Vosk model loaded")


def decode_audio_to_pcm16(raw: bytes) -> bytes:
    """Decode any browser/audio format (webm/opus, etc.) to 16kHz mono s16le PCM via ffmpeg.

    soundfile/libsndfile cannot decode webm/opus, so ffmpeg is the only
    reliable path.
    """
    proc = subprocess.run(
        ["ffmpeg", "-hide_banner", "-loglevel", "error",
         "-i", "pipe:0",
         "-ar", "16000", "-ac", "1",
         "-f", "s16le", "-acodec", "pcm_s16le", "pipe:1"],
        input=raw, capture_output=True, timeout=60,
    )
    if proc.returncode != 0 or not proc.stdout:
        log.error("ffmpeg decode failed: %s",
                  proc.stderr.decode(errors="ignore")[:300])
        raise RuntimeError("audio decode failed")
    return proc.stdout


def stt(audio_bytes: bytes, sample_rate: int = 16000) -> str:
    """Transcribe raw audio bytes to text using Vosk offline STT.

    Args:
        audio_bytes: Raw audio (webm/opus from MediaRecorder, or PCM16).
        sample_rate: Expected sample rate of input audio (Hz).

    Returns:
        Transcribed text string.
    """
    _ensure_vosk()

    # Decode via ffmpeg if input is not raw PCM16 (e.g. webm/opus blob)
    try:
        # Try ffmpeg decode first (handles webm/opus/mp3/wav/etc.)
        pcm = decode_audio_to_pcm16(audio_bytes)
    except Exception:
        # If ffmpeg fails, assume input is already raw s16le PCM at 16kHz
        pcm = audio_bytes

    rec = vosk.KaldiRecognizer(_vosk_model, 16000)
    rec.AcceptWaveform(pcm)
    result = json.loads(rec.FinalResult())
    return result.get("text", "").strip()


# ============================================================================
# ElevenLabs Text-to-Speech
# ============================================================================

def tts(text: str, voice_id: str | None = None,
        model_id: str | None = None) -> bytes:
    """Synthesize text to speech audio (MP3) via ElevenLabs API.

    Args:
        text: Text to speak.
        voice_id: ElevenLabs voice ID (default from ELEVENLABS_VOICE_ID env).
        model_id: ElevenLabs model ID (default from ELEVENLABS_MODEL_ID env).

    Returns:
        Raw MP3 audio bytes.

    Raises:
        RuntimeError: If ELEVENLABS_API_KEY is not set or the API call fails.
    """
    if not ELEVENLABS_API_KEY:
        raise RuntimeError("ELEVENLABS_API_KEY environment variable not set")

    vid = voice_id or ELEVENLABS_VOICE_ID
    mid = model_id or ELEVENLABS_MODEL_ID
    url = ELEVENLABS_TTS_URL.format(voice_id=vid)

    headers = {
        "xi-api-key": ELEVENLABS_API_KEY,
        "Content-Type": "application/json",
        "Accept": "audio/mpeg",
    }
    payload = {
        "text": text,
        "model_id": mid,
        "voice_settings": {
            "stability": 0.5,
            "similarity_boost": 0.75,
        },
    }

    try:
        resp = requests.post(url, headers=headers, json=payload, timeout=30)
        resp.raise_for_status()
        return resp.content
    except requests.RequestException as exc:
        log.error("ElevenLabs TTS failed: %s", exc)
        raise RuntimeError(f"TTS request failed: {exc}")


# ============================================================================
# Convenience: speak text to a file
# ============================================================================

def speak_to_file(text: str, output_path: str, **kwargs) -> str:
    """Synthesize text and write MP3 audio to a file.

    Returns the output path.
    """
    audio = tts(text, **kwargs)
    out = Path(output_path)
    out.write_bytes(audio)
    log.info("TTS audio written to %s (%d bytes)", out, len(audio))
    return str(out)


# ============================================================================
# Health / availability checks
# ============================================================================

def check_availability() -> dict:
    """Return availability status of STT and TTS backends."""
    stt_ok = False
    try:
        _ensure_vosk()
        stt_ok = True
    except Exception as exc:  # noqa: BLE001
        log.warning("Vosk STT unavailable: %s", exc)

    tts_ok = bool(ELEVENLABS_API_KEY)

    return {
        "stt": {
            "engine": "vosk",
            "model": "vosk-model-en-in-0.5",
            "available": stt_ok,
            "offline": True,
            "api_key_required": False,
        },
        "tts": {
            "engine": "elevenlabs",
            "voice_id": ELEVENLABS_VOICE_ID,
            "model_id": ELEVENLABS_MODEL_ID,
            "available": tts_ok,
            "offline": False,
            "api_key_required": True,
        },
    }


if __name__ == "__main__":
    import sys
    status = check_availability()
    print(json.dumps(status, indent=2))
    if len(sys.argv) > 1 and sys.argv[1] == "--test-tts" and ELEVENLABS_API_KEY:
        audio = tts("Hello, this is Thoshan's assistant testing text to speech.")
        print(f"TTS test OK: {len(audio)} bytes MP3")
