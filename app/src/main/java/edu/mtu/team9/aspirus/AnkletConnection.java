package edu.mtu.team9.aspirus;

/**
 * Created for Aspirus2
 * By: nicholas on 1/31/17.
 * Description:
 */

public class AnkletConnection {

    public final byte
            RUNNING =       (byte) 'U',
            READY =         (byte) 'R',
            STOP =          (byte) 'X',
            PAUSE=          (byte) 'P',
            MESSAGE =       (byte) 'M',
            COMMAND =       (byte) 'C',
            START =         (byte) 'S',
            STATUS =        (byte) 'S',
            HANDSHAKE =     (byte) 'H',
            LIFT_OFF  =     (byte) '^',
            HEEL_DOWN =     (byte) '_',
            EVENT =         (byte) 'E';
    public static final int CONNECTION_LOST = 1,
        CONNECTED = 2,
        CONNECTION_FAILED = 3;
}
