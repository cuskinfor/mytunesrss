package de.codewave.mytunesrss.transcoder;

import de.codewave.camel.mp4.LimitedInputStream;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.utils.io.LogStreamCopyThread;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * de.codewave.mytunesrss.transcoder.TranscoderStream
 */
public class TranscoderStream extends InputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranscoderStream.class);

    private Process myProcess;
    private TranscoderConfig myTranscoderConfig;
    private InputStream myInputStream;
    private File myCacheFile;
    private File myTempCacheFile;
    private OutputStream myCacheOutputStream;
    private AtomicBoolean closed = new AtomicBoolean();
    private File myInputFile;

    TranscoderStream(TranscoderConfig transcoderConfig, File inputFile, File cacheFile) throws IOException {
        myTranscoderConfig = transcoderConfig;
        myInputFile = inputFile;
        myCacheFile = cacheFile;
    }

    private void init() throws IOException {
        LOGGER.debug("Creating transcoder stream for transcoder \"" + myTranscoderConfig.getName() + "\", file \"" + myInputFile.getAbsolutePath() + "\", and cache file \"" + (myCacheFile != null ? myCacheFile.getAbsolutePath() : "---none---") + "\".");
        if (myCacheFile != null && myCacheFile.isFile()) {
            LOGGER.debug("Found transcoded file \"" + myCacheFile.getAbsolutePath() + "\" in cache.");
            if (!myCacheFile.setLastModified(System.currentTimeMillis())) { // touch file to prevent expiration
                LOGGER.debug("Could not touch cache file \"" + myCacheFile.getAbsolutePath() + "\".");
            }
            myInputStream = new BufferedInputStream(new FileInputStream(myCacheFile));
        } else {
            LOGGER.debug("No transcoded file in cache found.");
            if (myCacheFile != null) {
                myTempCacheFile = MyTunesRss.TEMP_CACHE.createTempFile();
                LOGGER.debug("Creating output stream for temporary cache file \"" + myTempCacheFile.getAbsolutePath() + "\".");
                myCacheOutputStream = new BufferedOutputStream(new FileOutputStream(myTempCacheFile));
            }
            List<String> transcodeCommand = MyTunesRssUtils.getDefaultVlcCommand(myInputFile);
            transcodeCommand.add("--no-sout-smem-time-sync");
            transcodeCommand.add("--sout=#transcode{" + myTranscoderConfig.getOptions() + "}:std{access=file,mux=" + StringUtils.defaultIfBlank(myTranscoderConfig.getTargetMux(), "dummy") + ",dst=-}");
            String msg = "Executing " + getName() + " command \"" + StringUtils.join(transcodeCommand, " ") + "\".";
            LOGGER.debug(msg);
            myProcess = new ProcessBuilder(transcodeCommand).start();
            MyTunesRss.SPAWNED_PROCESSES.add(myProcess);
            myInputStream = myProcess.getInputStream();
            LogStreamCopyThread stderrCopyThread = new LogStreamCopyThread(myProcess.getErrorStream(), false, LoggerFactory.getLogger("VLC"), LogStreamCopyThread.LogLevel.Error, msg, null);
            stderrCopyThread.setDaemon(true);
            stderrCopyThread.start();
        }
    }

    public int read() throws IOException {
        if (myInputStream == null) {
            init();
            if (myInputStream == null) {
                throw new IllegalStateException("No input stream available.");
            }
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
            init();
            if (myInputStream == null) {
                throw new IllegalStateException("No input stream available.");
            }
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
            init();
            if (myInputStream == null) {
                throw new IllegalStateException("No input stream available.");
            }
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
            init();
            if (myInputStream == null) {
                throw new IllegalStateException("No input stream available.");
            }
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
            init();
            if (myInputStream == null) {
                throw new IllegalStateException("No input stream available.");
            }
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
        if (closed.compareAndSet(false, true)) {
            LOGGER.debug("Closing transcoder stream.");
            final InputStream inputStream = myInputStream;
            myInputStream = null;
            if (myProcess != null) {
                LOGGER.debug("Transcoder process has been used.");
                if (myCacheOutputStream != null) {
                    LOGGER.debug("Cache file has been written.");
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                LOGGER.debug("Copying remaining transcoder stream contents into cache file.");
                                IOUtils.copyLarge(inputStream, myCacheOutputStream);
                            } catch (IOException e) {
                                LOGGER.warn("Could not finish cached transcoded file.", e);
                                myCacheFile.delete();
                            } finally {
                                try {
                                    LOGGER.debug("Closing cache output stream.");
                                    myCacheOutputStream.close();
                                    try {
                                        myProcess.waitFor();
                                        LOGGER.debug("VLC process exited with code " + myProcess.exitValue() + ".");
                                        if (myProcess.exitValue() == 0 && (!myCacheFile.exists() || myCacheFile.delete())) {
                                            if (!myTempCacheFile.renameTo(myCacheFile)) {
                                                LOGGER.warn("Could not rename temp file \"" + myTempCacheFile.getAbsolutePath() + "\" to transcoder cache file \"" + myCacheFile.getAbsolutePath() + "\".");
                                            }
                                        }
                                    } catch (InterruptedException e) {
                                        LOGGER.warn("Interrupted while waiting for transcoder process to finish.", e);
                                    }
                                } catch (IOException e) {
                                    LOGGER.warn("Could not close cache output stream.", e);
                                }
                                try {
                                    LOGGER.debug("Closing input stream.");
                                    inputStream.close();
                                } catch (IOException e) {
                                    LOGGER.warn("Could not close input stream.", e);
                                }
                                LOGGER.debug("Destroying process.");
                                myProcess.destroy();
                                MyTunesRss.SPAWNED_PROCESSES.remove(myProcess);
                                if (myCacheFile.isFile() && myCacheFile.length() == 0) {
                                    // delete empty files from cache (most likely VLC could not start at all)
                                    if (!myCacheFile.delete()) {
                                        LOGGER.debug("Could not delete empty cached file \"" + myCacheFile.getAbsolutePath() + "\".");
                                    }
                                }
                            }
                        }
                    }).start();
                } else {
                    LOGGER.debug("Destroying process.");
                    myProcess.destroy();
                    MyTunesRss.SPAWNED_PROCESSES.remove(myProcess);
                }
            } else if (inputStream != null) {
                LOGGER.debug("Closing input stream.");
                inputStream.close();
            }
        }
    }

    protected String getName() {
        return myTranscoderConfig.getName();
    }
}
