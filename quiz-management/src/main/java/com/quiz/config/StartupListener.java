package com.quiz.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Component
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = event.getApplicationContext().getEnvironment().getProperty("local.server.port");
        if (port == null) {
            port = "8080";
        }
        System.out.println("\n==================================================================");
        System.out.println("  QuizMaster Application is running successfully!");
        System.out.println("  Access URLs from your device and other devices on your LAN:");
        System.out.println("  - Local Host: http://localhost:" + port + "/");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.getHostAddress().contains(":")) { // skip IPv6
                        continue;
                    }
                    System.out.println("  - Local LAN IP (" + networkInterface.getDisplayName() + "): http://" + address.getHostAddress() + ":" + port + "/");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        System.out.println("==================================================================\n");
    }
}
