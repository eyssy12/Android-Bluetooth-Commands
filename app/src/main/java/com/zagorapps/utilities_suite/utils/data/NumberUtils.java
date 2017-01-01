package com.zagorapps.utilities_suite.utils.data;

/**
 * Created by eyssy on 31/12/2016.
 */

public class NumberUtils
{
    public static boolean isNumeric(String str)
    {
        try
        {
            double value = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }

        return true;
    }
}
