#include <stdio.h>
#include "JoyCar.h"
#include <wiringPi.h>
#include <iostream>
#include <pcf8591.h>
#include <softPwm.h>
#include <math.h>
#include <stdlib.h>

#define address 0x48       
#define pinbase 64
#define A0 pinbase + 0
#define A1 pinbase + 1
#define A2 pinbase + 2
#define A3 pinbase + 3
#define motorPin1 2
#define motorPin2 0 
#define enablePin 3

#define OFFSET_MS 3
#define SERVO_MIN_MS 5 + OFFSET_MS
#define SERVO_MAX_MS 25 + OFFSET_MS

#define Z_Pin 1
#define servoPin 4

using namespace std;

void communicate(int pin, int value) {
	softPwmWrite(pin, value);
}

JNIEXPORT jint JNICALL Java_JoyCar_initializeWiringPi(JNIEnv *env, jobject o) {
	if (wiringPiSetup() == -1) 
		return 0;
	else
		return 1;
}

JNIEXPORT void JNICALL Java_JoyCar_initializePCF8591(JNIEnv *env, jobject o) {
	pinMode(Z_Pin, INPUT);    
    pullUpDnControl(Z_Pin, PUD_UP);
	pcf8591Setup(pinbase, address);
}

int readAnalog(int pin) {
	return analogRead(pin);
}

int readDigital(int pin) {
	return digitalRead(pin);
}

JNIEXPORT jint JNICALL Java_JoyCar_readUpDown(JNIEnv *env, jobject o) {
  return readAnalog(A1);
}

JNIEXPORT jint JNICALL Java_JoyCar_readLeftRight(JNIEnv *env, jobject o) {
	return readAnalog(A0);
}
JNIEXPORT jboolean JNICALL Java_JoyCar_isButtonPressed(JNIEnv *env, jobject o) {
  return readDigital(Z_Pin) == 0;
}

long map(long value, long fromLow, long fromHigh, long toLow, long toHigh) {
    int result = (toHigh - toLow) * (value-fromLow) / (fromHigh - fromLow) + toLow;
    return result;
}

void servoInit(int pin) {
    softPwmCreate(pin, 0, 200);
}

JNIEXPORT void JNICALL Java_JoyCar_turnAtAngle(JNIEnv *env, jobject o, jint angle) {
	if (angle > 180)
        angle = 180;
    if (angle < 0)
        angle = 0;
    communicate(servoPin, map(angle, 0, 180, SERVO_MIN_MS, SERVO_MAX_MS));
    delay(100);
}

void servoWriteMS(int pin, int ms) { 
    if(ms > SERVO_MAX_MS)
        ms = SERVO_MAX_MS;
    if(ms < SERVO_MIN_MS)
        ms = SERVO_MIN_MS;
    communicate(pin, ms);
}

JNIEXPORT void JNICALL Java_JoyCar_initializeServo(JNIEnv *env, jobject o) {
	servoInit(servoPin);
}

JNIEXPORT void JNICALL Java_JoyCar_turnRight(JNIEnv *env, jobject o) {
	int i;
	for (i = SERVO_MIN_MS; i < SERVO_MAX_MS; i++) {
		servoWriteMS(servoPin, i);
        delay(100);
    }
}

JNIEXPORT void JNICALL Java_JoyCar_turnLeft(JNIEnv *env, jobject o) {
	int i;
	for (i = SERVO_MAX_MS; i > SERVO_MIN_MS; i--) { 
		servoWriteMS(servoPin, i);
        delay(100);
    }
}

void motor(int ADC) {
	int value = ADC -130;
	if (value > 0) {
		digitalWrite(motorPin1, HIGH);
		digitalWrite(motorPin2, LOW);
	} else if (value<0) {
		digitalWrite(motorPin1, LOW);
		digitalWrite(motorPin2, HIGH);
	} else {
		digitalWrite(motorPin1, LOW);
		digitalWrite(motorPin2, LOW);
	}
	communicate(enablePin, map(abs(value), 0, 130, 0, 255));
}

JNIEXPORT void JNICALL Java_JoyCar_initializeMotor(JNIEnv *env, jobject o) {
	pinMode(enablePin, OUTPUT);
	pinMode(motorPin1, OUTPUT);
	pinMode(motorPin2, OUTPUT);
	softPwmCreate(enablePin, 0, 100);
}

JNIEXPORT void JNICALL Java_JoyCar_runMotor(JNIEnv *env, jobject o, jint val) {
	motor(val);
	delay(100);
}