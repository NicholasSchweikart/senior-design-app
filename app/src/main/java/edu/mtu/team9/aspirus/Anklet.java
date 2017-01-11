package edu.mtu.team9.aspirus;

/**
 * Created by nssch on 10/30/2016.
 */

public class Anklet{

    public ANKLET_STATE ankletState;
    public String DEVICE_ADDRESS;
    public byte anklet_id;
    public int TOTAL_TIME = 0;
    public int TOTAL_STEPS = 0;

    public final byte RUNNING = (byte) 'U',
            READY = (byte) 'R',
            STOP = (byte) 'X',
            DATA = (byte) 'D',
            COMMAND = (byte) 'C',
            START = (byte) 'S',
            STATUS = (byte) 'S',
            HANDSHAKE = (byte) 'H';

    public byte[] handshakeMessage = new byte[3],
            startMessage = new byte[3],
            retxMessage = new byte[3],
            stopMessage = new byte[3];

    public Anklet(String device_address, char anklet_id){

        this.DEVICE_ADDRESS = device_address;

        ankletState = ANKLET_STATE.INIT;

        this.anklet_id = (byte)anklet_id;

        this.handshakeMessage[0] = HANDSHAKE;
        this.handshakeMessage[1] = this.anklet_id;

        this.startMessage[0] = COMMAND;
        this.startMessage[1] = START;

        this.stopMessage[0] = COMMAND;
        this.stopMessage[1] = STOP;

        this.retxMessage[0] = COMMAND;
        this.retxMessage[1] = READY;
    }

    public byte[] getReTXmessage(byte packet_number){
        retxMessage[2] = packet_number;
        return retxMessage;
    }
}
