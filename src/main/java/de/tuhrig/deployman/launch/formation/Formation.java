/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.launch.formation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import de.tuhrig.deployman.repo.FormationRepository;
import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class Formation {
  @SerializedName("name")
  private String name;

  @SerializedName("description")
  private String description;

  @SerializedName("machine")
  private Machine machine;

  @SerializedName("database")
  private Database database;

  private File file;

  private String fileName;

  public String getName() {
    return this.name;
  }

  public Database getDatabase() {
    return this.database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Machine getMachine() {
    return this.machine;
  }

  public void setMachine(Machine machine) {
    this.machine = machine;
  }

  public File getFile() {
    return this.file;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFile(File formationFile) {
    this.file = formationFile;
    this.fileName = this.file.getName();
  }

  @Override
  public String toString() {
    return "Formation '" + this.name + "' for machine " + this.machine; //$NON-NLS-1$ //$NON-NLS-2$
  }

  public static Formation read(File formationFile) {
    try (FileReader br = new FileReader(formationFile)) {
      Gson gson = new Gson();
      Formation formation = gson.fromJson(br, Formation.class);
      formation.setFile(formationFile);
      return formation;
    } catch (IOException e) {
      console.write("Cannot reade formation " + formationFile); //$NON-NLS-1$
      e.printStackTrace();
    }

    return null;
  }

  public static Formation read(String formationFile) {
    // if the configuration file doesn't exist, we try to find it
    // in our local repository and assume it is not a full qualified path
    if (!new File(formationFile).exists())
      formationFile = new FormationRepository().getLocaleFolder() + SLASH + formationFile;
    return read(new File(formationFile));
  }

  public boolean hasDatabaseDefinition() {
    return this.database != null;
  }

  public boolean hasInstanceDefinition() {
    return this.machine != null;
  }

  public boolean hasAutoScalingDefinition() {
    return this.machine != null && this.machine.getScaling() != null;
  }
}
