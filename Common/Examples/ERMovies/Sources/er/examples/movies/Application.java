//
// Application.java
// Project ERMovies
//
// Created by max on Thu Feb 27 2003
//
package er.examples.movies;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import er.extensions.ERXApplication;
import er.extensions.ERXNavigationManager;

public class Application extends ERXApplication {
    
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        super();
        
        initializeDatabase();

        System.out.println("Welcome to " + this.name() + "!");
    }

    public void finishInitialization() {
        super.finishInitialization();
        ERXNavigationManager.manager().configureNavigation();
    }

    public void initializeDatabase() {
    	
    	try {
			Class foundDriver = Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		java.sql.Connection conn = null;
		java.sql.Statement s = null;
    	try {
			conn = java.sql.DriverManager.getConnection("jdbc:derby:ERMovies;create=true");
			s = conn.createStatement();
			System.out.println("Creating connection and statement...");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (s != null) {
			
			boolean needCreate = false;

			try {
				java.sql.ResultSet rs = s.executeQuery("SELECT * FROM movie");
				System.out.println("Derby Database: Tables exist.");
			} catch (SQLException e1) {
				needCreate = true;
			}
			
			if (needCreate) {
				try {
					java.io.FileReader fRdr = new java.io.FileReader("Contents/Resources/database.sql");
					java.io.LineNumberReader rdr = new java.io.LineNumberReader(fRdr);
					String line = ""; String execLine = "";
					while (line != null) {
						line = rdr.readLine();
						if ((line != null) && (!line.equals("")) && (!line.startsWith("--")))
							s.execute(line);
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

    	try {
    		s.close();
			conn.close();
			System.out.println("...and closed connection.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
