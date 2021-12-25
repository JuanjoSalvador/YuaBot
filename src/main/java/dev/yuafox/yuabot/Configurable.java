package dev.yuafox.yuabot;

public interface Configurable {

    void init(YuaBot bot);
    boolean setup();
    boolean run();
    boolean end();
}
