#
# Resources:
#    https://iotdesignpro.com/projects/speech-recognition-on-raspberry-pi-for-voice-controlled-home-automation
#    https://pythonspot.com/speech-recognition-using-google-speech-api/
#    https://pypi.org/project/SpeechRecognition/1.2.3/
#
from subprocess import call
import speech_recognition as sr
import pyaudio

print(f"SpeechRecognition version {sr.__version__}, PyAudio version {pyaudio.__version__}")
print("----------------------------------------------------------------------------------")

r = sr.Recognizer()


def speak(mess):
    # call(["espeak", "-s140  -ven+18 -z", mess])
    call(["espeak", mess])


# Records.
# device_index may vary.
def listen():
    with sr.Microphone(device_index=0) as source:
        r.adjust_for_ambient_noise(source)
        print("Say Something")
        audio = r.listen(source)
        print("got it")
    return audio


# Speech to text part
def voice(audio):
    try:
        text = r.recognize_google(audio)
        print("you said: " + text)
        return text
    except sr.UnknownValueError:
        mess = "Google Speech Recognition could not understand"
        print(mess)
        speak(mess)
        return 0
    except sr.RequestError as e:
        print("Could not request results from Google")
        return 0


# Voice -> Text -> Processor
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


# Main loop
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
