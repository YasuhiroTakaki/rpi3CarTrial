package com.google.android.things.contrib.driver.adrptb8c;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.things.contrib.driver.adrptb8c.peripherals.motor.AdafruitDCMotor;
import com.google.android.things.contrib.driver.adrptb8c.peripherals.pwm.Pca9685;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by yasuhiro on 18/04/07.
 */

public class Adrptb8c implements AutoCloseable {
    private static final String TAG = Adrptb8c.class.getSimpleName();

    /**
     * Default I2C slave address.
     */
    private static final int I2C_ADDRESS0 = 0x60;
    private static final int I2C_ADDRESS1 = 0x70;

    private String mI2cBus = null;
    private I2cDevice mDevice;

    /**
     * Create a new driver for a  peripheral connected on the given I2C bus using the
     * {@link #I2C_ADDRESS0 default I2C address}.
     * @param bus
     */
    public Adrptb8c(String bus) throws IOException {
        mI2cBus = bus;
    }

    /**
     * Create a new driver for a  peripheral connected on the given I2C bus and using the
     * given I2C address.
     */
    public AdafruitDCMotor getMotor(int motor_num) throws IOException {
        PeripheralManager pioService = PeripheralManager.getInstance();
        int i2cAddress = (motor_num == 0)? I2C_ADDRESS0 : I2C_ADDRESS1;
        I2cDevice device = pioService.openI2cDevice(mI2cBus, i2cAddress);

        return createMotor(device, motor_num);
    }

    private Pca9685 mPwm = null;
    private AdafruitDCMotor[] mMotorList = new AdafruitDCMotor[2];
    private AdafruitDCMotor createMotor(I2cDevice device, int motor_num) {
        mDevice = device;
        mPwm = new Pca9685(mDevice);
        try {
            mPwm.initialize();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        AdafruitDCMotor motor = new AdafruitDCMotor(mPwm, 0);

        try {
            motor.setSpeed(255);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        mMotorList[motor_num] = motor;
        return motor;
    }

    /**
     * Close the device and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }
}