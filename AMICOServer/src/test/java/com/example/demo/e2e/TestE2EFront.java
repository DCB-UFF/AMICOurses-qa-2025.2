package com.example.demo.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.file.Paths;
import java.util.List;
import java.io.File;

import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
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

	protected String userCreatedInTest;

	@ParameterizedTest
	@CsvSource({
		// name,startDate,endDate,language,type,skill,description,msgExpected
		"SUCCESS Course,01/10/2024,25/12/2025,English,typeTest,skillTest,descTest,SUCCESS", // Dados válidos
		",01/10/2024,25/12/2025,English,typeTest,skillTest,descTest,FAIL", // Nome vazio
		"FAIL Course1,01/10/2024,25/12/2025,,typeTest,skillTest,descTest,FAIL", // Language vazio
		"FAIL Course2,01/10/2024,25/12/2025,English,typeTest,skillTest,,FAIL", // Description vazio
		"FAIL Course3,01/10/2024,25/12/2025,English,,skillTest,descTest,FAIL", // Type vazio
		"FAIL Course4,01/10/2024,25/12/2025,English,typeTest,,descTest,FAIL" // Skill vazio
	})
	public void checkCreateCourse(String name, String startDateStr, String endDateStr, String language,
			String type,  String skill, String description, String msgExpected, TestInfo testInfo) {
		// Login
		goToPage("login");
		this.loginUser("admin", "pass");

		String realName = (name == null) ? "" : name;
        String realLanguage = (language == null) ? "" : language;
        String realType = (type == null) ? "" : type;
        String realSkill = (skill == null) ? "" : skill;
        String realDesc = (description == null) ? "" : description;

		// Create course
		goToPage("admin");

		waitUntil(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(), 'Management')]")), "Management is not available", 5);
		driver.findElement(By.xpath("//span[contains(text(), 'Management')]")).click();
		waitUntil(ExpectedConditions.elementToBeClickable(By.linkText("Add Course")), "Add Course botton is not visible", 5);
		driver.findElement(By.linkText("Add Course")).click();

		WebElement nameField = getVisibleElement(By.id("name-input"));
		WebElement startDateField = getVisibleElement(By.name("startDate"));
		WebElement endDateField = getVisibleElement(By.name("endDate"));
		WebElement languageField = getVisibleElement(By.name("newLanguage"));
		WebElement typeField = getVisibleElement(By.name("newType"));	
		WebElement skillField = getVisibleElement(By.name("newSkill1"));
		WebElement descriptionField = getVisibleElement(By.id("description-input"));
		WebElement submit = getVisibleElement(By.cssSelector("button[type='submit']"));
		WebElement image = getVisibleElement(By.name("courseImage"));

        String imageUpload = System.getProperty("user.dir") + "/src/test/resources/test.jpeg";
        try { image.sendKeys(imageUpload); } catch (Exception e) {}
		
		sendKeysAfterClear(nameField, realName);
		sendKeysAfterClear(startDateField, startDateStr);
		sendKeysAfterClear(endDateField, endDateStr);
		sendKeysAfterClear(languageField, realLanguage);
		sendKeysAfterClear(typeField, realType);	
		sendKeysAfterClear(skillField, realSkill);
		sendKeysAfterClear(descriptionField, realDesc);
		sleep(200);

		submit.click();

		waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("dataTable1")), "Failed creating course", 3);
		log.info("Course created correctly");

		WebElement divTable = driver.findElement(By.className("table-responsive"));
		List<WebElement> pagesCourses = divTable.findElements(By.className("page-item"));
		
		if (pagesCourses.size() > 2) {
            WebElement lastPageCourses = pagesCourses.get(pagesCourses.size() - 2);
            lastPageCourses.click();
            sleep(400); // Tempo para tabela recarregar
        }

        WebElement lastCourseRow = getLastCourse();
        WebElement inputName = lastCourseRow.findElement(By.name("newName"));
        String nameLastCourse = inputName.getAttribute("value");

        if ("SUCCESS".equalsIgnoreCase(msgExpected)) {
            assertThat("Erro: O curso não foi criado!", nameLastCourse, IsEqualIgnoringCase.equalToIgnoringCase(realName));
            log.info("SUCESSO: Curso criado e encontrado na tabela: " + realName);

            WebElement btnDelete = lastCourseRow.findElement(By.name("btnDelete"));
            btnDelete.click();
            
            sleep(400);
            WebElement checkLastCourse = getLastCourse();
            String checkName = checkLastCourse.findElement(By.name("newName")).getAttribute("value");
            assertThat("Erro: Falha ao deletar curso", checkName, not(IsEqualIgnoringCase.equalToIgnoringCase(realName)));

        } else {

            if (!realName.isEmpty()) {
                assertThat("ERRO GRAVE: O sistema permitiu criar curso inválido: " + realName, 
                          nameLastCourse, not(IsEqualIgnoringCase.equalToIgnoringCase(realName)));
            }
            
            log.info("SUCESSO: O curso inválido não apareceu na última posição da tabela.");
        }

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
		// Teste para validar que o perfil de um usuário não é mostrado sem login
		goToPage("profile/amico");

		sleep(1000);

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

	@ParameterizedTest
	@CsvSource({
		// username,email,password,repeatPassword,msgExpected
		"u,auto,pass1234,pass1234,SUCCESS", //senha menor tamanho valido
		"u,auto,password123456,password123456,SUCCESS", //senha maior tamanho valido
		"user1,user1@gmail.com,password123,password123,SUCCESS", //username menor tamanho valido
		"user1user1user1,user2@gmail.com,password123,password123,SUCCESS", //username maior tamanho valido
		"userTest2,invalidEmail,password123,password123,FAIL", //email invalido
		"user,usertest1@gmail.com,password123,password123,FAIL", //username menor que o tamanho minimo
		"user1user1user12,usertest1@gmail.com,password123,password123,FAIL", //username maior que o tamanho maximo
		"userTest3,usertest1@gmail.com,passwor,passwor,FAIL", //senha menor que o tamanho minimo
		"userTest3,usertest1@gmail.com,password1234567,password1234567,FAIL", //senha maior que o tamanho maximo
		"userTest4,usertest1@gmail.com,password123,differentPass,FAIL" //senhas diferentes
	})
	public void checkSignUp(String username, String email, String password, String repeatPassword, String msgExpected) {

		String realUsername = username;
        String realEmail = email;
		
		if ("SUCCESS".equals(msgExpected.trim())){
			realUsername = username + System.currentTimeMillis();
			this.userCreatedInTest = realUsername;
			if (email.equals("auto")){
				realEmail = realUsername + "@gmail.com";
			}
		}

		goToPage("signup");

		WebElement userField = getVisibleElement(By.name("username"));
		WebElement emailField = getVisibleElement(By.name("userMail"));
		WebElement passField = getVisibleElement(By.name("password"));
		WebElement repeatPassField = getVisibleElement(By.name("repeatPassword"));
		WebElement submit = getVisibleElement(By.cssSelector("button[type='submit']"));

		sendKeysAfterClear(userField, realUsername);
		sendKeysAfterClear(emailField, realEmail);
		sendKeysAfterClear(passField, password);
		sendKeysAfterClear(repeatPassField, repeatPassword);
		submit.click();

		if ("SUCCESS".equals(msgExpected)) {
            waitUntil2(ExpectedConditions.urlContains("/registered"), 
                      "Falha: Deveria ter ido para /registered", 5);
            goToPage("login");
            loginUser(realUsername, password);
            goToPage("profile/" + realUsername);
            assertThat(driver.getCurrentUrl(), containsString("profile"));
            log.info("SUCESSO: Usuário criado e validado: " + realUsername);
        } else {
            sleep(1000); 
            try {
                WebElement campoProva = driver.findElement(By.name("repeatPassword"));
                assertTrue(campoProva.isDisplayed(), "Erro: O campo repeatPassword existe mas está invisível!");
                log.info("SUCESSO: O cadastro foi bloqueado e o formulário continua na tela.");
            } catch (NoSuchElementException e) {
                // SE ENTRAR AQUI, SIGNIFICA QUE O CAMPO SUMIU
                this.userCreatedInTest = realUsername; 
                throw new AssertionError("ERRO GRAVE: O campo 'repeatPassword' sumiu! O sistema cadastrou o usuário inválido?");
            }
		}
		
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
