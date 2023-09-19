package com.google.gcp.providers;

import com.typesafe.config.Config;
import java.util.function.Consumer;
import com.google.common.base.Joiner;

public abstract class ProviderAction implements Consumer<Config> { 

  public static String getKey(final String prefix, final String name) {
    return Joiner.on('/').skipNulls().join(prefix, name);
  }

}
