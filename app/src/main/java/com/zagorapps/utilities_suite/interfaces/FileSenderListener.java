package com.zagorapps.utilities_suite.interfaces;

import com.zagorapps.utilities_suite.models.QueuedFile;

/**
 * Created by eyssy on 05/02/2017.
 */

public interface FileSenderListener
{
    void onFileQueued(QueuedFile queuedFile);
    void onFileAccepted(QueuedFile queuedFile);
    void onFileSending(QueuedFile queuedFile, byte[] fileBytes, int remainingBytes);
    void onFileSendFinished(QueuedFile finishedFile);
}