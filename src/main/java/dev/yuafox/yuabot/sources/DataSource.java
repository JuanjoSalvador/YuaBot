package dev.yuafox.yuabot.sources;

import dev.yuafox.yuabot.Configurable;
import dev.yuafox.yuabot.YuaBot;

import java.io.File;

public interface DataSource extends Configurable {

    String getText();
    File getMedia();

}
