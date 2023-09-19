import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Systems;
import com.fizzed.blaze.Config;
import com.fizzed.blaze.Task;
import com.fizzed.blaze.local.LocalExec;
import com.fizzed.blaze.system.Exec;

import com.typesafe.config.*;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

// v0.0.5
public class main {
  
    private static final Config config = Contexts.config();
    private static final Path blazeDir = Contexts.baseDir(); 

    private static final String baseDir = config.value("base.dir").get();
    private static final String gcpDir = config.value("gcp.dir").get();
    private static final String gcpScenariosDir = config.value("gcp.scenarios.dir").get();

    private static final String gcpScriptsDir = config.value("gcp.scripts.dir").get();
    private static final String gcpScript = config.value("gcp.script").get();
    private static final String scenario = config.value("scenario").get();
    private static final String scenarioConf = scenario + ".conf";

    // private static final Map<String, String> execution = Splitter.on(':')
    //    .omitEmptyStrings().trimResults().withKeyValueSeparator('=').split(gcpScript);

    private static final String script = config.value("bootstrap.script").get();
    private static final String scriptConfigKey = ("script." + script).replaceAll("/", ".");
    private static final String task = config.value("bootstrap.task").get();

    private static final Path basePath = Paths.get(baseDir); 
    private static final Path gcpPath = Paths.get(gcpDir); 
    private static final Path gcpScriptsPath = Paths.get(gcpScriptsDir); 
    private static final Path gcpScenariosPath = Paths.get(gcpScenariosDir); 

    private static final Path gcpConfigPath = gcpPath.resolve("gcp.conf");
    private static final Path scenarioConfigPath = gcpScenariosPath.resolve(scenarioConf).normalize();

    private static final Path scriptConfigPath = gcpScriptsPath.resolve(script + ".conf").normalize();

    private static final com.typesafe.config.Config contextConfig =
        ConfigFactory.parseMap(ImmutableMap.<String, String>builder()
                .put("context.base_dir", baseDir)
                .put("context.gcp_dir", gcpDir)
                .put("context.scenarios_dir", gcpScenariosDir)
                 .put("context.gcp_script", script)
                 .put("context.gcp_script_config_key", scriptConfigKey)
                 .put("context.gcp_script_task", task)
                 .put("context.gcp_script_path", 
                     gcpScriptsPath.resolve(script).normalize().toString())
                .put("context.scenario", scenario)
                .put("context.scenario_file", scenarioConfigPath.toString())
                .build()
            );

    private static final com.typesafe.config.Config gcpConfig = 
        ConfigFactory.parseFile(gcpConfigPath.toFile()).withFallback(contextConfig);

    private static final com.typesafe.config.Config scriptConfig = 
        ConfigFactory.parseFile(scriptConfigPath.toFile()).withFallback(gcpConfig);

    private static final com.typesafe.config.Config scenarioConfig = 
        ConfigFactory.parseFile(scenarioConfigPath.toFile())
        .withFallback(scriptConfig).resolve();

    private static final com.typesafe.config.Config CONFIG = scenarioConfig;

    private static final com.typesafe.config.ConfigRenderOptions configRenderOptions =
        com.typesafe.config.ConfigRenderOptions.concise().setFormatted(true);

    @Task(order=0, value="prints effective build configuration")
    public void explain_config() {
        System.out.println(CONFIG.root().render());
    }

    @Task(order=1, value="prints effective build configuration")
    public void print_config() {
        System.out.println(CONFIG.root().render(configRenderOptions));
    }

    @Task(order=2, value="builds docker image")
    public void run() {
        final String dependencies = Joiner.on('|').join(CONFIG.getStringList("dependencies"));
        com.google.gcp.utils.Log.info("dependencies: " + dependencies);
        com.google.gcp.GCP.set(scenarioConfig);
        final Blaze blaze = 
            com.google.gcp.utils.Scripts.newBlaze(gcpScriptsPath, script, scenarioConfig);
        try {
            blaze.execute(task);
        } catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
}
