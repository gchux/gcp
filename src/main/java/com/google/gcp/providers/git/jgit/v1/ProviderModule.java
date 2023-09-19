package com.google.gcp.providers.git.jgit.v1;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.name.Named;
import com.google.inject.multibindings.MapBinder;
import com.google.gcp.providers.ProviderAction;
import com.google.gcp.providers.ScriptsProviderModule;
import com.google.gcp.providers.ScriptServiceProvider;

import com.google.gcp.providers.git.jgit.v1.actions.CloneSshRepository;

import com.typesafe.config.Config;
import java.util.Map;

public class ProviderModule extends ScriptsProviderModule {

  public static final String PROVIDER = "git.jgit.v1";
  public static final String ACTIONS = PROVIDER + ScriptServiceProvider.ACTIONS;

  public ProviderModule(final Config config) {
    super(config);
  }

  @Override
  protected void configure() { 

    final MapBinder<String, ProviderAction> actions = getActionsBinder(ACTIONS);

    actions.addBinding(
        ProviderAction.getKey(ACTIONS, CloneSshRepository.NAME))
      .to(CloneSshRepository.class).in(Scopes.SINGLETON);

    registerProvider(PROVIDER);
  }

  @Provides @Singleton @Named(PROVIDER)
  ScriptServiceProvider provideScriptServiceProvider(
      @Named(ACTIONS) Map<String, ProviderAction> actions) {
      return new ScriptServiceProvider(PROVIDER, getConfig(), actions);
  }

}
