#
# Resources:
#    https://iotdesignpro.com/projects/speech-recognition-on-raspberry-pi-for-voice-controlled-home-automation
#    https://pythonspot.com/speech-recognition-using-google-speech-api/
#    https://pypi.org/project/SpeechRecognition/1.2.3/
#
from subprocess import call
import speech_recognition as sr
import pyaudio
# import serial
# import RPi.GPIO as GPIO
# import os, time

print(f"SpeechRecognition version {sr.__version__}, PyAudio version {pyaudio.__version__}")
# for index, name in enumerate(sr.Microphone.list_microphone_names()):
#     print(f"Microphone with name \"{name}\" found for `Microphone(device_index={index})`")
print("----------------------------------------------------------------------------------")

r = sr.Recognizer()
# led = 27
# text = {}
# text1 = {}
# GPIO.setwarnings(False)
# GPIO.setmode(GPIO.BCM)
# GPIO.setup(led, GPIO.OUT)

def speak(mess):
    # call(["espeak", "-s140  -ven+18 -z", mess])
    call(["espeak", mess])


# device_index may vary.
def listen():
    with sr.Microphone(device_index=0) as source:
        r.adjust_for_ambient_noise(source)
        print("Say Something")
        audio = r.listen(source)
        print("got it")
    return audio


def voice(audio):
    try:
        text = r.recognize_google(audio)
        print("you said: " + text)
        return text
    except sr.UnknownValueError:
        speak("Google Speech Recognition could not understand")
        print("Google Speech Recognition could not understand")
        return 0
    except sr.RequestError as e:
        print("Could not request results from Google")
        return 0


def command_processor(text):
    if 'lights on' in text:
        # GPIO.output(led, 1)
        speak("okay  Sir, Switching ON the Lights")
        print("Lights on")
    elif 'lights off' in text:
        # GPIO.output(led, 0)
        speak("okay  Sir, Switching off the Lights")
        print("Lights Off")
    elif 'exit' in text or 'out' in text or 'you are fired' in text:
        return 0
    else:
        mess = f"Let me know what to do with {text}."
        speak(mess)
        print(mess)
    return 1


if __name__ == '__main__':
    keep_asking = True
    while keep_asking:
        try:
            audio = listen()     # Listen through the mic
            text = voice(audio)  # Translates into words
            if text != 0:
                result = command_processor(text)
                if result == 0:
                    keep_asking = False
                    mess = "Ok, I'm out."
                    print(mess)
                    speak(mess)
        except KeyboardInterrupt as ctrl_c:
            keep_asking = False
    print("\nExiting, bye.")

