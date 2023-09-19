package com.google.gcp.third_party.docker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fizzed.blaze.Systems;

import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;

import com.google.gcp.utils.Log;

public class Docker {

  private static final DockerClient client = DockerClientBuilder.getInstance().build();

  public static void buildImage(final Path dockerFile, final String imageTag) {
    final String imageId = client.buildImageCmd()
        .withDockerfile(dockerFile.toFile())
        .withTag(imageTag)
        .withQuiet(true)
        .withNoCache(true)
        .exec(new BuildImageResultCallback())
        .awaitImageId();
    Log.info("[DOCKER] – image_id: " + imageId);
  }

  public static void buildImageWithBuildKit(final Path dockerFile, final String imageTag) {
    Systems.exec("docker", "build")
      .args("-f", dockerFile.toString())
      .args("-t", imageTag)
      .arg(".")
      .pipeErrorToOutput(false)
      .run();
  }

  public static void pushImage(final String dockerRegistryUrl, 
      final String dockerRpositoryUrl, final String imageTag) {
    Systems.exec("docker", "push")
      .args(imageTag)
      .pipeErrorToOutput(false)
      .run();
  }

}
