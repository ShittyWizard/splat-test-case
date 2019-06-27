package testcase.logic;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class SearchThread implements Runnable {
    private static final int BUFFER_SIZE = 8192 * 4;
    private File dir;
    private String ext;
    private String text;
    private final List<File> filesQueue;

    public SearchThread(File dir, String ext, String text, List<File> filesQueue) {
        this.dir = dir;
        this.ext = ext;
        this.text = text;
        this.filesQueue = filesQueue;
    }

    @Override
    public void run() {
        findFiles(dir, ext, text);
    }

    private void findFiles(File dir, String ext, String text) {
        synchronized (filesQueue) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (final File file : files) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (!file.isDirectory()) {
                    if (file.toString().endsWith(ext) && searchText(file, text)) {
                        filesQueue.add(file);
                        filesQueue.notifyAll();
                    }
                } else {
                    findFiles(file, ext, text);
                }
            }
        }
    }

    private static boolean searchText(File file, String text) {
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file, "r");
            byte[] fileContent = new byte[BUFFER_SIZE];
            for (long pos = 0; pos < file.length(); pos += BUFFER_SIZE - text.length()) {
                accessFile.seek(pos);
                accessFile.read(fileContent);
                if (new String(fileContent).contains(text)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (accessFile != null) {
                    accessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
