package holworthy.sleepapp;

import java.io.File;

public class SleepFile {
    private File file;

    public SleepFile(File file){
        this.file = file;
    }

    @Override
    public String toString() {
        return file.getName().substring(0, file.getName().length() - 4);
    }

    public File getFile() {
        return file;
    }
}
