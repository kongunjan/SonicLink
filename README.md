# 🔊 SonicLink

### Acoustic FSK Communication System — sending data as sound between two computers

**SonicLink** is a Java-based acoustic communication prototype that transmits short text messages (and supported files) between computers using **sound instead of Wi-Fi, Bluetooth, or the internet**. The sender encodes data as Binary Frequency Shift Keyed (BFSK) audio tones and plays them through a speaker; the receiver captures the audio through a microphone, locates the transmission using a synchronization preamble, demodulates the tones with the **Goertzel algorithm**, and verifies the recovered data with a **CRC checksum**.

```text
Text / File
    ↓
UTF-8 Bytes
    ↓
Packet (header + payload + CRC)
    ↓
Bit Stream
    ↓
BFSK Modulation (1200 Hz / 2200 Hz)
    ↓
PCM Audio Samples
    ↓
🔊 Speaker  →  Acoustic Channel  →  🎙️ Microphone
    ↓
Preamble Detection (sync)
    ↓
Goertzel Frequency Detection
    ↓
Bit Stream
    ↓
Packet Decoding
    ↓
CRC Verification
    ↓
Original Message
```

---

## 📌 Project Status

> **Status: Functional prototype**

The full SonicLink communication pipeline has been implemented and verified end-to-end through real acoustic tests (speaker → air → microphone), not just software loopback:

- ✅ Text and file input handling
- ✅ Packet framing (magic bytes, version, length, payload, CRC)
- ✅ BFSK modulation (1200 Hz / 2200 Hz)
- ✅ PCM audio generation with click-free symbol ramping
- ✅ Speaker-based audio transmission
- ✅ Microphone audio capture
- ✅ Preamble-based synchronization with confidence thresholding
- ✅ Goertzel frequency detection
- ✅ Bit stream reconstruction
- ✅ Packet decoding and CRC-based integrity verification
- ✅ Command-line interface (`send`, `send-file`, `listen`, `self-test`, `help`)
- ✅ Unit test suite covering all core modules

Example successful transmission:

```text
✓ PACKET RECEIVED SUCCESSFULLY
Message: Hello SonicLink
Payload: 15 bytes
Protocol version: 1
✓ CRC verified
```

The current implementation targets **short-range, same-room, single-packet acoustic communication** — not a replacement for wireless protocols at scale. See [Future Enhancements](#-future-enhancements) for the planned next steps.

---

## ✨ Features

- 🔊 Acoustic transmission of text messages and text-file content
- 🎙️ Microphone-based reception with automatic packet synchronization
- 0️⃣1️⃣ Binary FSK modulation (1200 Hz = 0, 2200 Hz = 1)
- 🎯 Goertzel-based frequency detection (no full FFT required)
- 📡 Dedicated preamble tone with confidence-threshold sync detection
- 🧩 Structured, versioned packet protocol (magic bytes + length + payload + CRC)
- 🛡️ CRC-based integrity verification (detects, not corrects, corruption)
- 📂 File input support with chunked transmission
- 🧪 Built-in `self-test` command for a full software loopback check
- 💻 Command-line interface, no GUI dependency
- ☕ Java standard library only — no external runtime dependencies

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java 11+ | Core implementation |
| `javax.sound.sampled` | Audio capture and playback (Java Sound API) |
| Binary FSK | Digital modulation scheme |
| Goertzel Algorithm | Efficient single-frequency detection |
| CRC | Packet integrity / error detection |
| PCM (16-bit, 44.1 kHz) | Digital audio representation |
| Bash | Build automation (`build.sh`) |
| Custom JUnit-style test runner | Unit testing (`AllTestsRunner`) |

No external libraries or package managers are required — everything runs from the JDK alone.

---

## 📋 Prerequisites

- JDK 11 or later installed
- A working speaker (for sending) and microphone (for receiving)
- OS-level microphone permission granted to your terminal application

```bash
java -version
javac -version
```

---

## 🚀 Installation

```bash
git clone https://github.com/kongunjan/SonicLink.git
cd SonicLink
chmod +x build.sh   # if needed on macOS/Linux
bash build.sh
```

This compiles all Java sources and places the class files in `out/`.

---

## ▶️ Running SonicLink

All commands are run through `build.sh`, which compiles and then executes the CLI.

**Send a text message:**

```bash
bash build.sh run send "Hello SonicLink"
```

**Send a file:**

```bash
bash build.sh run send-file ./test.txt
```

**Listen for and decode an incoming transmission:**

```bash
bash build.sh run listen
```

**Run the built-in self-test (full software loopback, no audio hardware needed):**

```bash
bash build.sh run self-test
```

**Show usage/help:**

```bash
bash build.sh run help
```

### Local testing (one machine, two terminals)

```bash
# Terminal 1
bash build.sh run listen

# Terminal 2
bash build.sh run send "Hello SonicLink"
```

Keep the speaker volume high enough for the microphone to pick up the tones clearly. For a more realistic test, run the sender and receiver on two separate machines placed a short distance apart.

---

## 🧪 Testing

Run the full automated test suite:

```bash
bash build.sh test
```

| Test Class | Coverage |
|---|---|
| `BitStreamUtilsTest` | Byte ↔ bit conversion round-trips |
| `ChecksumValidatorTest` | CRC computation and validation, including deliberately corrupted input |
| `ModulatorTest` | FSK tone generation and PCM sample correctness |
| `DemodulatorTest` | Bit recovery from generated PCM samples |
| `GoertzelDetectorTest` | Frequency-energy detection accuracy and confidence scoring |
| `PacketCodecTest` | Packet encoding/decoding, including malformed-packet handling |
| `PreambleDetectorTest` | Synchronization detection and threshold tolerance |
| `AllTestsRunner` | Aggregates and runs the entire suite |

**Manual / acoustic test matrix** (document your results in the project report):

| Variable | Values to test |
|---|---|
| Distance | 0.5 m, 1 m, 2 m, 3 m, 5 m |
| Message length | Short, medium, long |
| Environment | Quiet room, normal room, noisy environment |
| Volume | Low, medium, high |

Useful metrics to record: successful transmissions, failed transmissions, CRC failures, approximate bit-error rate, and effective transmission time.

---

## ⚙️ System Architecture

```text
Input Data (text/file) → Packet + CRC → Bit Stream → BFSK Modulator → PCM → Speaker

Speaker → Acoustic Channel (air) → Microphone → Preamble Detector → Demodulator
        → Packet Decoder → CRC Verify → Output (recovered message)
```

SonicLink is **stateless** — every transmission is processed entirely in memory with no database or persistent storage involved.

---

## 📁 Project Structure

```text
SonicLink/
├── build.sh                              # compile + run helper script
├── README.md
├── statement.md                          # problem statement, scope, target users
└── src/
    ├── main/java/com/soniclink/
    │   ├── cli/
    │   │   └── SonicLinkCLI.java         # entry point, command dispatch (send/send-file/listen/self-test/help)
    │   ├── core/
    │   │   ├── SonicTransmitter.java     # orchestrates the send path
    │   │   ├── SonicReceiver.java        # orchestrates the receive path
    │   │   └── FileTransmitter.java      # file-input transmission path
    │   ├── codec/
    │   │   ├── Packet.java               # packet data model
    │   │   ├── PacketCodec.java          # packet encode/decode
    │   │   ├── Modulator.java            # bits → PCM (FSK encode)
    │   │   ├── Demodulator.java          # PCM → bits (decode orchestration)
    │   │   ├── GoertzelDetector.java     # core DSP: single-frequency energy detection
    │   │   └── PreambleDetector.java     # sync-tone detection with confidence threshold
    │   ├── audio/
    │   │   ├── AudioTransmitter.java     # plays PCM through the speaker
    │   │   └── AudioReceiver.java        # captures PCM from the microphone
    │   └── util/
    │       ├── BitStreamUtils.java       # byte[] ↔ bit[] conversion
    │       ├── ChecksumValidator.java    # CRC computation and validation
    │       ├── FileChunker.java          # splits file payloads into transmittable chunks
    │       ├── FileTextExtractor.java    # text extraction for supported file input
    │       └── SonicConfig.java          # shared constants (sample rate, frequencies, timing)
    └── test/java/com/soniclink/
        ├── BitStreamUtilsTest.java
        ├── ChecksumValidatorTest.java
        ├── ModulatorTest.java
        ├── DemodulatorTest.java
        ├── GoertzelDetectorTest.java
        ├── PacketCodecTest.java
        ├── PreambleDetectorTest.java
        └── AllTestsRunner.java
```

---

## 🧩 Module Responsibilities

| Module | Responsibility |
|---|---|
| `SonicLinkCLI` | Parses CLI arguments and dispatches to send/receive/self-test flows |
| `SonicTransmitter` / `SonicReceiver` | Coordinate the end-to-end send and receive pipelines |
| `FileTransmitter` | Routes file input through chunking and the existing message pipeline |
| `Packet` / `PacketCodec` | Define and (de)serialize the versioned packet format |
| `Modulator` / `Demodulator` | Convert between bit streams and PCM audio samples |
| `GoertzelDetector` | Computes energy at a target frequency for one symbol window |
| `PreambleDetector` | Scans incoming audio for the sync tone and confirms a valid packet start |
| `AudioTransmitter` / `AudioReceiver` | Wrap `javax.sound.sampled` for playback and capture |
| `BitStreamUtils` | Byte ↔ bit conversion |
| `ChecksumValidator` | CRC generation and verification |
| `FileChunker` / `FileTextExtractor` | Split and extract text content from file input |
| `SonicConfig` | Central home for shared constants so every layer agrees on the same scheme |

---

## 🔬 How the Signal Processing Works

**Binary FSK:** each bit is transmitted as one of two frequencies for a fixed 50 ms symbol duration, with a short amplitude ramp at symbol boundaries to avoid audible clicking.

| Bit | Frequency |
|---|---:|
| `0` | 1200 Hz |
| `1` | 2200 Hz |

**Goertzel detection:** for each symbol window, the receiver evaluates signal energy at both 1200 Hz and 2200 Hz using the Goertzel algorithm — an efficient way to evaluate a single DFT bin without computing a full FFT — and selects whichever frequency dominates as the recovered bit.

**Preamble sync:** a dedicated 3000 Hz tone precedes every packet. The receiver applies a dominant-to-other-frequency energy ratio as a confidence threshold before treating a position in the recording as a valid packet start, which avoids false synchronization on noise.

**Integrity check:** the sender computes a CRC over the payload and includes it in the packet. The receiver recomputes the CRC after decoding and reports a match or mismatch — this detects corruption but does not correct it.

---

## ⚠️ Current Limitations

- Fixed-duration recording window on the receiver rather than continuous, signal-triggered capture
- Fixed FSK frequencies and symbol timing (no adaptive transmission yet)
- Detection-only error handling — CRC identifies corrupted packets but cannot repair them
- Single-packet messages — no sequencing, acknowledgement, or retransmission yet
- File support is currently text-oriented; binary file reconstruction is not yet implemented

---

## 🔮 Future Enhancements

- **Automatic synchronization** — a stronger preamble/sync sequence for reliable auto-detection in noisier environments
- **Automatic start/stop detection** — replace the fixed recording window with continuous, signal-triggered capture
- **Error correction** — Hamming codes, Reed-Solomon codes, or general FEC alongside the existing CRC
- **Adaptive transmission** — measure ambient noise and adjust symbol duration, threshold, and frequency automatically
- **Higher-level protocol** — sequence numbers, acknowledgements, retransmission, duplicate detection, multi-packet messages
- **Better signal processing** — FFT-based analysis, band-pass filtering, adaptive thresholds, automatic gain normalization
- **Binary file reconstruction** — full multi-packet transfer and reassembly of arbitrary binary files
- **GUI / real-time visualization** — a waveform and spectrum view alongside the existing CLI
- **Performance benchmarking** — formal range, bit-rate, and packet-error-rate measurements across distances and noise levels

---

## 🔐 Security Considerations

SonicLink is an experimental communication system, **not a secure messaging protocol**. Transmitted audio can be recorded by anyone within acoustic range, and CRC provides integrity checking only — it does **not** provide encryption or authentication. Cryptographic protection could be added in a future version if secure communication becomes a requirement.

---

## 📚 References

1. John G. Proakis and Masoud Salehi, *Fundamentals of Communication Systems*, Pearson.
2. Richard G. Lyons, *Understanding Digital Signal Processing*, Pearson.
3. Steven W. Smith, *The Scientist and Engineer's Guide to Digital Signal Processing* — chapters on digital filters and spectral analysis.
4. Oracle Java Platform Documentation, `javax.sound.sampled` package.
5. Wikipedia, *Goertzel Algorithm*.
6. Wikipedia, *Frequency-Shift Keying*.
7. NIST publications on CRC and error-detection concepts.

> The references above are background resources for the underlying algorithms and technologies. The SonicLink implementation itself — packet format, module structure, and CLI — is original project code.

---

## 👩‍💻 Author

**Gunjan** — developed as an educational project exploring the intersection of Java, digital signal processing, digital communication theory, and audio engineering.

## 📜 License

This project is intended for educational and experimental use. Add a specific open-source license here if one is adopted for the repository.
