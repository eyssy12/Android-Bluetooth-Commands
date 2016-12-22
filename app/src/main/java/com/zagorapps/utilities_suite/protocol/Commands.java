package com.zagorapps.utilities_suite.protocol;

/**
 * Created by eyssy on 28/07/2016.
 */
public enum Commands
{
    LEFT_CLICK("LeftClick"),
    DOUBLE_TAP("DoubleTap"),
    RIGHT_CLICK("RightClick"),
    MIDDLE_CLICK("MiddleClick"),
    END_SESSION("EndSession");

    private final String value;

    Commands(String value)
    {
        this.value = value;
    }

    public boolean equalsName(String otherName)
    {
        return otherName != null && value.equals(otherName);
    }

    public String toString()
    {
        return this.value;
    }
}