import com.google.gcp.scripts.AbstractScript;

// v0.0.2
public class v1 extends AbstractScript {

  public void bootstrap() {
    initialize(com.google.gcp.GCP.get());
    exec();
  }

}
