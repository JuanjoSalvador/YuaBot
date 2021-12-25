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

    private Twitter twitterInstance;

    private File base;

    private File propertiesFile;
    private Properties properties;

    private File authFile;

    @Override
    public void init(YuaBot bot){
        this.twitterInstance = new TwitterFactory().getInstance();

        this.base = new File(bot.getBotFolder(), "twitter");

        this.propertiesFile = new File(this.base, ".properties");
        this.properties = new Properties();

        this.authFile = new File(this.base, "auth.ser");

        if(this.readCredentials()){
            this.readAuth();
        }
    }

    @Override
    public boolean setup() {
        this.base.mkdir();
        try {
            if(!propertiesFile.exists()){
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Consumer key:");
                String key = br.readLine();
                System.out.println("Consumer secret:");
                String secret = br.readLine();
                this.propertiesFile.createNewFile();
                this.properties.put("key", key);
                this.properties.put("secret", secret);
                this.properties.store(new FileOutputStream(propertiesFile), null);
            }

            if(this.readCredentials()){
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
                    fout = new FileOutputStream(this.authFile, false);
                    oos = new ObjectOutputStream(fout);
                    oos.writeObject(accessToken);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                } finally {
                    if(oos != null){
                        oos.close();
                    }
                }
                return true;

            }else{
                System.err.println("Invalid credentials.");
            }
        } catch (TwitterException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean run() {
        try {
            this.twitterInstance.verifyCredentials();
            return true;
        } catch (IllegalStateException | TwitterException e) {
            return false;
        }
    }

    @Override
    public boolean send(@NotNull DataSource data) {
        StatusUpdate statusUpdate = new StatusUpdate(data.getText());
        statusUpdate.setMedia(data.getMedia());
        try {
            this.twitterInstance.updateStatus(statusUpdate);
            return true;
        } catch (TwitterException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean end() {
        return true;
    }


    private boolean readCredentials() {
        try {
            if(!this.propertiesFile.exists()) return false;
            FileInputStream fileIn = new FileInputStream(this.propertiesFile);
            this.properties.load(fileIn);
            this.twitterInstance.setOAuthConsumer(this.properties.getProperty("key"), this.properties.getProperty("secret"));
            return true;
        }catch (Exception e){
            e.printStackTrace(System.err);
            return false;
        }
    }

    private void readAuth() {
        try {
            if(!this.authFile.exists()) return;
            FileInputStream fileIn = new FileInputStream(this.authFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            AccessToken accessToken = (AccessToken) in.readObject();
            this.twitterInstance.setOAuthAccessToken(accessToken);
            in.close();
        }catch (Exception e){
            e.printStackTrace(System.err);
        }
    }
}
