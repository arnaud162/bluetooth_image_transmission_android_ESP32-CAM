#include "BluetoothSerial.h"
#include "esp_camera.h"
#define CAMERA_MODEL_AI_THINKER // Has PSRAM
#include "camera_pins.h"
#include "time.h"
#include "soc/soc.h"           // Disable brownour problems
#include "soc/rtc_cntl_reg.h"  // Disable brownour problems

/* Check if Bluetooth configurations are enabled in the SDK */
/* If not, then you have to recompile the SDK */
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;
String sendFromComputerString="";
String receivedFromSmartphoneString="";

void setup() {
  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector
  Serial.begin(115200);

  // bluetooth configuration. quick and straightforward, isn't it ?
  SerialBT.begin("ESP32-CAM");
  Serial.println("Bluetooth Started! Ready to pair...");

  //camera configuration. lot of boilerplate, isn't it ?
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  
  // if PSRAM IC present, init with UXGA resolution and higher JPEG quality
  //                      for larger pre-allocated frame buffer.
  if(psramFound()){
    config.frame_size = FRAMESIZE_UXGA; //1600*1200
    config.jpeg_quality = 10;
    config.fb_count = 2;
  } 
  else 
  {
    config.frame_size = FRAMESIZE_SVGA; //800*600
    config.jpeg_quality = 12;
    config.fb_count = 1;
  }

  // Init Camera
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }
}

void loop() {
  sendFromComputerString="";
  receivedFromSmartphoneString="";
  
  if (Serial.available()) //strings can be sent to the smartphone. can be useful for debug. photo can be sent typing a 'p'.
  {
    sendFromComputerString = Serial.readStringUntil('\n');
    if (sendFromComputerString!="")
    {
      Serial.println("sendFromComputerString "+ sendFromComputerString);
      SerialBT.println(sendFromComputerString);
    }
  }

  //receiving command from the smartphone. used only for taking picture command.
  if (SerialBT.available())
  {
    receivedFromSmartphoneString = SerialBT.readString();
    Serial.println("received from bt : "+receivedFromSmartphoneString);
  }

    if (sendFromComputerString == "p" or receivedFromSmartphoneString == "{\"st\":3}"){
      long int t1 = millis(); //will allow us to measure how long the image transmission takes.
      Serial.print("ESP32-CAM will take a picture now \n");
      // Take Picture with Camera
      camera_fb_t * fb = NULL;
      fb = esp_camera_fb_get();  //now image can be acessed usins the fb pointer
      if(!fb) {
        Serial.println("Camera capture failed");
        return;
      }
      else{
        SerialBT.print(fb->len); //we begin by sendin picture size. will be used by the android app to count the bytes and know when image transmission ends.
        Serial.print("ESP32-CAM will send the picture now. size : ");
        Serial.print(fb->len);
        Serial.print("\n");
        delay(5); // a small delay to let the app understand what's happening
      }
      // image is fb->buf. we split it in packets and send them one after another.
      //theoretically all this could be done by just writing SerialBT.write(fb->buf, fb->len)
      //as the bluetoothSerial library handles the spliting in packets.
      //however many times wasn't working : android app was receving nothing. this is why spliting is done here.
      int packet_size = 330; //maximum packet size (MTU) in the bluetoothSerial library is 330. can be defined higher
      uint8_t packet[packet_size];
      int count=0;
      for(int i =0; i< int(fb->len/packet_size); i++){ //i loop runs trough the whole image
        for (int j=0; j<=packet_size; j++){ // j loop creates each packet byte byte
          packet[j] = fb->buf[i*packet_size+j];        
        }    
        count+=packet_size;  
        SerialBT.write(packet, packet_size); //packet is sent
      }
      Serial.print("count ");
      Serial.print(count);
      Serial.print("\n");
     // sending the last packet, which will very probably be < packet_size
     if (count< fb->len){
        int sizeOfLastPacket = fb->len-count;
        for (int j=0; j<=sizeOfLastPacket; j++){
          packet[j] = fb->buf[count+j];
        }  
        SerialBT.write(packet, sizeOfLastPacket);
  
        //with this we know in how much time the image was sent from the ESP32-CAM pov. generally around 1500ms.
        long int t2 = millis();
        Serial.print("image was sent in ");
        Serial.print(t2-t1);
        Serial.print("ms\n");
    }
  }
}
