package com.google.gcp.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.core.DependencyResolver;
import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.core.ContextHolder;
import com.fizzed.blaze.internal.ConfigImpl;
import com.fizzed.blaze.internal.ContextImpl;

import com.typesafe.config.Config;

import com.google.gcp.GCP;

import com.google.common.collect.ImmutableList;

public class Scripts {

  public static Blaze newBlaze(final Path scriptPath, final String script, final Config config) {

    com.google.gcp.GCP.set(config);

    final Path scriptFilePath = scriptPath.resolve(script);
    final Path scriptDirPath = scriptFilePath.getParent();

    final ConfigImpl scriptConfig = new ConfigImpl(config);
    final ContextImpl scriptContext = 
      new ContextImpl(scriptDirPath, null, scriptFilePath, scriptConfig);

    // replace context with resolved script context
    ContextHolder.set(scriptContext);

    final Blaze.Builder blazeBuilder = 
      new Blaze.Builder().file(scriptFilePath).directory(scriptDirPath);

    // resolve script base dependencies
    blazeBuilder.resolveDependencies();

    // Blaze fails to return an immutable view of these lists, and so we can exploit it
    final List<Dependency> resolvedDependencies = blazeBuilder.getDependencies();
    final List<File> resolvedDependencyJarFiles = blazeBuilder.getDependencyJarFiles();

    final List<Dependency> dependencies = getDependencies(config);

    List<File> dependencyJarFiles = Collections.emptyList();

    final DependencyResolver dependencyResolver = blazeBuilder.getDependencyResolver();
    try {
      dependencyJarFiles = 
        dependencyResolver.resolve(scriptContext, resolvedDependencies, dependencies);
    } catch(Exception e) {
      e.printStackTrace(System.err);
    }

    if( !dependencyJarFiles.isEmpty() ) {
      //dependencyJarFiles.clear();
      resolvedDependencies.addAll(dependencies);
      resolvedDependencyJarFiles.addAll(dependencyJarFiles);
    }

    return blazeBuilder.build();
  }

  private static List<Dependency> getDependencies(final Config config) {
    final List<String> requiredDependencies = config.getStringList("dependencies");
    
    if( requiredDependencies == null || requiredDependencies.isEmpty()) {
        return Collections.emptyList();
    }
     
    final List<Dependency> dependencies = 
      new ArrayList<>(requiredDependencies.size());
    
    requiredDependencies.stream().forEach((dependency) -> {
        dependencies.add(Dependency.parse(dependency));
    });
    
    // this is what Blaze should have done
    return ImmutableList.copyOf(dependencies);
  }

  public static Config getConfig() {
    return GCP.get();
  }

  public static Config getConfigForTask(String task) {
    final Config config = getConfig();
    final String scriptConfigkey = config.getString("context.gcp_script_config_key");
    final Config scriptConfig = config.getConfig(scriptConfigkey);
    return scriptConfig.getConfig("tasks." + task);
  }

}
