package er.modern.movies.test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.codeborne.selenide.WebDriverProvider;

/**
 * Custom Firefox driver, mainly to disable the annoying startup homepages that
 * have been added in recent versions and would cause test failures.
 */
public class FirefoxWebDriverProvider implements WebDriverProvider {
    
    @Override
    public WebDriver createDriver(DesiredCapabilities capabilities) {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("intl.accept_languages", "en");
        profile.setPreference("browser.startup.homepage_override.mstone", "ignore");
        profile.setPreference("signon.autologin.proxy", true);
        capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        return new FirefoxDriver(capabilities);
    }
    
}