package dev.yuafox.yuabot;

import java.io.File;

public class YuaBot {

    private static File baseFolder = new File("bots");
    private File botFolder;

    public YuaBot(String botName){
        if(!baseFolder.exists())
            baseFolder.mkdir();
        botFolder = new File(baseFolder, botName);
        if(!botFolder.exists())
            botFolder.mkdir();
    }

    public File getBotFolder(){
        return this.botFolder;
    }
}
