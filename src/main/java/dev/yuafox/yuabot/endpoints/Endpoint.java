package dev.yuafox.yuabot.endpoints;

import dev.yuafox.yuabot.Configurable;
import dev.yuafox.yuabot.YuaBot;
import dev.yuafox.yuabot.sources.DataSource;
import org.jetbrains.annotations.NotNull;

public interface Endpoint extends Configurable {
    boolean send(@NotNull YuaBot bot, @NotNull DataSource data);
}
