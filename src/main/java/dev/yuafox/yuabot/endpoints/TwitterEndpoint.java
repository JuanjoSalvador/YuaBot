package dev.yuafox.yuabot.endpoints;

import dev.yuafox.yuabot.YuaBot;
import dev.yuafox.yuabot.sources.DataSource;
import org.jetbrains.annotations.NotNull;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.*;
import java.util.Properties;

public class TwitterEndpoint implements Endpoint {

    private static final String FILENAME_AUTH = "TwitterAuth.ser";
    private static final String FILENAME_CONFIG = "TwitterConfig.properties";
    private static final Properties PROPERTIES = new Properties();

    private final Twitter twitterInstance;


    public TwitterEndpoint(){
        this.twitterInstance = new TwitterFactory().getInstance();
    }

    @Override
    public boolean send(@NotNull YuaBot bot, @NotNull DataSource data) {
        this.readAuth(bot);

        StatusUpdate statusUpdate = new StatusUpdate(data.getText(bot));
        statusUpdate.setMedia(data.getMedia(bot));
        try {
            this.twitterInstance.updateStatus(statusUpdate);
            return true;
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean setup(YuaBot bot) {
        try {
            File fileConfig = new File(bot.getBotFolder(), TwitterEndpoint.FILENAME_CONFIG);

            if(!fileConfig.exists()){
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Consumer key:");
                String key = br.readLine();
                System.out.println("Consumer secret:");
                String secret = br.readLine();
                TwitterEndpoint.PROPERTIES.put("key", key);
                TwitterEndpoint.PROPERTIES.put("secret", secret);
                TwitterEndpoint.PROPERTIES.store(new FileOutputStream(fileConfig), null);
            }

            this.twitterInstance.setOAuthConsumer(TwitterEndpoint.PROPERTIES.getProperty("key"), TwitterEndpoint.PROPERTIES.getProperty("secret"));

            RequestToken requestToken = this.twitterInstance.getOAuthRequestToken();
            AccessToken accessToken = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while(accessToken == null) {
                System.out.println("Open the following URL to login with Twitter.");
                System.out.println(requestToken.getAuthorizationURL());
                System.out.println("Then paste your PIN here:");

                String pin = br.readLine();
                if(pin.length() > 0){
                    accessToken = this.twitterInstance.getOAuthAccessToken(requestToken, pin);
                }else{
                    accessToken = this.twitterInstance.getOAuthAccessToken();
                }
            }

            ObjectOutputStream oos = null;
            FileOutputStream fout;
            try{
                fout = new FileOutputStream(new File(bot.getBotFolder(), TwitterEndpoint.FILENAME_AUTH), false);
                oos = new ObjectOutputStream(fout);
                oos.writeObject(accessToken);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if(oos != null){
                    oos.close();
                }
            }
            return true;
        } catch (TwitterException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean prepare(YuaBot bot) {
        if(!this.readCredentials(bot)) return false;
        this.readAuth(bot);
        try {
            this.twitterInstance.verifyCredentials();
            return true;
        } catch (IllegalStateException | TwitterException e) {
            return false;
        }
    }

    private boolean readCredentials(YuaBot bot) {
        try {
            File file = new File(bot.getBotFolder(), TwitterEndpoint.FILENAME_CONFIG);
            if(!file.exists()) return false;
            FileInputStream fileIn = new FileInputStream(file);
            TwitterEndpoint.PROPERTIES.load(fileIn);
            this.twitterInstance.setOAuthConsumer(TwitterEndpoint.PROPERTIES.getProperty("key"), TwitterEndpoint.PROPERTIES.getProperty("secret"));
            return true;
        }catch (Exception e){
            e.printStackTrace(System.err);
            return false;
        }
    }

    private void readAuth(YuaBot bot) {
        try {
            File file = new File(bot.getBotFolder(), TwitterEndpoint.FILENAME_AUTH);
            if(!file.exists()) return;
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            AccessToken accessToken = (AccessToken) in.readObject();
            this.twitterInstance.setOAuthAccessToken(accessToken);
            in.close();
        }catch (Exception e){
            e.printStackTrace(System.err);
        }
    }
}
