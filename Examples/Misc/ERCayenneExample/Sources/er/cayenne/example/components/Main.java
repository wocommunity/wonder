package er.cayenne.example.components;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

import com.webobjects.appserver.WOContext;

import er.cayenne.CayenneApplication;
import er.cayenne.example.persistent.Artist;
import er.cayenne.example.persistent.Gallery;
import er.cayenne.example.persistent.Painting;
import er.extensions.components.ERXComponent;

public class Main extends ERXComponent {
	
	private ObjectContext objContext;
	public Artist picasso;
	public Painting painting;
	
	public Main(WOContext context) {
		super(context);

		objContext = application().newObjectContext();

		newObjectsTutorial();
		selectTutorial();
		//deleteTutorial();
	}

	@Override
	public CayenneApplication application() {
		return (CayenneApplication)super.application();
	}
	
	private void newObjectsTutorial() {
		// creating new Artist
		picasso = objContext.newObject(Artist.class);
		picasso.setName("Pablo Picasso");
		picasso.setDateOfBirthString("18811025");

		// Creating other objects
		Gallery metropolitan = objContext.newObject(Gallery.class);
		metropolitan.setName("Metropolitan Museum of Art");

		Painting girl = objContext.newObject(Painting.class);
		girl.setName("Girl Reading at a Table");

		Painting stein = objContext.newObject(Painting.class);
		stein.setName("Gertrude Stein");

		// connecting objects together via relationships
		picasso.addToPaintings(girl);
		picasso.addToPaintings(stein);

		girl.setGallery(metropolitan);
		stein.setGallery(metropolitan);

		// saving all the changes above
		objContext.commitChanges();
	}

	@SuppressWarnings("unused")
	private void selectTutorial() {
		// SelectQuery examples
		SelectQuery select1 = new SelectQuery(Painting.class);
		List<Painting> paintings1 = objContext.performQuery(select1);

		Expression qualifier2 = ExpressionFactory.likeIgnoreCaseExp(
				Painting.NAME_KEY, "gi%");
		SelectQuery select2 = new SelectQuery(Painting.class, qualifier2);
		List<Painting> paintings2 = objContext.performQuery(select2);

		Calendar c = new GregorianCalendar();
		c.set(c.get(Calendar.YEAR) - 100, 0, 1, 0, 0, 0);

		Expression qualifier3 = Expression
				.fromString("artist.dateOfBirth < $date");
		qualifier3 = qualifier3.expWithParameters(Collections.singletonMap(
				"date", c.getTime()));
		SelectQuery select3 = new SelectQuery(Painting.class, qualifier3);
		List<Painting> paintings3 = objContext.performQuery(select3);
	}

	private void deleteTutorial() {
		// Delete object examples
		Expression qualifier = ExpressionFactory.matchExp(Artist.NAME_KEY,
				"Pablo Picasso");
		SelectQuery selectToDelete = new SelectQuery(Artist.class, qualifier);
		Artist picasso = (Artist) Cayenne.objectForQuery(objContext,
				selectToDelete);

		if (picasso != null) {
			objContext.deleteObjects(picasso);
			objContext.commitChanges();
		}
	}
	
}
