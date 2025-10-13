package pl.bzowski.utils;

public interface Driver {
    void openSystem();

    void fillNewTagDetails(String name);

    void submitNewTag();

    void assertNewTagCreated(String name);

    void askToCreateNew();

    void exit();

    void deleteTag(String name);

    void assertTagNotExists(String name);

    void lookAtList(String listName);

    void fillNewPersonDetails(String firstName, String lastName, String email, String defaultTag, String groups);

    void confirmNewPerson();

    void assertNewPersonCreated(String firstName);

    void fillFormFields(String fieldName, String groupName);

    void confirm(String formName);

    void check(String checkboxId, Boolean checkboxValue);

    void isChecked(String value);

    void edit(String rowData);

    void assertExistsOnList(String name);

    void fillFormValue(String id, String value);

    void assertNewEventCreated(String eventName);

    void select(String radioId);

    void lookAtDetails(String listName, String rowName);

    void assertMemberWasSelected(String memberEmail);
}
