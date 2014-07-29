/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.health;

import java.text.ParseException;
import java.util.Date;

import com.google.gson.Gson;

import de.tuhrig.deployman.DeployMan;
import de.tuhrig.deployman.launch.formation.Formation;
import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class MetaInfo {
  private Formation formation;
  private String host;
  private Date date;

  public MetaInfo(String log) {
    Gson gson = new Gson();
    String[] lines = log.split(NL);

    // get the formation json at the beginning of the log file
    // and parse it
    String rawJson = log.substring(log.indexOf("formation:") + 10, log.indexOf("--end") - 1); //$NON-NLS-1$ //$NON-NLS-2$
    this.formation = gson.fromJson(rawJson, Formation.class);

    for (String line : lines) {
      if (line.startsWith("host")) //$NON-NLS-1$
      {
        String host = line.split(":")[1]; //$NON-NLS-1$
        this.host = host.trim();
      }
      if (line.startsWith("timestamp")) //$NON-NLS-1$
      {
        try {
          String timestamp = line.split(":")[1]; //$NON-NLS-1$
          this.setDate(DeployMan.sdf.parse(timestamp));
        } catch (ParseException e) {
          // well...
        }
      }
    }
  }

  public String getHost() {
    return this.host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Formation getFormation() {
    return this.formation;
  }

  public void setFormation(Formation formation) {
    this.formation = formation;
  }

  @Override
  public String toString() {
    // @formatter:off
    return "Meta Information\n" //$NON-NLS-1$
        + "Host: " + this.host + "\n" //$NON-NLS-1$ //$NON-NLS-2$
        + "Formation: " + this.formation + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
    // @formatter:on
  }

  public Date getDate() {
    return this.date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
