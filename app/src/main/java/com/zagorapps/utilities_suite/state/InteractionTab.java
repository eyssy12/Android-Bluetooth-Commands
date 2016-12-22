package com.zagorapps.utilities_suite.state;

/**
 * Created by eyssy on 26/08/2016.
 */
public enum InteractionTab
{
    NOT_SET(-1),
    MOUSE(0),
    KEYBOARD(1),
    SYSTEM(2);

    private final int order;

    InteractionTab(int order)
    {
        this.order = order;
    }

    public static InteractionTab getEnumFromOrder(int order)
    {
        switch (order)
        {
            case 0:
                return MOUSE;
            case 1:
                return KEYBOARD;
            case 2:
                return SYSTEM;
            default:
                return NOT_SET;
        }
    }

    public static InteractionTab getDefaultTab()
    {
        return MOUSE;
    }

    public int getOrder()
    {
        return this.order;
    }
}