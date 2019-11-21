#
# Read NMEA Serial input in Python
#
add_library('serial')
import nmea_parser as NMEAParser

LF = 10 # LineFeed
DEBUG = False

latitude = " - No Latitude - "
longitude = " - No Longitude - "
    
    
def setup():
    size(640, 360)
    # Create the font
    textFont(createFont("Verdana", 36))
    #
    print Serial.list()
    gpsPort = Serial(this, "/dev/tty.usbmodem14201", 4800)
    gpsPort.bufferUntil(LF)
    

def draw():
    background(0)  # Set background to black
    # Draw the letter to the center of the screen
    textSize(36)
    text("GPS Data", 50, 50)
    text(latitude, 50, 100)
    text(longitude, 50, 150)
    

def serialEvent(event):
    global latitude
    global longitude
    inputSentence = event.readString()
    print("received: {}".format(inputSentence))
    try:
        nmea_obj = NMEAParser.parse_nmea_sentence(inputSentence)
        try:
            if nmea_obj["type"] == 'rmc':
                print("RMC => {}".format(inputSentence))
                print("RMC => {}".format(nmea_obj))
                if 'position' in nmea_obj['parsed']:
                    latitude = NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], NMEAParser.NS)
                    longitude = NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], NMEAParser.EW)
                    print("This is RMC: {} / {}".format(latitude, longitude))
            elif nmea_obj["type"] == 'gll':
                print("GLL => {}".format(nmea_obj))
                if 'position' in nmea_obj['parsed']:
                    latitude = NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], NMEAParser.NS)
                    longitude = NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], NMEAParser.EW)
                    print("This is GLL: {} / {}".format(latitude, longitude))
            else:
                print("{} => {}".format(nmea_obj["type"], nmea_obj))
        except AttributeError as ae:
            print("AttributeError for {}".format(nmea_obj))
    except NMEAParser.NoParserException as npe:
        # absorb
        if DEBUG:
            print("- No parser, {}".format(npe))
    except Exception as ex:
        print("\tOoops! {} {}".format(type(ex), ex))
        
