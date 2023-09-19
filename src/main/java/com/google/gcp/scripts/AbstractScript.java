package com.google.gcp.scripts;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.google.gcp.providers.ScriptServiceProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractScript {

  private static final String KEY_PROVIDER = "provider";
  private static final String PROVIDER_NOT_FOUND = "provider '%s' not found: %s";

  private Config config;
  private Injector injector;
  private Map<String, ScriptServiceProvider> scriptServiceProviders;
  private Set<String> scriptServiceProvidersNames;

  @SuppressWarnings("unchecked")
  static <K, V> TypeLiteral<Map<K, V>> mapOf(Class<K> keyType, Class<V> valueType) {
      return (TypeLiteral<Map<K, V>>) TypeLiteral.get(Types.mapOf(keyType, valueType));
  }

  protected void initialize(final Config config) {
    this.config = config;
    this.injector = Guice
      .createInjector(new ScriptsBootstrapModule(config));
    final TypeLiteral<Map<String, ScriptServiceProvider>> providersMapType = 
      AbstractScript.this.mapOf(String.class, ScriptServiceProvider.class);
    final Key<Map<String, ScriptServiceProvider>> providersMapKey = 
      Key.get(providersMapType, Names.named(ScriptServiceProvider.BINDINGS_KEY));
    this.scriptServiceProviders = this.injector.getInstance(providersMapKey);
    this.scriptServiceProvidersNames = this.scriptServiceProviders.keySet();
  }

  protected void exec() {
    for(final Config step 
        : this.config.getConfigList("steps")) {
      execStep(step);
    }
  }

  private void execStep(final Config step) {
    final String provider = step.getString(KEY_PROVIDER);
    checkArgument(this.scriptServiceProviders.containsKey(provider),
      PROVIDER_NOT_FOUND, provider, this.scriptServiceProvidersNames);
    final ScriptServiceProvider scriptServiceProvider = 
      checkNotNull(this.scriptServiceProviders.get(provider), 
          PROVIDER_NOT_FOUND, provider, this.scriptServiceProvidersNames);
    scriptServiceProvider.accept(step);
  }

}
