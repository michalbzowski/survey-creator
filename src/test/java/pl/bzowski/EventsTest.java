package pl.bzowski;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class EventsTest extends MyTestsBase {

    @Test
    public void shouldCreateEventWithoutAttendanceList() {
        registeredUser.lookAtList("events");
        registeredUser.askToCreateNew();
        registeredUser.fillEventFormFields("name: Event1",
                "location: Location 2/3",
                "datetimeInput: 2025-10-08T12:00",
                "description: Longer description for description purpose",
                "checkboxCount: 1",
                "checkboxId1: withAttendanceList",
                "checkboxValue1: False");
        registeredUser.confirm("events");
        registeredUser.lookAtList("events"); //need to go manually, because redirect after events submit
        registeredUser.assertNewEventCreated("eventName: Event1");
    }

    @Test
    public void shouldCreateEventWithAttendanceListWithTwoSelectedPersons() {
        registeredUser.lookAtList("events");
        registeredUser.askToCreateNew();
        registeredUser.fillEventFormFields("name: Event2",
                "location: Location 2/3",
                "datetimeInput: 2025-10-08T12:00",
                "description: Longer description for description purpose",
                "checkboxCount: 1",
                "checkboxId1: withAttendanceList",
                "checkboxValue1: True",
                "clickRadioId: choosePersons");
        registeredUser.fillEventFormFields("checkboxCount: 1", "checkboxId1: michal.bzowski@gmail.com", "checkboxValue1: true");
        registeredUser.fillEventFormFields("checkboxCount: 1", "checkboxId1: 00michal.bzowski@gmail.com", "checkboxValue1: true");
        registeredUser.confirm("events");
        registeredUser.lookAtList("events"); //need to go manually, because redirect after events submit
        registeredUser.assertNewEventCreated("eventName: Event2");
    }

    @Test
    public void shouldCreateEventWithAttendanceListWithOneSelectedGroup() {
        GroupsTest.createGroup(this.registeredUser, "groupName: Conductor", "checkboxCount: 1", "checkboxId1: michal.bzowski@gmail.com", "checkboxValue1: True");
        GroupsTest.createGroup(this.registeredUser, "groupName: Trombones", "checkboxCount: 2", "checkboxId1: michal.bzowski@gmail.com", "checkboxValue1: True",
                "checkboxId2: 00michal.bzowski@gmail.com", "checkboxValue2: True");

        registeredUser.lookAtList("events");
        registeredUser.askToCreateNew();
        registeredUser.fillEventFormFields("name: Event3",
                "location: Location 2/3",
                "datetimeInput: 2025-10-08T12:00",
                "description: Longer description for description purpose",
                "checkboxCount: 1",
                "checkboxId1: withAttendanceList",
                "checkboxValue1: True",
                "clickRadioId: chooseGroup");
        registeredUser.fillEventFormFields("checkboxCount: 1",
                "checkboxId1: Conductor", "checkboxValue1: true");
        registeredUser.confirm("events");
        registeredUser.lookAtList("events"); //need to go manually, because redirect after events submit
        registeredUser.assertNewEventCreated("eventName: Event3");
    }

    @Test
    public void shouldCreateEventWithAttendanceListWithTwoSelectedGroup() {
        registeredUser.lookAtList("events");
        registeredUser.askToCreateNew();
        registeredUser.fillEventFormFields("name: Event2",
                "location: Location 2/3",
                "datetimeInput: 2025-10-08T12:00",
                "description: Longer description for description purpose",
                "checkboxId: withAttendanceList",
                "checkboxValue: True",
                "clickRadioId: chooseGroup");
//        registeredUser.fillEventFormFields("checkboxId: michal.bzowski@gmail.com", "checkboxValue: true");
//        registeredUser.fillEventFormFields("checkboxId: 00michal.bzowski@gmail.com", "checkboxValue: true");
//        registeredUser.confirm("events");
//        registeredUser.lookAtList("events"); //need to go manually, because redirect after events submit
//        registeredUser.assertNewEventCreated("eventName: Event2");
    }

    @Test
    public void shouldCreateEventWithAttendanceListWithOneEntryForPersonWhoIsInTwoGroupsAtOnce() {
        registeredUser.lookAtList("events");
        registeredUser.askToCreateNew();
        registeredUser.fillEventFormFields("name: Event2",
                "location: Location 2/3",
                "datetimeInput: 2025-10-08T12:00",
                "description: Longer description for description purpose",
                "checkboxId: withAttendanceList",
                "checkboxValue: True",
                "clickRadioId: chooseGroup");
//        registeredUser.fillEventFormFields("checkboxId: michal.bzowski@gmail.com", "checkboxValue: true");
//        registeredUser.fillEventFormFields("checkboxId: 00michal.bzowski@gmail.com", "checkboxValue: true");
//        registeredUser.confirm("events");
//        registeredUser.lookAtList("events"); //need to go manually, because redirect after events submit
//        registeredUser.assertNewEventCreated("eventName: Event2");
    }

    @Test
    public void shouldNotCreateEventWithEmptyAttendanceListForSelectPersonOption() {

    }

    @Test
    public void shouldNotCreateEventWithEmptyGroupSelectionForSelectGroupOption() {

    }


}
