package com.google.gcp.providers;

import com.google.gcp.providers.ProviderAction;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.multibindings.MapBinder;
import com.typesafe.config.Config;
import java.util.Map;


public abstract class ScriptsProviderModule extends AbstractModule {

  private final Config config;

  protected ScriptsProviderModule(final Config config) {
    this.config = config;
  }

  protected final Config getConfig() {
    return this.config;
  }

  protected void registerProvider(final String provider) {
    final MapBinder<String, ScriptServiceProvider> providers = 
      MapBinder.newMapBinder(binder(), 
          String.class, ScriptServiceProvider.class, 
          Names.named(ScriptServiceProvider.BINDINGS_KEY));
    final Key<ScriptServiceProvider> providerKey = 
      Key.get(ScriptServiceProvider.class, Names.named(provider));
    providers.addBinding(provider).to(providerKey).in(Scopes.SINGLETON);
  }

  protected MapBinder<String, ProviderAction> 
    getActionsBinder(final String actions) {
    return MapBinder.newMapBinder(binder(), 
        String.class, ProviderAction.class, Names.named(actions));
  }

}
