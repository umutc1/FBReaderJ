package org.amse.ys.zip;

import java.util.*;
import java.io.*;

public abstract class Decompressor {
    public Decompressor(MyBufferedInputStream is, LocalFileHeader header) {
    }

    public abstract int read(byte b[], int off, int len) throws IOException, WrongZipFormatException;
    public abstract int read() throws IOException, WrongZipFormatException;

    protected Decompressor() {
    }

    private static Queue<DeflatingDecompressor> ourDeflators = new LinkedList<DeflatingDecompressor>();

    static void storeDecompressor(Decompressor decompressor) {
        if (decompressor instanceof DeflatingDecompressor) {
            synchronized (ourDeflators) {
                ourDeflators.add((DeflatingDecompressor)decompressor);
            }
        }
    }

    public static Decompressor init(MyBufferedInputStream is, LocalFileHeader header)
            throws WrongZipFormatException {
        switch (header.CompressionMethod) {
        case 0:
            return new NoCompressionDecompressor(is, header);
        case 8:
            synchronized (ourDeflators) {
                if (!ourDeflators.isEmpty()) {
                    DeflatingDecompressor decompressor = ourDeflators.poll();
                    decompressor.reset(is, header);
                    return decompressor;
                }
            }
            return new DeflatingDecompressor(is, header);
        default:
            throw new WrongZipFormatException("Unsupported method of compression");
        }
    }
    
    public int available() {
        return -1;
    }
}
