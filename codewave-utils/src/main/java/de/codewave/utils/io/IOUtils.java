/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.io;

import de.codewave.utils.*;
import org.apache.commons.codec.binary.*;
import org.apache.commons.lang3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

/**
 * IO Utilities.
 */
public class IOUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    /**
     * Read and return the lines of the reader in a list of strings.
     *
     * @param reader            A reader.
     * @param includeLineBreaks Return the line breaks in the strings if set to <code>true</code> or do not return line breaks in the strings if set
     *                          to <code>false</code>.
     *
     * @return A list of strings.
     *
     * @throws IOException
     */
    private static List<String> readTextLines(Reader reader, boolean includeLineBreaks) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        List<String> text = new ArrayList<String>();
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            if (includeLineBreaks) {
                line += System.getProperty("line.separator");
            }
            text.add(line);
        }
        return text;
    }

    /**
     * Read and return the lines of a URL in a list of strings.
     *
     * @param url               A URL.
     * @param includeLineBreaks Return the line breaks in the strings if set to <code>true</code> or do not return line breaks in the strings if set
     *                          to <code>false</code>.
     *
     * @return A list of strings.
     *
     * @throws IOException
     */
    public static List<String> readTextLines(URL url, boolean includeLineBreaks) throws IOException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(url.openStream());
            return readTextLines(reader, includeLineBreaks);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Read and return the lines of a URL in a list of strings.
     *
     * @param connection        A URL connection.
     * @param includeLineBreaks Return the line breaks in the strings if set to <code>true</code> or do not return line breaks in the strings if set
     *                          to <code>false</code>.
     *
     * @return A list of strings.
     *
     * @throws IOException
     */
    public static List<String> readTextLines(URLConnection connection, boolean includeLineBreaks) throws IOException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(connection.getInputStream());
            return readTextLines(reader, includeLineBreaks);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Get the identifier of a file. The identifier of a file is a unique base 64 encoded string. It is the base 64 encoded canonical path of the
     * file.
     *
     * @param file A file.
     *
     * @return The identifier of the file.
     *
     * @throws IOException
     */
    public static String getFileIdentifier(File file) throws IOException {
        if (file != null && file.exists()) {
            return new String(Base64.encodeBase64(file.getCanonicalPath().getBytes("UTF-8")), "UTF-8");
        }
        return null;
    }

    /**
     * Get the identifier of a file relative to a base directory. Get the identifier of a file. The identifier of a file is a unique base 64 encoded
     * string. It is the base 64 encoded canonical path of the file without the canonical path of the base directory.
     *
     * @param baseDir The base directory.
     * @param file    A file.
     *
     * @return The identifier of the file.
     *
     * @throws IOException
     */
    public static String getFileIdentifier(File baseDir, File file) throws IOException {
        if (baseDir != null && file != null && isContained(baseDir, file)) {
            return new String(Base64.encodeBase64(file.getCanonicalPath().substring(baseDir.getCanonicalPath().length()).getBytes("UTF-8")), "UTF-8");
        }
        return null;
    }

    /**
     * Get a hash value for the canonical name of a file.
     *
     * @param file A file.
     *
     * @return The identifier of the file.
     *
     * @throws IOException
     */
    public static String getFilenameHash(File file) throws IOException {
        if (file != null && file.exists()) {
            try {
                return new String(Hex.encodeHex(MessageDigest.getInstance("SHA-1").digest(file.getCanonicalPath().getBytes("UTF-8"))));
            } catch (NoSuchAlgorithmException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not create message digest.", e);
                }
            }
        }
        return null;
    }

    /**
     * Get a hash value for the canonical name of a file. The path relative to the base directory is used.
     *
     * @param baseDir The base directory.
     * @param file    A file.
     *
     * @return The identifier of the file.
     *
     * @throws IOException
     */
    public static String getFilenameHash(File baseDir, File file) throws IOException {
        if (baseDir != null && file != null && isContained(baseDir, file)) {
            try {
                return new String(MessageDigest.getInstance("SHA-1").digest(file.getCanonicalPath().substring(baseDir
                        .getCanonicalPath().length()).getBytes("UTF-8")));
            } catch (NoSuchAlgorithmException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not create message digest.", e);
                }
            }
        }
        return null;
    }

    /**
     * Check if a directory contains a file. Both the directory nor the file must exist. The method checks if the canonical file path starts with the
     * canonical directory path. The file need not be a direct child of the directory, it just needs to be somewhere below.
     *
     * @param dir  A directory.
     * @param file A file.
     *
     * @return <code>true</code> if the file is somewhere below the directory or <code>false</code> otherwise.
     *
     * @throws IOException
     */
    public static boolean isContained(File dir, File file) throws IOException {
        File cDir = dir.getCanonicalFile();
        File cFile = file.getCanonicalFile();
        if (cDir != null && cFile != null && cDir.exists() && cFile.exists() && cDir.isDirectory()) {
            while (cFile != null) {
                cFile = cFile.getParentFile();
                if (cDir.equals(cFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a directory contains a file. Both the directory nor the file must exist. The method checks if the canonical file path starts with the
     * canonical directory path. The file need not be a direct child of the directory, it just needs to be somewhere below.
     *
     * @param dir  A directory.
     * @param file A file.
     *
     * @return <code>true</code> if the file is somewhere below the directory or is the direcrory itself or <code>false</code> otherwise.
     *
     * @throws IOException
     */
    public static boolean isContainedOrSame(File dir, File file) throws IOException {
        File cDir = dir.getCanonicalFile();
        File cFile = file.getCanonicalFile();
        if (cDir.equals(cFile)) {
            return true;
        }
        return isContained(dir, file);
    }

    /**
     * Recursively process all files matching the specified filter.
     *
     * @param baseDir   The base directory (which is not processed itself).
     * @param processor The processor.
     * @param filter    The filter.
     */
    public static void processFiles(File baseDir, FileProcessor processor, FileFilter filter) {
        File[] files = baseDir.listFiles(filter);
        if (files != null) {
            for (File file : files) {
                processor.process(file);
                try {
                    if (file.isDirectory() && file.exists() && !isContained(file, baseDir)) {
                        processFiles(file, processor, filter);
                    }
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not check if base dir is contained in file.", e);
                    }
                }
            }
        }
    }

    /**
     * Get the ancestor of a file.
     *
     * @param file  A file.
     * @param level The steps back to the ancestor, i.e. 0 returns the file itselt, 1 returns the parent, 2 the grandparent, etc.
     *
     * @return The ancestor file oir <code>null</code> if no such ancestor exists, i.e. the level was too large.
     */
    public static File getAncestor(File file, int level) {
        for (int i = 0; i < level && file != null; i++) {
            file = file.getParentFile();
        }
        return file;
    }

    /**
     * Find files with a specific name in any subfolder of a directory.
     *
     * @param baseDir  A base directory.
     * @param fileName A file name.
     *
     * @return A collection of all files somewhere below the base directory with the specified name.
     */
    public static Collection<File> find(File baseDir, final String fileName) {
        return find(baseDir, fileName, new Trigger());
    }

    /**
     * Find files with a specific name in any subfolder of a directory.
     *
     * @param baseDir       A base directory.
     * @param fileName      A file name.
     * @param cancelTrigger A trigger for cancelling the search.
     *
     * @return A collection of all files somewhere below the base directory with the specified name.
     */
    public static Collection<File> find(File baseDir, final String fileName, Trigger cancelTrigger) {
        Collection<File> found = new HashSet<File>();
        if (baseDir != null && StringUtils.isNotEmpty(fileName) && !cancelTrigger.isTriggered()) {
            File[] subFiles = baseDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file != null && (file.isDirectory() || (file.getName() != null && file.getName().equals(fileName)));
                }
            });
            if (subFiles != null && !cancelTrigger.isTriggered()) {
                for (File file : subFiles) {
                    if (cancelTrigger.isTriggered()) {
                        break;
                    }
                    try {
                        if (file.isDirectory() && file.exists() && !isContained(file, baseDir)) {
                            found.addAll(find(file, fileName, cancelTrigger));
                        } else {
                            found.add(file);
                        }
                    } catch (IOException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not check if base dir is contained in file.", e);
                        }
                    }
                }
            }
        }
        return found;
    }

    /**
     * Close an input stream ignoring any IOExceptions (IOException will be logged).
     * @param stream An input stream.
     */
    public static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch(IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not close input stream.", e);
                }
            }
        }
    }

    /**
     * Close an output stream ignoring any IOExceptions (IOException will be logged).
     * @param stream An output stream.
     */
    public static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch(IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not close output stream.", e);
                }
            }
        }
    }
    /**
     * Close a reader ignoring any IOExceptions (IOException will be logged).
     * @param reader A reader.
     */
    public static void close(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch(IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not close reader.", e);
                }
            }
        }
    }

    /**
     * Close a writer ignoring any IOExceptions (IOException will be logged).
     * @param writer A writer.
     */
    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch(IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not close writer.", e);
                }
            }
        }
    }
}
