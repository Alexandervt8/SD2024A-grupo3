package com.mycompany.apac;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class Apac2 {
    public static void main(String[] args) {
        try (Ignite ignite = Ignition.start()) {
            System.out.println("Apache Ignite started successfully!");
        }
    }
}
