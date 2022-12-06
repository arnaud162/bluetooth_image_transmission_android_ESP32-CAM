# bluetooth_image_transmission_android_ESP32-CAM

-Upload ino file to ESP32-CAM. Procedure detailed here : using arduino IDE https://docs.espressif.com/projects/arduino-esp32/en/latest/installing.html. wiring (add link)

-Compile android project using android studio and upload it to your favourite smartphone.

-Use the "take photo" button. ESP32-CAM will take a photo and send it to the app. Except 1 to 2s for transmission of a 1600x1200 JPG (100~200kb).

Transmission takes so long because of inefficiencies on the android side and the RFCOMM protocol which doesn't seem adapted for image transmission.
Currently working on a L2CAP image transmission but can't garantee it will work, few informations are available.
