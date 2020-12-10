package com.bookit.step_definitions;


import com.bookit.pages.SelfPage;
import com.bookit.pages.SignInPage;
import com.bookit.utilities.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;

import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.*;


public class MyInfoStepDefs {

	String globalEmail;
	Response response;
	String token;

	String dbFirstName;
	String dbLastName;
	String dbRole;
	String dbTeamName;
	int dbBatch;
	String dbCampus;
	int campusID;



	@Given("user logs in using {string} {string}")
	public void user_logs_in_using(String email, String password) {
	    Driver.get().get(ConfigurationReader.get("url"));
	    Driver.get().manage().window().maximize();
	    SignInPage signInPage = new SignInPage();
	    signInPage.email.sendKeys(email);
	    signInPage.password.sendKeys(password);
		BrowserUtils.waitFor(1);
	    signInPage.signInButton.click();

		globalEmail = email;
		token = BookItApiUtils.generateToken(email,password);

	    	    
	}

	@When("user is on the my self page")
	public void user_is_on_the_my_self_page() {
		BrowserUtils.waitFor(3);
	    SelfPage selfPage = new SelfPage();
	    selfPage.goToSelf();


		
	}

	@Then("name and role information of user in UI, API and DB must be match")
	public void name_and_role_information_of_user_in_UI_API_and_DB_must_be_match() {

		// information from DB
		String query = "select u.id, firstname, lastname, role, u.team_id, t.name, t.batch_number, b.isgraduated, t.campus_id, c.location\n" +
				"from users u join team t\n" +
				"on u.team_id = t.id\n" +
				"inner join batch b\n" +
				"on t.batch_number = b.number\n" +
				"inner join campus c\n" +
				"on t.campus_id = c.id\n" +
				"where u.email = '"+globalEmail+"';";

		Map<String, Object> queryMap = DBUtils.getRowMap(query);
		System.out.println("queryMap = " + queryMap);
		dbFirstName = (String) queryMap.get("firstname");
		dbLastName = (String) queryMap.get("lastname");
		dbRole = (String) queryMap.get("role");
		dbTeamName = (String) queryMap.get("name");
		dbBatch = (int) queryMap.get("batch_number");
		dbCampus = (String) queryMap.get("location");
		campusID = (int) queryMap.get("campus_id");

		String dbFullName = dbFirstName+" "+dbLastName;
		System.out.println("dbFullName = " + dbFullName);
		System.out.println("dbRole = " + dbRole);

		// information from API
		String url = ConfigurationReader.get("qa2api.uri")+"/api/users/me";

		response=     given().accept(ContentType.JSON)
				.and()
				.header("Authorization",token)
				.when()
				.get(url);

		JsonPath jsonPath = response.jsonPath();

		String apiFirstName = jsonPath.getString("firstName");
		String apiLastName = jsonPath.getString("lastName");
		String apiRole = jsonPath.getString("role");
		String apiFullName = apiFirstName+" "+apiLastName;
		System.out.println("apiFirstName = " + apiFirstName);
		System.out.println("apiLastName = " + apiLastName);
		System.out.println("apiRole = " + apiRole);
		System.out.println("apiFullName = " + apiFullName);

		// information from UI
		SelfPage selfPage = new SelfPage();

		String actualUIFullName = selfPage.name.getText();
		String actualUIRole = selfPage.role.getText();
		System.out.println("actualUIFullName = " + actualUIFullName);
		System.out.println("actualUIRole = " + actualUIRole);

		// Compare DB vs API
		Assert.assertEquals(dbFullName, apiFullName);
		Assert.assertEquals(dbRole,apiRole);

		// Compare API and UI
		Assert.assertEquals(dbFullName, actualUIFullName);
		Assert.assertEquals(dbRole, actualUIRole);
	}

	@Then("team information of user in UI, API and DB must be match")
	public void team_information_of_user_in_UI_API_and_DB_must_be_match() {

		System.out.println("dbTeamName = " + dbTeamName);

		// information from API
		String url = "https://cybertek-reservation-api-qa2.herokuapp.com/api/teams/my";
		response=     given().accept(ContentType.JSON)
				.and()
				.header("Authorization",token)
				.when()
				.get(url);

		JsonPath jsonPath = response.jsonPath();
		String apiTeamName = jsonPath.getString("name");
		System.out.println("apiTeamName = " + apiTeamName);

		// information from UI
		SelfPage selfPage = new SelfPage();
		String UITeamName = selfPage.team.getText();
		System.out.println("UITeamName = " + UITeamName);

		// Compare
		Assert.assertEquals(dbTeamName,apiTeamName);
		Assert.assertEquals(apiTeamName, UITeamName);

	}

	@Then("batch information of user in UI, API and DB must be match")
	public void batch_information_of_user_in_UI_API_and_DB_must_be_match() {

		// information from DB
		System.out.println("dbBatch = " + dbBatch);

		// batch information from API
		String url = "https://cybertek-reservation-api-qa2.herokuapp.com/api/batches/my";
		response=     given().accept(ContentType.JSON)
				.and()
				.header("Authorization",token)
				.when()
				.get(url);

		JsonPath jsonPath = response.jsonPath();
		int apiBatch = jsonPath.getInt("number");
		System.out.println("apiBatch = " + apiBatch);

		// information from UI
		SelfPage selfPage = new SelfPage();
		String batchText = selfPage.batch.getText();
		batchText = batchText.replace("#", "");
		int UIbatch = Integer.parseInt(batchText);
		System.out.println("UIbatch = " + UIbatch);

		// Compare
		Assert.assertEquals(dbBatch, apiBatch);
		Assert.assertEquals(UIbatch,apiBatch);
	}

	@Then("campus information of user in UI, API and DB must be match")
	public void campus_information_of_user_in_UI_API_and_DB_must_be_match() {

		// information from DB
		System.out.println("dbCampus = " + dbCampus);

		// information from API
		String url = "https://cybertek-reservation-api-qa2.herokuapp.com/api/campuses/my";
		response=     given().accept(ContentType.JSON)
				.and()
				.header("Authorization",token)
				.when()
				.get(url);

		JsonPath jsonPath = response.jsonPath();
		String apiCampus = jsonPath.getString("location");
		System.out.println("apiCampus = " + apiCampus);

		// information from UI
		SelfPage selfPage = new SelfPage();
		String UICampus = selfPage.campus.getText();
		System.out.println("UICampus = " + UICampus);

		// Compare
		Assert.assertEquals(dbCampus, apiCampus);
		Assert.assertEquals(apiCampus,UICampus);

	}




}
