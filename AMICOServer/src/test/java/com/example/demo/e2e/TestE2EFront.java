package com.example.demo.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.Assert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.text.IsEqualIgnoringCase;

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

		waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Management')]")), "Menu Management não apareceu", 5);
		driver.findElement(By.xpath("//span[contains(text(), 'Management')]")).click();
		waitUntil(ExpectedConditions.elementToBeClickable(By.linkText("Add Course")), "Botão Add Course não apareceu", 5);
		driver.findElement(By.linkText("Add Course")).click();

		List<WebElement> name_inputs = driver.findElements(By.id("name-input"));
		WebElement visibleInput = null;
		for (WebElement input : name_inputs) {
			if (input.isDisplayed()) {
				visibleInput = input;
				break;
			}
		}
		if (visibleInput != null) {
			visibleInput.clear();
			visibleInput.sendKeys("test");
		} else {
			throw new RuntimeException("Erro: Nenhum campo visível encontrado!");
		}
		WebElement startDate = driver.findElement(By.name("startDate"));
		WebElement endDate = driver.findElement(By.name("endDate"));
		startDate.sendKeys("10/02/2019");
		endDate.sendKeys("10/06/2019");
		List<WebElement> newLanguage_inputs = driver.findElements(By.name("newLanguage"));
		visibleInput = null;
		for (WebElement input : newLanguage_inputs) {
			if (input.isDisplayed()) {
				visibleInput = input;
				break;
			}
		}
		if (visibleInput != null) {
			visibleInput.clear();
			visibleInput.sendKeys("Spanish");
		} else {
			throw new RuntimeException("Erro: Nenhum campo visível encontrado!");
		}

		// WebElement newLanguage = driver.findElement(By.name("newLanguage"));
		// WebElement newType = driver.findElement(By.name("newType"));
		List<WebElement> newType_inputs = driver.findElements(By.name("newType"));
		visibleInput = null;
		for (WebElement input : newType_inputs) {
			if (input.isDisplayed()) {
				visibleInput = input;
				break;
			}
		}
		if (visibleInput != null) {
			visibleInput.clear();
			visibleInput.sendKeys("test");
		} else {
			throw new RuntimeException("Erro: Nenhum campo visível encontrado!");
		}
		// WebElement newSkill = driver.findElement(By.name("newSkill1"));
		List<WebElement> newSkill_inputs = driver.findElements(By.name("newSkill1"));
		visibleInput = null;
		for (WebElement input : newSkill_inputs) {
			if (input.isDisplayed()) {
				visibleInput = input;
				break;
			}
		}
		if (visibleInput != null) {
			visibleInput.clear();
			visibleInput.sendKeys("test");
		} else {
			throw new RuntimeException("Erro: Nenhum campo visível encontrado!");
		}
		// WebElement newDescription = driver.findElement(By.name("newDescription"));
		List<WebElement> newDescription_inputs = driver.findElements(By.name("newDescription"));
		visibleInput = null;
		for (WebElement input : newDescription_inputs) {
			if (input.isDisplayed()) {
				visibleInput = input;
				break;
			}
		}
		if (visibleInput != null) {
			visibleInput.clear();
			visibleInput.sendKeys("test");
		} else {
			throw new RuntimeException("Erro: Nenhum campo visível encontrado!");
		}		
		WebElement image = driver.findElement(By.name("courseImage"));
		WebElement divForm = driver.findElement(By.className("form-group"));
		// WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));

		String imageUpload = System.getProperty("user.dir") + "/src/test/resources/test.jpeg";
		

		// newLanguage.sendKeys("Spanish");
		// newType.sendKeys("test");
		// newSkill.sendKeys("test");
		// newDescription.sendKeys("test");

		try {
			image.sendKeys(imageUpload);
		} catch (Exception e) {
			log.info("Impossible to find the image");
			e.printStackTrace();
		}

		sleep(2000);

		List<WebElement> submit = driver.findElements(By.cssSelector("button[type='submit']"));
		visibleInput = null;
		for (WebElement input : submit) {
			if (input.isDisplayed()) {
				visibleInput = input;
				break;
			}
		}
		if (visibleInput != null) {
			visibleInput.click();;
		} else {
			throw new RuntimeException("Erro: Nenhum campo visível encontrado!");
		}	
		// submit.click();

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
		sleep(3000);

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

		sleep(2000);

		// Go to first subject
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.tagName("section")), "Failed opening course", 2);
		WebElement subjects = driver.findElement(By.tagName("section"));
		List<WebElement> buttonsSubjects = subjects.findElements(By.tagName("a"));
		WebElement firstSubject = buttonsSubjects.get(0);
		firstSubject.click();

		sleep(2000);
		
		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.className("tab-pane")),
				"Failed opening subject", 12);
		List<WebElement> tabs = driver.findElements(By.className("tab-pane"));
		WebElement tab = tabs.get(0);
		
		List<WebElement> files = tab.findElements(By.className("item-content"));
		WebElement firstFile = files.get(0);
		firstFile.click();

		// Check download
		sleep(2000);
		checkDownloadFile();
		
		// Logout
		this.goToPage();
		this.logout();
	}

	@Test
	public void checkShowProfile() {
		// Go to profile
		goToPage("profile/amico");

		// Wait for load page
		sleep(4000);

		// Check if not show profile
		String currentUrl = driver.getCurrentUrl();
		assertThat(currentUrl, not(containsString("profile")));
		log.info("The profile not show correctly");
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
}
