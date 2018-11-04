/*
 * Define connection parameters used in huzzah.smart.TCP.watch.ino
 */

#ifdef __PI_NET__
#define _SSID "Pi-Net"
#define _PASSWORD "raspberrypi"
#define _HOST "192.168.127.1"
#define _HTTP_PORT 9999
#endif

#ifdef __SONIC_AT_HOME__
#define _SSID "Sonic-00e0"
#define _PASSWORD "67369c7831"
#define _HOST "192.168.42.4"
#define _HTTP_PORT 9998
#endif

// Etc...
