package com.zagorapps.utilities_suite.models;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.io.File;

/**
 * Created by eyssy on 05/02/2017.
 */

public class QueuedFile
{
    private final File file;
    private final String fileName;
    private final DateTime time;
    private final long checksum;

    public QueuedFile(@NonNull File file)
    {
        String[] split = file.getAbsolutePath().split("/");
        String fileNameWithExtension = split[split.length - 1];

        this.file = file;
        this.fileName = fileNameWithExtension;
        this.time = DateTime.now();
        this.checksum = file.hashCode() * time.getMillis();
    }

    public File getFile()
    {
        return file;
    }

    public String getFileName()
    {
        return fileName;
    }

    public DateTime getTime()
    {
        return time;
    }

    public long getChecksum()
    {
        return checksum;
    }

    public long getTotalBytes()
    {
        return file.length();
    }
}
