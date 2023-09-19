import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fizzed.blaze.Contexts;
import com.typesafe.config.Config;

import com.google.gcp.utils.Scripts;
import com.google.gcp.utils.Log;
import com.google.gcp.third_party.apache.Maven;
import com.google.gcp.third_party.docker.Docker;

public class java_service {

  private static final com.fizzed.blaze.Config config = Contexts.config();

  public void exec_maven() {
    final Config taskConfig = Scripts.getConfigForTask("exec_maven");
    final String pom = taskConfig.getString("pom");
    final List<String> goals = taskConfig.getStringList("goals");
    Log.info("[exec_maven] – pom: " + pom);
    Log.info("[exec_maven] – goals: " + goals);
    Maven.execute(Paths.get(pom), goals);
  }

  public void exec_docker_build() {
    final Config taskConfig = Scripts.getConfigForTask("exec_docker_build");
    final Path dockerFile = Paths.get(taskConfig.getString("docker.file"));
    final String imageTag = taskConfig.getString("docker.image_tag");
    Docker.buildImageWithBuildKit(dockerFile, imageTag);
  }

  public void exec_docker_push() {
    final Config taskConfig = Scripts.getConfigForTask("exec_docker_push");
    final String imageTag = taskConfig.getString("docker.image_tag");
    final String dockerRegistryUrl = taskConfig.getString("docker.registry_url");
    final String dockerRepositoryUrl = taskConfig.getString("docker.repository_url");
    Docker.pushImage(dockerRegistryUrl, dockerRepositoryUrl, imageTag);
  }

}
