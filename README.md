# SonicLink

Transmit short text messages between two computers using sound — no Wi-Fi
or Bluetooth required. Data is encoded as audio tones (Frequency Shift
Keying) played through the speaker, and decoded on another machine by
listening through the microphone using the Goertzel algorithm.

> Status: project skeleton — core encoding/decoding logic is being
> implemented. See "Project Structure" below for what each file does.

## Overview

- **Sender**: converts a text message into bits, encodes each bit as one
  of two audio tones, and plays the resulting audio through the speaker.
- **Receiver**: listens through the microphone, detects which tone is
  present in each time window using the Goertzel algorithm, and
  reconstructs the original message.
- **Integrity check**: a checksum is transmitted alongside the message so
  the receiver can detect (not correct) errors caused by background noise.

## Features

- Encode and transmit text messages as audio (FSK modulation)
- Decode incoming audio back into text (Goertzel-based tone detection)
- Checksum-based transmission integrity verification
- Fully command-line driven, no GUI required

## Technologies Used

- Java 11+ (standard library only — `javax.sound.sampled` for audio I/O)
- No external dependencies

## Prerequisites

- JDK 11 or later installed (`java -version` to check)
- A working speaker (for sending) and microphone (for receiving) on your
  machine, with OS-level microphone permissions granted to your terminal

## Setup & Installation

1. Clone the repository:
   ```
   git clone https://github.com/kongunjan/SonicLink.git
   cd SonicLink
   ```

2. Build the project:
   ```
   ./build.sh
   ```
   (On Windows, run the equivalent commands manually — see `build.sh` for
   the two commands it runs, or use `javac` directly from PowerShell.)

## Running the Project

**To send a message** (run on the sending machine):
```
java -cp out com.soniclink.cli.SonicLinkCLI send "Hello World"
```

**To listen for a message** (run on the receiving machine, or a second
terminal on the same machine to test locally):
```
java -cp out com.soniclink.cli.SonicLinkCLI listen
```

For a first test, try running both commands on the **same machine** in two
terminal windows, with your speaker volume up — this avoids needing two
physical devices while you're debugging.

## Testing

A simple round-trip test for the bit/byte conversion utility is included:
```
java -cp out com.soniclink.BitStreamUtilsTest
```

Additional manual testing approach (document your actual results in the
project report):
- Test at varying distances between speaker and microphone
- Test with varying message lengths
- Measure bit-error rate at each distance/volume combination

## Project Structure

```
SonicLink/
├── build.sh                          # compile helper script
├── README.md
├── statement.md                      # problem statement, scope, target users
└── src/
    ├── main/java/com/soniclink/
    │   ├── cli/
    │   │   └── SonicLinkCLI.java     # entry point, argument parsing
    │   ├── codec/
    │   │   ├── Modulator.java        # bits -> audio samples (FSK encode)
    │   │   ├── Demodulator.java      # audio samples -> bits (decode orchestration)
    │   │   └── GoertzelDetector.java # core DSP: detect a tone's presence in a window
    │   ├── audio/
    │   │   ├── AudioTransmitter.java # plays PCM bytes through speaker
    │   │   └── AudioReceiver.java    # captures PCM bytes from microphone
    │   └── util/
    │       ├── BitStreamUtils.java   # byte[] <-> bit[] conversion
    │       ├── ChecksumValidator.java# integrity checking
    │       └── SonicConfig.java      # shared constants (frequencies, sample rate, etc.)
    └── test/java/com/soniclink/
        └── BitStreamUtilsTest.java   # round-trip test
```

## Known Limitations / Future Enhancements

- Currently uses a fixed capture duration on the receiver rather than
  continuous streaming with automatic start/stop detection
- Error *correction* (not just detection) could be added with Hamming
  codes for better reliability in noisy environments
- Transmission speed (baud rate) is fixed; could be made adaptive based
  on measured ambient noise

## References

- (add the specific articles/resources you personally used while
  implementing the Goertzel algorithm and FSK scheme)
