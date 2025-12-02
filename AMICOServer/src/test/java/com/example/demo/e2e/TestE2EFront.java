package com.example.demo.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Paths;
import java.util.List;
import java.io.File;

import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.s;

import io.github.bonigarcia.seljup.SeleniumExtension;

@ExtendWith(SeleniumExtension.class)
public class TestE2EFront extends ElastestBaseTest {
	
	final static Logger log = getLogger(lookup().lookupClass());

	final static String PATH_DOWNLOAD = Paths.get(System.getProperty("user.dir"), "download-temporal").toString();

	@Test
	public void checkCreateCourse(TestInfo testInfo) {
		// Login
		goToPage("login");
		this.loginUser("admin", "pass");

		// Create course
		goToPage("admin");

		waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Management')]")), "Management is not available", 5);
		driver.findElement(By.xpath("//span[contains(text(), 'Management')]")).click();
		waitUntil(ExpectedConditions.elementToBeClickable(By.linkText("Add Course")), "Add Course botton is not visible", 5);
		driver.findElement(By.linkText("Add Course")).click();

		WebElement name = getVisibleElement(By.id("name-input"));
		WebElement startDate = getVisibleElement(By.name("startDate"));
		WebElement endDate = getVisibleElement(By.name("endDate"));
		WebElement language = getVisibleElement(By.name("newLanguage"));
		WebElement type = getVisibleElement(By.name("newType"));	
		WebElement skill = getVisibleElement(By.name("newSkill1"));
		WebElement description = getVisibleElement(By.id("description-input"));
		WebElement submit = getVisibleElement(By.cssSelector("button[type='submit']"));
		WebElement image = getVisibleElement(By.name("courseImage"));
		// WebElement divForm = driver.findElement(By.className("form-group"));
		
		sendKeysAfterClear(name, "test");
		sendKeysAfterClear(startDate, "10/02/2019");
		sendKeysAfterClear(endDate, "10/06/2019");
		sendKeysAfterClear(language, "Spanish");
		sendKeysAfterClear(type, "test");	
		sendKeysAfterClear(skill, "test");
		sendKeysAfterClear(description, "test");

		String imageUpload = System.getProperty("user.dir") + "/src/test/resources/test.jpeg";

		try {
			image.sendKeys(imageUpload);
		} catch (Exception e) {
			log.info("Impossible to find the image");
			e.printStackTrace();
		}

		sleep(200);

		submit.click();

		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("dataTable1")), "Failed creating course", 2);
		log.info("Course created correctly");

		// Go to last courses
		WebElement divTable = driver.findElement(By.className("table-responsive"));
		List<WebElement> pagesCourses = divTable.findElements(By.className("page-item"));
		WebElement lastPageCourses = pagesCourses.get(pagesCourses.size() - 2);
		lastPageCourses.click();

		// Select last course
		WebElement lastCourse = getLastCourse();
		WebElement inputName = lastCourse.findElement(By.name("newName"));
		WebElement buttonDeleteLastCourse = lastCourse.findElement(By.name("btnDelete"));
		String nameLastCourse = inputName.getAttribute("value");

		// Check if the last course is equals than course added and delete it
		assertThat("Failed adding course", nameLastCourse, IsEqualIgnoringCase.equalToIgnoringCase("test"));
		buttonDeleteLastCourse.click();

		// Wait remove course
		sleep(400);

		// Check if the course is deleted
		lastCourse = getLastCourse();
		inputName = lastCourse.findElement(By.name("newName"));
		nameLastCourse = inputName.getAttribute("value");

		assertThat("Failed deleting course", nameLastCourse, not(IsEqualIgnoringCase.equalToIgnoringCase("test")));
		log.info("Course deleted correctly");

		// Logout
		this.goToPage();
		this.logout();
	}

	@Test
	public void checkDownload(TestInfo testInfo) { // Login
		goToPage("login");
		loginUser("amico", "pass");

		// Profile
		goToPage("profile/amico");

		// Go to course
		WebElement inscribedCourses = driver.findElement(By.id("inscribed-courses"));
		List<WebElement> buttonsCourses = inscribedCourses.findElements(By.tagName("a"));
		WebElement firstCourse = buttonsCourses.get(0);
		firstCourse.click();

		sleep(1000);

		// Go to first subject
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.tagName("section")), "Failed opening course", 2);
		WebElement subjects = driver.findElement(By.tagName("section"));
		List<WebElement> buttonsSubjects = subjects.findElements(By.tagName("a"));
		WebElement firstSubject = buttonsSubjects.get(0);
		firstSubject.click();

		sleep(1000);
		
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.className("tab-pane")),
				"Failed opening subject", 12);
		List<WebElement> tabs = driver.findElements(By.className("tab-pane"));
		WebElement tab = tabs.get(0);
		
		List<WebElement> files = tab.findElements(By.className("item-content"));
		WebElement firstFile = files.get(0);
		firstFile.click();

		sleep(1000);
		checkDownloadFile();

		this.cleanDownloadFolder();
		
		this.goToPage();
		this.logout();
	}

	@Test
	public void checkShowProfile() {
		// Go to profile
		goToPage("profile/amico");

		// Wait for load page
		sleep(2000);

		// Check if not show profile
		String currentUrl = driver.getCurrentUrl();
		assertThat(currentUrl, not(containsString("profile")));
		log.info("The profile not show correctly");
	}

	@Test
	public void checkInvalidLogin() {
		goToPage("login");

		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.name("username")), "No login page", 2);

		WebElement userField = driver.findElement(By.name("username"));
		WebElement passField = driver.findElement(By.name("password"));

		WebElement divSubmit = driver.findElement(By.className("form-check"));
		WebElement submit = divSubmit.findElement(By.tagName("button"));

		userField.sendKeys("invalidUser");
		passField.sendKeys("invalidPass");
		submit.click();

		sleep(1000);
		goToPage("profile/invalidUser");
		String currentUrl = driver.getCurrentUrl();
		assertThat(currentUrl, not(containsString("profile")));

		log.info("Invalid login checked successfully");
	}

	@Test
	public void checkSignUp(){
		String username = "u" + System.currentTimeMillis();
		String password = "password123";

		goToPage("signup");
		sleep(1000);

		WebElement userField = getVisibleElement(By.name("username"));
		WebElement emailField = getVisibleElement(By.name("userMail"));
		WebElement passField = getVisibleElement(By.name("password"));
		WebElement repeatPassField = getVisibleElement(By.name("repeatPassword"));
		WebElement submit = getVisibleElement(By.cssSelector("button[type='submit']"));

		sendKeysAfterClear(userField, username);
		sendKeysAfterClear(emailField, username + "@gmail.com");
		sendKeysAfterClear(passField, password);
		sendKeysAfterClear(repeatPassField, password);
		submit.click();

		waitUntil2(ExpectedConditions.urlContains("/registered"), 
                  "Don't go to '/registered'", 5);

		goToPage("login");
		loginUser(username, password);
		goToPage("profile/" + username);
		String currentUrl = driver.getCurrentUrl();
        assertThat(currentUrl, containsString("profile"));
		sleep(2000);
        log.info("Successfully created user: " + username);
	}

	public void loginUser(String name, String pass) {
		// Wait show form login
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.name("username")), "No login page", 1);

		// Load form
		WebElement userField = driver.findElement(By.name("username"));
		WebElement passField = driver.findElement(By.name("password"));

		WebElement divSubmit = driver.findElement(By.className("form-check"));
		WebElement submit = divSubmit.findElement(By.tagName("button"));

		// Write credentials
		userField.sendKeys(name);
		passField.sendKeys(pass);
		submit.click();

		// Check login
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.className("masthead")), "Login failed", 2);

		WebElement menuBar = driver.findElement(By.id("navbarResponsive"));
		List<WebElement> elementsBar = menuBar.findElements(By.tagName("a"));
		assertThat(elementsBar.get(elementsBar.size() - 1).getText()).isEqualToIgnoringCase("MY PROFILE");

		log.info("Loggin successful, user {}", name);
	}

	public void logout() {
		WebElement menuBar = driver.findElement(By.id("navbarResponsive"));
		List<WebElement> elementsBar = menuBar.findElements(By.tagName("a"));

		WebElement buttonLogout;
		if (elementsBar.size() == 2) {
			buttonLogout = elementsBar.get(0);
		} else {
			buttonLogout = elementsBar.get(1);
		}

		// Click logout button
		buttonLogout.click();

		// Check logout
		menuBar = driver.findElement(By.id("navbarResponsive"));
		elementsBar = menuBar.findElements(By.tagName("a"));

		assertThat(elementsBar.get(elementsBar.size() - 1).getText()).isEqualToIgnoringCase("LOG IN");

		log.info("Logout successful");
	}

	public void goToPage() {
		String url = sutUrl;

		this.driver.get(url);
	}

	public void goToPage(String page) {
		String url = sutUrl;

		this.driver.get(url + page);
	}

	public void waitUntil(ExpectedCondition<WebElement> expectedCondition, String errorMessage, int seconds) {
		WebDriverWait waiter = new WebDriverWait(driver, seconds);

		try {
			waiter.until(expectedCondition);
		} catch (org.openqa.selenium.TimeoutException timeout) {
			log.error(errorMessage);
			throw new org.openqa.selenium.TimeoutException(
					"\"" + errorMessage + "\" (checked with condition) > " + timeout.getMessage());
		}
	}

	public void waitUntil2(ExpectedCondition<Boolean> expectedCondition, String errorMessage, int seconds) {
		WebDriverWait waiter = new WebDriverWait(driver, seconds);

		try {
			waiter.until(expectedCondition);
		} catch (org.openqa.selenium.TimeoutException timeout) {
			log.error(errorMessage);
			throw new org.openqa.selenium.TimeoutException(
					"\"" + errorMessage + "\" (checked with condition) > " + timeout.getMessage());
		}
	}

	private void checkDownloadFile() {
		driver.get("chrome://downloads/");	
		sleep(1000);
		
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.tagName("downloads-manager")), "test", 2);
		WebElement downloads = (WebElement) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName(\"downloads-manager\")[0].shadowRoot.getElementById(\"downloadsList\")");
		List<WebElement> downloadItems = downloads.findElements(By.tagName("downloads-item"));

		assertThat("Download failed", 1, is(downloadItems.size()));
		log.info("Correct download lesson of course");

	}

	private WebElement getLastCourse() {
		WebElement divTable = driver.findElement(By.id("dataTable1"));
		List<WebElement> trsTable = divTable.findElements(By.tagName("tr"));
		WebElement lastCourse = trsTable.get(trsTable.size() - 1);

		return lastCourse;
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public WebElement getVisibleElement(By locator){
		List<WebElement> inputs = driver.findElements(locator);

		WebElement visibleInput = null;

		for (WebElement input : inputs) {
			if (input.isDisplayed()) {
				visibleInput = input;
				break;
			}
		}

		if (visibleInput == null) {
			throw new RuntimeException("Error: No visible fields found");
		}
		return visibleInput;
	}

	public void sendKeysAfterClear(WebElement element, String text) {
		element.clear();
		element.sendKeys(text);
	}

	public void cleanDownloadFolder() {
		try {
			File downloadDir = new File(PATH_DOWNLOAD);

			if (downloadDir.exists() && downloadDir.isDirectory()) {
				File[] files = downloadDir.listFiles();
				
				if (files != null) {
					for (File file : files) {
						file.delete();
					}
				}
				log.info("Downloads directory clean: " + PATH_DOWNLOAD);
			}
		} catch (Exception e) {
			log.error("Error to clear directory: " + e.getMessage());
		}
	}
}
