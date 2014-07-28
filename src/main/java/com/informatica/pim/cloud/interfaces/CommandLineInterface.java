/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.interfaces;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;

import com.informatica.pim.cloud.DeployMan;
import com.informatica.pim.cloud.aws.Aim;
import com.informatica.pim.cloud.aws.Ec2;
import com.informatica.pim.cloud.aws.Rds;
import com.informatica.pim.cloud.docker.DockerRemoteClient;
import com.informatica.pim.cloud.launch.FormationValidator;
import com.informatica.pim.cloud.launch.Launcher;
import com.informatica.pim.cloud.repo.ConfigRepository;
import com.informatica.pim.cloud.repo.FileMonitor;
import com.informatica.pim.cloud.repo.ImageRepository;
import com.informatica.pim.cloud.repo.Repository;
import com.informatica.pim.cloud.ssh.RemoteLog;
import com.informatica.pim.cloud.ssh.SshClient;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * This is the command line interface for this projekt. It contains a main-method to run the project
 * as a JAR-file from the command line. It create a command line parser with several options. This
 * parser is used to parse the command given to the main method and to call the appropriate actions
 * depending on the command. This class is the entry point to the application.
 * 
 * @author tuhrig
 */
public class CommandLineInterface // NO_UCD (unused code)
{
  /**
   * A simple functional interface to implement the call-backs executed by each command. This
   * interface is only used in this class and therefore locale to this class (and it's really small
   * ;).
   */
  private static interface Actor {
    public void act(CommandLine cmd);
  }

  private static final String OPT_INFO = "info";
  private static final String OPT_CONFIG = "config";
  private static final String OPT_HELP = "help";
  private static final String OPT_EXIT = "exit";
  private static final String OPT_SSH = "ssh";

  private static final String OPT_HEALTH = "health";
  private static final String OPT_INSTANCES = "instances";
  private static final String OPT_DATABASES = "databases";
  private static final String OPT_TERMINATE = "kill";
  private static final String OPT_RUN = "run";
  private static final String OPT_TAG = "tag";
  private static final String OPT_BIND = "bind";
  private static final String OPT_LOG = "log";
  private static final String OPT_VALIDATE = "validate";

  private static final String OPT_INIT = "init";
  private static final String OPT_REPO = "rp";
  private static final String OPT_FORMATIONS = "formations";

  private static final String OPT_MONITOR = "monitor";
  private static final String OPT_SYNC = "sync";
  private static final String OPT_UPLOAD_IMAGE = "upload_image";
  private static final String OPT_UPLOAD_CONFIG = "upload_config";

  private static final String OPT_CREATE_PROFILE = "create_profile";
  private static final String OPT_OPEN_PORT = "open_port";

  private static final String OPT_DOCKER_IMAGES = "docker_images";
  private static final String OPT_DOCKER_CONTAINERS = "docker_containers";
  private static final String OPT_DOCKER_INFO = "docker_info";

  private static CommandLine cmd;
  private static Options options = new Options();
  private static Parser parser = new BasicParser();
  private static Map<String, Actor> handlers = new HashMap<>();

  public static void main(String[] args) throws Exception {
    DeployMan.disableLogger();
    DeployMan.readSystemProperties();

    Properties props = new Properties();
    props.load(new FileInputStream("log4j.properties"));
    PropertyConfigurator.configure(props);

    // if we start the program without any command line
    // option, we start the interactive shell
    if (args.length == 0) {
      DeployMan.printTitle();
      openRepl();
    }
    // if we have command line arguments, we execute these
    // arguments directly (without printing the header to keep
    // everything as short as possible)
    else {
      initializeOptions();
      parse(args);
      act();
    }
  }

  private static void openRepl() throws IOException {
    // we need to initialize the options first to
    // enable the autocompletion which needs the
    // names of the options (to complete them)
    initializeOptions();

    PrintWriter out = new PrintWriter(System.out);
    ConsoleReader reader = new ConsoleReader();
    reader.setBellEnabled(false);
    reader.addCompleter(new StringsCompleter(handlers.keySet()));
    reader.setHistoryEnabled(true);

    do {
      try {
        String line = reader.readLine("$: ");
        out.flush();

        if (!line.startsWith(MINUS))
          line = MINUS + line;

        String[] args = line.split(BLANK);
        console.newLine();

        // writes the parsed arguments back to the console
        // this is somehow good for debugging but a little
        // bit much clutter
        // console.write( "<< " + Arrays.toString( args ) );
        // console.newLine();

        initializeOptions();
        parse(args);
        act();

      } catch (Exception e) {
        e.printStackTrace();
        console.newLine();
      }
    } while (true);
  }

  private static void act() throws Exception {
    // if we have not option, the user just pressed enter
    // and we display a new line just like in any other CMD
    if (cmd.getOptions().length == 0)
      return;

    handlers.get(cmd.getOptions()[0].getOpt()).act(cmd);
  }

  private static void parse(String[] args) throws ParseException {
    cmd = parser.parse(options, args);
  }

  @SuppressWarnings("nls")
  private static void initializeOptions() {
    // @formatter:off

    //
    //
    // GENERAL
    //
    //

    build(OPT_EXIT, "Exits the interactive shell.", cmd -> {

      shutdown();

    });

    build(OPT_INFO, "Displayes information about Deploy-Man.", cmd -> {

      DeployMan.printInfo();

    });

    build(OPT_CONFIG, "Configures Deploy-Man with a properties file.", cmd -> {

      String file = value(OPT_CONFIG);
      DeployMan.setUserPropertiesFile(file);
      DeployMan.saveSystemProperties();

    }, "file");

    build(OPT_HELP, "Displayes this text.", cmd -> {

      DeployMan.printHelp(options);

    });

    //
    //
    // REPO
    //
    //

    build(OPT_REPO, "Shows information about the repository.", cmd -> {

      String location = value(OPT_REPO);
      new Repository().printInfo(location);

    }, "location");

    build(OPT_FORMATIONS, "Shows all available formations.", cmd -> {

      new Repository().printInfo("formations");

    });

    build(OPT_INIT, "Creates a S3 bucket used to store images and configs.", cmd -> {

      new Repository().init();

    });

    //
    //
    // DOCKER
    //
    //

    build(OPT_DOCKER_IMAGES, "Displayes Docker images.", cmd -> {

      String instance = value(OPT_DOCKER_IMAGES);
      new DockerRemoteClient().printImages(instance);

    }, "instance");

    build(OPT_DOCKER_INFO, "Displayes Docker information.", cmd -> {

      String instance = value(OPT_DOCKER_INFO);
      new DockerRemoteClient().printInfo(instance);

    }, "instance");

    build(OPT_DOCKER_CONTAINERS, "Displayes Docker containers.", cmd -> {

      String instance = value(OPT_DOCKER_CONTAINERS);
      new DockerRemoteClient().printContainers(instance);

    }, "instance");

    //
    //
    // UPLOAD
    //
    //

    build(OPT_MONITOR, "Monitors the locale images or configs (including deletion!) to S3.",
        cmd -> {

          String folder = value(OPT_MONITOR);
          new FileMonitor(false).monitor(folder);

        }, "monitor");

    build(OPT_SYNC, "Syncs locale images or configs to S3.", cmd -> {

      String folder = value(OPT_SYNC);
      new FileMonitor().sync(folder);

    }, "folder");

    build(OPT_UPLOAD_IMAGE, "Uploads a image from the locale repository to the S3 bucket.",
        cmd -> {

          String file = value(OPT_UPLOAD_IMAGE);
          new ImageRepository().uploadLocaleFile(file);

        }, "file");

    build(OPT_UPLOAD_CONFIG, "Uploads a config from the locale repository to the S3 bucket.",
        cmd -> {

          String file = value(OPT_UPLOAD_CONFIG);
          new ConfigRepository().uploadLocaleFile(file);

        }, "file");

    //
    //
    // EC2
    //
    //

    build(OPT_INSTANCES, "Displayes AWS EC2 instances.", cmd -> {

      new Ec2().printEC2Instances();

    });

    build(OPT_TERMINATE, "Terminates an EC2 instance.", cmd -> {

      String id = value(OPT_TERMINATE);
      new Ec2().terminateEC2InstanceById(id);

    }, "instance");

    build(OPT_HEALTH, "Checks the health status of the instance.", cmd -> {

      String instance = value(OPT_HEALTH);
      console.printHealth(instance);

    }, "instance");

    build(OPT_LOG, "Prints the last log messages of the deployment log.", cmd -> {

      String instance = value(OPT_LOG);
      new RemoteLog().printLast10LinesOfDeployManLog(instance);

    }, "instance");

    build(OPT_TAG, "Tags an EC2 instance.", cmd -> {

      String id = value(OPT_TAG, 0);
      String tag = value(OPT_TAG, 1);
      new Ec2().tag(id, tag);

    }, "instance", "tag");

    build(OPT_CREATE_PROFILE, "Creates a profile to use AWS S3 in AWS EC2.", cmd -> {

      new Aim().createS3BucketProfile();

    });

    //
    //
    // SSH
    //
    //

    build(OPT_SSH, "Opens a SSH session with the given machine.", cmd -> {

      new SshClient().openSshShell(value(OPT_SSH));

    }, "instance");

    //
    //
    // PORT
    //
    //

    build(OPT_OPEN_PORT, "Opens the port on the given security group.", cmd -> {

      String[] params = values(OPT_OPEN_PORT);
      String group = params[0];
      int port = Integer.valueOf(params[1]);
      new Ec2().openPort(group, port);

    }, "group", "port");

    build(OPT_BIND, "Binds the instance to the Elastic IP.", cmd -> {

      String instance = value(OPT_BIND, 0);
      String ip = value(OPT_BIND, 1);
      new Ec2().associate(instance, ip);

    }, "instance", "ip");

    //
    //
    // DATABASES
    //
    //

    build(OPT_DATABASES, "Displayes AWS RDBs instances.", cmd -> {

      new Rds().printDatabases();

    });

    //
    //
    // LAUNCH
    //
    //

    build(OPT_RUN, "Runs a formation file.", cmd -> {

      String file = value(OPT_RUN);
      new Launcher().run(file);

    }, "file");

    build(OPT_VALIDATE, "Validates a formation file and prints messages.", cmd -> {

      String file = value(OPT_VALIDATE);
      for (String message : new FormationValidator().validate(file))
        console.write(message);
      console.newLine();

    }, "file");

    // @formatter:on
  }

  @SuppressWarnings("static-access")
  private static void build(String name, String description, Actor handler, String... options) {
    int args = options.length;

    String argNames = "";

    if (args == 1)
      argNames = options[0];
    else
      argNames = StringUtils.join(options, "> <");

    Option option =
        OptionBuilder.withArgName(argNames).hasArgs(args).withValueSeparator(' ')
            .withDescription(description).create(name);

    add(option, handler);
  }

  private static void shutdown() {
    console.write("Shutdown...");
    System.exit(0);
  }

  private static String value(String option, int index) {
    return values(option)[index];
  }

  private static String[] values(String option) {
    return cmd.getOptionValues(option);
  }

  private static String value(String option) {
    return cmd.getOptionValue(option);
  }

  /**
   * Add an Apache Commons CLI option and a handler object to call when the option occurs.
   */
  @SuppressWarnings("nls")
  private static void add(Option option, Actor handler) {
    // @formatter:off

    // decorate the actual handler with a handler
    // which tracks the execution time
    handlers.put(option.getOpt(), cmd -> {

      Date before = new Date();
      handler.act(cmd);
      Date after = new Date();

      console.write("[Done in " + (after.getTime() - before.getTime()) + "ms]");
      console.newLine();

    });

    options.addOption(option);

    // @formatter:on
  }
}
