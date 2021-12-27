package dev.yuafox.yuabot.endpoints;

import dev.yuafox.yuabot.Action;
import dev.yuafox.yuabot.sources.DataSource;
import org.jetbrains.annotations.NotNull;

public interface Endpoint extends Action {
    boolean send(@NotNull DataSource data);
}
