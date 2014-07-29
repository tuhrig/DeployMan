/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.repo;

import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class Repository implements IRepository {
  private LocaleRepository locale = new LocaleRepository();
  private RemoteRepository remote = new RemoteRepository();

  @Override
  public void init() {
    console.write("Initialize repository (locale and remote)"); //$NON-NLS-1$
    this.remote.init();
    this.locale.init();
    console.newLine();
  }

  public void printInfo(String location) {
    if (location.equals("locale")) //$NON-NLS-1$
      this.locale.printInfo();

    else if (location.equals("remote")) //$NON-NLS-1$
      this.remote.printInfo();

    else if (location.equals("formations")) //$NON-NLS-1$
      new FormationRepository().printLocaleObjects();

    else if (location.equals("setups")) //$NON-NLS-1$
      new SetupRepository().printLocaleObjects();

    else {
      console.write("Use either 'locale' or 'remote' or 'formations' or 'setups'"); //$NON-NLS-1$
      console.newLine();
    }

  }

  @Override
  public boolean exists() {
    return this.locale.exists() && this.remote.exists();
  }

  @Override
  public void printInfo() {
    this.locale.printInfo();
    this.remote.printInfo();
  }
}
