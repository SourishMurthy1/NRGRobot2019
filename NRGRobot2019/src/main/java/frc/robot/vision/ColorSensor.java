/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.vision;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.wpi.first.wpilibj.I2C;

/**
 * Add your docs here.
 */
public class ColorSensor {

    protected final static int CMD = 0x80;
    protected final static int MULTI_BYTE_BIT = 0x20;

    protected final static int ENABLE_REGISTER = 0x00;
    protected final static int ATIME_REGISTER = 0x01;
    protected final static int PPULSE_REGISTER = 0x0E;

    protected final static int ID_REGISTER = 0x12;
    protected final static int CDATA_REGISTER = 0x14;
    protected final static int RDATA_REGISTER = 0x16;
    protected final static int GDATA_REGISTER = 0x18;
    protected final static int BDATA_REGISTER = 0x1A;
    protected final static int PDATA_REGISTER = 0x1C;

    protected final static int PON = 0b00000001;
    protected final static int AEN = 0b00000010;
    protected final static int PEN = 0b00000100;
    protected final static int WEN = 0b00001000;
    protected final static int AIEN = 0b00010000;
    protected final static int PIEN = 0b00100000;
    private final double integrationTime = 10;

    private I2C sensor;

    private ByteBuffer buffy = ByteBuffer.allocate(8);
    private Boolean status;

    public ColorSensor(I2C.Port port) {
        buffy.order(ByteOrder.LITTLE_ENDIAN);

        sensor = new I2C(port, 0x39);
        // 0x39 is the address of the Vex ColorSensor V2

        sensor.write(CMD | 0x00, PON | AEN | PEN);

        // configures the integration time (time for updating color data)
        sensor.write(CMD | 0x01, (int) (256 - integrationTime / 2.38));
        sensor.write(CMD | 0x0E, 0b1111);
        // readColorSensor();
        // System.out.println(status());
    }

    public Color readColorSensor() {
        buffy.clear();
        if(sensor.read(CMD | MULTI_BYTE_BIT | RDATA_REGISTER, 8, buffy)){
            System.out.println("Fail");
        }else{
            System.out.println("Success");
        }
        Color color = new Color();
        color.red = (int) buffy.getShort(0) & 0xFFFF;
        color.green = (int) buffy.getShort(2) & 0xFFFF;
        color.blue = (int) buffy.getShort(4) & 0xFFFF;
        color.prox = (int) buffy.getShort(6) & 0xFFFF;
        return color;
    }

    public int status() {
        buffy.clear();
        sensor.read(CMD | 0x13, 1, buffy);
        return buffy.get(0);
    }

    public void free() {
        sensor.free();
    }

    public class Color {
        public int red;
        public int green;
        public int blue;
        public int prox;
    }
}