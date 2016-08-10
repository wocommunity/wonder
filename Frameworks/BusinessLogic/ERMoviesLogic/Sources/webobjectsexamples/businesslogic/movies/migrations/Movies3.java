package webobjectsexamples.businesslogic.movies.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class Movies3 extends ERXMigrationDatabase.Migration {
	@Override
	public NSArray<ERXModelVersion> modelDependencies() {
		return null;
	}
  
	@Override
	public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		// DO NOTHING
	}

	@Override
	public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		ERXMigrationTable talentPhotoTable = database.existingTableNamed("talentphoto");
		ERXMigrationTable directorTable = database.existingTableNamed("director");
		ERXMigrationTable votingTable = database.existingTableNamed("voting");
		ERXMigrationTable movieTable = database.existingTableNamed("movie");
		ERXMigrationTable plotSummaryTable = database.existingTableNamed("PlotSummary");
		ERXMigrationTable talentTable = database.existingTableNamed("talent");
		ERXMigrationTable movieRoleTable = database.existingTableNamed("movierole");
		ERXMigrationTable reviewTable = database.existingTableNamed("review");
		ERXMigrationTable userTable = database.existingTableNamed("rentalsuser");
		ERXMigrationTable rentalTable = database.existingTableNamed("rental");
		ERXMigrationTable feeTable = database.existingTableNamed("fee");
		ERXMigrationTable unitTable = database.existingTableNamed("unit");
		ERXMigrationTable videoTable = database.existingTableNamed("video");
		ERXMigrationTable customerTable = database.existingTableNamed("customer");
		ERXMigrationTable creditCardTable = database.existingTableNamed("creditcard");
		
		userTable.addForeignKey("CUSTOMER_ID", "customer", "CUSTOMER_ID");
		rentalTable.addForeignKey("CUSTOMER_ID", "customer", "CUSTOMER_ID");
		rentalTable.addForeignKey("UNIT_ID", "unit", "UNIT_ID");
		feeTable.addForeignKey("FEE_TYPE_ID", "feetype", "FEE_TYPE_ID");
		feeTable.addForeignKey("RENTAL_ID", "rental", "RENTAL_ID");
		//unitTable.addForeignKey("VIDEO_ID", "video", "VIDEO_ID");
		videoTable.addForeignKey("MOVIE_ID", "movie", "MOVIE_ID");
		videoTable.addForeignKey("RENTAL_TERMS_ID", "rentalterms", "RENTAL_TERMS_ID");
		customerTable.addForeignKey("CUSTOMER_ID", "creditcard", "CUSTOMER_ID");
		creditCardTable.addForeignKey("CUSTOMER_ID", "customer", "CUSTOMER_ID");
		
		//talentPhotoTable.addForeignKey("TALENT_ID", "talent", "TALENT_ID");
		directorTable.addForeignKey("MOVIE_ID", "movie", "MOVIE_ID");
		//directorTable.addForeignKey("TALENT_ID", "talent", "TALENT_ID");
		votingTable.addForeignKey("MOVIE_ID", "movie", "MOVIE_ID");
		//movieTable.addForeignKey("MOVIE_ID", "plotsummary", "MOVIE_ID");
		//movieTable.addForeignKey("STUDIO_ID", "studio", "STUDIO_ID");
		//movieTable.addForeignKey("MOVIE_ID", "voting", "MOVIE_ID");
		//plotSummaryTable.addForeignKey("MOVIE_ID", "movie", "MOVIE_ID");
		//talentTable.addForeignKey("TALENT_ID", "talentphoto", "TALENT_ID");
		movieRoleTable.addForeignKey("MOVIE_ID", "movie", "MOVIE_ID");
		//movieRoleTable.addForeignKey("TALENT_ID", "talent", "TALENT_ID");
		reviewTable.addForeignKey("MOVIE_ID", "movie", "MOVIE_ID");
	}
}