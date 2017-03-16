package edu.mtu.team9.aspirus.anklet;

/**
 * Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9
 * Description: This class is essential an H file for the project. All anklet related constants
 * are define here to simplify making global changes.
 */
class AnkletConst {

     static final byte
            COMMAND_RESPONSE_FLAG = '#',
            RUNNING_FLAG        = 'U',
            READY_FLAG          = 'R',
            CSV_ENABLED_FLAG    = 'E',
            CSV_DISABLED_FLAG   = 'D';

     static final byte[]
            START_MESSAGE       ={'S'},
            STOP_MESSAGE        ={'X'},
            ENABLE_CSV_MESSAGE  ={'E'},
            DISABLE_CSV_MESSAGE ={'D'},
            RESET_MESSAGE       ={'Z'};

     static final int
            CONNECTION_LOST = 1,
            CONNECTED = 2,
            CONNECTION_FAILED = 3,
            STATE_CONNECTING = 3,
            STATE_CONNECTED = 2,
            STATE_READY = 1,
            STATE_RUNNING = 0;
}
