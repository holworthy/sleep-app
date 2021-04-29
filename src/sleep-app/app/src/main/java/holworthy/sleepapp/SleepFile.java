package holworthy.sleepapp;

import java.io.File;

public class SleepFile {
    private File file;
    private String name;

    SleepFile(File file){
        this.file = file;
        this.name = file.getName().substring(0, file.getName().length()-4);
    }

    @Override
    public String toString() {
        return name;
    }
}
