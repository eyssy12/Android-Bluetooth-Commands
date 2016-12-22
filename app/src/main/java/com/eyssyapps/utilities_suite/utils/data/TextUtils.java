package com.eyssyapps.utilities_suite.utils.data;

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

        // because we don't know how many of the bytes are null,
        // we need to add them to a temporary list which would give us a final count for the byte array
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
