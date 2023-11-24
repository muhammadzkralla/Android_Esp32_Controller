#include <WiFi.h>
#include <WiFiClient.h>

const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_PASSWORD";
const uint16_t port = 80;

WiFiServer server(port);
WiFiClient client;

void setup() {
  pinMode(12, OUTPUT);
  pinMode(14, OUTPUT);
  Serial.begin(115200);
  randomSeed(analogRead(A0));
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }

  Serial.println("Connected to WiFi");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
  Serial.print("Port: ");
  Serial.println(port);

  server.begin();
}

void loop() {
  if (!client.connected()) {
    client = server.available();
    if (client) {
      Serial.println("Client connected");
    }
  }

  if (client.connected()) {
    digitalWrite(12, HIGH);
    digitalWrite(14, LOW);
    if (client.available()) {
      String receivedData = client.readStringUntil('\n');
      Serial.print("Received from app: ");
      Serial.println(receivedData);

      String responseMessage = "Hello from ESP32! " + String(random(0, 100));
      client.println(responseMessage);
      Serial.println("Response sent: " + responseMessage);
    }
  } else {
    client.stop();
    digitalWrite(12, LOW);
    digitalWrite(14, LOW);
  }
}
