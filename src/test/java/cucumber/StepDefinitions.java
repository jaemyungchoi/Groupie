package cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.checkerframework.checker.units.qual.C;
import org.junit.BeforeClass;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Step definitions for Cucumber tests.
 */
public class StepDefinitions {
    private static final String ROOT_URL = "https://localhost:8443/";

    private WebDriver driver;

    private String tempEventTitle;

    @Before
    public void initDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        chromeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        driver = new ChromeDriver(chromeOptions);
    }
//    @Before
//    public void setImplicitWait() {
//        driver.manage().timeouts().implicitlyWait(Duration.ofHours(1));
//    }

    @Given("I am on the index page")
    public void i_am_on_the_index_page() {
        driver.get(ROOT_URL);
    }

    @When("I click the link {string}")
    public void i_click_the_link(String linkText) {
        driver.findElement(By.linkText(linkText)).click();
    }

    @Then("I should see header {string}")
    public void i_should_see_header(String header) {
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains(header));
    }

    @Then("I should see text {string}")
    public void i_should_see_text(String text) {
        assertTrue(driver.getPageSource().contains(text));
    }

    //Step Definitions for signup.feature
    @Given("I am on the sign up page")
    public void i_am_on_the_sign_up_page() {
        driver.get(ROOT_URL + "signup");
    }

    @When("I input {string} in the field {string}")
    public void i_input_in_the_field(String string, String string2) {
        WebElement input = driver.findElement(By.cssSelector("input[placeholder='" + string2 + "']"));
        input.sendKeys(string);
    }

    @When("I click button {string}")
    public void i_click_button(String string) {
        WebElement button = driver.findElement(By.xpath("//*[contains(@class, 'ui') and contains(@class, 'button') and text()='" + string + "']"));
        button.click();
    }

    @Then("I should see error message {string}")
    public void i_should_see_error_message(String string) {
        WebElement error_message = driver.findElement(By.cssSelector("div.error.message"));
        String text = error_message.getText();
        assertEquals(string, text);
    }

    //Step Definitions for login.feature
    @When("I press Log In Submit")
    public void i_press_log_in_submit() {
        driver.findElement(By.cssSelector("#login-submit")).click();
    }

    @Then("I should see the loggedin page open")
    public void i_should_see_the_loggedin_page_open() {
        assertEquals(driver.getCurrentUrl(), ROOT_URL + "loggedin.jsp");
    }

    @Then("I should land on {string}")
    public void i_should_land_on_url(String string) {
        assertEquals(string, driver.getCurrentUrl());
    }

    // block_list.feature
    @Given("I log in with username {string} and password {string}")
    public void i_log_in_with_username_and_password(String string, String string2) {
        i_am_on_the_index_page();
        i_input_in_the_field(string, "Enter Username");
        i_input_in_the_field(string2, "Enter Password");
        i_click_button("Log In");
    }

    @When("I go to the block list page")
    public void i_go_to_the_block_list_page() {
        driver.get(ROOT_URL + "block-list");
    }

    @Then("I should see {string} in my block list")
    public void i_should_see_in_my_block_list(String string) {
        List<WebElement> entries = driver.findElements(By.xpath("//td//p[text()='" + string + "']"));
        assertEquals(1, entries.size());
    }

    @When("I click delete button next to {string}")
    public void i_click_delete_button_next_to(String string) {
        WebElement entry = driver.findElement(By.xpath("//td[//p[text()='" + string + "']]"));
        WebElement button = entry.findElement(By.cssSelector(".ui.button"));
        button.click();
    }

    @Then("I should not see {string} in my block list")
    public void i_should_not_see_in_my_block_list(String string) {
        List<WebElement> entries = driver.findElements(By.xpath("//td//p[text()='" + string + "']"));
        assertEquals(0, entries.size());
    }

    // create_proposal.feature
    @When("I go to the create proposal page")
    public void i_go_to_the_create_proposal_page() {
        driver.get(ROOT_URL + "create-proposal");
    }

    @When("I wait for things to load \\(time limit {string} milliseconds)")
    public void i_wait_for_things_to_load_time_limit_milliseconds(String string) {
        new WebDriverWait(driver, Duration.ofMillis(Integer.parseInt(string)))
                .until(ExpectedConditions.and(
                        ExpectedConditions.numberOfElementsToBe(By.cssSelector(".ui.loading"), 0),
                        ExpectedConditions.numberOfElementsToBe(By.cssSelector(".loading.icon"), 0),
                        ExpectedConditions.numberOfElementsToBe(By.cssSelector(".ui.placeholder"), 0),
                        ExpectedConditions.numberOfElementsToBe(By.cssSelector(".ui.active.dimmer"), 0)
                ));
    }

    @Then("I should see at least one USC related event and its title links to a Ticketmaster event page")
    public void i_should_see_at_least_one_USC_related_event_and_its_title_links_to_a_Ticketmaster_event_page() {
        WebElement title = driver.findElement(By.xpath("//div[@id='search-results']//div[@class='item']//a[contains(text(), 'USC')]"));
        assertTrue(title.getAttribute("href").contains("ticket"));
    }

    @When("I write down the title of the first event and click Add button next to the event")
    public void i_write_down_the_title_of_the_first_event_and_click_Add_button_next_to_the_event() {
        WebElement eventItem = driver.findElement(By.cssSelector("#search-results .item"));
        WebElement title = eventItem.findElement(By.cssSelector(".header"));
        tempEventTitle = title.getText();
        WebElement button = eventItem.findElement(By.cssSelector(".ui.button"));
        button.click();
    }

    @Then("I should see that event show up in Selected Events section")
    public void i_should_see_that_event_show_up_in_Selected_Events_section() {
        WebElement title = driver.findElement(By.xpath("//div[@id='selected-events']//div[@class='item']//a[text()='" + tempEventTitle + "']"));
    }

    @When("I click Remove button next to that event in Selected Events section")
    public void i_click_Remove_button_next_to_that_event_in_Selected_Events_section() {
        WebElement eventItem = driver.findElement(By.xpath("//div[@id='selected-events']//div[@class='item'][//a[text()='" + tempEventTitle + "']]"));
        WebElement button = eventItem.findElement(By.cssSelector(".ui.button"));
        button.click();
    }

    @Then("I should not see that event show up in Selected Events section")
    public void i_should_not_see_that_event_show_up_in_Selected_Events_section() {
        List<WebElement> matchingEvents = driver.findElements(By.xpath("//div[@id='selected-events']//div[@class='item']//a[text()='" + tempEventTitle + "']"));
        assertEquals(0, matchingEvents.size());
    }

    @When("I input {string} in the invite user dropdown")
    public void i_input_in_the_invite_user_dropdown(String string) {
        driver.findElement(By.xpath("//div[@id='invite-dropdown']/input")).sendKeys(string);
    }

    @When("I focus on the invite user dropdown and press enter")
    public void i_focus_on_the_invite_user_dropdown_and_press_enter() {
        driver.findElement(By.xpath("//div[@id='invite-dropdown']/input")).sendKeys(Keys.ENTER);
    }

    @Then("I should see {string} in the invited users list")
    public void i_should_see_in_the_invited_users_list(String string) {
        driver.findElement(By.xpath("//div[contains(@class, 'ui') and contains(@class, 'label') and text()='" + string + "']"));
    }

    @Then("I should not see {string} in the invited users list")
    public void i_should_not_see_in_the_invited_users_list(String string) {
        List<WebElement> labels = driver.findElements(By.xpath("//div[contains(@class, 'ui') and contains(@class, 'label') and text()='" + string + "']"));
        assertEquals(0, labels.size());
    }

    @Then("I should see {string} marked as unavailable in the candidate list")
    public void i_should_see_marked_as_unavailable_in_the_candidate_list(String string) {
        driver.findElement(By.xpath("//div[@id='invite-dropdown']/div[contains(@class, 'menu')]/div[contains(@class, 'item') and span[text()='" + string + "'] and span[text()='unavailable']]"));
    }

    @When("I click the delete icon next to user {string}")
    public void i_click_the_delete_icon_next_to(String string) {
        WebElement icon = driver.findElement(By.xpath("//div[contains(@class, 'ui') and contains(@class, 'label') and text()='" + string + "']/i[@class='delete icon']"));
        icon.click();
    }

    @When("I mouseover the delete icon next to {string}")
    public void i_mouseover_the_delete_icon_next_to(String string) {
        WebElement label = driver.findElement(By.xpath("//div[contains(@class, 'ui') and contains(@class, 'label') and text()='" + string + "']"));
        WebElement icon = label.findElement(By.cssSelector(".delete.icon"));
        Actions builder = new Actions(driver);
        builder.moveToElement(icon).perform();
    }

    @Then("I should see the delete icon next to {string} turn red")
    public void i_should_see_the_delete_icon_next_to_turn_red(String string) {
        WebElement label = driver.findElement(By.xpath("//div[contains(@class, 'ui') and contains(@class, 'label') and text()='" + string + "']"));
        WebElement icon = label.findElement(By.cssSelector(".delete.icon"));
        assertEquals("rgba(255, 0, 0, 1)", icon.getCssValue("color"));
    }

    @When("I select {string} in the {string} dropdown")
    public void i_select_in_the_dropdown(String string, String string2) {
        WebElement dropdown = driver.findElement(By.xpath("//label[text()='" + string2 + "']/following-sibling::div[contains(@class, 'dropdown')]"));
        dropdown.click();
        WebElement item = dropdown.findElement(By.xpath(".//div[contains(@class, 'item') and span[text()='" + string + "']]"));
        item.click();
    }

    // proposal-list.feature
    @When("I go to the proposal list page")
    public void i_go_to_the_proposal_list_page() {
        driver.get(ROOT_URL);
    }

    @Then("I should see header one {string}")
    public void i_should_see_header_one(String header) {
        assertTrue(driver.findElement(By.cssSelector("h1")).getText().contains(header));
    }

    @Then("I should see {string} marked as {string}")
    public void i_should_see_marked_as(String proposal, String status) {
        WebElement proposalItem = driver.findElement(By.xpath("//div[@class='ui segment' and h2[text()='Proposals']]"
                + "/div[contains(@class, 'items')]/div[@class='item' and //a[text()='" + proposal + "']]"));
        WebElement label = proposalItem.findElement(By.cssSelector(".ui.label"));
        assertEquals(status, label.getText());
    }

    @And("I should see {string} in {string} under {string}")
    public void i_should_see_in_under(String name, String list, String proposal) {
        WebElement proposalItem = driver.findElement(By.xpath("//div[@class='ui segment' and h2[text()='Proposals']]"
                + "/div[contains(@class, 'items')]/div[@class='item' and descendant::a[text()='" + proposal + "']]"));
        WebElement userList = proposalItem.findElement(By.xpath(".//div[@class='header' and text()='" + list + "']/following-sibling::div[@class='description']"));
        assertTrue(userList.getText().contains(name));
    }

    private Date getProposalDate(WebElement proposalItem) throws ParseException {
        WebElement meta = proposalItem.findElement(By.cssSelector("div.meta"));
        String metaText = meta.getText();
        String[] metaFragments = metaText.replaceAll("(?<=\\d)(st|nd|rd|th)", "").split(" on ");
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy, hh:mm a", Locale.ENGLISH);
        Date date = formatter.parse(metaFragments[metaFragments.length - 1]);
        try {
            WebElement bestEvent = proposalItem.findElement(By.xpath(".//div[@class='header' and text()='Best Event']/following-sibling::div[@class='description']"));
            String bestEventText = bestEvent.getText();
            String[] bestEventFragments = bestEventText.replaceAll("(?<=\\d)(st|nd|rd|th)", "").split("; ");
            date = formatter.parse(bestEventFragments[bestEventFragments.length - 1]);
        } catch (NoSuchElementException ex) {
            // no best event, just use the proposal's creation time
        }
        return date;
    }

    @Then("I should see proposals ordered from newest to oldest")
    public void i_should_see_proposals_ordered_from_newest_to_oldest() throws ParseException {
        List<WebElement> proposalItems = driver.findElements(By.xpath("//div[@class='ui segment' and h2[text()='Proposals']]"
                + "/div[contains(@class, 'items')]/div[@class='item']"));
        Date lastDate = null;
        for (WebElement proposalItem : proposalItems) {
            Date currDate = getProposalDate(proposalItem);
            assertTrue(lastDate == null || currDate.compareTo(lastDate) <= 0);
            lastDate = currDate;
        }
    }

    @Then("I should see proposals ordered from oldest to newest")
    public void i_should_see_proposals_ordered_from_oldest_to_newest() throws ParseException {
        List<WebElement> proposalItems = driver.findElements(By.xpath("//div[@class='ui segment' and h2[text()='Proposals']]"
                + "/div[contains(@class, 'items')]/div[@class='item']"));
        Date lastDate = null;
        for (WebElement proposalItem : proposalItems) {
            Date currDate = getProposalDate(proposalItem);
            assertTrue(lastDate == null || currDate.compareTo(lastDate) >= 0);
            lastDate = currDate;
        }
    }

    @And("I click {string} toggle")
    public void i_click_toggle(String arg0) {
        WebElement toggle = driver.findElement(By.xpath("//div[@class='ui toggle checkbox' and label[text()='" + arg0 + "']]"));
        toggle.click();
    }

    @And("I click button {string} on the calendar's toolbar")
    public void i_click_button_on_the_calendars_toolbar(String arg0) {
        WebElement button = driver.findElement(By.xpath("//div[@class='rbc-calendar']//button[text()='" + arg0 + "']"));
        button.click();
    }

    @Then("I should see proposal {string} on the calendar")
    public void i_should_see_proposal_on_the_calendar(String arg0) {
        driver.findElement(By.xpath("//div[@class='rbc-calendar']//div[contains(@class,'rbc-event')]/div[@class='rbc-event-content' and text()='" + arg0 + "']"));
    }

    @Then("I should not see proposal {string} on the calendar")
    public void i_should_not_see_proposal_on_the_calendar(String arg0) {
        List<WebElement> proposals = driver.findElements(By.xpath("//div[@class='rbc-calendar']//div[contains(@class,'rbc-event')]/div[@class='rbc-event-content' and text()='" + arg0 + "']"));
        assertEquals(0, proposals.size());
    }

    @And("I forward the calendar to {string}")
    public void i_forward_the_calendar_to(String arg0) {
        while (!driver.findElement(By.cssSelector("div.rbc-calendar span.rbc-toolbar-label")).getText().equals(arg0)) {
            i_click_button_on_the_calendars_toolbar("Next");
        }
    }

    // proposal details page
    @When("I go to details page for proposal {string}")
    public void i_go_to_details_page_for_proposal(String string) {
        i_go_to_the_proposal_list_page();
        i_wait_for_things_to_load_time_limit_milliseconds("10000");
        i_click_the_proposal_title(string);
    }

    @When("I click the proposal title {string}")
    public void i_click_the_proposal_title(String string) {
        WebElement title = driver.findElement(By.xpath("//div[@class='ui segment' and h2[text()='Proposals']]"
                + "/div[contains(@class, 'items')]/div[@class='item']//a[text()='" + string + "']"));
        title.click();
    }

    @When("I go to details page for draft {string}")
    public void i_go_to_details_page_for_draft(String string) {
        i_go_to_the_proposal_list_page();
        i_wait_for_things_to_load_time_limit_milliseconds("10000");
        WebElement title = driver.findElement(By.xpath("//div[@class='ui segment' and h2[text()='Drafts']]"
                + "/div[contains(@class, 'items')]/div[@class='item']//a[text()='" + string + "']"));
        title.click();
    }

    @When("I click the delete button next to event {string}")
    public void i_click_the_delete_button_next_to_event(String string) {
        WebElement eventCard = driver.findElement(By.xpath("//div[@class='ui card' and //a[text()='" + string + "']]"));
        WebElement deleteButton = eventCard.findElement(By.xpath(".//button[text()='Delete']"));
        deleteButton.click();
    }

    @When("I should not see event {string}")
    public void i_should_not_see_event(String string) {
        List<WebElement> eventCards = driver.findElements(By.xpath("//div[@class='ui card' and //a[text()='" + string + "']]"));
        assertEquals(0, eventCards.size());
    }

    @And("I should not see {string} under {string}")
    public void i_should_not_see_under(String arg0, String arg1) {
        WebElement section = driver.findElement(By.xpath("//div[@class='ui small header' and text()='"
                + arg1 + "']/following-sibling::div[@class='ui labels']"));
        List<WebElement> names = section.findElements(By.xpath("div[@class='ui label' and text()='" + arg0 + "']"));
        assertEquals(0, names.size());
    }

    @And("I should see {string} under {string}")
    public void i_should_see_under(String arg0, String arg1) {
        WebElement section = driver.findElement(By.xpath("//div[@class='ui small header' and text()='"
                + arg1 + "']/following-sibling::div[@class='ui labels']"));
        List<WebElement> names = section.findElements(By.xpath("div[@class='ui label' and text()='" + arg0 + "']"));
        assertEquals(1, names.size());
    }

    @And("I select {string} in dropdown {string} under event {string}")
    public void i_select_in_dropdown_under_event(String value, String name, String event) {
        WebElement dropdown = driver.findElement(By.xpath("//div[@class='ui card' and //a[text()='" + event + "']]"
                + "//label[text()='" + name + "']/following-sibling::div[contains(@class, 'dropdown')]"));
        dropdown.click();
        WebElement item = dropdown.findElement(By.xpath(".//div[contains(@class, 'item') and span[text()='" + value + "']]"));
        item.click();
    }

    @And("I refresh the page")
    public void i_refresh_the_page() {
        driver.navigate().refresh();
    }

    @Then("I should see dropdown {string} has value {string} under event {string}")
    public void i_should_see_dropdown_has_value_under_event(String name, String value, String event) {
        WebElement dropdown = driver.findElement(By.xpath("//div[@class='ui card' and .//a[text()='" + event + "']]"
                + "//label[text()='" + name + "']/following-sibling::div[contains(@class, 'dropdown')]"));
        WebElement text = dropdown.findElement(By.cssSelector(".divider.text"));
        assertEquals(value, text.getText());
    }

    public void i_should_see_event_label_value(String event, String value, String icon) {
        WebElement eventCard = driver.findElement(By.xpath("//div[@class='ui card' and //a[text()='" + event + "']]"));
        WebElement label = eventCard.findElement(By.xpath(".//div[@class='ui label' and i[@class='" + icon + "']]"));
        assertEquals(value, label.getText());
    }

    @And("I should see {string} has yes count {string}")
    public void i_should_see_has_yes_count(String event, String value) {
        i_should_see_event_label_value(event, value, "check icon");
    }

    @And("I should see {string} has no count {string}")
    public void i_should_see_has_no_count(String event, String value) {
        i_should_see_event_label_value(event, value, "cancel icon");
    }

    @And("I should see {string} has maybe count {string}")
    public void iShouldSeeHasMaybeCount(String event, String value) {
        i_should_see_event_label_value(event, value, "help icon");
    }

    @And("I should see {string} has average excitement score {string}")
    public void iShouldSeeHasAverageExcitementScore(String event, String value) {
        i_should_see_event_label_value(event, value, "heart icon");
    }

    @Then("I should see {string} selected as the best event")
    public void i_should_see_selected_as_the_best_event(String title) {
        WebElement bestEventCards = driver.findElement(By.xpath("//h3[text()='Best Event']/following-sibling::div[contains(@class, 'cards')]"));
        WebElement bestEventTitle = bestEventCards.findElement(By.cssSelector(".ui.card > .content > .header"));
        assertEquals(title, bestEventTitle.getText());
    }

    @And("I should not see the {string} Button")
    public void i_should_not_see_the_button(String buttonText) {
        List<WebElement> buttons = driver.findElements(By.xpath("//*[contains(@class, 'ui') and contains(@class, 'button') and text()='" + buttonText + "']"));
        assertEquals(0, buttons.size());
    }

    @And("I should see {string} links to a Ticketmaster detail page")
    public void i_should_see_links_to_a_ticketmaster_detail_page(String event) {
        WebElement eventCard = driver.findElement(By.xpath("//div[@class='ui card' and //a[text()='" + event + "']]"));
        WebElement title = eventCard.findElement(By.cssSelector("a.header"));
        assertTrue(title.getAttribute("href").contains("ticket"));
    }

    @And("I should see {string} has meta {string}")
    public void i_should_see_has_meta(String event, String metaText) {
        WebElement eventCard = driver.findElement(By.xpath("//div[@class='ui card' and //a[text()='" + event + "']]"));
        WebElement meta = eventCard.findElement(By.cssSelector("div.meta"));
        assertEquals(metaText, meta.getText());
    }

    @After()
    public void after() {
        driver.quit();
    }

    @And("I wait")
    public void iWait() {
        driver.manage().timeouts().implicitlyWait(Duration.ofHours(24));
        driver.findElement(By.cssSelector(".random-class-should-not-exist"));
    }
}
