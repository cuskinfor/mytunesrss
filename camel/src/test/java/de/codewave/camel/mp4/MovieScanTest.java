package de.codewave.camel.mp4;

import de.codewave.utils.io.FileProcessor;
import de.codewave.utils.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

public class MovieScanTest {

    private Mp4Parser parser = new Mp4Parser();

    @Test
    @Ignore
    public void scanFiles() {
        IOUtils.processFiles(new File("/Network/Servers/readynas.local/iTunes/iTunes Media/Movies"), new FileProcessor() {
                    public void process(File file) {
                        String fileName = file.getName().toLowerCase();
                        if (file.isFile() && (fileName.endsWith(".mp4") || fileName.endsWith(".m4v") || fileName.endsWith(".mov"))) {
                            System.out.println(file.getName());
                            try {
                                YearAtom atom = (YearAtom) parser.parseAndGet(file, "moov.udta.meta.ilst.\u00a9day");
                                if (atom != null) {
                                    System.out.println(atom.getText() + " --> " + atom.getYear());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new FileFilter() {
            public boolean accept(File file) {
                return true;
            }
        }
        );
    }

    private void displayAtoms(Collection<Mp4Atom> atoms, int inset) throws IOException {
        for (Mp4Atom atom : atoms) {
            for (int i = 0; i < inset; i++) {
                System.out.print(" ");
            }
            System.out.println(atom);
            displayAtoms(atom.getChildren(), inset + 4);
        }
    }
}
