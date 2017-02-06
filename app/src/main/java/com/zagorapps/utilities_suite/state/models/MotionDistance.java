package com.zagorapps.utilities_suite.state.models;

import com.zagorapps.utilities_suite.Enumerations.Coordinate;

import java.math.BigDecimal;

/**
 * Created by eyssy on 26/08/2016.
 */
public class MotionDistance
{
    private float distanceX, distanceY;
    private boolean modified = false;

    public MotionDistance(float distanceX, float distanceY)
    {
        this.distanceX = distanceX;
        this.distanceY = distanceY;
    }

    public boolean isModified()
    {
        return this.modified;
    }

    public float getDistanceX()
    {
        return distanceX;
    }

    public void setDistanceX(float distanceX)
    {
        this.distanceX = distanceX;

        this.modified = true;
    }

    public float getDistanceY()
    {
        return this.distanceY;
    }

    public void setDistanceY(float distanceY)
    {
        this.distanceY = distanceY;

        this.modified = true;
    }

    private void roundX()
    {
        // Windows only allows int's as inputs hence we need to make this a whole value

        if (this.isDistanceXPositiveChange() && (this.distanceX >= 0.5 && this.distanceX <= 1.0))
        {
            this.setDistanceX(1.0f);
        }
        else if (this.distanceX <= -0.5 && this.distanceX >= -1.0)
        {
            this.setDistanceX(-1.0f);
        }
    }

    private void roundY()
    {
        // Windows only allows int's as inputs hence we need to make this a whole value

        if (this.isDistanceYPositiveChange() && (this.distanceY >= 0.5 && this.distanceY <= 1.0))
        {
            this.setDistanceY(1.0f);
        }
        else if (this.distanceY <= -0.5 && this.distanceY >= -1.0)
        {
            this.setDistanceY(-1.0f);
        }
    }

    public boolean isDistanceXPositiveChange()
    {
        return this.distanceX >= 0;
    }

    public boolean isDistanceYPositiveChange()
    {
        return this.distanceY >= 0;
    }

    public boolean shouldSend()
    {
        this.roundX();
        this.roundY();

        return this.distanceX >= 1.0 || this.distanceX <= -1.0 || this.distanceY >= 1.0 || this.distanceY <= -1.0;
    }

    public static float increaseMouseMovement(float movingUnits, float sensitivity, Coordinate movingUnitsType, int decimalPlace)
    {
        int min, max;
        if (movingUnitsType == Coordinate.X)
        {
            max = 1920;
            min = 0;

            if (movingUnits < 0)
            {
                max = 0;
                min = -1920;
            }
        }
        else
        {
            max = 1080;
            min = 0;

            if (movingUnits < 0)
            {
                max = 0;
                min = -1080;
            }
        }

        float result = movingUnits * sensitivity;

        result = Math.max(min, result);
        result = Math.min(max, result);

        return decimalPlace > 0
            ? new BigDecimal(Float.toString(result)).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).floatValue()
            : result;
    }
}
