## Video Streaming from the Raspberry Pi

- `sudo apt-get-install vlc`
- `sudo raspi-config` to enable the camera module
- The run `raspivid -o - -t 0 -hf -vf -w 800 -h 400 -fps 24 | cvlc -vvv stream:///dev/stdin --sout '#standard{access=http,mux=ts,dst=:8160}' :demux=h264`
    - Streaming on port `8160` here
- From another machine, use `vlc player`, and open (`File > Open > Network`) the stream `http://192.168.42.15:8160` (or whatever the Raspberry Pi's IP address is).
- Done!
    
