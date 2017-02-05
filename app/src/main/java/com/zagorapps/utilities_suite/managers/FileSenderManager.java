package com.zagorapps.utilities_suite.managers;

import android.support.annotation.NonNull;

import com.zagorapps.utilities_suite.interfaces.FileSenderListener;
import com.zagorapps.utilities_suite.models.QueuedFile;
import com.zagorapps.utilities_suite.utils.data.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by eyssy on 05/02/2017.
 */

public class FileSenderManager
{
    private ConcurrentLinkedQueue<QueuedFile> fileQueue;

    private FileSenderListener listener;

    private int remainingBytes;
    private FileInputStream queuedFileStream;

    private boolean fileSendInProgress;

    public FileSenderManager(@NonNull FileSenderListener listener)
    {
        this.fileQueue = new ConcurrentLinkedQueue<>();
        this.listener = listener;
        this.fileSendInProgress = false;
    }

    public void enqueue(@NonNull File file)
    {
        if (StringUtils.isValid(file.getAbsolutePath()))
        {
            QueuedFile queuedFile = new QueuedFile(file);

            fileQueue.add(queuedFile);
            listener.onFileQueued(queuedFile);
        }
    }

    public void beginSendQueuedFile()
    {
        if (!fileSendInProgress)
        {
            try
            {
                queuedFileStream = new FileInputStream(fileQueue.peek().getFile());
                remainingBytes = queuedFileStream.available();

                fileSendInProgress = true;

                listener.onFileAccepted(fileQueue.peek());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void finalizeQueuedFile()
    {
        listener.onFileSendFinished(fileQueue.poll());

        fileSendInProgress = false;

        if (fileQueue.size() > 0)
        {
            beginSendQueuedFile();
        }
    }

    public void sendNextAvailable()
    {
        try
        {
            byte[] fileBytes;
            if (remainingBytes < 2048)
            {
                fileBytes = new byte[remainingBytes];
            }
            else
            {
                fileBytes = new byte[2048];
            }

            for (int i = 0; i < fileBytes.length; i++)
            {
                fileBytes[i] = (byte) queuedFileStream.read();
            }

            remainingBytes -= fileBytes.length;

            listener.onFileSending(fileQueue.peek(), fileBytes, remainingBytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}