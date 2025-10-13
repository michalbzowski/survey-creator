-- Tworzymy tabelę members
    create table members (
        teamAnswered boolean not null,
        id uuid not null,
        linkToken uuid not null unique,
        personId uuid,
        teamId uuid not null,
        personEmail varchar(255),
        personFirstName varchar(255),
        personLastName varchar(255),
        personTag varchar(255),
        primary key (id)
    )

-- Tworzymy tabelę łączącą osoby i grupy (wiele do wielu)
CREATE TABLE team_member (
    team_id UUID NOT NULL,
    member_id UUID NOT NULL,
    PRIMARY KEY (team_id, member_id),
    CONSTRAINT fk_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE,
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
);

-- Indeksy dla szybszych zapytań
CREATE INDEX idx_team_member_team ON person_group(team_id);
CREATE INDEX idx_team_member_member ON person_group(member_id);
