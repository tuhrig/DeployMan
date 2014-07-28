/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.console;

import com.bethecoder.ascii_table.ASCIITable;
import com.bethecoder.ascii_table.spec.IASCIITableAware;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class StringConsole implements IConsole {
  private StringBuilder builder = new StringBuilder();

  @Override
  public void write(Object message) {
    this.builder.append(message + NL);
  }

  @Override
  public void writeNl(String message) {
    this.builder.append(message + NL);
    newLine();
  }

  @Override
  public void newLine() {
    this.builder.append(NL);
  }

  @Override
  public void printTable(IASCIITableAware tableModel) {
    this.builder.append(ASCIITable.getInstance().getTable(tableModel));
  }

  @Override
  public void exception(Exception exception, String message) {
    this.builder.append(message + NL);
    this.builder.append(exception.getMessage() + NL);
  }
}
