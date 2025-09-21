package pl.bzowski.utils;

public interface Driver {
    void openSystem();

    void fillNewTagDetails(String name);

    void submitNewTag();

    void assertNewTagCreated(String name);

    void askToCreateNewTag();

    void lookAtTagsList();

    void exit();

    void deleteTag(String name);

    void assertTagNotExists(String name);

    void lookAtPersonsList();

    void askToCreateNewPerson();

    void fillNewPersonDetails(String firstName, String lastName, String email, String defaultTag, String groups);

    void confirmNewPerson();

    void assertNewPersonCreated(String firstName);
}
