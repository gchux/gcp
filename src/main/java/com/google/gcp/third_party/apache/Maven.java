package com.google.gcp.third_party.apache;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.wrapper.WrapperExecutor;
import org.apache.maven.wrapper.Installer;
import org.apache.maven.wrapper.BootstrapMainStarter;
import org.apache.maven.wrapper.HashAlgorithmVerifier;
import org.apache.maven.wrapper.DefaultDownloader;
import org.apache.maven.wrapper.PathAssembler;

import com.google.common.collect.ObjectArrays;

import com.google.gcp.utils.Scripts;

public class Maven {

  public static void cleanPackage(final Path pom) {
    execute(pom, new String[]{ "clean", "package" });
  }

  public static void execute(final Path pom, final List<String> goals) {
    execute(pom, goals.stream().toArray(String[]::new));
  }

  public static void execute(final Path pom, final String[] goals) {
    final String baseDir = pom.getParent().toString(); 
    final String dotMvnDir = baseDir + "/.mvn";
    final String mvnUserHome = dotMvnDir + "/home";

    final Path mvnUserHomePath = Paths.get(mvnUserHome);
    final Path mvnWrapperProperties = 
      Paths.get(dotMvnDir + "/wrapper/maven-wrapper.properties");

    System.setProperty("maven.multiModuleProjectDirectory", baseDir);
    System.setProperty("maven.user.home", mvnUserHome);

    String[] mvnArgs = new String[] { "-f", pom.toString() };
    mvnArgs = ObjectArrays.concat(mvnArgs, goals, String.class);

    final String mvnWrapperVersion = 
      Scripts.getConfig().getString("tools.maven.wrapper.version");

    try {
      WrapperExecutor
        .forWrapperPropertiesFile(mvnWrapperProperties)
        .execute(mvnArgs, new Installer(
                  new DefaultDownloader("mvnw", mvnWrapperVersion),
                  new HashAlgorithmVerifier(),
                  new PathAssembler(mvnUserHomePath)),
          new BootstrapMainStarter());
    } catch(Exception e) {
      e.printStackTrace(System.err);
    }
  }    

}
