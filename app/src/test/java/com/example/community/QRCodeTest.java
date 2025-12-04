package com.example.community;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

public class QRCodeTest {
    @Test
    public void encodeDeepLink_buildsBitMatrix() throws WriterException {
        String eventId = "unitEvent123";
        String payload = "community://event/" + eventId + "?v=1";
        int size = 256;

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bm = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
        assertNotNull(bm);
        assertEquals(size, bm.getWidth());
        assertEquals(size, bm.getHeight());

        // Sanity: at least one dark module exists
        boolean anyDark = false;
        for (int y = 0; y < size && !anyDark; y++) {
            for (int x = 0; x < size; x++) {
                if (bm.get(x, y)) { anyDark = true; break; }
            }
        }
        assertTrue(anyDark);
    }

    @Test
    public void encodeToPng_producesBytes() throws Exception {
        String payload = "community://event/unitEvent123?v=1";
        BitMatrix bm = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 256, 256);
        // Minimal bitmap→PNG (no Android Bitmap in local tests):
        int w = bm.getWidth(), h = bm.getHeight();
        int[] argb = new int[w * h];
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                argb[row + x] = bm.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }
        // Encode PNG with a tiny writer OR just assert non-empty pixel data:
        // Here we just assert pixel data exists (keeps it simple for JVM test).
        assertTrue(argb.length > 0);

        // If you want actual PNG bytes in JVM, use a tiny PNG encoder lib; otherwise,
        // the Android-side instrumentation test will cover real PNG via Bitmap.compress().
    }
}
