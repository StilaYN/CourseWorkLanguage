package ru.langauge.coursework.view_logic;

import javafx.scene.control.TextArea;

public class TextEditCommand implements Command {

    private String oldText;
    private String newText;
    private final TextArea contrrolTextArea;
    private int position;

    @Override
    public void execute() {
        position = position + newText.length() - oldText.length();
        contrrolTextArea.setText(newText);
        contrrolTextArea.positionCaret(position);
        contrrolTextArea.requestFocus();
    }

    @Override
    public void repeat() {
        int length = newText.length() - oldText.length();
        if (length > 0) {
            String insertSubstring =  newText.substring(position - length, position);
            contrrolTextArea.insertText(position - 1, insertSubstring);
        } else if (length < 0) {
            contrrolTextArea.deleteText(position + length, position);
        }
        position += length;
        contrrolTextArea.requestFocus();
        contrrolTextArea.positionCaret(position);
        oldText = newText;
        newText = contrrolTextArea.getText();
    }

    @Override
    public void cancel() {
        position = position - newText.length() + oldText.length();
        contrrolTextArea.setText(oldText);
        contrrolTextArea.positionCaret(position);
        contrrolTextArea.requestFocus();
    }

    public TextEditCommand(String oldText, String newText, TextArea contrrolTextArea) {
        this.oldText = oldText;
        this.newText = newText;
        this.contrrolTextArea = contrrolTextArea;
        this.position = contrrolTextArea.getCaretPosition();
    }

    @Override
    public Command copy() {
        return new TextEditCommand(oldText, newText, contrrolTextArea);
    }
}
