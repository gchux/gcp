package com.google.gcp.providers;

import com.typesafe.config.Config;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ScriptServiceProvider implements Consumer<Config> {

  public static final String BINDINGS_KEY = "scripts_service_providers";
  public static final String ACTIONS = ":actions";

  private static final String KEY_ACTION = "action";
  private static final String KEY_ACTIONS = "actions";
  private static final String KEY_PROVIDES = "provides";
  private static final String KEY_PROVIDER = "provider";
  private static final String KEY_PROVIDERS = "providers";
  private static final String KEY_NAME = "name";
  private static final String KEY_ARGS = "args";

  private final String providerKey;
  private final Config config;
  private final Map<String, ProviderAction> actions;

  public static final String prefix(final String providerKey) {
    return providerKey + ACTIONS;
  }

  public ScriptServiceProvider(
      final String providerKey, final Config config, 
      final Map<String, ProviderAction> actions) {
    this.providerKey = providerKey;
    this.config = config;
    this.actions = actions;
  }

  @Override
  public void accept(final Config step) {
    final String providerKey = step.getString(KEY_PROVIDER);
    checkState(this.providerKey.equals(providerKey),
        "provider keys mismatch: '%s' != '%s' ", 
        this.providerKey, providerKey);
    this.exec(step.getConfigList(KEY_ACTIONS));
  }

  private void exec(final List<? extends Config> actions) {
    for(final Config action : actions) { 
      exec(action);
    }
  }

  private void exec(final Config action) {
    final String actionName = action.getString(KEY_NAME);
    checkArgument(isProviderOf(actionName), 
        "%s does not provide %s", 
        this.providerKey, actionName);
    final String prefix = ScriptServiceProvider.this.prefix(this.providerKey);
    doService(ProviderAction.getKey(prefix, actionName), action.getConfig(KEY_ARGS));
  }

  private boolean isProviderOf(final String actionName) {
    return this.config.getConfig(KEY_PROVIDERS)
      .getConfig(this.providerKey)
      .getStringList(KEY_PROVIDES)
      .contains(actionName);
  }

  private void doService(final String actionName, final Config actionArgs) {
    final Consumer<Config> action = 
      checkNotNull(this.actions.get(actionName), "unregistered action: %s", actionName);
    action.accept(actionArgs);
  } 

}
