package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindTrackImageQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.utils.io.LogStreamCopyThread;
import de.codewave.utils.io.StreamCopyThread;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public abstract class AbstractTranscoderStream extends InputStream {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTranscoderStream.class);

    private Process myTargetProcess;
    private Process mySourceProcess;

    public AbstractTranscoderStream(Track track, String targetBinary, String sourceBinary, int outputBitRate, int outputSampleRate)
            throws IOException {
        final String[] targetCommand = new String[getTargetArguments().split(" ").length + 1];
        targetCommand[0] = targetBinary;
        int i = 1;
        for (String part : getTargetArguments().split(" ")) {
            targetCommand[i++] = part;
        }
        replaceTokens(targetCommand, track, outputBitRate, outputSampleRate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing " + getTargetName() + " command \"" + StringUtils.join(targetCommand, " ") + "\".");
        }
        final String[] sourceCommand = new String[getSourceArguments().split(" ").length + 1];
        sourceCommand[0] = sourceBinary;
        i = 1;
        for (String part : getSourceArguments().split(" ")) {
            sourceCommand[i++] = part;
        }
        final File tempFile = File.createTempFile("mytunesrss-", "." + FilenameUtils.getExtension(track.getFile().getName()));
        tempFile.deleteOnExit();
        FileUtils.copyFile(track.getFile(), tempFile);
        track.setFile(tempFile);
        replaceTokens(sourceCommand, track, outputBitRate, outputSampleRate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing " + getSourceName() + " command \"" + StringUtils.join(sourceCommand, " ") + "\".");
        }
        mySourceProcess = Runtime.getRuntime().exec(sourceCommand);
        myTargetProcess = Runtime.getRuntime().exec(targetCommand);
        new StreamCopyThread(mySourceProcess.getInputStream(), false, myTargetProcess.getOutputStream(), true) {
            @Override
            protected void afterExecution(Exception e) {
                tempFile.delete();
                if (e != null) {
                    MyTunesRss.ADMIN_NOTIFY.notifyTranscodingFailure(sourceCommand, targetCommand, e);
                }
            }
        }.start();
        new LogStreamCopyThread(mySourceProcess.getErrorStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug)
                .start();
        new LogStreamCopyThread(myTargetProcess.getErrorStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug)
                .start();
    }

    public int read() throws IOException {
        return myTargetProcess.getInputStream().read();
    }

    @Override
    public void close() throws IOException {
        mySourceProcess.destroy();
        myTargetProcess.destroy();
        super.close();
    }

    protected abstract String getSourceName();

    protected abstract String getTargetName();

    protected abstract String getSourceArguments();

    protected abstract String getTargetArguments();

    public static void replaceTokens(String[] command, Track track, int outputBitRate, int outputSampleRate) {
        for (int i = 0; i < command.length; i++) {
            if ("{bitrate}".equals(command[i])) {
                command[i] = Integer.toString(outputBitRate);
            } else if ("{samplerate}".equals(command[i])) {
                command[i] = Integer.toString(outputSampleRate);
            } else if ("{info.album}".equals(command[i])) {
                command[i] = track.getAlbum();
            } else if ("{info.artist}".equals(command[i])) {
                command[i] = track.getOriginalArtist();
            } else if ("{info.track}".equals(command[i])) {
                command[i] = track.getName();
            } else if ("{info.genre}".equals(command[i])) {
                command[i] = track.getGenre();
            } else if ("{info.comment}".equals(command[i])) {
                command[i] = track.getComment();
            } else if ("{info.pos.number}".equals(command[i])) {
                command[i] = Integer.toString(track.getPosNumber());
            } else if ("{info.pos.size}".equals(command[i])) {
                command[i] = Integer.toString(track.getPosSize());
            } else if ("{info.time}".equals(command[i])) {
                command[i] = Integer.toString(track.getTime());
            } else if ("{info.track.number}".equals(command[i])) {
                command[i] = Integer.toString(track.getTrackNumber());
            } else if ("{info.image.file}".equals(command[i])) {
                replaceImageToken(track, command, i);
            } else if ("{infile}".equals(command[i])) {
                try {
                    command[i] = track.getFile().getCanonicalPath();
                } catch (IOException e) {
                    LOG.warn("Could not get canonical path for track file \"" + track.getFile().getName() + "\", trying absolute path instead.");
                    command[i] = track.getFile().getAbsolutePath();
                }
            }
        }
    }

    private static void replaceImageToken(Track track, String[] command, int i) {
        try {
            File imageFile = File.createTempFile("mytunesrss-temp-image", ".jpg");
            imageFile.deleteOnExit();
            DataStoreSession transaction = MyTunesRss.STORE.getTransaction();
            byte[] data = new byte[0];
            try {
                data = transaction.executeQuery(new FindTrackImageQuery(track.getId(), 128));
                if (data != null && data.length > 0) {
                    Image image = new Image("image/jpeg", data);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(imageFile);
                        fos.write(image.getData());
                    } catch (IOException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not create image file.", e);
                        }

                    } finally {
                        IOUtils.closeQuietly(fos);
                    }
                }
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not query image data.", e);
                }
            } finally {
                transaction.commit();
            }
            try {
                command[i] = imageFile.getCanonicalPath();
            } catch (IOException e) {
                command[i] = imageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create temp file for image.", e);
            }
        }
    }
}