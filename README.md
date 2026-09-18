# 🔊 SonicLink

### Offline text communication using sound

**SonicLink** is a Java-based acoustic communication system that allows short text messages to be transmitted between computers using **sound instead of Wi-Fi, Bluetooth, or the internet**.

The sender converts text into binary data, modulates the bits into audio tones using **Frequency Shift Keying (FSK)**, and plays the signal through a speaker. A receiving computer captures the audio through a microphone and uses the **Goertzel algorithm** to identify the transmitted frequencies and reconstruct the original message.

```text
Text
  ↓
Bytes
  ↓
Bits
  ↓
FSK Modulation
  ↓
Audio Tones 🔊
  ↓
Microphone 🎙️
  ↓
Goertzel Detection
  ↓
Bits
  ↓
Bytes
  ↓
CRC Verification
  ↓
Original Text
```

---

## 📌 Project Status

> **Status: Functional prototype**

The core SonicLink communication pipeline has been implemented and tested:

* ✅ Text-to-bit conversion
* ✅ FSK modulation
* ✅ PCM audio generation
* ✅ Speaker-based audio transmission
* ✅ Microphone audio capture
* ✅ Goertzel frequency detection
* ✅ Bit reconstruction
* ✅ Packet encoding/decoding
* ✅ CRC-based integrity verification
* ✅ Command-line interface
* ✅ Unit tests for core utilities

Example successful transmission:

```text
================================
Message: Hello SonicLink
Payload: 15 bytes
Protocol version: 1
✓ CRC verified
```

The current implementation is designed for **short-distance experimental acoustic communication** rather than high-speed data transfer.

---

# 🧠 How SonicLink Works

SonicLink treats sound as a physical communication channel.

Instead of sending:

```text
Computer A ─── Wi-Fi ───> Computer B
```

SonicLink uses:

```text
Computer A
    │
    │ Generate sound
    ▼
 🔊 Speaker
    │
    │ Acoustic signal
    ▼
 🎙️ Microphone
    │
    │ Capture samples
    ▼
Computer B
```

The system uses **Frequency Shift Keying (FSK)** to represent binary information.

For SonicLink:

| Bit | Frequency |
| --- | --------: |
| `0` |   1200 Hz |
| `1` |   2200 Hz |

For example:

```text
10110

1 → 2200 Hz
0 → 1200 Hz
1 → 2200 Hz
1 → 2200 Hz
0 → 1200 Hz
```

The receiver examines each symbol window and determines which frequency is stronger.

---

# ⚙️ System Architecture

```text
                    SONICLINK
                       │
          ┌────────────┴────────────┐
          │                         │
       SENDER                    RECEIVER
          │                         │
     Text Message              Microphone
          │                         │
          ▼                         ▼
    Bit Conversion             PCM Capture
          │                         │
          ▼                         ▼
    Packet Encoding          Signal Processing
          │                         │
          ▼                         ▼
    FSK Modulator            Goertzel Detector
          │                         │
          ▼                         ▼
    Audio Samples             Detected Bits
          │                         │
          ▼                         ▼
       Speaker                Packet Decoder
                                    │
                                    ▼
                              CRC Verification
                                    │
                                    ▼
                              Original Message
```

---

# 🔬 Digital Signal Processing

## Frequency Shift Keying

FSK is a digital modulation technique where different frequencies represent different symbols.

SonicLink uses two frequencies:

```text
Binary 0 → 1200 Hz
Binary 1 → 2200 Hz
```

Each bit is transmitted for a fixed symbol duration.

This produces an audio waveform that can be detected by the receiver.

---

## 🎯 Goertzel Algorithm

The receiver cannot simply ask:

> "Is there a 2200 Hz sound?"

It must analyze a sampled audio window and determine which target frequency contains more energy.

SonicLink uses the **Goertzel algorithm**, which is particularly useful for detecting specific frequencies without calculating a complete FFT.

For every symbol window, the receiver evaluates the energy around:

```text
1200 Hz
2200 Hz
```

Conceptually:

```text
Audio Window
     │
     ├──────────────► Goertzel @ 1200 Hz
     │                       │
     │                       ▼
     │                  Energy E0
     │
     └──────────────► Goertzel @ 2200 Hz
                             │
                             ▼
                        Energy E1

             Compare E0 and E1
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
       E0 > E1             E1 > E0
          │                   │
          ▼                   ▼
          0                   1
```

---

# 🛡️ Data Integrity

Audio communication is susceptible to:

* Background noise
* Echo
* Speaker distortion
* Microphone sensitivity
* Environmental interference
* Incorrect symbol detection

To detect corrupted transmissions, SonicLink includes a **CRC-based integrity check**.

The transmitted packet contains the payload together with integrity information.

The receiver calculates the CRC again and compares it with the transmitted value.

```text
Sender

Message
   ↓
Payload
   ↓
CRC Calculation
   ↓
Packet
   ↓
FSK Transmission


Receiver

Audio
   ↓
Detected Bits
   ↓
Packet
   ↓
CRC Calculation
   ↓
Compare CRCs
   ↓
┌───────────────┐
│ Match         │ → ✓ CRC verified
│ Mismatch      │ → ✗ Corrupted packet
└───────────────┘
```

CRC provides **error detection**, not error correction.

---

# 📦 Packet Structure

SonicLink uses a structured packet rather than transmitting raw text directly.

A conceptual packet is:

```text
┌─────────┬─────────┬───────────┬─────────┬─────────┐
│ Version │ Length  │  Payload  │   CRC   │  ...    │
└─────────┴─────────┴───────────┴─────────┴─────────┘
```

This allows the receiver to determine:

* Protocol version
* Payload size
* Message contents
* Transmission integrity

The protocol can therefore be extended in future versions without redesigning the entire communication system.

---

# ✨ Features

* 🔊 Acoustic text transmission
* 🎙️ Microphone-based reception
* 0️⃣1️⃣ FSK binary modulation
* 🎯 Goertzel frequency detection
* 🧩 Structured packet protocol
* 🛡️ CRC-based integrity verification
* 💻 Command-line interface
* ☕ Java standard library implementation
* 🌐 No Wi-Fi required
* 📡 No Bluetooth required
* 📦 No external Java dependencies

---

# 🛠️ Technologies Used

| Technology            | Purpose                      |
| --------------------- | ---------------------------- |
| Java 11+              | Core implementation          |
| `javax.sound.sampled` | Audio input/output           |
| FSK                   | Digital modulation           |
| Goertzel Algorithm    | Frequency detection          |
| CRC                   | Error detection              |
| PCM                   | Digital audio representation |
| Bash                  | Build automation             |
| JUnit / Java tests    | Testing                      |

The project intentionally avoids external runtime dependencies and relies primarily on the Java standard library.

---

# 📋 Prerequisites

Before running SonicLink, make sure you have:

* JDK 11 or later
* A working speaker
* A working microphone
* Microphone permissions enabled for the terminal/application

Check Java:

```bash
java -version
```

Check the compiler:

```bash
javac -version
```

---

# 🚀 Installation

Clone the repository:

```bash
git clone https://github.com/kongunjan/SonicLink.git
```

Enter the project directory:

```bash
cd SonicLink
```

Build the project:

```bash
./build.sh
```

If macOS/Linux reports a permission error:

```bash
chmod +x build.sh
./build.sh
```

---

# ▶️ Running SonicLink

## Sender

Run:

```bash
java -cp out com.soniclink.cli.SonicLinkCLI send "Hello SonicLink"
```

The program converts the message into a packet, modulates the bits into audio and plays the resulting signal through the speaker.

---

## Receiver

Run:

```bash
java -cp out com.soniclink.cli.SonicLinkCLI listen
```

The receiver captures audio from the microphone and attempts to reconstruct the transmitted packet.

A successful transmission produces output similar to:

```text
================================
Message: Hello SonicLink
Payload: 15 bytes
Protocol version: 1
✓ CRC verified
```

---

# 🧪 Local Testing

You can test SonicLink without two physical computers.

Open two terminals on the same computer.

### Terminal 1

```bash
java -cp out com.soniclink.cli.SonicLinkCLI listen
```

### Terminal 2

```bash
java -cp out com.soniclink.cli.SonicLinkCLI send "Hello SonicLink"
```

Keep the speaker volume high enough for the microphone to detect the signal.

For more realistic testing, place two computers several distances apart and measure whether the message is decoded successfully.

---

# 🧪 Testing Strategy

SonicLink can be evaluated using several experimental conditions.

### Distance

Test at different speaker-microphone distances:

```text
0.5 m
1 m
2 m
3 m
5 m
```

### Message Length

Test:

```text
Short message
Medium message
Long message
```

### Environmental Noise

Compare performance in:

```text
Quiet room
Normal room
Noisy environment
```

### Volume

Repeat the tests at different speaker volume levels.

Useful measurements include:

* Successful transmissions
* Failed transmissions
* CRC failures
* Bit errors
* Effective transmission time
* Approximate bit-error rate

---

# 📁 Project Structure

```text
SonicLink/
│
├── build.sh
├── README.md
├── statement.md
│
└── src/
    │
    ├── main/
    │   └── java/
    │       └── com/
    │           └── soniclink/
    │
    │               ├── cli/
    │               │   └── SonicLinkCLI.java
    │               │
    │               ├── codec/
    │               │   ├── Modulator.java
    │               │   ├── Demodulator.java
    │               │   └── GoertzelDetector.java
    │               │
    │               ├── audio/
    │               │   ├── AudioTransmitter.java
    │               │   └── AudioReceiver.java
    │               │
    │               └── util/
    │                   ├── BitStreamUtils.java
    │                   ├── ChecksumValidator.java
    │                   └── SonicConfig.java
    │
    └── test/
        └── java/
            └── com/
                └── soniclink/
                    └── BitStreamUtilsTest.java
```

---

# 🧩 Module Responsibilities

### `SonicLinkCLI`

Provides the command-line interface and handles commands such as:

```text
send
listen
```

### `Modulator`

Converts binary data into FSK audio samples.

### `Demodulator`

Coordinates the decoding process and converts detected frequencies back into bits.

### `GoertzelDetector`

Performs frequency-energy analysis on individual audio windows.

### `AudioTransmitter`

Sends generated PCM audio through the computer's speaker.

### `AudioReceiver`

Captures PCM audio through the microphone.

### `BitStreamUtils`

Handles conversion between:

```text
bytes ↔ bits
```

### `ChecksumValidator`

Calculates and validates packet integrity.

### `SonicConfig`

Stores shared communication parameters such as:

```text
Sample rate
FSK frequencies
Symbol duration
```

---

# 📐 Communication Pipeline

The complete transmission process is:

```text
             TRANSMITTER
                  │
                  ▼
          "Hello SonicLink"
                  │
                  ▼
             UTF-8 Bytes
                  │
                  ▼
             Bit Stream
                  │
                  ▼
          Packet Construction
                  │
                  ▼
            CRC Generation
                  │
                  ▼
             FSK Modulator
                  │
                  ▼
             PCM Samples
                  │
                  ▼
              🔊 Speaker
                  │
                  │ Sound
                  ▼
             🎙️ Microphone
                  │
                  ▼
            PCM Samples
                  │
                  ▼
         Goertzel Detection
                  │
                  ▼
             Bit Stream
                  │
                  ▼
           Packet Decoding
                  │
                  ▼
             CRC Check
                  │
                  ▼
          "Hello SonicLink"
```

---

# ⚠️ Current Limitations

SonicLink is an experimental acoustic modem, so it has several limitations.

### Fixed capture duration

The receiver currently uses a predefined capture duration instead of continuously detecting when a transmission begins and ends.

### Fixed transmission parameters

The current implementation uses fixed FSK frequencies and symbol timing.

### Noise sensitivity

Strong background noise or acoustic interference can cause incorrect frequency detection.

### Limited data rate

The system prioritizes simplicity and reliability over high-speed communication.

### Error detection only

CRC can identify corrupted packets but does not repair them.

---

# 🔮 Future Enhancements

Possible improvements include:

### Automatic synchronization

Add a preamble and synchronization sequence so the receiver can automatically detect the beginning of a transmission.

### Automatic start/stop detection

Replace fixed-duration recording with continuous monitoring and signal detection.

### Error correction

Implement techniques such as:

* Hamming codes
* Reed-Solomon codes
* Forward Error Correction (FEC)

### Adaptive transmission

Measure environmental noise and automatically adjust:

* Symbol duration
* Detection threshold
* Transmission frequency
* Data rate

### Higher-level protocol

Add:

* Packet sequence numbers
* Acknowledgements
* Retransmission
* Duplicate detection
* Multiple-message support

### Better signal processing

Potential future approaches include:

* FFT-based analysis
* Band-pass filtering
* Adaptive thresholds
* Automatic gain normalization

### Continuous communication

A future version could support:

```text
SonicLink
   ↕
Continuous acoustic channel
   ↕
Real-time messages
```

---

# 🔐 Security Considerations

SonicLink is designed as an experimental communication system, **not a secure messaging protocol**.

The transmitted audio can potentially be recorded by anyone within acoustic range.

CRC provides integrity checking but **does not provide encryption or authentication**.

Future versions could add cryptographic protection if secure communication becomes a project requirement.

---

# 🎯 Project Goals

The primary goal of SonicLink is to demonstrate how fundamental concepts from:

* Digital communication
* Signal processing
* Computer networking
* Error detection
* Audio processing
* Java programming

can be combined to create a working communication system without conventional wireless networking.

The project provides a practical example of how **information can be represented as physical signals and recovered through signal analysis**.

---

# 📚 References

The implementation is based on established concepts in digital communications and digital signal processing.

Recommended references:

1. **John G. Proakis and Masoud Salehi**, *Fundamentals of Communication Systems*, Pearson.

2. **Richard G. Lyons**, *Understanding Digital Signal Processing*, Pearson.

3. **Steven W. Smith**, *The Scientist and Engineer's Guide to Digital Signal Processing*, chapters covering digital filters and spectral analysis.

4. **NIST**, *Digital Signature Standard / CRC-related standards and publications*, for background on integrity and error-detection concepts.

5. **Oracle Java Documentation**, `javax.sound.sampled` package documentation, for Java audio capture and playback.

6. **Wikipedia**, *Goertzel Algorithm*, for introductory background on single-frequency detection.

7. **Wikipedia**, *Frequency-Shift Keying*, for introductory background on FSK digital modulation.

> **Note:** The references above are background resources for the underlying algorithms and technologies. The exact implementation in SonicLink is original project code.

---

# 👩‍💻 Author

**Gunjan**

SonicLink was developed as an educational project exploring the intersection of:

```text
Java
   +
Digital Signal Processing
   +
Digital Communication
   +
Audio Engineering
```

---

# 📜 License

This project is intended for educational and experimental use.

If a specific open-source license is added to the repository, this section should be updated accordingly.

---

## ⭐ Project Concept

> **What if two computers could talk without Wi-Fi, Bluetooth, or cables?**

SonicLink explores that question using something much older and simpler:

**sound.** 🔊
