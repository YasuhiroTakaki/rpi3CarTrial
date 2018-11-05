package com.google.android.things.contrib.driver.adrptb8c.peripherals.motor;

import com.google.android.things.contrib.driver.adrptb8c.peripherals.pwm.Pca9685;

import java.io.IOException;

/**
 * Created by yasuhiro on 18/04/07.
 */

public class AdafruitDCMotor {
    private static final String TAG = AdafruitDCMotor.class.getSimpleName();

    public static final int DRIVE_FORWARD  = 1;
    public static final int DRIVE_BACKWARD = 2;
    public static final int DRIVE_BRAKE    = 3;
    public static final int DRIVE_RELEASE  = 4;

    private class MotorControlPins {
        /* package */ int pwmPin = -1;
        /* package */ int in1Pin = -1;
        /* package */ int in2Pin = -1;
        /* package */ MotorControlPins(int pwm_pin, int in1_pin, int in2_pin) {
            pwmPin = pwm_pin;
            in1Pin = in1_pin;
            in2Pin = in2_pin;
        }
    }
    private MotorControlPins mPins = null;
    private Pca9685 mPwm = null;

    public AdafruitDCMotor(Pca9685 pwm, int motor_num) {
        mPwm = pwm;
        switch(motor_num) {
        case 0:
            mPins = new MotorControlPins(8, 10, 9);
            break;
        case 1:
            mPins = new MotorControlPins(13, 11, 12);
            break;
        case 2:
            mPins = new MotorControlPins(2, 4, 3);
            break;
        case 3:
            mPins = new MotorControlPins(7, 5, 6);
            break;
        default:
            throw new IllegalArgumentException("Invalid pin num");
        }
    }

    public void drive(int mode) throws IOException {
        switch (mode) {
        case DRIVE_FORWARD:
            mPwm.setPin(mPins.in2Pin, false);
            mPwm.setPin(mPins.in1Pin, true);
            break;
        case DRIVE_BACKWARD:
            mPwm.setPin(mPins.in1Pin, false);
            mPwm.setPin(mPins.in2Pin, true);
            break;
        case DRIVE_RELEASE:
            mPwm.setPin(mPins.in1Pin, false);
            mPwm.setPin(mPins.in2Pin, false);
            break;
        case DRIVE_BRAKE:
            /* NOTHING TO DO */
            break;
        }
    }

    public void setSpeed(int speed) throws IOException {
        if (speed < 0)
            speed = 0;
        if (speed > 255)
            speed = 255;
        mPwm.setPwm(mPins.pwmPin, (short)0, (short)(speed*16));
    }
}
