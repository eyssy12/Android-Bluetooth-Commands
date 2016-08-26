package com.eyssyapps.bluetoothcommandsender.utils.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by eyssy on 16/07/2016.
 */
public class TextUtils
{
    public static final char BYTE_NULL_INDICATOR = 0;

    public static final String CHARSET_UTF_8 = "UTF-8";

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

    public static byte[] getBytesForCharset(String data, String charset)
    {
        try
        {
            return data.getBytes(charset);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return new byte[]{};
    }

    public static String decodeBytesFromCharset(byte[] data, String charset)
    {
        if (data == null || data.length < 1)
        {
            return "";
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        CharBuffer charBuffer = Charset.forName(charset).decode(buffer);



        return charBuffer.toString();
    }

    public static byte[] trimBytes(byte[] source)
    {
        ArrayList<Byte> trimmed = new ArrayList<>();

        for (int i = 0; i < source.length; i++)
        {
            if (source[i] != BYTE_NULL_INDICATOR)
            {
                trimmed.add(source[i]);
            }
        }

        byte[] result = new byte[trimmed.size()];
        for (int i = 0; i < trimmed.size(); i++)
        {
            result[i] = trimmed.get(i);
        }

        return result;
    }

    public static byte[] getBytesForUtf8(String data, int count)
    {
        if (data.length() > count)
        {
            count = data.length();
        }

        byte[] bytes = new byte[count];

        try
        {
            byte[] converted = data.getBytes("UTF-8");

            for (int i = 0; i < converted.length; i++)
            {
                bytes[i] = converted[i];
            }

            return bytes;
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return new byte[]{};
    }
}
