package dev.yuafox.yuabot;

import dev.yuafox.yuabot.endpoints.Endpoint;
import dev.yuafox.yuabot.sources.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args){
        Map<String, List<String>> params = getParams(args);

        if(params == null) return;
        if(params.get("name") == null) return;

        System.out.println("""
                __     __          ____        _  \s
                \\ \\   / /         |  _ \\      | | \s
                 \\ \\_/ /   _  __ _| |_) | ___ | |_\s
                  \\   / | | |/ _` |  _ < / _ \\| __|
                   | || |_| | (_| | |_) | (_) | |_\s
                   |_| \\__,_|\\__,_|____/ \\___/ \\__|""".indent(1));


        YuaBot bot = new YuaBot(params.get("name").get(0));
        System.out.format("Bot name: %s\n", params.get("name").get(0));
        System.out.format("Version %s\n", "v1.1");

        if(params.get("setup") != null){
            for(String param : params.get("setup")) {
                try {
                    System.out.format("Configuring %s...\n", param);
                    Class<? extends Action> clazz = Class.forName(param).asSubclass(Action.class);
                    Action configurable = clazz.getConstructor().newInstance();
                    configurable.init(bot);
                    boolean done = configurable.setup();
                    if(done)
                        System.out.println("OK.");
                    else
                        System.err.println("Error!");
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        if(params.get("input") != null && params.get("output") != null){
            try {
                System.out.println("Loading...");

                String inputClazzName = params.get("input").get(0);
                String outputClazzName = params.get("output").get(0);
                DataSource input;
                Endpoint output;
                boolean ready = true;

                System.out.format("Input: %s\n", inputClazzName);
                System.out.format("Output: %s\n", outputClazzName);

                System.out.println("Preparing input...");
                Class<? extends DataSource> inputClazz = Class.forName(inputClazzName).asSubclass(DataSource.class);
                input = inputClazz.getConstructor().newInstance();
                input.init(bot);
                if(input.run()) System.out.println("OK.");
                else {
                    System.err.println("Input not ready!");
                    ready = false;
                }

                System.out.println("Preparing output...");
                Class<? extends Endpoint> outputClazz = Class.forName(outputClazzName).asSubclass(Endpoint.class);
                output = outputClazz.getConstructor().newInstance();
                output.init(bot);
                if(output.run()) System.out.println("OK.");
                else {
                    System.err.println("Output not ready!");
                    ready = false;
                }

                if(ready) {
                    System.out.println("Sending data...");
                    output.send(input);
                    System.out.println("OK.");
                }

                input.end();
                output.end();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Errors found! Action not completed.");
                System.exit(1);
            }
        }

        System.out.println("Done without errors.");
        System.exit(0);
    }


    public static Map<String, List<String>> getParams(String[] args){
        final Map<String, List<String>> params = new HashMap<>();

        List<String> options = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return null;
                }

                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            } else {
                System.err.println("Illegal parameter usage");
                return null;
            }
        }

        return params;
    }
}
