# Music-Recognition
This is a simple implementation of the music recognition algorithm based on major frequencies. For more information please read the report of the project.

## Description and components
This implementation is based on the Java programming language. For the theory please read the report and the references.

### Microphone data recorder
This module has the needed functions implemented for recording audio from the microphone and returning the data in a byte array.

### Music recognizer
This implements the main recognition algorithm based on the major frequencies in the signal. Please read the report for the full description of how the algorithm works.

### Utility
In this package, there are a number of helper functions which are used in the algorithm.

#### Fast Fourier transform
This function is taken from the website of Princeton University (the link is in the code) and implements FFT in Java.

#### Other
Other functions and classes are simply useful data structures and peripheral functions used in the algorithm that are useful when working with audio data.
