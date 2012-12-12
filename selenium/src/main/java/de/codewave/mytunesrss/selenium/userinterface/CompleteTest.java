package de.codewave.mytunesrss.selenium.userinterface;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CompleteTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompleteTest.class);
    private static final int WAIT_INTERVAL = 100;
    private static final int TIMEOUT = 60000;
    private static final String BASE_URL = "http://127.0.0.1:47110";

    public static void main(String[] args) throws Exception {
        int maxThrads = Integer.parseInt(args[0]);
        final int maxLoops = Integer.parseInt(args[1]);
        final String display = args[2];
        Thread[] threads = new Thread[maxThrads];
        for (int i = 0; i < maxThrads; i++) {
            final String threadName = "selenium_" + CompleteTest.class.getSimpleName() + "_" + (i + 1);
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        runLoop(UUID.randomUUID().toString(), "selenium", threadName, maxLoops, display);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, threadName);
            threads[i].start();
        }
    }

    private static void runLoop(String username, String password, String threadName, int times, String display) throws Exception {
        FirefoxBinary firefoxBinary = new FirefoxBinary();
        firefoxBinary.setEnvironmentProperty("DISPLAY", display);
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("webdriver.load.strategy", "unstable");
        WebDriver driver = new FirefoxDriver(firefoxBinary, firefoxProfile) {
            @Override
            public WebElement findElement(By by) {
                System.out.println("searching element: " + by);
                delayExecution();
                WebElement element = super.findElement(by);
                System.out.println("found element: " + element);
                return element;
            }

            @Override
            protected Response execute(String driverCommand, Map<String, ?> parameters) {
                System.out.println("executing driver command: " + driverCommand + " with parameters: " + parameters);
                delayExecution();
                try {
                    return super.execute(driverCommand, parameters);
                } finally {
                    delayExecution();
                }
            }

            @Override
            protected void log(SessionId sessionId, String commandName, Object toLog, When when) {
                super.log(sessionId, commandName, toLog, when);
                System.out.println("session:\"" + sessionId + "\", command:\"" + commandName + "\", when:\"" + when + "\", msg:" + toLog);
            }
        };
        try {
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            driver.navigate().to(BASE_URL + "/mytunesrss/");
            driver.findElement(By.id("linkSelfReg")).click();
            driver.findElement(By.id("reg_username")).clear();
            driver.findElement(By.id("reg_username")).sendKeys(username);
            driver.findElement(By.id("reg_password")).clear();
            driver.findElement(By.id("reg_password")).sendKeys(password);
            driver.findElement(By.id("reg_retypepassword")).clear();
            driver.findElement(By.id("reg_retypepassword")).sendKeys(password);
            driver.findElement(By.id("reg_email")).clear();
            driver.findElement(By.id("reg_email")).sendKeys("mdescher@codewave.de");
            driver.findElement(By.id("linkSubmit")).click();
            for (int i = 1; i <= times; i++) {
                LOGGER.debug("Starting {} of {} for thread {}.", new Object[] {i, times, threadName});
                testComplete(driver, BASE_URL, username, password);
            }
        } finally {
            driver.quit();
        }
    }

    private static void delayExecution() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static void testComplete(WebDriver driver, String baseUrl, String username, String password) throws Exception {
        driver.navigate().to(baseUrl + "/mytunesrss/");
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("linkSubmitLogin")).click();
        driver.findElement(By.id("functionsDialogName10")).click();
        driver.findElement(By.id("linkParentFolder")).click();
        driver.findElement(By.id("functionsDialogName14")).click();
        driver.findElement(By.id("linkHomeFolder")).click();
        driver.findElement(By.id("functionsDialogName10")).click();
        driver.findElement(By.id("linkButtonBrowseTrack1")).click();
        driver.findElement(By.id("linkPage3")).click();
        driver.findElement(By.id("linkPage2")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkNameBrowseTrack8")).click();
        driver.findElement(By.id("linkPage1")).click();
        driver.findElement(By.id("linkPage2")).click();
        driver.findElement(By.id("functionsDialogName10")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkNameBrowseTrack5")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkRefreshRandomPlaylist")).click();
        driver.findElement(By.id("linkNameBrowseTrack5")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.name("searchTerm")).clear();
        driver.findElement(By.name("searchTerm")).sendKeys("ofspring");
        new Select(driver.findElement(By.name("searchFuzziness"))).selectByIndex(0);
        driver.findElement(By.id("linkDoSearch")).click();
        new Select(driver.findElement(By.name("searchFuzziness"))).selectByIndex(1);
        driver.findElement(By.id("linkDoSearch")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkDoSearch")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkBrowseArtist")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkStatsAlbum")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkStatsArtist")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkStatsGenre")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkBrowseArtist")).click();
        driver.findElement(By.id("linkBrowseAlbum")).click();
        driver.findElement(By.id("linkBrowseGenre")).click();
        driver.findElement(By.id("linkBrowseArtist")).click();
        driver.findElement(By.id("linkSection0")).click();
        driver.findElement(By.id("linkSection1")).click();
        driver.findElement(By.id("linkSection2")).click();
        driver.findElement(By.id("linkSection3")).click();
        driver.findElement(By.id("linkSection4")).click();
        driver.findElement(By.id("linkSection5")).click();
        driver.findElement(By.id("linkSection6")).click();
        driver.findElement(By.id("linkSection7")).click();
        driver.findElement(By.id("linkSection8")).click();
        driver.findElement(By.id("linkSectionAll")).click();
        driver.findElement(By.id("linkSection1")).click();
        driver.findElement(By.id("linkFilter")).click();
        driver.findElement(By.id("filterText")).clear();
        driver.findElement(By.id("filterText")).sendKeys("ace");
        driver.findElement(By.id("linkApplyFilter")).click();
        driver.findElement(By.id("filterText")).clear();
        driver.findElement(By.id("filterText")).sendKeys("");
        driver.findElement(By.id("linkApplyFilter")).click();
        driver.findElement(By.id("linkTracksOfArtist14")).click(); // Alice Cooper
        driver.findElement(By.id("linkPage1")).click();
        driver.findElement(By.id("linkPage2")).click();
        driver.findElement(By.id("linkPage3")).click();
        driver.findElement(By.id("linkPage4")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkAlbumsOfArtistAlbum4")).click(); // AC/DC
        driver.findElement(By.id("linkPage1")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkBrowseMovie")).click();
        driver.findElement(By.id("linkPage8")).click();
        driver.findElement(By.id("functionsDialogName15")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkBrowseTvShow")).click();
        driver.findElement(By.id("functionsDialogName6")).click();
        driver.findElement(By.id("functionsDialogName2")).click();
        driver.findElement(By.id("linkPage1")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("functionsDialogName1")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("functionsDialogName0")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("functionsDialogName8")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkBrowsePhotoAlbum")).click();
        driver.findElement(By.id("functionsDialogName0")).click(); // show diddl album
        driver.findElement(By.id("linkPage2")).click();
        driver.findElement(By.id("img2")).click(); // show a single photo
        driver.findElement(By.id("leftphotobutton")).click();
        driver.findElement(By.id("leftphotobutton")).click();
        driver.findElement(By.id("rightphotobutton")).click();
        driver.findElement(By.id("rightphotobutton")).click();
        driver.findElement(By.id("rightphotobutton")).click();
        driver.findElement(By.id("exiflink")).click();
        waitForElementText(driver, By.id("value_Modify_Date"), "2011:09:04 01:05:15"); // verify modiy date
        driver.findElement(By.id("close_exif")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("functionsDialogName1")).click();
        driver.findElement(By.id("linkPage1")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("functionsDialogName4")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkManagePlaylists")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkBrowseArtist")).click();
        driver.findElement(By.id("linkNewPlaylist")).click();
        driver.findElement(By.id("linkAddToPlaylist1")).click();
        waitForElementText(driver, By.id("editPlaylistTrackCount"), "12"); // A-Teens
        driver.findElement(By.id("linkAddToPlaylist3")).click();
        waitForElementText(driver, By.id("editPlaylistTrackCount"), "34"); // ABBA
        driver.findElement(By.id("linkFinish")).click();
        driver.findElement(By.id("playlistName")).clear();
        driver.findElement(By.id("playlistName")).sendKeys("Ateens und Abba");
        driver.findElement(By.id("linkSave")).click();
        driver.findElement(By.id("linkTracks0")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkEdit0")).click();
        waitForElement(driver, By.id("trackTableRow19"));
        driver.findElement(By.id("linkUp3")).click();
        driver.findElement(By.id("linkUp2")).click();
        driver.findElement(By.id("linkDown2")).click();
        driver.findElement(By.id("linkDown3")).click();
        driver.findElement(By.id("linkDelete0")).click();
        driver.findElement(By.id("linkSave")).click();
        driver.findElement(By.id("linkTracks0")).click();
        driver.findElement(By.id("functionsDialogName3")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkManagePlaylists")).click();
        driver.findElement(By.id("linkEditSmartPlayist")).click();
        new Select(driver.findElement(By.id("newFieldType"))).selectByValue("artist");
        driver.findElement(By.id("linkAddCriteria")).click();
        driver.findElement(By.id("smartCriteriaValue0")).clear();
        driver.findElement(By.id("smartCriteriaValue0")).sendKeys("lady gaga");
        driver.findElement(By.id("linkSubmit")).click();
        driver.findElement(By.name("smartPlaylist.playlist.name")).clear();
        driver.findElement(By.name("smartPlaylist.playlist.name")).sendKeys("Lady Gaga");
        driver.findElement(By.id("linkSubmit")).click();
        driver.findElement(By.id("linkTracks1")).click();
        driver.findElement(By.id("linkPage1")).click();
        driver.findElement(By.id("linkPage2")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkBrowseArtist")).click();
        driver.findElement(By.id("linkBrowseAlbum")).click();
        driver.findElement(By.id("linkArtistName1")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkArtistName5")).click();
        driver.findElement(By.id("linkBack")).click();
        driver.findElement(By.id("linkEditPlaylist")).click();
        driver.findElement(By.id("linkPlaylistDialogNew")).click();
        driver.findElement(By.id("linkAddToPlaylist0")).click();
        driver.findElement(By.id("linkAddToPlaylist1")).click();
        driver.findElement(By.id("linkFinish")).click();
        driver.findElement(By.id("playlistName")).clear();
        driver.findElement(By.id("playlistName")).sendKeys("Depeche und Def");
        driver.findElement(By.id("linkSave")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkBrowseArtist")).click();
        driver.findElement(By.id("linkBrowseAlbum")).click();
        driver.findElement(By.id("linkEditPlaylist")).click();
        new Select(driver.findElement(By.id("playlistSelection"))).selectByVisibleText("Depeche und Def");
        driver.findElement(By.id("linkPlaylistDialogEdit")).click();
        driver.findElement(By.id("linkAddToPlaylist13")).click();
        driver.findElement(By.id("linkFinish")).click();
        driver.findElement(By.id("playlistName")).clear();
        driver.findElement(By.id("playlistName")).sendKeys("Depeche und Def und Off");
        driver.findElement(By.id("linkSave")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkManagePlaylists")).click();
        driver.findElement(By.id("deleteWithConfirmation1")).click();
        driver.findElement(By.id("linkConfirmDelPlaylistNo")).click();
        driver.findElement(By.id("deleteWithConfirmation2")).click();
        driver.findElement(By.id("linkConfirmDelPlaylistYes")).click();
        driver.findElement(By.id("deleteWithConfirmation0")).click();
        driver.findElement(By.id("linkConfirmDelPlaylistYes")).click();
        driver.findElement(By.id("deleteWithConfirmation0")).click();
        driver.findElement(By.id("linkConfirmDelPlaylistYes")).click();
        driver.findElement(By.id("linkPortal")).click();
        driver.findElement(By.id("linkLogout")).click();
    }

    private static void waitForElement(WebDriver driver, By by) throws InterruptedException {
        for (int millis = 0; !isElement(driver, by); millis+= WAIT_INTERVAL) {
            if (millis >= TIMEOUT) {
                throw new TimeoutException("Timeout waiting for element.");
            }
            Thread.sleep(WAIT_INTERVAL);
        }
    }

    private static void waitForElementText(WebDriver driver, By by, String text) throws InterruptedException {
        for (int millis = 0; !text.equals(driver.findElement(by).getText()); millis+= WAIT_INTERVAL) {
            if (millis >= TIMEOUT) {
                throw new TimeoutException("Timeout waiting for element.");
            }
            Thread.sleep(WAIT_INTERVAL);
        }
    }

    private static boolean isElement(WebDriver driver, By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
