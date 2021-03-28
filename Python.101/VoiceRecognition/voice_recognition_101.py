#
# Resources:
#    https://iotdesignpro.com/projects/speech-recognition-on-raspberry-pi-for-voice-controlled-home-automation
#    https://pythonspot.com/speech-recognition-using-google-speech-api/
#    https://pypi.org/project/SpeechRecognition/1.2.3/
#
from subprocess import call
import speech_recognition as sr
import serial
# import RPi.GPIO as GPIO
import os, time

print(f"SpeechRecognition version {sr.__version__}")

r = sr.Recognizer("en_US")
led = 27
text = {}
text1 = {}
# GPIO.setwarnings(False)
# GPIO.setmode(GPIO.BCM)
# GPIO.setup(led, GPIO.OUT)


def listen1():
    with sr.Microphone(device_index=2) as source:
        r.adjust_for_ambient_noise(source)
        print("Say Something")
        audio = r.listen(source)
        print("got it")
    return audio


def voice(audio1):
    try:
        text1 = r.recognize_google(audio1)
        ##         call('espeak '+text, shell=True)
        print("you said: " + text1)
        return text1
    except sr.UnknownValueError:
        call(["espeak", "-s140  -ven+18 -z", "Google Speech Recognition could not understand"])
        print("Google Speech Recognition could not understand")
        return 0
    except sr.RequestError as e:
        print("Could not request results from Google")
        return 0


def main(text):
    audio1 = listen1()
    text = voice(audio1)
    if 'light on' in text:
        # GPIO.output(led, 1)
        call(["espeak", "-s140  -ven+18 -z", "okay  Sir, Switching ON the Lights"])
        print("Lights on")
    elif 'light off' in text:
        # GPIO.output(led, 0)
        call(["espeak", "-s140  -ven+18 -z", "okay  Sir, Switching off the Lights"])
        print("Lights Off")
    text = {}


if __name__ == '__main__':
    while (1):
        audio1 = listen1()
        text = voice(audio1)
        if text == 'hello':
            text = {}
            call(["espeak", "-s140  -ven+18 -z", " Okay master, waiting for your command"])
            main(text)
        else:
            call(["espeak", "-s140 -ven+18 -z", " Please repeat"])
