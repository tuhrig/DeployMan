/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import com.informatica.pim.cloud.repo.LocaleRepository;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class CloudInitScript {
  private String header = ""; //$NON-NLS-1$
  private String script = ""; //$NON-NLS-1$
  private Properties replacements = new Properties();
  private List<CloudInitScript> scripts = new ArrayList<>();

  public CloudInitScript(String header) {
    this.header = header;
  }

  public CloudInitScript withFile(String file) {
    try {
      String folder = getSystemProperty(SYSTEM_SCRIPTS_FOLDER);
      this.script += new String(Files.readAllBytes(Paths.get(folder + file)));
      this.script += NL;
      this.script += NL;
      return this;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public CloudInitScript withCommand(String command) {
    this.script += "echo 'starting command'" + NL; //$NON-NLS-1$
    this.script += command + NL;
    this.script += "echo 'started command'" + NL; //$NON-NLS-1$
    this.script += NL;
    return this;
  }

  public CloudInitScript withProperties(Properties properties) {
    Properties merged = new Properties();
    merged.putAll(this.replacements);
    merged.putAll(properties);
    this.replacements = merged;
    return this;
  }

  public CloudInitScript withScript(CloudInitScript otherScript) {
    this.scripts.add(otherScript);
    return this;
  }

  /**
   * Renders the CloudInit script. This method will replace all variables with their values and
   * return a string of the script. It will _first_ append all files and then append all other
   * scripts _afterwards_.
   */
  public String render() {
    String rendering = this.header + NL + this.script;

    for (Entry<Object, Object> replacement : this.replacements.entrySet()) {
      String key = "{{" + replacement.getKey() + "}}"; //$NON-NLS-1$ //$NON-NLS-2$
      String value = replacement.getValue().toString();

      rendering = rendering.replace(key, value);
    }

    for (CloudInitScript otherScript : this.scripts)
      rendering += NL + otherScript.render();

    return rendering.trim() + NL;
  }

  public String renderAsBase64() {
    return Base64.getEncoder().encodeToString(render().getBytes());
  }

  public void save() {
    String saveLocation = getSaveLocation();

    console.write("Save init script to " + saveLocation); //$NON-NLS-1$

    try (PrintWriter writer = new PrintWriter(saveLocation, "UTF-8")) //$NON-NLS-1$
    {
      writer.print(render());
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      console.write("Cannot save init script"); //$NON-NLS-1$
      e.printStackTrace();
    }
  }

  public List<File> getInitScripts() {
    String location = new LocaleRepository().getLocation();
    File folder = new File(location);
    List<File> initScripts = new ArrayList<>();
    for (File file : folder.listFiles()) {
      if (file.isFile())
        initScripts.add(file);
    }
    return initScripts;
  }

  private String getSaveLocation() {
    return getUserProperty(REPO_LOCALE) + "/init_script_" + sdf.format(new Date()) + ".sh"; //$NON-NLS-1$ //$NON-NLS-2$;
  }

  @Override
  public String toString() {
    return render();
  }

  public File getInitScript(String name) {
    for (File file : getInitScripts()) {
      if (file.getName().equals(name))
        return file;
    }
    return null;
  }
}
