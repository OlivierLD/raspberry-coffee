/*
 * Define connection parameters used in huzzah.smart.TCP.watch.ino
 */

#ifdef __RASPI_LOGGER__
#define _SSID "Pi-Net"
#define _PASSWORD "raspberrypi"
#define _HOST "192.168.127.1"
#define _HTTP_PORT 9999
#else
#define _SSID "Sonic-00e0"
#define _PASSWORD "67369c7831"
#define _HOST "192.168.42.4"
#define _HTTP_PORT 9998
#endif

