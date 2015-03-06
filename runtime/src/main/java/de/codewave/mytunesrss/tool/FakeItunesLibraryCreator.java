package de.codewave.mytunesrss.tool;

import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.PListHandlerListener;
import de.codewave.utils.xml.XmlUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class FakeItunesLibraryCreator {

    private static Set<String> missingTemplates = new HashSet<>();

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        final File templateDir = new File(args[0]);
        File iTunesLibraryXmlFile = new File(args[1]);
        final String commonPrefix = args[2];
        URL iTunesLibraryXml = iTunesLibraryXmlFile.toURI().toURL();
        PListHandler handler = new PListHandler();
        handler.addListener("/plist/dict[Tracks]/dict", new PListHandlerListener() {
            @Override
            public boolean beforeDictPut(Map dict, String key, Object value) {
                try {
                    process((Map)value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            private void process(Map track) throws IOException {
                String trackType = (String) track.get("Track Type");
                if (trackType == null || "File".equals(trackType)) {
                    String filename = ItunesLoader.getFileNameForLocation((String) track.get("Location"));
                    if (filename.startsWith(commonPrefix)) {
                        File fakeFile = new File(filename.substring(commonPrefix.length()));
                        File templateFile = findTemplate(templateDir, filename);
                        if (templateFile != null && templateFile.isFile() && templateFile.canRead()) {
                            fakeFile.getParentFile().mkdirs();
                            System.out.println("linking \"" + templateFile.getAbsolutePath() + "\" to \"" + fakeFile.getAbsolutePath() + "\".");
                            Files.createSymbolicLink(fakeFile.toPath(), templateFile.toPath());
                        }
                    }
                }
            }

            @Override
            public boolean beforeArrayAdd(List list, Object o) {
                return false;
            }
        });
        XmlUtils.parseApplePList(iTunesLibraryXml, handler);
        System.out.println("Templates for the following extensions were missing: " + Arrays.toString(missingTemplates.toArray(new String[missingTemplates.size()])));
    }

    private static final Map<String, File> templateCache = new HashMap<>();

    private static File findTemplate(File templateDir, final String filename) {
        final String extension = StringUtils.lowerCase(FilenameUtils.getExtension(filename));
        if (templateCache.containsKey(extension)) {
            return templateCache.get(extension);
        }
        File[] templates = templateDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FilenameUtils.getExtension(name).equalsIgnoreCase(extension);
            }
        });
        if (templates.length > 0) {
            templateCache.put(extension, templates[0]);
            return templates[0];
        }  else {
            templateCache.put(extension, null);
            missingTemplates.add(StringUtils.lowerCase(FilenameUtils.getExtension(filename)));
            return null;
        }
    }

}
