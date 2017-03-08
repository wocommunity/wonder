package er.modern.movies.test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import org.junit.Test;
import org.openqa.selenium.By;

public class ListFilterIT extends AbstractSelenideIT {

    @Test
    public void filterByPartialTitleTest() {
        open("/");
        $(By.linkText("Login")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        $("input[type=search]").setValue("fiction");
        // verify "Pulp Fiction" is listed
        $(".ListMovieObjTable").shouldHave(text("Pulp Fiction"));
        // and that it is the only match
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(1);
    }

    @Test
    public void filterByCategoryTest() {
        open("/");
        $(By.linkText("Login")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        $("input[type=search]").setValue("noir");
        // verify "Heathers" is listed
        $(".ListMovieObjTable").shouldHave(text("Heathers"));
        // and that it is one of 5 matches
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(5);
    }

    @Test
    public void filterByPlotSummaryTest() {
        open("/");
        $(By.linkText("Login")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        $("input[type=search]").setValue("dark");
        // verify "Heathers" is listed
        $(".ListMovieObjTable").shouldHave(text("Heathers"));
        // and that it is one of 2 matches
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(2);
        $("input[type=search]").setValue("darkn");
        // verify "Apocalypse Now" is listed
        $(".ListMovieObjTable").shouldHave(text("Apocalypse Now"));
        // and that it is the only match
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(1);
    }

    @Test
    public void filterByDateTest() {
        open("/");
        $(By.linkText("Login")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        $("input[type=search]").setValue("1987");
        // verify "The Untouchables" is listed
        $(".ListMovieObjTable").shouldHave(text("The Untouchables"));
        // and that it is one of 3 matches
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(3);
        $("input[type=search]").setValue("01/03/1987");
        // verify "The Untouchables" is listed
        $(".ListMovieObjTable").shouldHave(text("The Untouchables"));
        // and that it is the only match
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(1);
    }

    @Test
    public void filterByRevenueTest() {
        open("/");
        $(By.linkText("Login")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        $("input[type=search]").setValue("400000");
        // verify "Jurassic Park" is listed
        $(".ListMovieObjTable").shouldHave(text("Jurassic Park"));
        // and that it is one of 5 matches
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(5);
    }

    @Test
    public void filterByStudioPopUp() {
        open("/");
        $(By.linkText("Login")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        // make sure the object count is as expected
        $(".ERMDBatchSize_Wrapper").shouldHave(text("88"));
        // choose "Universal Pictures" from studio pop-up
        $(".ListFilterChoices select").selectOption("Universal Pictures");
        // verify "Jurassic Park" is listed
        $(".ListMovieObjTable").shouldHave(text("Uncle Buck"));
        // and that it is one of 7 matches
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(7);
    }
    
    @Test
    public void filterRolesRelationship() {
        open("/");
        $(By.linkText("Login")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        $$(".ListMovieObjTable tr.ObjRow")
                .findBy(text("Bis ans Ende der Welt [Until the End of the World]"))
                .find(By.linkText("Edit")).click();
        $(By.linkText("Roles")).click();
        $("input[type=search]").setValue("farb");
        // verify "Jurassic Park" is listed
        $(".EditRelationshipEmbeddedMovieRoleObjTable").shouldHave(text("Von Sydow"));
        // and that it is one of 5 objects in the batch
        $$(".EditRelationshipEmbeddedMovieRoleObjTable tr.ObjRow").shouldHaveSize(5);
        // and that 6 objects matched the filter
        $(".ERMDBatchSize_Wrapper").shouldHave(text("6"));

        $("input[type=search]").setValue("gue");
        // verify "Jurassic Park" is listed
        $(".EditRelationshipEmbeddedMovieRoleObjTable").shouldHave(text("Hotel guest"));
        // and that it is one of 3 matching objects
        $$(".EditRelationshipEmbeddedMovieRoleObjTable tr.ObjRow").shouldHaveSize(3);
        // and that the list size is being shown correctly
        $(".ERMDBatchSize_Wrapper").shouldHave(text("3"));
    }




}