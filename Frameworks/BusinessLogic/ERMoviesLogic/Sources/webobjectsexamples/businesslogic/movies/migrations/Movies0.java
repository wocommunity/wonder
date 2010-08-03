package webobjectsexamples.businesslogic.movies.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.jdbc.ERXJDBCUtilities;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class Movies0 extends ERXMigrationDatabase.Migration {
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
		
		ERXMigrationTable talentPhotoTable = database.newTableNamed("talentphoto");
		talentPhotoTable.newBlobColumn("PHOTO", true);
		talentPhotoTable.newIntegerColumn("TALENT_ID", false);
		talentPhotoTable.create();
	 	talentPhotoTable.setPrimaryKey("TALENT_ID");

		ERXMigrationTable directorTable = database.newTableNamed("director");
		directorTable.newIntegerColumn("MOVIE_ID", false);
		directorTable.newIntegerColumn("TALENT_ID", false);
		directorTable.create();
	 	directorTable.setPrimaryKey("MOVIE_ID", "TALENT_ID");

		ERXMigrationTable votingTable = database.newTableNamed("voting");
		votingTable.newIntegerColumn("MOVIE_ID", false);
		votingTable.newIntegerColumn("NUMBER_OF_VOTES", true);
		votingTable.newDoubleColumn("RUNNING_AVERAGE", 20, 9, true);
		votingTable.create();
	 	votingTable.setPrimaryKey("MOVIE_ID");

		ERXMigrationTable movieTable = database.newTableNamed("movie");
		movieTable.newStringColumn("CATEGORY", 20, true);
		movieTable.newTimestampColumn("DATE_RELEASED", true);
		movieTable.newIntegerColumn("MOVIE_ID", false);
		movieTable.newStringColumn("POSTER_NAME", 255, true);
		movieTable.newStringColumn("RATED", 10, true);
		movieTable.newBigDecimalColumn("REVENUE", 10, 2, true);
		movieTable.newIntegerColumn("STUDIO_ID", true);
		movieTable.newStringColumn("TITLE", 255, false);
		movieTable.newStringColumn("TRAILER_NAME", 255, true);
		movieTable.create();
	 	movieTable.setPrimaryKey("MOVIE_ID");

		ERXMigrationTable plotSummaryTable = database.newTableNamed("PlotSummary");
		plotSummaryTable.newIntegerColumn("MOVIE_ID", 9, false);
		plotSummaryTable.newStringColumn("SUMMARY", 10000000, true);
		plotSummaryTable.create();
	 	plotSummaryTable.setPrimaryKey("MOVIE_ID");

		ERXMigrationTable studioTable = database.newTableNamed("studio");
		studioTable.newBigDecimalColumn("BUDGET", 15, 2, false);
		studioTable.newStringColumn("NAME", 40, false);
		studioTable.newIntegerColumn("STUDIO_ID", false);
		studioTable.create();
	 	studioTable.setPrimaryKey("STUDIO_ID");

		ERXMigrationTable talentTable = database.newTableNamed("talent");
		talentTable.newStringColumn("FIRST_NAME", 20, false);
		talentTable.newStringColumn("LAST_NAME", 30, false);
		talentTable.newIntegerColumn("TALENT_ID", 9, false);
		talentTable.create();
	 	talentTable.setPrimaryKey("TALENT_ID");

		ERXMigrationTable movieRoleTable = database.newTableNamed("movierole");
		movieRoleTable.newIntegerColumn("MOVIE_ID", false);
		movieRoleTable.newStringColumn("ROLE_NAME", 30, true);
		movieRoleTable.newIntegerColumn("TALENT_ID", false);
		movieRoleTable.create();
	 	movieRoleTable.setPrimaryKey("TALENT_ID", "MOVIE_ID");

		ERXMigrationTable reviewTable = database.newTableNamed("review");
		reviewTable.newIntegerColumn("MOVIE_ID", false);
		reviewTable.newStringColumn("REVIEW", 10000000, true);
		reviewTable.newStringColumn("REVIEWER", 50, true);
		reviewTable.newIntegerColumn("REVIEW_ID", false);
		reviewTable.create();
	 	reviewTable.setPrimaryKey("REVIEW_ID");
	 	
		ERXMigrationTable userTable = database.newTableNamed("rentalsuser");
		userTable.newIntegerColumn("ACCESS_LEVEL", false);
		userTable.newIntegerColumn("CUSTOMER_ID", true);
		userTable.newStringColumn("PASSWORD", 20, false);
		userTable.newIntegerColumn("USER_ID", false);
		userTable.newStringColumn("LOGIN", 20, false);
		userTable.create();
	 	userTable.setPrimaryKey("USER_ID");

		ERXMigrationTable rentalTable = database.newTableNamed("rental");
		rentalTable.newIntegerColumn("CUSTOMER_ID", false);
		rentalTable.newTimestampColumn("DATE_OUT", false);
		rentalTable.newTimestampColumn("DATE_RETURNED", true);
		rentalTable.newIntegerColumn("RENTAL_ID", false);
		rentalTable.newIntegerColumn("UNIT_ID", false);
		rentalTable.create();
	 	rentalTable.setPrimaryKey("RENTAL_ID");

		ERXMigrationTable feeTable = database.newTableNamed("fee");
		feeTable.newBigDecimalColumn("AMOUNT", 10, 2, false);
		feeTable.newTimestampColumn("DATE_PAID", true);
		feeTable.newIntegerColumn("FEE_ID", false);
		feeTable.newIntegerColumn("FEE_TYPE_ID", false);
		feeTable.newIntegerColumn("RENTAL_ID", false);
		feeTable.create();
	 	feeTable.setPrimaryKey("FEE_ID");

		ERXMigrationTable unitTable = database.newTableNamed("unit");
		unitTable.newTimestampColumn("DATE_ACQUIRED", false);
		unitTable.newStringColumn("NOTES", 100, true);
		unitTable.newIntegerColumn("UNIT_ID", false);
		unitTable.newIntegerColumn("VIDEO_ID", false);
		unitTable.create();
	 	unitTable.setPrimaryKey("UNIT_ID");

		ERXMigrationTable videoTable = database.newTableNamed("video");
		videoTable.newIntegerColumn("MOVIE_ID", false);
		videoTable.newIntegerColumn("RENTAL_TERMS_ID", false);
		videoTable.newIntegerColumn("VIDEO_ID", false);
		videoTable.create();
	 	videoTable.setPrimaryKey("VIDEO_ID");

		ERXMigrationTable rentalTermsTable = database.newTableNamed("rentalterms");
		rentalTermsTable.newIntegerColumn("CHECK_OUT_LENGTH", false);
		rentalTermsTable.newBigDecimalColumn("COST", 10, 2, false);
		rentalTermsTable.newBigDecimalColumn("DEPOSIT_AMOUNT", 10, 2, false);
		rentalTermsTable.newStringColumn("NAME", 20, false);
		rentalTermsTable.newIntegerColumn("RENTAL_TERMS_ID", false);
		rentalTermsTable.create();
	 	rentalTermsTable.setPrimaryKey("RENTAL_TERMS_ID");

		ERXMigrationTable customerTable = database.newTableNamed("customer");
		customerTable.newStringColumn("CITY", 20, false);
		customerTable.newIntegerColumn("CUSTOMER_ID", false);
		customerTable.newStringColumn("FIRST_NAME", 20, false);
		customerTable.newStringColumn("LAST_NAME", 30, false);
		customerTable.newTimestampColumn("MEMBER_SINCE", true);
		customerTable.newStringColumn("PHONE", 10, true);
		customerTable.newStringColumn("STATE", 2, true);
		customerTable.newStringColumn("STREET_ADDRESS", 50, true);
		customerTable.newStringColumn("ZIP", 10, true);
		customerTable.create();
	 	customerTable.setPrimaryKey("CUSTOMER_ID");

		ERXMigrationTable creditCardTable = database.newTableNamed("creditcard");
		creditCardTable.newTimestampColumn("AUTHORIZATION_DATE", false);
		creditCardTable.newStringColumn("AUTHORIZATION_NUM", 10, false);
		creditCardTable.newStringColumn("CARD_NUMBER", 25, false);
		creditCardTable.newIntegerColumn("CUSTOMER_ID", false);
		creditCardTable.newTimestampColumn("EXPIRATION_DATE", false);
		creditCardTable.newBigDecimalColumn("CLIMIT", 10, 2, false);
		creditCardTable.create();
	 	creditCardTable.setPrimaryKey("CUSTOMER_ID");

		ERXMigrationTable feeTypeTable = database.newTableNamed("feetype");
		feeTypeTable.newIntegerColumn("ENABLED", false);
		feeTypeTable.newStringColumn("FEE_TYPE", 15, false);
		feeTypeTable.newIntegerColumn("FEE_TYPE_ID", false);
		feeTypeTable.newIntegerColumn("ORDER_BY", 9, false);
		feeTypeTable.create();
	 	feeTypeTable.setPrimaryKey("FEE_TYPE_ID");	
		
		ERXJDBCUtilities.executeUpdateScriptFromResourceNamed(database
				.adaptorChannel(), "Movies0-"
				+ ERXJDBCUtilities.databaseProductName(database
						.adaptorChannel()) + ".sql", "ERMoviesLogic");
	}
}