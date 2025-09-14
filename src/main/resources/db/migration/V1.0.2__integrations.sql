-- Flyway migration: V1__create_messenger_and_integrations_tables.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- Rozszerzenie do UUID, jeśli jest potrzebne

CREATE TABLE messenger_user_agreement (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    psid VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    messenger_registration_key UUID NOT NULL,
    registered_user_id UUID NOT NULL,
    agree BOOLEAN NOT NULL
);

CREATE INDEX idx_messenger_registration_key ON messenger_user_agreement(messenger_registration_key);
CREATE INDEX idx_registered_user_id_mua ON messenger_user_agreement(registered_user_id);

CREATE TABLE integrations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registered_user_id UUID NOT NULL,
    configuration JSONB
);

CREATE INDEX idx_registered_user_id_integrations ON integrations(registered_user_id);
