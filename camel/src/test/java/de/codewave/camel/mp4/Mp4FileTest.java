/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * de.codewave.camel.mp4.Mp4FileTest
 */
public class Mp4FileTest {

    private static enum Atom {
        Album("moov.udta.meta.ilst.\u00a9alb.data"),
        Artist("moov.udta.meta.ilst.\u00a9ART.data"),
        AlbumArtist("moov.udta.meta.ilst.aART.data"),
        Title("moov.udta.meta.ilst.\u00a9nam.data"),
        TrackNumber("moov.udta.meta.ilst.trkn.data"),
        DiskNumber("moov.udta.meta.ilst.disk.data"),
        Genre("moov.udta.meta.ilst.\u00a9gen.data"),
        Stsd("moov.trak.mdia.minf.stbl.stsd"),
        Cover("moov.udta.meta.ilst.covr.data"),
        Composer("moov.udta.meta.ilst.\u00a9wrt.data"),
        Comment("moov.udta.meta.ilst.\u00a9cmt.data"),
        Year("moov.udta.meta.ilst.\u00a9day.data"),
        Series("moov.udta.meta.ilst.tvsh.data"),
        Season("moov.udta.meta.ilst.tvsn.data"),
        Episode("moov.udta.meta.ilst.tves.data"),
        Compilation("moov.udta.meta.ilst.cpil"),
        Stik("moov.udta.meta.ilst.stik");

        private String myPath;

        Atom(String path) {
            myPath = path;
        }

        public String getPath() {
            return myPath;
        }
    }

    private static final Collection<String> ALL_ATOM_NAMES = new ArrayList<String>();
    static {
        for (Atom atom : Atom.values()) {
            ALL_ATOM_NAMES.add(atom.getPath());
        }
    }

    private Mp4Parser myParser = new Mp4Parser();
    private URL dasBesteUrl;

    @Before
    public void setUp() throws IOException {
        dasBesteUrl = getClass().getClassLoader().getResource("DasBeste.m4p");
    }

    @Test
    public void testReadAtomsDasBeste() throws IOException {
        MoovAtom moovAtom = (MoovAtom) myParser.parseAndGet(dasBesteUrl, "moov");
        assertEquals("Laut gedacht (Standard Version)", moovAtom.getAlbum());
        assertEquals("Andreas Nowak, Johannes Stolle, Stefanie Kloß & Thomas Stolle", moovAtom.getComposer());
        assertEquals("Silbermond", moovAtom.getAlbumArtist());
        assertEquals("Silbermond", moovAtom.getArtist());
        assertEquals("image/jpeg", moovAtom.getCoverAtom().getMimeType());
        assertEquals(7L, moovAtom.getTrackNumber().longValue());
        assertEquals(1, moovAtom.getDiskAtom().getNumber());
        assertEquals(1, moovAtom.getDiskAtom().getSize());
        assertEquals(2006, moovAtom.getYear().intValue());
        assertEquals(StikAtom.Type.Normal, moovAtom.getMediaType());
        assertEquals(284, moovAtom.getDurationSeconds());
        assertNull(moovAtom.getGenre());
        assertEquals("mp4a", moovAtom.getCodec());
        assertTrue(moovAtom.isDrmProtected());
    }

    @Test
    public void testGetChild() throws IOException {
        Mp4Atom atom = myParser.parseAndGet(dasBesteUrl, "moov");
        assertEquals("udta", atom.getFirstChild("udta").getId());
        assertEquals("meta", atom.getFirstChild("udta.meta").getId());
        assertEquals("ilst", atom.getFirstChild("udta.meta.ilst").getId());
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

    @Test
    public void testReadItcAtoms() throws Exception {
        System.out.println("test.itc:");
        URL testItcUrl = getClass().getClassLoader().getResource("test.itc");
        displayAtoms(myParser.parse(testItcUrl).toList(), 0);
    }

    @Test
    public void testReadAtomsAnDich() throws IOException {
        System.out.println("An Dich.m4a:");
        displayAtoms(myParser.parse(getClass().getClassLoader().getResource("AnDich.m4a")).toList(), 0);
        MoovAtom moovAtom = (MoovAtom) myParser.parseAndGet(getClass().getClassLoader().getResource("AnDich.m4a"), "moov");
        assertEquals("mp4a", moovAtom.getCodec());
        assertFalse(moovAtom.isDrmProtected());
    }

    @Test
    @Ignore
    public void testJenTheFredo() throws IOException {
        System.out.println("Jen the Fredo:");
        displayAtoms(myParser.parse(new File("/Network/Servers/readynas.local/iTunes/iTunes Media/TV Shows/The IT Crowd/Season 4/01 Jen the Fredo.mp4")).toList(), 0);
    }

    @Test
    @Ignore
    public void testJenTheFredoToFastStart() throws IOException {
        File file = new File("/Network/Servers/readynas.local/iTunes/iTunes Media/TV Shows/The IT Crowd/Season 4/01 Jen the Fredo.mp4");
        FileOutputStream out = new FileOutputStream(new File("/Users/mdescher/Desktop/JenTheFredoCopy.mp4"));
        Mp4Utils.writeFastStart(file, out);
        out.close();
    }

    @Test
    @Ignore
    public void testJenTheFredoToFastStartStream() throws IOException {
        File file = new File("/Network/Servers/readynas.local/iTunes/iTunes Media/TV Shows/The IT Crowd/Season 4/01 Jen the Fredo.mp4");
        File dstFile = new File("/Users/mdescher/Desktop/JenTheFredoAsStream.mp4");
        FileOutputStream out = new FileOutputStream(dstFile);
        IOUtils.copy(Mp4Utils.getFastStartInputStream(file), out);
        out.close();
        System.out.println("---src---");
        displayAtoms(new Mp4Parser(0, true).parse(file).toList(), 0);
        System.out.println("---dst---");
        displayAtoms(new Mp4Parser(0, true).parse(dstFile).toList(), 0);
    }

    @Test
    public void testAcademyOfStMartinInTheFieldsConcerto() throws IOException {
        Mp4AtomList atoms = new Mp4Parser().parse(getClass().getResource("/AcademyOfStMartinInTheFieldsConcerto.m4a"));
        CompilationAtom compilationAtom = (CompilationAtom) atoms.getFirst("moov.udta.meta.ilst.cpil");
        System.out.println("compilation = " + compilationAtom.isCompilation());
    }

    @Test
    public void testHarderBetterFasterStronger() throws IOException {
        assertNotNull(new Mp4Parser().parseAndGet(getClass().getResource("/HarderBetterFasterStronger.m4a"), "moov"));
        assertEquals(2, new Mp4Parser().parse(getClass().getResource("/HarderBetterFasterStronger.m4a"), ALL_ATOM_NAMES.toArray(new String[ALL_ATOM_NAMES.size()])).toList().size());
        MoovAtom moovAtom = (MoovAtom) myParser.parseAndGet(getClass().getResource("/HarderBetterFasterStronger.m4a"), "moov");
        assertEquals("mp4a", moovAtom.getCodec());
        assertFalse(moovAtom.isDrmProtected());
    }

    @Test
    public void testAriaReprise() throws IOException {
        MoovAtom moov = (MoovAtom) new Mp4Parser().parseAndGet(getClass().getResource("/Aria_Reprise.m4a"), "moov");
        assertNotNull(moov);
        assertEquals("Goldberg Variations", moov.getAlbum());
        assertEquals("Bach - Adras Schiff", moov.getAlbumArtist());
        assertEquals("Bach - Adras Schiff", moov.getArtist());
        assertEquals("Bach, Johann Sebastian", moov.getComposer());
        assertNull(moov.getGenre());
        assertEquals("Aria Reprise", moov.getTitle());
        MoovAtom moovAtom = (MoovAtom) myParser.parseAndGet(getClass().getResource("/Aria_Reprise.m4a"), "moov");
        assertEquals("mp4a", moovAtom.getCodec());
        assertFalse(moovAtom.isDrmProtected());
    }


    @Test
    public void testStealthFighters() throws IOException {
        for (int i = 1; i < 6; i++) {
            MoovAtom moov = (MoovAtom) new Mp4Parser().parseAndGet(getClass().getResource("/StealthFighters" + i + ".mp4"), "moov");
            assertNotNull(moov);
            assertEquals("WW II History", moov.getAlbum());
            assertEquals("National Geographic Channel", moov.getArtist());
            assertEquals("History", moov.getGenre());
            assertEquals(i + ". Hitler's Stealth Fighter (Part " + i + ")", moov.getTitle());
        }
    }

    @Test
    public void testAllIReallyWant() throws IOException {
        MoovAtom moov = (MoovAtom) new Mp4Parser().parseAndGet(getClass().getResource("/AllIReallyWant.m4a"), "moov");
        assertNotNull(moov);
        assertEquals("Jagged Little Pill", moov.getAlbum());
        assertEquals("Alanis Morissette", moov.getArtist());
        assertEquals("All I Really Want", moov.getTitle());
        MoovAtom moovAtom = (MoovAtom) myParser.parseAndGet(getClass().getResource("/AllIReallyWant.m4a"), "moov");
        assertEquals("mp4a", moovAtom.getCodec());
        assertFalse(moovAtom.isDrmProtected());
    }
}
