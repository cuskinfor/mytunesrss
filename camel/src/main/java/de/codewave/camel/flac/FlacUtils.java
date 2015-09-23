package de.codewave.camel.flac;

import de.codewave.camel.Endianness;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.camel.flac.FlacUtils
 */
public class FlacUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlacUtils.class);

    public static Map<FlacMetadataType, List<FlacMetadata>> readMetadataBlocks(InputStream is) throws IOException {
        Map<FlacMetadataType, List<FlacMetadata>> metadataMap = new HashMap<FlacMetadataType, List<FlacMetadata>>();
        byte[] buffer = new byte[4];
        if (is.read(buffer) != 4) {
            throw new IOException("Could not read enough data from stream.");
        }
        if (!StringUtils.equals(new String(buffer, "UTF-8"), "fLaC")) {
            throw new IOException("No FLAC file magic bytes found.");
        }
        boolean lastBlock = false;
        while (!lastBlock) {
            if (is.read(buffer) != 4) {
                throw new IOException("Could not read enough data from stream.");
            }
            lastBlock = (buffer[0] & 0x80) == 0x80;
            FlacMetadataType type = FlacMetadataType.getTypeForValue(buffer[0] & 0x7f);
            int blocksize = (int) getValue(ArrayUtils.subarray(buffer, 1, 4), Endianness.Big);
            FlacMetadataData data = createData(type, is, blocksize);
            if (data != null) {
                if (!metadataMap.containsKey(type)) {
                    metadataMap.put(type, new ArrayList<FlacMetadata>());
                }
                metadataMap.get(type).add(new FlacMetadata(type, data));
            }
        }
        return metadataMap;
    }


    static long getValue(byte[] buffer, Endianness endianness) {
        if (endianness == Endianness.Little) {
            ArrayUtils.reverse(buffer);
        }
        return new BigInteger(1, buffer).longValue();
    }

    static long getValue(InputStream is, int byteCount, Endianness endianness) throws IOException {
        byte[] buffer = new byte[byteCount];
        if (is.read(buffer) == byteCount) {
            if (endianness == Endianness.Little) {
                ArrayUtils.reverse(buffer);
            }
            return new BigInteger(1, buffer).longValue();
        }
        throw new IOException("Could not read enough data from stream.");
    }

    private static FlacMetadataData createData(FlacMetadataType type, InputStream is, int blocksize) throws IOException {
        LOGGER.debug("Found " + type.name() + " metadata block in FLAC file.");
        ReadCountInputStream rcis = new ReadCountInputStream(is);
        try {
            switch (type) {
                case APPLICATION:
                    return null;
                case CUESHEET:
                    return null;
                case PADDING:
                    return null;
                case PICTURE:
                    return null;
                case SEEKTABLE:
                    return null;
                case STREAMINFO:
                    return new FlacMetadataStreaminfo(rcis);
                case VORBIS_COMMENT:
                    return null;
                default:
                    return null;
            }
        } finally {
            is.skip(blocksize - rcis.getCount());
        }
    }

    public static byte[] getBits(byte[] buffer, int start, int count) {
        byte[] result = ArrayUtils.subarray(buffer, (start - 1) / 8, (start + count - 1) / 8);
        int shift = result.length * 8 - count;
        for (int i = 0; i < shift; i++) {
            for (int k = result.length - 1; k >= 0; k--) {
                boolean rightmostSet = (result[k] & 0x01B) == 0x01B;
                result[k] = (byte) (result[k] >> 1);
                if (rightmostSet && i < result.length - 1) {
                    result[k + 1] = (byte) (result[k + 1] & 0x80B);
                }
            }
        }
        return ArrayUtils.subarray(result, result.length - ((count - 1) / 8) + 1, result.length);
    }
}
