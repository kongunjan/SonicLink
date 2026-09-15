# Problem Statement

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
