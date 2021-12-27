package dev.yuafox.yuabot.sources;

import dev.yuafox.yuabot.Action;

import java.io.File;

public interface DataSource extends Action {

    String getText();
    File getMedia();

}
