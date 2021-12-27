package dev.yuafox.yuabot.sources;

import dev.yuafox.yuabot.YuaBot;
import dev.yuafox.yuabot.utils.Https;
import twitter4j.JSONArray;
import twitter4j.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.Random;

public class RedditSource implements DataSource {

    private File base;

    private File propertiesFile;
    private Properties properties;

    private File imagesFolder;

    private String text;
    private File image;

    @Override
    public void init(YuaBot bot){
        this.base = new File(bot.getBotFolder(), "reddit");

        this.propertiesFile = new File(this.base, ".properties");
        this.properties = new Properties();

        this.imagesFolder = new File(this.base, "images");

        this.readProperties();
    }

    @Override
    public boolean setup() {
        this.base.mkdir();
        this.imagesFolder.mkdir();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter subreddit names separated with spaces: (space foxes badcode)");
            String subreddits = br.readLine();
            this.propertiesFile.createNewFile();
            this.properties.put("subreddit-random", subreddits);
            this.properties.store(new FileOutputStream(propertiesFile), null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean run() {
        try {
            String[] subreddits = this.properties.getProperty("subreddit-random").split(" ");
            Random rand = new Random();
            String subreddit = subreddits[rand.nextInt(subreddits.length)];

            this.readRandomPost(subreddit);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public File getMedia() {
        return this.image;
    }

    @Override
    public boolean end() {
        try {
            this.clearImages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean readProperties() {
        try {
            if(!this.propertiesFile.exists()) return false;
            FileInputStream fileIn = new FileInputStream(this.propertiesFile);
            this.properties.load(fileIn);
            return true;
        }catch (Exception e){
            e.printStackTrace(System.err);
            return false;
        }
    }

    private void readRandomPost(String subreddit) throws IOException {
        JSONArray json = Https.getJsonArray(String.format("https://www.reddit.com/r/%s/random.json", subreddit));
        JSONObject baseData = json.getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data");

        String imgUrl = baseData.getString("url_overridden_by_dest");
        String tag = baseData.optString("link_flair_text");

        this.text = baseData.getString("title")+"\n\n"+
                ( tag.equals("") ? "" : "\uD83C\uDFF7️ "+tag+"\n" )  +
                "\uD83D\uDD17 https://reddit.com"+baseData.getString("permalink");
        this.image = Https.download(imgUrl, this.imagesFolder.getAbsolutePath()+"/"+new URL(imgUrl).getFile());
    }

    private void clearImages() throws IOException {
        Path directory = this.imagesFolder.toPath();
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}