//
// Application.java
// Project ERMovies
//
// Created by max on Thu Feb 27 2003
//
package er.examples.movies;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.navigation.ERXNavigationManager;

public class Application extends ERXApplication {

  public static void main(String argv[]) {
    ERXApplication.main(argv, Application.class);
  }

  public Application() {
    System.out.println("Welcome to " + name() + "!");
  }

  @Override
  public void finishInitialization() {
    super.finishInitialization();
    ERXNavigationManager.manager().configureNavigation();
  }
}
