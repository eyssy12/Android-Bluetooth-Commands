package com.zagorapps.utilities_suite.handlers;

import android.os.Handler;
import android.os.Message;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by eyssy on 22/01/2017.
 */

public abstract class HandlerBase extends Handler
{
    private static final HashSet<Integer> USED_IDS = new HashSet<>();
    private static final Random RANDOMISER = new Random();

    private final int id;

    HandlerBase()
    {
        super();

        int temp = RANDOMISER.nextInt();
        USED_IDS.add(temp);
//
//        int temp;
//        do
//        {
//            temp = RANDOMISER.nextInt();
//        } while (!USED_IDS.contains(temp));

        this.id = temp;
    }
    
    public int getId()
    {
        return this.id;
    }

    public abstract void handleMessage(Message msg);
}