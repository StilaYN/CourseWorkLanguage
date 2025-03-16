package ru.langauge.coursework.core.service;

import ru.langauge.coursework.core.FileManager;
import ru.langauge.coursework.view_logic.CommandManager;

import java.util.UUID;

public class FileService {

    private String currentFileName;

    private final FileManager fileManager = new FileManager();

    private final CommandManager commandManager;

    public FileService(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void save(String content) {
        if (currentFileName == null) {
            UUID uuid = UUID.randomUUID();
            currentFileName = uuid.toString() + ".txt";
        }
        fileManager.write(currentFileName, content);
        commandManager.doSaveState();
    }

    public void save(String fileName, String content) {
        if (fileName != null) {
            currentFileName = fileName;
            fileManager.write(currentFileName, content);
            commandManager.doSaveState();
        } else {
            throw new IllegalArgumentException("File name cannot be null");
        }
    }

    public String read(String fileName) {
        if (fileName != null) {
            currentFileName = fileName;
            return fileManager.read(currentFileName);
        } else {
            throw new IllegalArgumentException("File name cannot be null");
        }
    }


}
