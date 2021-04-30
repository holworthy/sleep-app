package holworthy.sleepapp;

import java.io.File;

public class SleepFile {
    private File file;

    public SleepFile(File file){
        this.file = file;
    }

    @Override
    public String toString() {
        String[] parts = file.getName().substring(0, file.getName().length() - 4).split("-");
        return parts[0] + "-" + parts[1] + "-" + parts[2] + " " + parts[3] + ":" + parts[4] + ":" + parts[5];
    }

    public File getFile() {
        return file;
    }
}
