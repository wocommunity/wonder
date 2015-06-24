package er.modern.movies.test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import org.junit.Test;
import org.openqa.selenium.By;

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

}
