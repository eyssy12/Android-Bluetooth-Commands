package com.zagorapps.utilities_suite.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by eyssy on 05/01/2017.
 */

public class MessageBuilder
{
    private static MessageBuilder BUILDER;

    private static Gson GSON;

    public static MessageBuilder DefaultInstance()
    {
        if (BUILDER == null)
        {
            BUILDER = new MessageBuilder();

            return BUILDER;
        }

        return BUILDER;
    }

    private MessageBuilder()
    {
        GSON = new GsonBuilder().create();
    }

    public JsonObject getBaseObject()
    {
        return new JsonObject();
    }

    public String toJson(JsonElement element)
    {
        return GSON.toJson(element);
    }
}
