# bluetooth_image_transmission_android_ESP32

Upload ino file to ESP32-CAM
Compile android project using android studio and upload it to your favourite smartphone.
Send a "p" to ESP32-CAM using serial monitor. ESP32-CAM will take a photo and send it to the app. Except 1 to 2s for transmission of a 1280x800 JPEG (~100kb).
Transmission takes so much because of inefficiencies on the android side and of the RFCOMM protocol which doesn't seem adapted for image transmission.
Currently working on a L2CAP image transmission but can't garantee it will work, few informations are available.
