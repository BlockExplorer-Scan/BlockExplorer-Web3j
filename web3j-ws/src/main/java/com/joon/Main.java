package com.joon;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

    public static void main(String[] args) {

        try {
            context.getBean(WebsocketStarter.class).start();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

}
