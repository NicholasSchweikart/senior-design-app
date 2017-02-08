package edu.mtu.team9.aspirus;

/**
 * Created for Aspirus2
 * By: nicholas on 1/31/17.
 * Description:
 */

public class AnkletConst {

    public static final byte
            COMMAND_RESPONSE_FLAG = '#',
            RUNNING_FLAG        = 'U',
            READY_FLAG          = 'R',
            CSV_ENABLED_FLAG    = 'E',
            CSV_DISABLED_FLAG   = 'D';

    public static final byte[]
            START_MESSAGE       ={'S'},
            STOP_MESSAGE        ={'X'},
            ENABLE_CSV_MESSAGE  ={'E'},
            DISABLE_CSV_MESSAGE ={'D'},
            RESET_MESSAGE       ={'Z'};

    public static final int
            CONNECTION_LOST = 1,
            CONNECTED = 2,
            CONNECTION_FAILED = 3,
            STATE_CONNECTING = 3,
            STATE_CONNECTED = 2,
            STATE_READY = 1,
            STATE_RUNNING = 0;
}
