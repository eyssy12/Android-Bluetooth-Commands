package com.zagorapps.utilities_suite.protocol;

import java.text.SimpleDateFormat;

/**
 * Created by eyssy on 05/01/2017.
 */

public class Constants
{
    public static final String KEY_IDENTIFIER = "id",
        KEY_ACTION = "action",
        KEY_FILE = "file",
        KEY_NAME = "name",
        KEY_TOTAL = "total",
        KEY_CHECKSUM = "checksum",
        KEY_TYPE = "type",
        KEY_VALUE = "value",
        KEY_REMAINING = "remaining",
        KEY_COMMAND = "cmd",
        KEY_VOICE = "voice",
        KEY_VOLUME = "vol",
        KEY_SCREEN = "screen",
        KEY_VOLUME_ENABLED = "volOn",
        KEY_MOTION = "motion",
        KEY_MOTION_X = "x",
        KEY_MOTION_Y = "y",
        KEY_BATTERY = "battery",
        KEY_BATTERY_STATE = "batState",
        KEY_BATTERY_CHARGE_TYPE = "chargeType",
        KEY_BATTERY_CHARGING = "batCharge",
        KEY_SYNC_STATE = "syncState";

    public static final String VALUE_BEGIN_FILE_TRANSFER = "beginFileTransfer",
        VALUE_FILE_SENDING = "fileSending",
        VALUE_FILE_SENT = "fileSent";

    public static final String VALUE_CHARGE_OK = "ok",
        VALUE_CHARGE_LOW = "low",
        VALUE_CHARGE_TYPE_AC = "ac",
        VALUE_CHARGE_TYPE_USB = "usb";

    public static final String VALUE_SYNC_REQUEST = "Request",
        VALUE_SYNC_RESPONSE = "Response",
        VALUE_SYNC_RESPONSE_ACK = "ResponseAck";

    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
}
