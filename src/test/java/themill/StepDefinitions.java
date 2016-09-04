package themill;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StepDefinitions {

    WebDriver webdriver;

    String baseUrl = "http://filemailer.beam.tv/DTqcgSdQNY/NJPSDwbNGk/";
    String user;
    String downloadDir = System.getProperty("user.home") + "/TheMillDownloads";
    long downloadTimeoutInSec = 60;


    private FirefoxProfile firefoxProfile() throws Throwable {

        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("browser.download.folderList",2);
        firefoxProfile.setPreference("browser.download.manager.showWhenStarting",false);
        firefoxProfile.setPreference("browser.download.dir", downloadDir);
        firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk","application/zip");

        return firefoxProfile;
    }

    /* copied from https://www.seleniumeasy.com/selenium-tutorials/verify-file-after-downloading-using-webdriver-java */
    private   String getDownloadedDocumentName(String downloadDir, String fileExtension)
    {

        System.out.println("Download dir is " + downloadDir);
        String downloadedFileName = null;
        boolean valid;
        boolean found = false;


        try
        {
            Path downloadFolderPath = Paths.get(downloadDir);
            System.out.println("downloadFolderPath dir is " + downloadFolderPath.toAbsolutePath().toString());
            WatchService watchService = FileSystems.getDefault().newWatchService();
            downloadFolderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            long startTime = System.currentTimeMillis();
            do
            {
                WatchKey watchKey;
                watchKey = watchService.poll(downloadTimeoutInSec,TimeUnit.SECONDS);
                long currentTime = (System.currentTimeMillis()-startTime)/1000;
                if(currentTime> downloadTimeoutInSec)
                {
                    System.out.println("Download operation timed out.. Expected file was not downloaded");
                    return downloadedFileName;
                }

                for (WatchEvent event : watchKey.pollEvents())
                {
                    WatchEvent.Kind kind = event.kind();
                    if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE) || kind.equals(StandardWatchEventKinds.ENTRY_MODIFY))
                    {
                        String fileName = event.context().toString();
                        System.out.println("New File Created:" + fileName);
                        if(fileName.endsWith(fileExtension))
                        {
                            downloadedFileName = fileName;
                            System.out.println("Downloaded file found with extension " + fileExtension + ". File name is " +

                                    fileName);
                            Thread.sleep(500);
                            found = true;
                            break;
                        }
                    }
                }
                if(found)
                {
                    return downloadedFileName;
                }
                else
                {
                    currentTime = (System.currentTimeMillis()-startTime)/1000;
                    if(currentTime> downloadTimeoutInSec)
                    {
                        System.out.println("Failed to download expected file");
                        return downloadedFileName;
                    }
                    valid = watchKey.reset();
                }
            } while (valid);
        }

        catch (InterruptedException e)
        {
            System.out.println("Interrupted error - " + e.getMessage());
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {
            System.out.println("Download operation timed out.. Expected file was not downloaded");
        }
        catch (Exception e)
        {
            System.out.println("Error occured - " + e.getMessage());
            e.printStackTrace();
        }
        return downloadedFileName;
    }

    @Before
    public void beforeScenario() throws Throwable {

        webdriver = new FirefoxDriver(firefoxProfile());
        webdriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @After
    public void afterScenario() {
        webdriver.close();
    }


    @Given("^As a (.+)$")
    public void as_a_user(String assignedUser)  {
        user = assignedUser;
    }

    @When("^I go to the project web page$")
    public void go_to_the_project_web_page() throws Throwable {
        String usernameAsB64 = Base64.getEncoder().encodeToString(user.getBytes("utf-8"));
        webdriver.get( baseUrl + usernameAsB64);
    }

    @Then("^I should have my (.+) displayed in the project so that I know which (.+) I am working on$")
    public void i_should_have_my_title_displayed_in_the(String title, String project) throws Throwable {
        String str = webdriver.findElements(By.className("jobtitle")).get(0).getText().trim();
        assertThat(str, is("Adtext, "+ title +" - " + project));
    }


    @Then("^I should have an option to show/hide metadata$")
    public void i_should_have_an_option_to_show_hide_metadata() throws Throwable {

       boolean exist=  webdriver.findElements(By.xpath("//a[@class='show-metadata']")).isEmpty();
        assertThat(exist, is(false));
    }


    @Then("^So that I should be able to see details of the file when necessary$")
    public void so_that_i_should_be_able_to_see_details_of_the_file_when_necessary() throws Throwable {

        WebElement webelement = webdriver.findElement(By.xpath("//a[@class='show-metadata']"));
        webelement.click();
        Thread.sleep(2000); // can be commented out,  just to be able to see the details of the file on the page
        // once its clicked on a manual run
        boolean displayed= webdriver.findElement(By.xpath("//*[@class='metadata']")).isDisplayed();

        assertThat(true, is(displayed));

        webelement.click();

        boolean hidden= webdriver.findElements(By.xpath("//*[@class='metadata']")).isEmpty();

        assertThat(true, is(hidden));

    }


    @When("^I click the download button$")
    public void i_click_the_download_button() throws Throwable {

        WebElement webelement = webdriver.findElement(By.xpath("//a[@class='downloadable']"));
        webelement.click();

    }

    @Then("^I should be able to download multiple files$")
    public void i_should_be_able_to_download_multiple_files() throws Throwable {

        // Check and see all the files and format types to select and download just after clicking the download button

        WebElement webelement = webdriver.findElement(By.xpath(".//*[@id='wrapper']"));
        // wrapper id captured from the html source code
        assertThat(webelement.isDisplayed(), is(true));

    }


    @Then("^So that I should be able to playback/view a group of files when the link goes offline$")
    public void so_that_i_should_be_able_to_playback_view_a_group_of_files() throws Throwable {
        // clear download folder
        File file = new File(downloadDir);
        FileUtils.deleteDirectory(file);
        file.mkdir();
        List<WebElement> webelements = webdriver.findElements(By.cssSelector(".selection.image-mpg1"));


        //Select  .mpg1 files
        for (WebElement webelement : webelements) {
            WebElement clickable = webelement.findElement(By.cssSelector(".filemailer-icon.filemailer-icon-un-checked.ng-scope"));
            clickable.click();
            Thread.sleep(1000); // can be commented out,  just to be able to see the files clicked while manual checking
        }

        //Download the selected files
        WebElement downloadButton = webdriver.findElement(By.xpath("//*[@id='transcode-download-button']"));
        downloadButton.click();


        WebElement downloadLink = webdriver.findElement(By.xpath("//a[@class='downloadlink']"));
        downloadLink.click();


        String downloadedFile = getDownloadedDocumentName(downloadDir, "zip");

        assertThat(downloadedFile, is("Adtext_FileMailer_Test_J107066.zip"));

    }
}
