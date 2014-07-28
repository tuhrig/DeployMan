/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch;

import java.util.UUID;

import com.informatica.pim.cloud.console.Console;
import com.informatica.pim.cloud.console.StringConsole;
import com.informatica.pim.cloud.launch.formation.Formation;

/**
 * @author tuhrig
 */
public class LaunchJob {
  private final String id;

  private Console console = new Console(new StringConsole());

  private Formation formation;

  public LaunchJob() {
    this.id = UUID.randomUUID().toString();
  }

  public String getId() {
    return this.id;
  }

  public void launch(String file) {
    this.setFormation(Formation.read(file));

    new Thread() {
      @Override
      public void run() {
        new Launcher(LaunchJob.this.console).run(file);
      }
    }.start();
  }

  public Formation getFormation() {
    return this.formation;
  }

  public void setFormation(Formation formation) {
    this.formation = formation;
  }

}
