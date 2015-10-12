package er.modern.movies.test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

/**
 * Test integration of ERCPreference values for batch size and sort order.
 *
 */
public class ERCPreferIT extends AbstractSelenideIT {

    @Test
    public void listPageBatchSizeTest() {
        open("/");
        $(By.linkText("Login")).click();
        $(".QueryAllStudioLine").$(By.linkText("Find")).click();
        // default batch size is 10
        $$(".ListStudioObjRow").shouldHaveSize(10);
        // change batch size to 5
        $(".ERMDBatchSize_BatchValue").click();
        $(".ERMDBatchSize_BatchForm input").setValue("5");
        $(".ERMDBatchSize_BatchForm input").sendKeys(Keys.TAB);
        // verify change is effective
        $$(".ListStudioObjRow").shouldHaveSize(5);
        // leave page and return
        $(By.linkText("Home")).click();
        $(".QueryAllStudioLine").$(By.linkText("Find")).click();
        // verify batch size has been persisted
        $$(".ListStudioObjRow").shouldHaveSize(5);
        // reset batch size to 10
        $(".ERMDBatchSize_BatchValue").click();
        $(".ERMDBatchSize_BatchForm input").setValue("10");
        $(".ERMDBatchSize_BatchForm input").sendKeys(Keys.TAB);
        // verify change is effective
        $$(".ListStudioObjRow").shouldHaveSize(10);
    }

    @Test
    public void listPageSortOrderTest() {
        open("/");
        $(By.linkText("Login")).click();
        $(".QueryAllStudioLine").$(By.linkText("Find")).click();
        // default sort order is ascending by name
        $(".ListStudioObjTable").shouldHave(text("20th Century Fox"));
        // change sort order to descending by name
        $(By.linkText("Name")).click();
        // verify sort order is changed
        $(".ListStudioObjTable").shouldNotHave(text("20th Century Fox"));
        $(".ListStudioObjTable").shouldHave(text("ZZ Studios"));
        // leave page and return
        $(By.linkText("Home")).click();
        $(".QueryAllStudioLine").$(By.linkText("Find")).click();
        // verify sort order has been persisted
        $(".ListStudioObjTable").shouldNotHave(text("20th Century Fox"));
        $(".ListStudioObjTable").shouldHave(text("ZZ Studios"));
        // change sort order back to ascending by name
        $(By.linkText("Name")).click();
        // verify sort order is changed
        $(".ListStudioObjTable").shouldNotHave(text("ZZ Studios"));
        $(".ListStudioObjTable").shouldHave(text("20th Century Fox"));
    }

    @Test
    public void editRelationshipBatchSizeTest() {
        open("/");
        $(By.linkText("Login")).click();
        $$(".QueryAllQuery").findBy(text("Movie")).find("input")
                .setValue("GoodFellas");
        $(By.linkText("Find")).click();
        $(By.linkText("Edit")).click();
        $(By.linkText("Roles")).click();
        // default batch size is 5
        $$(".EditRelationshipEmbeddedMovieRoleObjRow").shouldHaveSize(5);
        // the movie has 6 roles
        $(".ERMDBatchSize_Wrapper").shouldHave(text("6 items"));
        // change batch size to 10
        $(".ERMDBatchSize_BatchValue").click();
        $(".ERMDBatchSize_BatchForm input").setValue("10");
        $(".ERMDBatchSize_BatchForm input").sendKeys(Keys.TAB);
        // verify change is effective
        $$(".EditRelationshipEmbeddedMovieRoleObjRow").shouldHaveSize(6);
        // cancel edit and edit again
        $(By.linkText("Cancel")).click();
        $(By.linkText("Edit")).click();
        $(By.linkText("Roles")).click();
        // verify batch size has been persisted
        $$(".EditRelationshipEmbeddedMovieRoleObjRow").shouldHaveSize(6);
        // reset batch size to 5
        $(".ERMDBatchSize_BatchValue").click();
        $(".ERMDBatchSize_BatchForm input").setValue("5");
        $(".ERMDBatchSize_BatchForm input").sendKeys(Keys.TAB);
        $$(".EditRelationshipEmbeddedMovieRoleObjRow").shouldHaveSize(5);
    }

     @Test
    public void editRelationshipSortOrderTest() {
        open("/");
        $(By.linkText("Login")).click();
        $$(".QueryAllQuery").findBy(text("Movie")).find("input")
                .setValue(" WOF The Next Big Thing");
        $(By.linkText("Find")).click();
        $(By.linkText("Edit")).click();
        $(By.linkText("People")).click();
        $$(".EditRelationshipEmbeddedTalentObjRow").shouldHaveSize(2); 
        // on first run, no sort order is defined so clicking 
        // on "Last Name" will sort in ascending order
        $(By.linkText("Last Name")).click();
        $$(".EditRelationshipEmbeddedTalentObjRow").get(0).shouldHave(text("Belk"));
        // invert sort order to make test idempotent
        $(By.linkText("Last Name")).click();
        $$(".EditRelationshipEmbeddedTalentObjRow").get(0).shouldHave(text("Trujillo-Vian"));
        // verify sort order is 'descending'
        $(".ComboTHLinkDes").should(Condition.exist);
        // cancel edit and edit again
        $(By.linkText("Cancel")).click();
        $(By.linkText("Edit")).click();
        $(By.linkText("People")).click();
        // verify sort order has been persisted
        $$(".EditRelationshipEmbeddedTalentObjRow").shouldHaveSize(2); 
        $$(".EditRelationshipEmbeddedTalentObjRow").get(0).shouldHave(text("Trujillo-Vian"));
        // verify sort order is 'descending'
        $(".ComboTHLinkDes").should(Condition.exist);

    }
    
}