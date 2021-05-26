package cribbage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class File {
    private static File instance;

    private File() {

    }

    public static File getInstance() {
        if(instance == null) {
            instance = new File();
        }

        return instance;
    }

    public void append(String fileName, String textToAppend) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName,true))) {
            writer.write(textToAppend);
        } catch(IOException e) {

        }
    }

    public void clear(String fileName) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("");
        } catch(IOException e) {

        }
    }
}
