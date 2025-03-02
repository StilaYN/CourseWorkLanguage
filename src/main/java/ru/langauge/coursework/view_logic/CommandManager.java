package ru.langauge.coursework.view_logic;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandManager {

    private final Deque<Command> doStack;

    private final Deque<Command> undoStack;

    private Boolean isCommandExecution;

    public CommandManager(int commandLimit) {
        doStack = new ArrayDeque<>(commandLimit);
        undoStack = new ArrayDeque<>(commandLimit);
        isCommandExecution = false;
    }

    public void repeatCommand(){
        if(!doStack.isEmpty()){
            isCommandExecution = true;
            Command command = doStack.getFirst();
            Command newCommand = null;
            newCommand = command.copy();
            doStack.push(newCommand);
            newCommand.repeat();
            isCommandExecution = false;
        }
    }

    public void executeCommand(Command command) {
        doStack.push(command);
        undoStack.clear();
        command.execute();
    }

    public void redoCommand() {
        if (!undoStack.isEmpty()) {
            isCommandExecution = true;
            Command command = undoStack.pop();
            command.execute();
            doStack.push(command);
            isCommandExecution = false;
        }
    }

    public void cancelCommand() {
        if (!doStack.isEmpty()) {
            isCommandExecution = true;
            Command command = doStack.pop();
            undoStack.push(command);
            command.cancel();
            isCommandExecution = false;
        }
    }

    public Boolean getCommandExecution() {
        return isCommandExecution;
    }
}
