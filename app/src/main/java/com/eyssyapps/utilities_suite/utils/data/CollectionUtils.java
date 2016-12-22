package com.eyssyapps.utilities_suite.utils.data;

/**
 * Created by eyssy on 01/09/2016.
 */
public class CollectionUtils
{
    public static byte[] concat(byte[] first, byte[]... subsequent)
    {
        int subsequentTotalLength = 0;

        for (int i = 0; i < subsequent.length; i++)
        {
            subsequentTotalLength += subsequent[i].length;
        }

        byte[] newCopy = new byte[first.length + subsequentTotalLength];

        for (int i = 0; i < first.length; i++)
        {
            newCopy[i] = first[i];
        }

        int subsequentPointer = first.length;

        for (int i = 0; i < subsequent.length; i++)
        {
            byte[] toConcat = subsequent[i];

            for (int j = 0; j < toConcat.length; j++)
            {
                newCopy[subsequentPointer] = toConcat[j];

                subsequentPointer++;
            }
        }

        return newCopy;
    }

    public static byte[] padBytes(byte[] source, int length)
    {
        if (source.length >= length)
        {
            return source;
        }

        byte[] out = new byte[source.length + (length - source.length)];

        for (int i = 0; i < source.length; i++)
        {
            out[i] = source[i];
        }

        return out;
    }
}
