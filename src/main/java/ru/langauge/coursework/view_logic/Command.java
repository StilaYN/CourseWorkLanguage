package ru.langauge.coursework.view_logic;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface Command {

    int getId();

    void setId(int id);

    void execute();

    void repeat();

    void cancel();

    Command copy();

}
