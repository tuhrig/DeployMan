/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.console;

import com.bethecoder.ascii_table.spec.IASCIITableAware;

/**
 * @author tuhrig
 */
public interface IConsole {
  public void write(Object message);

  public void writeNl(String message);

  public void newLine();

  public void printTable(IASCIITableAware tableModel);

  public void exception(Exception exception, String message);
}
