# Problem Statement
SonicLink Project Statement

Project Title

SonicLink: Acoustic FSK Communication System

1. Project Overview

SonicLink is an experimental acoustic communication system developed in Java. It demonstrates how digital information can be converted into an audio signal, transmitted through an acoustic channel, captured by a microphone, and reconstructed at the receiving end.

2. Problem Statement

Most modern digital communication relies on Wi-Fi, Bluetooth, mobile networks, or wired connections. SonicLink explores sound as an alternative short-range communication medium and investigates the practical steps required to encode, transmit, receive, and verify digital data acoustically.

3. Proposed Solution

The sender converts input data into a structured packet, adds CRC information, converts the packet into bits, modulates the bits using BFSK, and plays the resulting PCM waveform through a speaker.

The receiver captures microphone audio, detects the synchronization preamble, identifies the transmitted frequencies, reconstructs the bits, decodes the packet, and verifies the CRC.

4. Objectives
#Implement BFSK modulation in Java.
#Generate and process PCM audio.
#Transmit digital information acoustically.
#Implement packet-based communication.
#Implement CRC-based error detection.
#Develop transmitter and receiver components.
#Provide a command-line interface.
#Explore practical acoustic communication constraints.


5. Communication Method

SonicLink uses Binary Frequency Shift Keying:

0 → 1200 Hz
1 → 2200 Hz

Each symbol is represented by one of these frequencies for a configured symbol duration.

6. System Architecture

Input → Packet → CRC → Bits → BFSK → PCM → Speaker
                                              ↓
                                        Acoustic Channel
                                              ↓
Microphone ← PCM ← Demodulation ← Preamble ← Audio
   ↓
Packet Decode → CRC Check → Output



7. Reliability

CRC is included to detect errors introduced during transmission. The receiver recalculates the checksum and compares it with the transmitted value before accepting the packet.

8. File Input

The command-line interface supports file input workflows for TXT and can process PDF/image input through text extraction or OCR where the required local tools are available.

9. Implementation

The implementation is divided into audio, codec, CLI, and utility components. This separation keeps packet handling and signal processing independent from user interaction and audio-device access.

10. Testing

Testing covers binary conversion, packet encoding and decoding, frequency detection, CRC validation, modulation/demodulation, and software loopback. Actual acoustic transmission was also exercised through the speaker output.

11. Expected Outcome

The expected outcome is a working prototype that demonstrates:

DATA → BITS → FREQUENCIES → SOUND
SOUND → FREQUENCIES → BITS → DATA

with CRC used as an integrity check.

12. Educational Significance

The project combines digital communication, signal processing, audio I/O, data encoding, error detection, Java OOP, file processing, testing, and command-line application development.

13. Future Scope

Future versions can add adaptive modulation, stronger error correction, higher speeds, automatic calibration, binary file reconstruction, real-time visualization, and a graphical interface.

14. Conclusion

SonicLink demonstrates an end-to-end acoustic digital communication pipeline. It provides a practical implementation of BFSK, packetization, audio processing, synchronization, demodulation, and CRC validation.

Project Status

Working Prototype

Language: Java
Modulation: BFSK
Medium: Acoustic Audio
Integrity Check: CRC
Interface: Command Line
Wireless data transfer today almost always depends on Wi-Fi or Bluetooth,
both of which require paired hardware, network configuration, or
compatible protocols. In situations where neither is available or
practical (e.g. simple proximity-based data exchange, low-tech
environments, or educational demonstrations of digital communication
principles), there is no simple, dependency-free way to transmit a small
piece of data between two computers.

SonicLink addresses this by using a channel every computer already has:
sound. It encodes data as audio tones and decodes it back using signal
processing techniques (FSK modulation and the Goertzel algorithm),
demonstrating that reliable, error-checked data transfer is possible
using nothing but a speaker and a microphone.

## Scope

- Encode short text messages into audio tones and transmit them via the
  system speaker
- Decode audio captured via the system microphone back into the original
  text message
- Detect transmission errors using a checksum
- Command-line interface only, no GUI dependency
- Designed for short-range (same room), short-message use cases —
  not intended as a replacement for real wireless protocols at scale

## Target Users

- Students learning digital signal processing and communication theory
  concepts, who want a hands-on, audible demonstration of encoding/
  decoding rather than a purely theoretical one
- Anyone needing simple, ad-hoc data transfer between two nearby devices
  without configuring Wi-Fi/Bluetooth

## High-Level Features

- FSK-based audio modulation and demodulation
- Goertzel algorithm-based tone detection (efficient, targeted frequency
  detection without a full FFT)
- Checksum-based integrity verification
- Simple CLI: `send "<message>"` and `listen`
