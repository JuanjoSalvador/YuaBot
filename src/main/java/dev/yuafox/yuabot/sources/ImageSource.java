package dev.yuafox.yuabot.sources;

import dev.yuafox.yuabot.YuaBot;

import java.io.*;
import java.sql.*;
import java.util.*;

public class ImageSource implements DataSource {

    private File base;
    private File imagesFolder;

    private File propertiesFile;
    private Properties properties;

    private Connection connection;

    private String text;
    private File image;

    @Override
    public void init(YuaBot bot) {
        this.base = new File(bot.getBotFolder(), "images");
        this.imagesFolder = new File(this.base, "data");

        this.propertiesFile = new File(this.base, ".properties");
        this.properties = new Properties();

        this.connection = null;
    }

    @Override
    public boolean setup() {
        try {
            this.base.mkdir();
            this.imagesFolder.mkdir();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("No repeat image interval:");
            String number = br.readLine();
            this.propertiesFile.createNewFile();
            this.properties.put("no-repeat-interval", number);
            this.properties.put("no-repeat-current", "0");
            this.properties.put("last-modified", ""+this.imagesFolder.lastModified());
            this.properties.store(new FileOutputStream(propertiesFile), null);
            if(!this.createConnection()) return false;

            PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE images (" +
                    "name TEXT NOT NULL PRIMARY KEY,"+
                    "text TEXT NOT NULL,"+
                    "last INTEGER DEFAULT 0"+
                    ");");
            statement.execute();
            return true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean run() {
        try {
            if(!this.readProperties()) return false;
            if(this.connection == null){
                if(!this.createConnection()) return false;
            }

            long lastModified = Long.parseLong(this.properties.getProperty("last-modified"));
            long interval = Long.parseLong(this.properties.getProperty("no-repeat-interval"));
            long current = Long.parseLong(this.properties.getProperty("no-repeat-current"));

            // Update table
            if(this.imagesFolder.lastModified() != lastModified){
                System.out.println("Change in image folder detected! Updating...");
                PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM images;");
                ResultSet result = statement.executeQuery();

                List<String> namesOnDatabase = new LinkedList<>();
                while (result.next()) {
                    namesOnDatabase.add(result.getString(1));
                }
                result.close(); statement.close();

                List<String> namesOnFolder = Arrays.asList(Objects.requireNonNull(this.imagesFolder.list()));

                List<String> newNames = new ArrayList<>(namesOnFolder);
                newNames.removeAll(namesOnDatabase);

                newNames.forEach((name) -> {
                    try {
                        PreparedStatement st = this.connection.prepareStatement(
                                "INSERT INTO images VALUES (?, '', 0);");
                        st.setString(1, name);
                        st.execute();
                        st.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });

                List<String> removeNames = new ArrayList<>(namesOnDatabase);
                removeNames.removeAll(namesOnFolder);

                removeNames.forEach((name) -> {

                    try {
                        PreparedStatement st = this.connection.prepareStatement(
                                "DELETE FROM images WHERE name=?;");
                        st.setString(1, name);
                        st.execute();
                        st.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });

                this.properties.put("last-modified", ""+this.imagesFolder.lastModified());
            }

            // Get random element from table
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM images WHERE last<=? OR last=0 ORDER BY RANDOM() LIMIT 1;");
            statement.setLong(1, current-interval);
            ResultSet resultSet = statement.executeQuery();
            String name = resultSet.getString(1);
            this.text = resultSet.getString(2);
            this.image = new File(this.imagesFolder, name);
            if(!this.image.exists()) return false;

            this.properties.put("no-repeat-current", ""+(current+1));
            this.properties.store(new FileOutputStream(propertiesFile), null);

            PreparedStatement updateSt = this.connection.prepareStatement(
                    "UPDATE images SET last=? WHERE name=?");
            updateSt.setLong(1, current-interval);
            updateSt.setString(2, name);
            updateSt.executeUpdate();
            resultSet.close(); statement.close();
            return true;
        } catch (IOException | SQLException e) {
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
            this.connection.close();
            this.connection = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean createConnection(){
        if(!this.base.exists() || this.connection != null) return false;
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+this.base.getPath()+"/data.db");
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
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

}