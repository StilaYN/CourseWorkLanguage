package ru.langauge.coursework.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {

    public void write(String fileName, String content) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String read(String fileName) {
        try(BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            StringBuilder content = new StringBuilder();
            String currentLine;
            while((currentLine = reader.readLine()) != null){
                content.append(currentLine + "\n ");
            }
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
