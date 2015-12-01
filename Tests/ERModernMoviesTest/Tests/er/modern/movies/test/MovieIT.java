package er.modern.movies.test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import org.junit.Test;
import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;

public class MovieIT extends AbstractSelenideIT {

    @Test
    public void searchByCompleteTitleTest() {
        open("/");
        $(By.linkText("Login")).click();
        $$(".QueryAllQuery").findBy(text("Movie")).find("input")
                .setValue(" EOF Next Generation");
        $(By.linkText("Find")).click();
        // verify "EOF Next Generation" is listed
        $(".ListMovieObjTable").shouldHave(text("EOF Next Generation"));
        // and that it is the only match
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(1);
    }

    @Test
    public void searchByPartialTitleTest() {
        open("/");
        $(By.linkText("Login")).click();
        // use the movie query page
        $(By.linkText("Movies")).click();
        $(".TitleLine").find("select").selectOption("contains");
        $(".TitleLine").find("input").setValue("Next");
        $(By.linkText("Find")).click();
        // verify "EOF Next Generation" is listed
        $(".ListMovieObjTable").shouldHave(text("EOF Next Generation"));
        // and that there are two matches
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(2);
    }

    @Test
    public void renameTitleTest() {
        open("/");
        $(By.linkText("Login")).click();
        $(By.linkText("Movies")).click();
        // get list of all movies
        $(By.linkText("Find")).click();
        // edit movie with title "EOF Next Generation"
        $$(".ListMovieObjTable tr.ObjRow").findBy(text("EOF Next Generation"))
                .find(By.linkText("Edit")).click();
        // set title to "Cayenne"
        $(".TitleLabel").parent().parent().find("input").setValue("Cayenne");
        $(By.linkText("Save")).click();
        // select second batch
        $(By.linkText("2")).click();
        // edit movie with title "Cayenne"
        $$(".ListMovieObjTable tr").findBy(text("Cayenne")).find(By.linkText("Edit"))
                .click();
        // set title to " EOF Next Generation"
        $(".TitleLabel").parent().parent().find("input").setValue(" EOF Next Generation");
        $(By.linkText("Save")).click();
        // select first batch
        $(By.linkText("1")).click();
        // verify "EOF Next Generation" is listed
        $(".ListMovieObjTable").shouldHave(text("EOF Next Generation"));
    }

    @Test
    public void createMovieTest() {
        open("/");
        $(By.linkText("Login")).click();
        // use the movie query page
        $(By.linkText("Movies")).click();
        $(By.linkText("New")).click();
        // trigger validation exception
        $(By.linkText("Next")).click();
        $(".ErrBlock").shouldHave(text("Please provide a title."));
        $(".TitleLine").find("input").setValue("Snatch");
        $(".CategoryLine").find("input").setValue("Comedy");
        $(".DateReleasedLine").find("input").setValue("2000-09-01");
        $(By.linkText("Next")).click();
        // edit to one relation, fill a few characters
        $(".StudioLine").find("input").setValue("Colum");
        // there should be only one match
        $$("div.auto_complete ul li").shouldHaveSize(1);
        // select matching studio
        $("div.auto_complete ul li").click();
        $(".StudioLine").find("input").shouldHave(value("Columbia Pictures"));
        // when the relation is set, a delete button should be shown
        $(".StudioLine .DeleteObjButton").shouldBe(Condition.present);
        $(By.linkText("Next")).click();
        // add a new director
        $(".DirectorsLine").$(By.linkText("New")).click();
        $(".FirstNameLine").find("input").setValue("Guy");
        $(".LastNameLine").find("input").setValue("Ritchie");
        $(By.linkText("Save")).click();
        // verify a director has been set
        $(".ERMDBatchSize_Wrapper").shouldHave(text("1 item"));
        $(By.linkText("Next")).click();
        // add a role
        $(".RolesLine").$(By.linkText("New")).click();
        $(".RoleNameLine").find("input").setValue("Mickey O'Neil");
        // search for existing actor
        $(".TalentLine").$(By.linkText("Search")).click();
        $(".FirstNameLine").find("input").setValue("Brad");
        $(By.linkText("Find")).click();
        // there should be only one match
        $$(".SelectEmbeddedTalentObjRow").shouldHaveSize(1);
        // select matching actor
        $(".SelectEmbeddedTalentObjRow").$(By.linkText("Select")).click();
        $(By.linkText("Return")).click();
        // save role
        $(".CreateEmbeddedMovieRoleWrapper").$(By.linkText("Save")).click();
        // cancel creation
        $(By.linkText("Cancel")).click();
        // we should see a warning
        $(".Message").shouldHave(
                text("Are you sure you want to stop creating this Movie?"));
        $(By.linkText("Cancel")).click();
        // save
        $(By.linkText("Save")).click();
        // search for the newly created movie
        $(".TitleLine").find("input").setValue("Snatch");
        $(By.linkText("Find")).click();
        // there should be only one match
        $$(".ListMovieObjRow").shouldHaveSize(1);
        // delete the movie
        $(".ListMovieObjRow").find(".DeleteObjButton").click();
        $(By.linkText("Delete")).click();
        $(".ERMDEmptyListMessage").shouldHave(text("No matching Movie records found."));
    }

}
