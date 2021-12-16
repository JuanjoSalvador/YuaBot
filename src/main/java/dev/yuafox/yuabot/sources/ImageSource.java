package dev.yuafox.yuabot.sources;

import dev.yuafox.yuabot.YuaBot;

import java.io.File;
import java.util.Random;

public class ImageSource implements DataSource {

    @Override
    public boolean setup(YuaBot bot) {
        File imageFolder = new File(bot.getBotFolder(), "images");
        if(imageFolder.exists()) return true;
        return imageFolder.mkdir();
    }

    @Override
    public boolean prepare(YuaBot bot) {
        return new File(bot.getBotFolder(), "images").exists();
    }

    @Override
    public String getText(YuaBot bot) {
        return "";
    }

    @Override
    public File getMedia(YuaBot bot) {
        File[] files = new File(bot.getBotFolder(), "images").listFiles();
        assert files != null;

        Random rand = new Random();
        return files[rand.nextInt(files.length)];
    }
}
