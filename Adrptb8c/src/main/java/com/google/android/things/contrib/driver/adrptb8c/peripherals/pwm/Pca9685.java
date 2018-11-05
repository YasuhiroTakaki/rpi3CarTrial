package com.google.android.things.contrib.driver.adrptb8c.peripherals.pwm;

import android.util.Log;

import com.google.android.things.pio.I2cDevice;

import java.io.IOException;

public class Pca9685 {
    private static final String TAG = Pca9685.class.getSimpleName();

    // Registers/etc:
    private static final int PCA9685_ADDRESS = 0x40;
    private static final int MODE1           = 0x00;
    private static final int MODE2           = 0x01;
    private static final int SUBADR1         = 0x02;
    private static final int SUBADR2         = 0x03;
    private static final int SUBADR3         = 0x04;
    private static final int PRESCALE        = 0xFE;
    private static final int LED0_ON_L       = 0x06;
    private static final int LED0_ON_H       = 0x07;
    private static final int LED0_OFF_L      = 0x08;
    private static final int LED0_OFF_H      = 0x09;
    private static final int ALL_LED_ON_L    = 0xFA;
    private static final int ALL_LED_ON_H    = 0xFB;
    private static final int ALL_LED_OFF_L   = 0xFC;
    private static final int ALL_LED_OFF_H   = 0xFD;

    // Bits:
    private static final byte RESTART        = (byte)0x80;
    private static final byte SLEEP          = (byte)0x10;
    private static final byte ALLCALL        = (byte)0x01;
    private static final byte INVRT          = (byte)0x10;
    private static final byte OUTDRV         = (byte)0x04;

    // Pins:
    private static final int PIN_NO_MIN      = 0;
    private static final int PIN_NO_MAX      = 15;

    private I2cDevice mDevice = null;

    // PCA9685 PWM LED/servo controller.
    public Pca9685(I2cDevice device) {
        mDevice = device;
    }

    public void initialize() throws IOException {
        // Initialize the PCA9685.
        short on = 0, off = 0;
        setAllPwm(on, off);
        write8(MODE2, OUTDRV);
        write8(MODE1, ALLCALL);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte mode1 = read8(MODE1);
        mode1 = (byte)(mode1 & ~SLEEP);  // wake up (reset sleep)
        write8(MODE1, mode1);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setPwmFreq(int freq_hz) throws IOException {
        // Set the PWM frequency to the provided value in hertz.
        double pre_scale_val = 25000000.0; // 25MHz
        pre_scale_val /= 4096.0;           // 12-bit
        pre_scale_val /= (double)freq_hz;
        pre_scale_val -= 1.0;
        Log.i(TAG, "Setting PWM frequency to "+freq_hz+"Hz");
        Log.i(TAG, "Estimated pre-scale:"+pre_scale_val);
        byte pre_scale = (byte)Math.floor(pre_scale_val + 0.5);
        Log.i(TAG, "Final pre-scale:"+pre_scale);
        byte old_mode = read8(MODE1);
        byte new_mode = (byte)((old_mode & 0x7F) | 0x10); // sleep
        write8(MODE1, new_mode); // go to sleep
        write8(PRESCALE, pre_scale);
        write8(MODE1, old_mode);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        write8(MODE1, (byte)(old_mode|0x80));
    }

    public void setPwm(int channel, short on, short off) throws IOException {
        // Sets a single PWM channel.
        write8(LED0_ON_L+4*channel, (byte) (on & 0xFF));
        write8(LED0_ON_H+4*channel, (byte) (on >> 8));
        write8(LED0_OFF_L+4*channel, (byte) (off & 0xFF));
        write8(LED0_OFF_H+4*channel, (byte) (off >> 8));
    }

    public void setAllPwm(short on, short off) throws IOException {
        // Sets all PWM channels.
        write8(ALL_LED_ON_L, (byte) (on & 0xFF));
        write8(ALL_LED_ON_H, (byte) (on >> 8));
        write8(ALL_LED_OFF_L, (byte) (off & 0xFF));
        write8(ALL_LED_OFF_H, (byte) (off >> 8));
    }

    public void setPin(int pin, boolean value) throws IOException {
        if (pin < PIN_NO_MIN || PIN_NO_MAX < pin) {
            throw new IndexOutOfBoundsException("PWM pin must be between 0 and 15 inclusive");
        }

        if (value)
            setPwm(pin, (short)0, (short)4096);
        else
            setPwm(pin, (short)4096, (short)0);
    }

    /***
     * Write 8bit row data to the given column.
     * @param column
     * @param data ROW8
     */
    private void write8(int column, byte data) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C device not opened");
        }
        Log.e(TAG, "colum:"+column+" data:"+data);
        mDevice.writeRegByte(column, data);
    }
    /***
     * Read 8bit row data to the given column.
     * @param column
     */
    private byte read8(int column) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C device not opened");
        }
        return mDevice.readRegByte(column);
    }
}
