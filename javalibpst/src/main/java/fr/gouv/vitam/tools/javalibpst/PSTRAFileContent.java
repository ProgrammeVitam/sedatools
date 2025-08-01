package fr.gouv.vitam.tools.javalibpst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PSTRAFileContent extends PSTFileContent {

    protected RandomAccessFile file;

    public PSTRAFileContent(final File file) throws FileNotFoundException {
        this.file = new RandomAccessFile(file, "r");
    }

    public RandomAccessFile getFile() {
        return this.file;
    }

    public void setFile(final RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public void seek(final long index) throws IOException {
    //    collectCallStack(); cf PSTFileContent
        this.file.seek(index);
    }

    @Override
    public long getFilePointer() throws IOException {
        return this.file.getFilePointer();
    }

    @Override
    public int read() throws IOException {
    //    collectCallStack(); cf PSTFileContent
        return this.file.read();
    }

    @Override
    public int read(final byte[] target) throws IOException {
    //    collectCallStack(); cf PSTFileContent
        return this.file.read(target);
    }

    @Override
    public byte readByte() throws IOException {
   //     collectCallStack(); cf PSTFileContent
        return this.file.readByte();
    }

    @Override
    public void close() throws IOException {
    //    systemOutTraceLines(); cf PSTFileContent
        this.file.close();
    }

}
