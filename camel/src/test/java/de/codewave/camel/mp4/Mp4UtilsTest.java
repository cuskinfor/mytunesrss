package de.codewave.camel.mp4;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Mp4UtilsTest {

    @Test
    public void testIsMp4Stream() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/AnDich.m4a");
        Mp4Utils.CheckStreamResult checkStreamResult = Mp4Utils.isMp4Stream(inputStream);
        assertTrue(checkStreamResult.isMp4Stream());
        Mp4AtomList mp4AtomList = new Mp4Parser(0, true).parse(checkStreamResult.getStream());
        assertEquals("ftyp", mp4AtomList.toList().get(0).getId());
    }

    @Test
    @Ignore
    public void removeDataAtom() throws IOException {
        for (File sFile : new File("/Users/mdescher/Desktop/audio").listFiles()) {
            File tFile = new File("src/test/resources/" + sFile.getName());
            Mp4AtomList atoms = new Mp4Parser().parse(sFile);
            Mp4Atom ftyp = atoms.getFirst("ftyp");
            Mp4Atom moov = atoms.getFirst("moov");
            FileOutputStream fos = new FileOutputStream(tFile);
            FileInputStream fis = new FileInputStream(sFile);
            fis.skip(ftyp.getOffset());
            IOUtils.copy(new LimitedInputStream(fis, ftyp.getAtomSize()), fos);
            fis.close();
            fis = new FileInputStream(sFile);
            fis.skip(moov.getOffset());
            IOUtils.copy(new LimitedInputStream(fis, moov.getAtomSize()), fos);
            fis.close();
            fos.close();
        }
    }
}
