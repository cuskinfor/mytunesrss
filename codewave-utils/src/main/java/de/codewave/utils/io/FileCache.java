package de.codewave.utils.io;

import de.codewave.utils.cache.ExpiringCache;
import de.codewave.utils.cache.ExpiringCacheItem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Class for caching file references. Adding a file will add a reference to the file. When the timeout is reached, the file will be deleted from the
 * cache and the file system. The timeout is reset whenever a file from the cache is touched or retrieved.
 */
public class FileCache extends ExpiringCache<FileCache.FileInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCache.class);

    public FileCache(String name, int cleanupInterval, int maxItems) {
        super(name, cleanupInterval, maxItems);
    }

    public synchronized void add(String identifier, File file, long timeout) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding file with identifier \"" + identifier + "\" and timeout \"" + timeout + "\" to cache.");
        }
        super.add(new FileInfo(identifier, file, timeout));
    }

    public static class FileInfo extends ExpiringCacheItem {
        String myFilePath;

        private FileInfo(String identifier, File file, long timeout) {
            super(identifier, timeout);
            myFilePath = file.getAbsolutePath();
        }

        protected void onItemExpired() {
            FileUtils.deleteQuietly(getFile());
        }

        public File getFile() {
            return new File(myFilePath);
        }
    }
}