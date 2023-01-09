package dev.karina;

import java.io.FileInputStream;
import java.util.Properties;

public class Manipular {
    public static Properties getProp() {
        Properties props = new Properties();

        try (FileInputStream file = new FileInputStream("src/main/properties/login.properties")) {
            props.load(file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return props;
    }

}
