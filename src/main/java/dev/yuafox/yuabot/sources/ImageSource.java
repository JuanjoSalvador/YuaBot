package dev.yuafox.yuabot.sources;

import dev.yuafox.yuabot.YuaBot;

import java.io.File;
import java.util.Random;

public class ImageSource implements DataSource {

    private YuaBot bot = null;
    private File imageFolder = null;

    @Override
    public void init(YuaBot bot) {
        this.bot = bot;
        this.imageFolder = new File(bot.getBotFolder(), "images");
    }

    @Override
    public boolean setup() {
        if(this.imageFolder.exists()) return true;
        return this.imageFolder.mkdir();
    }

    @Override
    public boolean run() {
        return this.imageFolder.exists();
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public File getMedia() {
        File[] files = new File(this.bot.getBotFolder(), "images").listFiles();
        assert files != null;

        Random rand = new Random();
        return files[rand.nextInt(files.length)];
    }

    @Override
    public boolean end() {
        return true;
    }
}
