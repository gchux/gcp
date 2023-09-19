package com.google.gcp.utils;

import com.fizzed.blaze.Contexts;
import org.slf4j.Logger;

public class Log {

  private static final Logger LOG = Contexts.logger();

  public static void info(final String text) {
    LOG.info(text);
  }

}
