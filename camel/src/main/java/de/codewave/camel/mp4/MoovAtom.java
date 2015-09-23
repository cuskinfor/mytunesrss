package de.codewave.camel.mp4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.InflaterInputStream;

public class MoovAtom extends Mp4Atom {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoovAtom.class);

    public MoovAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    MoovAtom decompress(Mp4Parser parser) throws DecompressException {
        Mp4Atom dcom = getFirstChild("cmov.dcom");
        Mp4Atom cmvd = getFirstChild("cmov.cmvd");
        if (dcom != null && cmvd != null) {
            String decompressionType = dcom.getDataAsString(0, "US-ASCII");
            if ("zlib".equals(decompressionType)) {
                InflaterInputStream decompressor = new InflaterInputStream(new ByteArrayInputStream(cmvd.getData()));
                try {
                    return (MoovAtom) parser.parseAndGet(decompressor, "moov");
                } catch (IOException e) {
                    throw new DecompressException("Could not decompress MOOV atom.", e);
                } finally {
                    try {
                        decompressor.close();
                    } catch (IOException e) {
                        throw new DecompressException("Could not decompress MOOV atom.", e);
                    }
                }
            } else {
                throw new DecompressException("Cannot decompress MOOV atom with compression type \"" + decompressionType + "\".");
            }
        } else if (dcom != null) {
            throw new DecompressException("Missing CMVD atom.");
        } else if (cmvd != null) {
            throw new DecompressException("Missing DCOM atom.");
        } else {
            return this;
        }
    }

    public String getAlbumArtist() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.aART");
        return atom != null ? atom.getText() : null;
    }

    public String getSortAlbumArtist() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.soaa");
        return atom != null ? atom.getText() : null;
    }

    public String getAlbum() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.\u00a9alb");
        return atom != null ? atom.getText() : null;
    }

    public String getSortAlbum() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.soal");
        return atom != null ? atom.getText() : null;
    }

    public String getArtist() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.\u00a9ART");
        return atom != null ? atom.getText() : null;
    }

    public String getSortArtist() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.soar");
        return atom != null ? atom.getText() : null;
    }

    public String getTitle() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.\u00a9nam");
        return atom != null ? atom.getText() : null;
    }

    public String getGenre() {
        GenreAtom atom = (GenreAtom) getFirstChild("udta.meta.ilst.\u00a9gen");
        return atom != null ? atom.getGenre() : null;
    }

    public String getComposer() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.\u00a9wrt");
        return atom != null ? atom.getText() : null;
    }

    public String getComment() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.\u00a9cmt");
        return atom != null ? atom.getText() : null;
    }

    public Integer getYear() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.\u00a9day");
        if (atom == null) {
            return null;
        }
        String text = atom.getText();
        int i = 0;
        while (i < text.length() && Character.isDigit(text.charAt(i))) {
            i++;
        }
        try {
            return Integer.parseInt(text.substring(0, i));
        } catch (NumberFormatException e) {
            LOGGER.warn("Could not parse year \"" + text + "\".");
        }
        return null;
    }

    public CoverAtom getCoverAtom() {
        return (CoverAtom) getFirstChild("udta.meta.ilst.covr");
    }

    public Long getTrackNumber() {
        NumericAtom atom = (NumericAtom) getFirstChild("udta.meta.ilst.trkn");
        return atom != null ? atom.getValue() : null;
    }

    public String getTvShow() {
        TextAtom atom = (TextAtom) getFirstChild("udta.meta.ilst.tvsh");
        return atom != null ? atom.getText() : null;
    }

    public Long getTvSeason() {
        NumericAtom atom = (NumericAtom) getFirstChild("udta.meta.ilst.tvsn");
        return atom != null ? atom.getValue() : null;
    }

    public Long getTvEpisode() {
        NumericAtom atom = (NumericAtom) getFirstChild("udta.meta.ilst.tves");
        return atom != null ? atom.getValue() : null;
    }

    public boolean isCompilation() {
        CompilationAtom atom = (CompilationAtom) getFirstChild("udta.meta.ilst.cpil");
        return atom != null ? atom.isCompilation() : false;
    }

    public DiskAtom getDiskAtom() {
        return (DiskAtom) getFirstChild("udta.meta.ilst.disk");
    }

    public String getCodec() {
        SampleDescriptionsAtom sampleDescriptionsAtom = (SampleDescriptionsAtom) getFirstChild("trak.mdia.minf.stbl.stsd");
        if (sampleDescriptionsAtom != null) {
            CodecAtom codecAtom = (CodecAtom) findFirstChildWithType(CodecAtom.class);
            if (codecAtom != null) {
                return codecAtom.getId();
            } else {
                Mp4Atom drmsAtom = findFirstChildWithType(DrmsAtom.class);
                if (drmsAtom != null) {
                    OriginalFormatAtom originalFormatAtom = (OriginalFormatAtom) drmsAtom.findFirstChildWithType(OriginalFormatAtom.class);
                    if (originalFormatAtom != null) {
                        return originalFormatAtom.getValue();
                    }
                }
            }
        }
        return null;
    }

    public boolean isDrmProtected() {
        SampleDescriptionsAtom sampleDescriptionsAtom = (SampleDescriptionsAtom) getFirstChild("trak.mdia.minf.stbl.stsd");
        return sampleDescriptionsAtom != null && sampleDescriptionsAtom.findFirstChildWithType(DrmsAtom.class) != null;
    }

    public StikAtom.Type getMediaType() {
        StikAtom stikAtom = (StikAtom) getFirstChild("udta.meta.ilst.stik");
        return stikAtom != null ? stikAtom.getType() : StikAtom.Type.Normal;
    }
    
    public long getDurationSeconds() {
        MovieHeaderAtom mvhd = (MovieHeaderAtom)getFirstChild("mvhd");
        return Math.max(0L, mvhd.getDuration() / mvhd.getTimeScale());
    }

    public MovieHeaderAtom getMovieHeaderAtom() {
        return (MovieHeaderAtom)getFirstChild("mvhd");
    }
}
