package com.google.gcp.providers.git.jgit.v1.actions;

import com.google.gcp.providers.ProviderAction;
import com.typesafe.config.Config;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

public class CloneSshRepository extends ProviderAction {

  public static final String NAME = "clone_ssh_repository";

  @Override
  public void accept(Config args) {
    System.out.println(args);
    final Path target = Paths.get(args.getString("target"));
    final String repository = args.getString("repository");
    try {
      this.clone(repository, target).call();
    } catch(Exception e) {
      e.printStackTrace(System.err);
    }
  }

  private CloneCommand clone(final String repository, final Path target) {
    return Git.cloneRepository()
      .setURI(repository).setDirectory(target.toFile())
      .setTransportConfigCallback(getTransportConfigCallback());
  }

  private TransportConfigCallback getTransportConfigCallback() {
    final Path userHomePath = FS.DETECTED.userHome().toPath();
    final Path dotSshPath = userHomePath.resolve(".ssh").normalize();
    final SshdSessionFactory sshSessionFactory = 
      new SshdSessionFactoryBuilder()
        .setPreferredAuthentications("publickey")
        .setHomeDirectory(userHomePath.toFile())
        .setSshDirectory(dotSshPath.toFile())
        .build(null);
    return new SshTransportConfigCallback(sshSessionFactory);
  }

  private static class SshTransportConfigCallback implements TransportConfigCallback {
    private final SshdSessionFactory sshSessionFactory;
    SshTransportConfigCallback(SshdSessionFactory sshSessionFactory) {
      this.sshSessionFactory = sshSessionFactory;
    }
    @Override
    public void configure(Transport transport) {
      SshTransport sshTransport = (SshTransport) transport;
      sshTransport.setSshSessionFactory(sshSessionFactory);
    }
  }

}
