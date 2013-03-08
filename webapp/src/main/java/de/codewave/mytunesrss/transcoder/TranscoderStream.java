package de.codewave.mytunesrss.transcoder;

import de.codewave.camel.mp4.LimitedInputStream;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.utils.io.FileCache;
import de.codewave.utils.io.LogStreamCopyThread;
import de.codewave.utils.io.StreamCopyThread;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * de.codewave.mytunesrss.transcoder.TranscoderStream
 */
public class TranscoderStream extends InputStream {
    private static final Logger LOG = LoggerFactory.getLogger(TranscoderStream.class);

    private Process myProcess;
    private TranscoderConfig myTranscoderConfig;
    private InputStream myInputStream;
    private File myCacheFile;
    private OutputStream myCacheOutputStream;

    TranscoderStream(TranscoderConfig transcoderConfig, File inputFile, File cacheFile) throws IOException {
        LOG.debug("Creating transcoder stream for transcoder \"" + transcoderConfig.getName() + "\", file \"" + inputFile.getAbsolutePath() + "\", and cache file \"" + cacheFile.getAbsolutePath() + "\".");
        myCacheFile = cacheFile;
        if (cacheFile != null && cacheFile.isFile()) {
            LOG.debug("Found transcoded file \"" + cacheFile.getAbsolutePath() + "\" in cache.");
            cacheFile.setLastModified(System.currentTimeMillis()); // touch file to prevent expiration
            myInputStream = new BufferedInputStream(new FileInputStream(cacheFile));
        } else {
            LOG.debug("No transcoded file in cache found.");
            if (cacheFile != null) {
                LOG.debug("Creating output stream for cache file \"" + cacheFile.getAbsolutePath() + "\".");
                myCacheOutputStream = new BufferedOutputStream(new FileOutputStream(cacheFile));
            }
            myTranscoderConfig = transcoderConfig;
            List<String> transcodeCommand = MyTunesRssUtils.getDefaultVlcCommand(inputFile);
            transcodeCommand.add("--no-sout-smem-time-sync");
            transcodeCommand.add("--sout=#transcode{" + transcoderConfig.getOptions() + "}:std{access=file,mux=" + StringUtils.defaultIfBlank(transcoderConfig.getTargetMux(), "dummy") + ",dst=-}");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing " + getName() + " command \"" + StringUtils.join(transcodeCommand, " ") + "\".");
            }
            myProcess = new ProcessBuilder(transcodeCommand).start();
            MyTunesRss.SPAWNED_PROCESSES.add(myProcess);
            myInputStream = myProcess.getInputStream();
            //StreamCopyThread stderrCopyThread = new StreamCopyThread(myProcess.getErrorStream(), false, new NullOutputStream(), true);
            LogStreamCopyThread stderrCopyThread = new LogStreamCopyThread(myProcess.getErrorStream(), false, LoggerFactory.getLogger("VLC"), LogStreamCopyThread.LogLevel.Error);
            stderrCopyThread.setDaemon(true);
            stderrCopyThread.start();
        }
    }

    public int read() throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        int i = myInputStream.read();
        if (i != -1 && myCacheOutputStream != null) {
            myCacheOutputStream.write(i);
        }
        return i;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        int i = myInputStream.read(bytes);
        if (i > 0 && myCacheOutputStream != null) {
            myCacheOutputStream.write(bytes, 0, i);
        }
        return i;
    }

    @Override
    public int read(byte[] bytes, int start, int length) throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        int i = myInputStream.read(bytes, start, length);
        if (i > 0 && myCacheOutputStream != null) {
            myCacheOutputStream.write(bytes, 0, i);
        }
        return i;
    }

    @Override
    public long skip(long l) throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        if (myCacheOutputStream != null) {
            LimitedInputStream limitedInputStream = new LimitedInputStream(myInputStream, l);
            try {
                return IOUtils.copyLarge(limitedInputStream, myCacheOutputStream);
            } finally {
                limitedInputStream.close();
            }
        } else {
            return myInputStream.skip(l);
        }
    }

    @Override
    public int available() throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        return myInputStream.available();
    }

    @Override
    public void mark(int i) {
        throw new UnsupportedOperationException("Mark not supported!");
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException("Reset not supported!");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing transcoder stream.");
        final InputStream inputStream = myInputStream;
        myInputStream = null;
        if (myProcess != null) {
            LOG.debug("Transcoder process has been used.");
            if (myCacheOutputStream != null) {
                LOG.debug("Cache file has been written.");
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            LOG.debug("Copying remaining transcoder stream contents into cache file.");
                            IOUtils.copyLarge(inputStream, myCacheOutputStream);
                        } catch (IOException e) {
                            LOG.warn("Could not finish cached transcoded file.", e);
                            myCacheFile.delete();
                        } finally {
                            try {
                                LOG.debug("Closing cache output stream.");
                                myCacheOutputStream.close();
                            } catch (IOException e) {
                                LOG.warn("Could not close cache output stream.", e);
                            }
                            try {
                                LOG.debug("Closing input stream.");
                                inputStream.close();
                            } catch (IOException e) {
                                LOG.warn("Could not close input stream.", e);
                            }
                            LOG.debug("Destroying process.");
                            myProcess.destroy();
                            MyTunesRss.SPAWNED_PROCESSES.remove(myProcess);
                        }
                    }
                }).start();
            } else {
                LOG.debug("Destroying process.");
                myProcess.destroy();
                MyTunesRss.SPAWNED_PROCESSES.remove(myProcess);
            }
        } else {
            LOG.debug("Closing input stream.");
            inputStream.close();
        }
    }

    protected String getName() {
        return myTranscoderConfig.getName();
    }
}