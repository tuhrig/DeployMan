/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.LogManager;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.informatica.pim.cloud.console.Console;
import com.informatica.pim.cloud.console.TerminalConsole;

public class DeployMan {
  public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

  public static final String TAB = "\t";
  public static final String BLANK = " ";
  public static final String NL = "\n";
  public static final String SLASH = "/";
  public static final String MINUS = "-";

  public static final String IMAGE_FOLDER = "images/";
  public static final String CONFIG_FOLDER = "configs/";
  public static final String FORMATION_FOLDER = "formations/";
  public static final String SETUP_FOLDER = "setups/";

  public static final String SYSTEM_USER_PROPERTIES = "configuration";
  public static final String SYSTEM_WEB_ROOT = "web.root";
  public static final String SYSTEM_WEB_USER = "web.user";
  public static final String SYSTEM_WEB_PASSWORD = "web.password";
  public static final String SYSTEM_SCRIPTS_FOLDER = "scripts.folder";

  private static Properties systemProperties;

  private static final String PROPERTIES_FILE = "./deployman.properties";

  public static final String EC2_INSTANCE_KEY = "ec2.instance.key";

  public static final String REPO_BUCKET = "repo.bucket";
  public static final String REPO_PROFILE = "repo.profile";
  public static final String REPO_ROLE = "repo.role";
  public static final String REPO_LOCALE = "repo.locale";

  public static final String AWS_REGION = "aws.region";

  public static final String SSH_KEY = "ssh.key";

  public static final Console console = new Console(new TerminalConsole());

  public static void saveSystemProperties() {
    try {
      FileWriter writer = new FileWriter(PROPERTIES_FILE);
      systemProperties.store(writer, "Set configuration file");
    } catch (IOException e) {
      console.exception(e, "Cannot save configuration file");
    }
  }

  public static void readSystemProperties() throws IOException {
    FileReader reader = new FileReader(PROPERTIES_FILE);
    systemProperties = new Properties();
    systemProperties.load(reader);
  }

  public static String getUserProperty(String key) {
    return readUserProperties().getProperty(key);
  }

  public static String getSystemProperty(String key) {
    return systemProperties.getProperty(key);
  }

  public static void setSystemProperty(String key, String value) {
    systemProperties.setProperty(key, value);
  }

  public static Properties readUserProperties() {
    try {
      FileReader reader = new FileReader(getSystemProperty(SYSTEM_USER_PROPERTIES));
      Properties properties = new Properties();
      properties.load(reader);
      return properties;
    } catch (IOException e) {
      console.exception(e, "Cannot read user properties.");
      return new Properties();
    }
  }

  public static void setUserPropertiesFile(String file) {
    setSystemProperty(SYSTEM_USER_PROPERTIES, file);
  }

  public static String getUserPropertiesFilePath() {
    return getSystemProperty(SYSTEM_USER_PROPERTIES);
  }

  public static File getUserProperitesFile() {
    return new File(getUserPropertiesFilePath());
  }

  public static void printInfo() {
    String userPropertiesFile = getUserPropertiesFilePath();

    console.write("Configuration: " + userPropertiesFile);

    if (userPropertiesFile != null && !userPropertiesFile.trim().equals(""))
      for (Entry<Object, Object> entry : readUserProperties().entrySet())
        console.write(TAB + entry.getKey() + ": " + entry.getValue());
    else
      console.write("Not configured!");

    console.newLine();
  }

  public static void printTitle() throws IOException {
    String title = new String(Files.readAllBytes(Paths.get("title.txt")));
    console.writeNl(title);
  }

  public static void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(800, "OPTION [ARGS]", "Help", options, "");
    console.newLine();
  }

  public static void disableLogger() {
    LogManager.getLogManager().reset();
  }

  public String getLocalHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "";
    }
  }
}
