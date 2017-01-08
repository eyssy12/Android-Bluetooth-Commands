package com.zagorapps.utilities_suite.protocol;

/**
 * Created by eyssy on 05/01/2017.
 */

public enum ServerCommands
{
    CLOSE("Close");

    private final String value;

    ServerCommands(String value)
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