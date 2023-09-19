package com.google.gcp.scripts;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;

class ScriptsBootstrapModule extends AbstractModule {

  private Config config;

  public ScriptsBootstrapModule(final Config config) {
    this.config = config;
  }

  protected Config getConfig() {
    return this.config;
  }

  @Override
  protected void configure() {

    install(new com.google.gcp.providers.git.jgit.v1.ProviderModule(this.config));
    // install(new com.google.gcp.providers.apache.maven.v1.ProviderModule(this.config));

  }


}
