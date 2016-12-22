package com.eyssyapps.utilities_suite.protocol;

/**
 * Created by eyssy on 31/07/2016.
 */
public enum ServerCommands
{
    BACKSPACE("Backspace"),
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
