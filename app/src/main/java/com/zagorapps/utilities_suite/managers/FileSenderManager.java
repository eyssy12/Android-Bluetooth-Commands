package com.zagorapps.utilities_suite.managers;

import android.support.annotation.NonNull;

import com.zagorapps.utilities_suite.interfaces.FileSenderListener;
import com.zagorapps.utilities_suite.models.QueuedFile;
import com.zagorapps.utilities_suite.utils.data.CollectionUtils;
import com.zagorapps.utilities_suite.utils.data.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private final ScheduledExecutorService senderHaltedDetector;
    private ScheduledFuture<?> restarterFuture;

    private final Runnable restarter = new Runnable()
    {
        @Override
        public void run()
        {
            if (fileSendInProgress)
            {
                prepareNextAvailable();
            }
        }
    };

    public FileSenderManager(@NonNull FileSenderListener listener)
    {
        this.fileQueue = new ConcurrentLinkedQueue<>();
        this.listener = listener;
        this.fileSendInProgress = false;
        this.senderHaltedDetector = Executors.newSingleThreadScheduledExecutor();
    }

    public void enqueue(@NonNull File file)
    {
        queueFile(file);
    }

    public void enqueue(@NonNull List<File> files, boolean prioritiseFiles)
    {
        if (!CollectionUtils.isEmpty(files))
        {
            if (prioritiseFiles)
            {
                // TODO: apply prioritisation (i.e. smaller files first || load balancer of 60% smaller and 40% bigger ones)
                for (File file : files)
                {
                    queueFile(file);
                }
            }
            else
            {
                for (File file : files)
                {
                    queueFile(file);
                }
            }
        }
    }

    public void beginQueuedFileSend()
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
            beginQueuedFileSend();
        }
    }

    public void prepareNextAvailable()
    {
        if (fileSendInProgress)
        {
            try
            {
                byte[] fileBytes;
                if (remainingBytes < 4096)
                {
                    fileBytes = new byte[remainingBytes];
                }
                else
                {
                    fileBytes = new byte[4096];
                }

                long totalBytes = fileQueue.peek().getTotalBytes();
                double division = ((double)totalBytes - (double)remainingBytes) / (double)totalBytes;
                int progress = (int)(division * 100);

                remainingBytes -= queuedFileStream.read(fileBytes);

                listener.onFileSending(fileQueue.peek(), fileBytes, remainingBytes, progress);

                // we want to cancel the current runnable because our sender is not halted by this stage
                invalidateRestarter();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void invalidateRestarter()
    {
        if (restarterFuture != null)
        {
            restarterFuture.cancel(true);
        }

        restarterFuture = senderHaltedDetector.schedule(restarter, 1500, TimeUnit.MILLISECONDS);
        // the future restarter ensures that our file sender will be able to continue sending data whenever the data sending gets halted (not sure why it happens, need to investigate further)
    }

    private void queueFile(File file)
    {
        if (StringUtils.isValid(file.getAbsolutePath()))
        {
            QueuedFile queuedFile = new QueuedFile(file);

            fileQueue.add(queuedFile);
            listener.onFileQueued(queuedFile);
        }
    }
}