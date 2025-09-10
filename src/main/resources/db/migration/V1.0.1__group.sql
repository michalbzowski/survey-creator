-- Tworzymy tabelę grup
CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    registered_user_id UUID NOT NULL
);

-- Tworzymy tabelę łączącą osoby i grupy (wiele do wielu)
CREATE TABLE person_group (
    person_id UUID NOT NULL,
    group_id UUID NOT NULL,
    PRIMARY KEY (person_id, group_id),
    CONSTRAINT fk_person FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE CASCADE,
    CONSTRAINT fk_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);

-- Indeksy dla szybszych zapytań
CREATE INDEX idx_person_group_person ON person_group(person_id);
CREATE INDEX idx_person_group_group ON person_group(group_id);
