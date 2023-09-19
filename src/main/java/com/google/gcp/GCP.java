package com.google.gcp;

import com.typesafe.config.*;

import com.google.gcp.utils.Log;

public class GCP {

  private static final ThreadLocal<Config> CONFIG =
    new ThreadLocal<Config>() {
      @Override protected Config initialValue() {
        Log.info("Creating default context for thread " + Thread.currentThread().getName());
        return ConfigFactory.empty("GCP");
      }
    };

  static public void set(Config config) {
    CONFIG.set(config);
  }

  static public Config get() {
    Config config = CONFIG.get();

    if (config == null) {
      throw new IllegalStateException("Config not bound. Did you forget to call "
          + GCP.class.getCanonicalName() + ".set(config)?");
    }

    return config;
  }

}
