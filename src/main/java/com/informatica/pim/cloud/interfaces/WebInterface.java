/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.interfaces;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import spark.Request;
import spark.Response;
import spark.Route;

import com.google.gson.Gson;
import com.informatica.pim.cloud.DeployMan;
import com.informatica.pim.cloud.aws.Ec2;
import com.informatica.pim.cloud.aws.Rds;
import com.informatica.pim.cloud.docker.DockerRemoteClient;
import com.informatica.pim.cloud.health.MachineStatus;
import com.informatica.pim.cloud.launch.CloudInitScript;
import com.informatica.pim.cloud.launch.LaunchJob;
import com.informatica.pim.cloud.launch.LaunchJobStore;
import com.informatica.pim.cloud.repo.ConfigRepository;
import com.informatica.pim.cloud.repo.FormationRepository;
import com.informatica.pim.cloud.repo.ImageRepository;
import com.informatica.pim.cloud.repo.SetupRepository;
import com.qmetric.spark.authentication.AuthenticationDetails;
import com.qmetric.spark.authentication.BasicAuthenticationFilter;

import static com.informatica.pim.cloud.DeployMan.*;
import static spark.Spark.*;

public class WebInterface // NO_UCD (unused code)
{
  /**
   * A simple functional interface to implement the call-backs executed by each route. This
   * interface is only used in this class and therefore locale to this class (and it's really small
   * ;).
   */
  private static interface IRoute {
    public Object handle(Request request, Response response);
  }

  private static final Logger log = Logger.getLogger(WebInterface.class.getName());
  private static final Gson gson = new Gson();
  private static final Map<String, String> routes = new HashMap<>();

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

    // @formatter:off


    //
    //
    // General
    //
    //

    get(route("/routes", "Returns all available routes.", (request, response) -> {

      return gson.toJson(routes);
    }));

    //
    //
    // Instances
    //
    //

    get(route("/instances", "Returns all instances.", (request, response) -> {

      return gson.toJson(new Ec2().getEC2Instances());
    }));

    get(route("/instances/:instance", "Returns detailed information about the EC2 instance.", (
        request, response) -> {

      String instanceId = request.params(":instance");
      return gson.toJson(MachineStatus.getMachineStatus(instanceId));
    }));

    get(route("/instances/:instance/kill", "Terminates the given instance.",
        (request, response) -> {

          String instanceId = request.params(":instance");
          new Ec2().terminateEC2InstanceById(instanceId);
          response.redirect("/#/instances-view");
          return "killing...";
        }));

    get(route("/databases", "Returns all databases.", (request, response) -> {

      return gson.toJson(new Rds().getDatabases());
    }));

    get(route("/inits", "Returns all previously created init logs.", (request, response) -> {

      return gson.toJson(new CloudInitScript("").getInitScripts());
    }));

    get(route("/inits/:name", "Returns the content of the given init log.",
        (request, response) -> {

          String name = request.params(":name");
          return gson.toJson(new CloudInitScript("").getInitScript(name));
        }));

    //
    //
    // Jobs
    //
    //

    get(route("/jobs", "Returns all jobs.", (request, response) -> {

      return gson.toJson(LaunchJobStore.getJobs());
    }));

    get(route("/jobs/:id", "Returns the given job.", (request, response) -> {

      String id = request.params(":id");
      return gson.toJson(LaunchJobStore.getJob(id));
    }));

    //
    //
    // Docker
    //
    //

    get(route("/docker/:instance/containers/:container",
        "Returns all Docker containers on the given instance.", (request, response) -> {

          String instanceId = request.params(":instance");
          String containerId = request.params(":container");
          return gson.toJson(new DockerRemoteClient().getContainer(instanceId, containerId));
        }));

    get(route("/docker/:instance/containers/:container/restart",
        "Restarts the given Docker container on the instance.", (request, response) -> {

          String instanceId = request.params(":instance");
          String containerId = request.params(":container");
          new DockerRemoteClient().stopContainer(instanceId, containerId);
          new DockerRemoteClient().startContainer(instanceId, containerId);
          response.redirect("/#/instance-view/" + instanceId);
          return "restarting...";
        }));

    get(route("/docker/:instance/containers/:container/stop",
        "Stops the given Docker container on the instance.", (request, response) -> {

          String instanceId = request.params(":instance");
          String containerId = request.params(":container");
          new DockerRemoteClient().stopContainer(instanceId, containerId);
          response.redirect("/#/instance-view/" + instanceId);
          return "stopping...";
        }));

    get(route("/docker/:instance/containers/:container/remove",
        "Removes the given Docker container on the instance.", (request, response) -> {

          String instanceId = request.params(":instance");
          String containerId = request.params(":container");
          new DockerRemoteClient().removeContainer(instanceId, containerId);
          response.redirect("/#/instance-view/" + instanceId);
          return "removing...";
        }));

    get(route("/docker/:instance/containers/:container/start",
        "Starts the given Docker container on the instance.", (request, response) -> {

          String instanceId = request.params(":instance");
          String containerId = request.params(":container");
          new DockerRemoteClient().startContainer(instanceId, containerId);
          response.redirect("/#/instance-view/" + instanceId);
          return "starting...";
        }));

    get(route("/docker/:instance/containers/:container/log",
        "Returns the log of the given Docker container on the instance.", (request, response) -> {

          String instanceId = request.params(":instance");
          String containerId = request.params(":container");
          return new DockerRemoteClient().getLogOfContainer(instanceId, containerId);
        }));

    //
    //
    // Repo
    //
    //

    get(route("/repo/locale/settings", "Returns all locale settings.", (request, response) -> {

      return gson.toJson(DeployMan.readUserProperties().entrySet());
    }));

    get(route("/repo/locale/setups", "Returns information about all locale setups.", (request,
        response) -> {

      return gson.toJson(new SetupRepository().getLocaleFiles());
    }));

    get(route("/repo/locale/formations", "Returns all locale formation files.",
        (request, response) -> {

          return gson.toJson(new FormationRepository().getFormations());
        }));

    get(route("/repo/locale/formations/:file", "Returns the given locale formation file.", (
        request, response) -> {

      String file = request.params(":file");
      return gson.toJson(new FormationRepository().getFormationByFileName(file));
    }));

    get(route("/repo/locale/formations/:file/run", "Runs the given locale formation file.", (
        request, response) -> {

      String file = request.params(":file");

      LaunchJob launchJob = LaunchJobStore.createNewJob();
      launchJob.launch(file);

      // response.redirect("/#/jobs-view/" + launchJob.getId());
        response.redirect("/#/jobs-view");
        return "launching...";
      }));

    get(route("/repo/remote/configs", "Returns all configurations in the S3 bucket.", (request,
        response) -> {

      return gson.toJson(new ConfigRepository().getRemoteConfigsWithUrl());
    }));

    get(route("/repo/remote/images", "Returns all images in the S3 bucket.",
        (request, response) -> {

          return gson.toJson(new ImageRepository().getRemoteImagesWithUrl());
        }));

    // @formatter:on
  }

  private static Route route(String route, String description, IRoute handler) {
    log.info("create route " + route);
    Route newRoute = new Route(route) {
      @Override
      public Object handle(Request request, Response response) {
        log.info("call route '" + route + "'");
        return handler.handle(request, response);
      }
    };

    routes.put(route, description);
    return newRoute;
  }
}
