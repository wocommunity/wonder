package er.modern.movies.test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import org.junit.Test;
import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;

/**
 * Some simple tests for the automatic navigation feature of ERXNavigation.
 */
public class NavigationIT extends AbstractSelenideIT {

    @Test
    public void defaultActionTest() {
        open("/");
        $(By.linkText("Login")).click();
        $(By.linkText("Home")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
    }

    @Test
    public void movieNavigationTest() {
        open("/");
        $(By.linkText("Login")).click();
        // from Home to Movies.Search
        $(By.linkText("Find")).click();
        $(By.linkText("Movies")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        $$(".ListMovieObjTable tr.ObjRow").shouldHaveSize(10);
        // edit first movie, we stay with Movies.Search
        $(By.linkText("Edit")).click();
        $(By.linkText("Movies")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        // cancel edit, we're still at Movies.Search
        $(By.linkText("Cancel")).click();
        $(By.linkText("Movies")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        // create new, we change to Movies.New
        $(By.linkText("New")).click();
        $(By.linkText("Movies")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("New")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        $(By.linkText("Cancel")).click();
        // we're back to Movies.Search
        $(By.linkText("Movies")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
    }
    
    @Test
    public void adminNavigationTest() {
        open("/");
        $(By.linkText("Login")).click();
        // from Home to Admin – no second level selected
        $(By.linkText("Admin")).click();
        $(By.linkText("Admin")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Talent")).parent().shouldNotHave(Condition.cssClass("Nav2Selected"));
        // we move on to Admin.Talent.Search
        $(By.linkText("Find")).click();
        $$(".ListTalentObjTable tr.ObjRow").shouldHaveSize(10);
        $(By.linkText("Admin")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Talent")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav3Selected"));
        // editing, we stay at Admin.Talent.Search
        $(By.linkText("Edit")).click();
        $(By.linkText("Admin")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Talent")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav3Selected"));
        // cancel edit, we're still at Admin.Talent.Search
        $(By.linkText("Cancel")).click();
        $(By.linkText("Admin")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Talent")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav3Selected"));
        // create new, we change to Admin.Talent.New
        $(By.linkText("New")).click();
        $(By.linkText("Admin")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Talent")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        $(By.linkText("New")).parent().shouldHave(Condition.cssClass("Nav3Selected"));
        $(By.linkText("Cancel")).click();
        // we're back to Admin.Talent.Search
        $(By.linkText("Admin")).parent().shouldHave(Condition.cssClass("Nav1Selected"));
        $(By.linkText("Talent")).parent().shouldHave(Condition.cssClass("Nav2Selected"));
        $(By.linkText("Search")).parent().shouldHave(Condition.cssClass("Nav3Selected"));
    }
}