package de.codewave.camel.mp4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Mp4AtomFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4AtomFactory.class);

    private static final Map<String, Class<? extends Mp4Atom>> CLASS_MAP = new HashMap<String, Class<? extends Mp4Atom>>();
    static {
        CLASS_MAP.put("stik", StikAtom.class);
        CLASS_MAP.put("moov", MoovAtom.class);
        CLASS_MAP.put("stsd", SampleDescriptionsAtom.class);
        CLASS_MAP.put("drms", DrmsAtom.class);
        CLASS_MAP.put("alac", CodecAtom.class);
        CLASS_MAP.put("mp4a", CodecAtom.class);
        CLASS_MAP.put("frma", OriginalFormatAtom.class);
        CLASS_MAP.put("covr", CoverAtom.class);
        CLASS_MAP.put("\u00a9alb", TextAtom.class);
        CLASS_MAP.put("\u00a9ART", TextAtom.class);
        CLASS_MAP.put("aART", TextAtom.class);
        CLASS_MAP.put("\u00a9nam", TextAtom.class);
        CLASS_MAP.put("trkn", NumericAtom.class);
        CLASS_MAP.put("disk", DiskAtom.class);
        CLASS_MAP.put("\u00a9gen", GenreAtom.class);
        CLASS_MAP.put("\u00a9wrt", TextAtom.class);
        CLASS_MAP.put("\u00a9cmt", TextAtom.class);
        CLASS_MAP.put("\u00a9day", YearAtom.class);
        CLASS_MAP.put("covr", CoverAtom.class);
        CLASS_MAP.put("tvsh", TextAtom.class);
        CLASS_MAP.put("tvsn", NumericAtom.class);
        CLASS_MAP.put("tves", NumericAtom.class);
        CLASS_MAP.put("cpil", CompilationAtom.class);
        CLASS_MAP.put("mvhd", MovieHeaderAtom.class);
        CLASS_MAP.put("soal", TextAtom.class);
        CLASS_MAP.put("soar", TextAtom.class);
        CLASS_MAP.put("soaa", TextAtom.class);
    }

    private static final Map<Class<? extends Mp4Atom>, Long> ADDITIONAL_SKIP_SIZES = new HashMap<Class<? extends Mp4Atom>, Long>();
    static {
        ADDITIONAL_SKIP_SIZES.put(DrmsAtom.class, 28L);
        ADDITIONAL_SKIP_SIZES.put(SampleDescriptionsAtom.class, 8L);
    }

    private final Mp4Parser myParser;

    Mp4AtomFactory(Mp4Parser parser) {
        myParser = parser;
    }

    long getAdditionalSkipSize(String path) {
        Class<? extends Mp4Atom> clazz = CLASS_MAP.get(path.substring(path.lastIndexOf('.') + 1));
        if (clazz != null) {
            Long skipSize = ADDITIONAL_SKIP_SIZES.get(clazz);
            if (skipSize != null) {
                LOGGER.debug("Additional skip size for \"" + path + "\" is " + skipSize + " bytes.");
                return skipSize;
            }
        }
        LOGGER.debug("No additional skip size for \"" + path + "\".");
        return 0;
    }

    Mp4Atom create(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        Class<? extends Mp4Atom> clazz = CLASS_MAP.get(path.substring(path.lastIndexOf('.') + 1));
        Mp4Atom atom = null;
        if (clazz != null) {
            try {
                atom = clazz.getDeclaredConstructor(String.class, long.class, List.class, byte[].class, long.class).newInstance(path, offset, children, data, atomSize);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                LOGGER.warn("Could not create Mp4Atom class instance for \"" + path + "\".", e);
            }
        }
        if (atom == null) {
            atom = new Mp4Atom(path, offset, children, data, atomSize);
        }
        try {
            return atom instanceof MoovAtom ? ((MoovAtom)atom).decompress(myParser) : atom;
        } catch (DecompressException e) {
            LOGGER.warn("Could not decompress MOOV atom.", e);
            return atom;
        }
    }

}
