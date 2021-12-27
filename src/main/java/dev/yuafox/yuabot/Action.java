package dev.yuafox.yuabot;

public interface Action {

    void init(YuaBot bot);
    boolean setup();
    boolean run();
    boolean end();
}
