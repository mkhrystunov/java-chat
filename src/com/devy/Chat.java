package com.devy;

public class Chat {

    // from client
    public static final int PORTNUM = 11111;
    public static final int MAX_LOGIN_LENGTH = 20;
    public static final char SEPARATOR = '\\';
    public static final char COMMAND = '\\';
    public static final char CMD_LOGIN = 'L';
    public static final char CMD_QUIT = 'Q';
    public static final char CMD_MSG = 'M';
    public static final char CMD_BCAST = 'B';

    // from server
    public static final char RESP_PUBLIC = 'P';
    public static final char RESP_PRIVATE = 'M';
    public static final char RESP_SYSTEM = 'S';

    public static boolean isValidLoginName(String login) {
        return login.length() <= MAX_LOGIN_LENGTH;
    }
}
