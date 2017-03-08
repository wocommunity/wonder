package er.modern.movies.test;

import static com.codeborne.selenide.Screenshots.screenshots;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;

import com.codeborne.selenide.impl.ScreenShotLaboratory;

public abstract class AbstractSelenideIT {

    /**
     * Overriding screenshot laboratory to let the jenkins junit-attachment
     * plugin know about files to attach to the test report.
     */
    static {

        screenshots = new ScreenShotLaboratory() {

            @Override
            protected void copyFile(InputStream in, File targetFile) throws IOException {
                super.copyFile(in, targetFile);
                System.err.println("[[ATTACHMENT|" + targetFile.getAbsolutePath() + "]]");
            }

        };

    }

    @Before
    public void setUp() throws Exception {
        // use custom firefox instance
//        System.setProperty("browser", "er.modern.movies.test.FirefoxWebDriverProvider");
        System.setProperty("selenide.baseUrl",
                "http://127.0.0.1:9876/cgi-bin/WebObjects/ERModernMoviesTest.woa");
        // use phantomjs headless browser
        System.setProperty("browser", "phantomjs");
    }

}
