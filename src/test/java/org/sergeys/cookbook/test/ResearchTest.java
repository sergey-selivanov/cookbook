package org.sergeys.cookbook.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class ResearchTest {

    @Test
    void testWinVersion() {
        String ver = "0.0.5-dev.0.11+20210113T101703Z";
        String fix = ver.replaceAll("-dev.*", ".10113");
        System.out.println(fix);

        ver = "0.0.6";
        fix = ver.replaceAll("-dev.*", ".10113");
        System.out.println(fix);


        System.out.println(System.getProperty("user.home"));

        Properties p = new Properties();
        p.put("test", "{{HOME_DIR}}");

        //System.out.println(p.getProperty("test"));

        p.forEach((k, v) -> {
            System.out.println(v);
            //p.replace(k, v.toString().replaceFirst("\\{\\{HOME_DIR\\}\\}", System.getProperty("user.home")));
            //p.replace(k, v.toString().replaceFirst("\\{\\{HOME_DIR\\}\\}", "ololo"));
            p.replace(k, v.toString().replace("{{HOME_DIR}}", System.getProperty("user.home")));
            System.out.println(p.getProperty((String) k));
        });
    }

}
