/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.interfaces;

import static de.tuhrig.deployman.DeployMan.SYSTEM_WEB_PASSWORD;
import static de.tuhrig.deployman.DeployMan.SYSTEM_WEB_ROOT;
import static de.tuhrig.deployman.DeployMan.SYSTEM_WEB_USER;
import static de.tuhrig.deployman.DeployMan.getSystemProperty;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.SparkBase.externalStaticFileLocation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.gson.Gson;
import com.qmetric.spark.authentication.AuthenticationDetails;
import com.qmetric.spark.authentication.BasicAuthenticationFilter;

import de.tuhrig.deployman.DeployMan;
import de.tuhrig.deployman.aws.Ec2;
import de.tuhrig.deployman.aws.Rds;
import de.tuhrig.deployman.docker.DockerRemoteClient;
import de.tuhrig.deployman.health.MachineStatus;
import de.tuhrig.deployman.launch.CloudInitScript;
import de.tuhrig.deployman.launch.LaunchJob;
import de.tuhrig.deployman.launch.LaunchJobStore;
import de.tuhrig.deployman.repo.ConfigRepository;
import de.tuhrig.deployman.repo.FormationRepository;
import de.tuhrig.deployman.repo.ImageRepository;
import de.tuhrig.deployman.repo.SetupRepository;

public class WebInterface // NO_UCD (unused code)
{
  private static final Logger log = Logger.getLogger(WebInterface.class.getName());
  private static final Gson gson = new Gson();

  @SuppressWarnings("nls")
  public static void main(String[] args) throws IOException {
    // load the initial config first
    DeployMan.readSystemProperties();

    org.apache.logging.julbridge.JULLog4jBridge.assimilate();

    Properties props = new Properties();
    props.load(new FileInputStream("log4j.properties"));
    PropertyConfigurator.configure(props);

    log.info("config end");

    // get and set the web root folder where the static
    // files (HTML, JS and CSS) are located
    String webRoot = DeployMan.getSystemProperty(SYSTEM_WEB_ROOT);
    log.info("Web root folder = " + webRoot);
    externalStaticFileLocation(webRoot);

    before(new BasicAuthenticationFilter("/*", new AuthenticationDetails(
        getSystemProperty(SYSTEM_WEB_USER), getSystemProperty(SYSTEM_WEB_PASSWORD))));

    //
    //
    // Instances
    //
    //

    get("/instances", (request, response) -> {

      return gson.toJson(new Ec2().getEC2Instances());
    });

    get("/instances/:instance", (request, response) -> {

      String instanceId = request.params(":instance");
      return gson.toJson(MachineStatus.getMachineStatus(instanceId));
    });

    get("/instances/:instance/kill", (request, response) -> {

      String instanceId = request.params(":instance");
      new Ec2().terminateEC2InstanceById(instanceId);
      response.redirect("/#/instances-view");
      return "killing...";
    });

    get("/databases", (request, response) -> {

      return gson.toJson(new Rds().getDatabases());
    });

    get("/inits", (request, response) -> {

      return gson.toJson(new CloudInitScript("").getInitScripts());
    });

    get("/inits/:name", (request, response) -> {

      String name = request.params(":name");
      return gson.toJson(new CloudInitScript("").getInitScript(name));
    });

    //
    //
    // Jobs
    //
    //

    get("/jobs", (request, response) -> {

      return gson.toJson(LaunchJobStore.getJobs());
    });

    get("/jobs/:id", (request, response) -> {

      String id = request.params(":id");
      return gson.toJson(LaunchJobStore.getJob(id));
    });

    //
    //
    // Docker
    //
    //

    get("/docker/:instance/containers/:container", (request, response) -> {

      String instanceId = request.params(":instance");
      String containerId = request.params(":container");
      return gson.toJson(new DockerRemoteClient().getContainer(instanceId, containerId));
    });

    get("/docker/:instance/containers/:container/restart", (request, response) -> {

      String instanceId = request.params(":instance");
      String containerId = request.params(":container");
      new DockerRemoteClient().stopContainer(instanceId, containerId);
      new DockerRemoteClient().startContainer(instanceId, containerId);
      response.redirect("/#/instance-view/" + instanceId);
      return "restarting...";
    });

    get("/docker/:instance/containers/:container/stop", (request, response) -> {

      String instanceId = request.params(":instance");
      String containerId = request.params(":container");
      new DockerRemoteClient().stopContainer(instanceId, containerId);
      response.redirect("/#/instance-view/" + instanceId);
      return "stopping...";
    });

    get("/docker/:instance/containers/:container/remove", (request, response) -> {

      String instanceId = request.params(":instance");
      String containerId = request.params(":container");
      new DockerRemoteClient().removeContainer(instanceId, containerId);
      response.redirect("/#/instance-view/" + instanceId);
      return "removing...";
    });

    get("/docker/:instance/containers/:container/start", (request, response) -> {

      String instanceId = request.params(":instance");
      String containerId = request.params(":container");
      new DockerRemoteClient().startContainer(instanceId, containerId);
      response.redirect("/#/instance-view/" + instanceId);
      return "starting...";
    });

    get("/docker/:instance/containers/:container/log", (request, response) -> {

      String instanceId = request.params(":instance");
      String containerId = request.params(":container");
      return new DockerRemoteClient().getLogOfContainer(instanceId, containerId);
    });

    //
    //
    // Repo
    //
    //

    get("/repo/locale/settings", (request, response) -> {

      return gson.toJson(DeployMan.readUserProperties().entrySet());
    });

    get("/repo/locale/setups", (request, response) -> {

      return gson.toJson(new SetupRepository().getLocaleFiles());
    });

    get("/repo/locale/formations", (request, response) -> {

      return gson.toJson(new FormationRepository().getFormations());
    });

    get("/repo/locale/formations/:file", (request, response) -> {

      String file = request.params(":file");
      return gson.toJson(new FormationRepository().getFormationByFileName(file));
    });

    get("/repo/locale/formations/:file/run", (request, response) -> {

      String file = request.params(":file");

      LaunchJob launchJob = LaunchJobStore.createNewJob();
      launchJob.launch(file);

      // response.redirect("/#/jobs-view/" + launchJob.getId());
        response.redirect("/#/jobs-view");
        return "launching...";
      });

    get("/repo/remote/configs", (request, response) -> {

      return gson.toJson(new ConfigRepository().getRemoteConfigsWithUrl());
    });

    get("/repo/remote/images", (request, response) -> {

      return gson.toJson(new ImageRepository().getRemoteImagesWithUrl());
    });
  }
}
