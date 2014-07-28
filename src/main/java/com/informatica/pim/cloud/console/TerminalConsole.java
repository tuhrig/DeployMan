/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.console;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.spec.IASCIITableAware;

/**
 * @author tuhrig
 */
public class TerminalConsole implements IConsole {
  private boolean logging;

  public TerminalConsole() {
    this(true);
  }

  public TerminalConsole(boolean logging) {
    this.logging = logging;
  }

  @Override
  public void write(Object message) {
    if (this.logging)
      System.out.println(message);
  }

  @Override
  public void writeNl(String message) {
    write(message);
    newLine();
  }

  @Override
  public void newLine() {
    if (this.logging)
      System.out.println();
  }

  @Override
  public void printTable(IASCIITableAware tableModel) {
    if (this.logging)
      ASCIITable.getInstance().printTable(tableModel);
  }

  @Override
  public void exception(Exception e, String message) {
    write(message);
    e.printStackTrace();
    newLine();
  }
}
