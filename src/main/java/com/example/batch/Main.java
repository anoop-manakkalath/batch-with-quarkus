package com.example.batch;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main {
    void main(String... args) {
    	System.setProperty("jberet.executor.virtual-threads", "true");
        System.setProperty("smallrye.virtual-threads.enabled", "true");
        Quarkus.run(args);
    }
}
