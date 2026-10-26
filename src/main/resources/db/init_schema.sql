-- Схема БД для лабораторной работы №1 (Information Systems)
-- Выполнять от имени пользователя s465029 в базе studs на хосте pg
-- Схема: s465029

SET search_path TO s465029;

-- Последовательности для автогенерации id (генерация полей id средствами БД)
CREATE SEQUENCE IF NOT EXISTS book_creature_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS coordinates_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS magic_city_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS ring_seq START WITH 1 INCREMENT BY 1;

-- Координаты (1:1 с BookCreature)
CREATE TABLE IF NOT EXISTS coordinates (
    id BIGINT PRIMARY KEY DEFAULT nextval('coordinates_seq'),
    x BIGINT NOT NULL,
    y BIGINT NOT NULL
);

-- Кольцо (ссылка от BookCreature, может быть null)
CREATE TABLE IF NOT EXISTS ring (
    id BIGINT PRIMARY KEY DEFAULT nextval('ring_seq'),
    name VARCHAR(255) NOT NULL,
    power BIGINT,
    CONSTRAINT ring_name_not_empty CHECK (length(trim(name)) > 0),
    CONSTRAINT ring_power_positive CHECK (power IS NULL OR power > 0)
);

-- Волшебный город (ссылка от BookCreature, может быть null)
CREATE TABLE IF NOT EXISTS magic_city (
    id BIGINT PRIMARY KEY DEFAULT nextval('magic_city_seq'),
    name VARCHAR(255) NOT NULL,
    area FLOAT NOT NULL,
    population BIGINT NOT NULL,
    establishment_date TIMESTAMP,
    governor VARCHAR(32) NOT NULL,
    capital BOOLEAN NOT NULL,
    population_density DOUBLE PRECISION NOT NULL,
    CONSTRAINT magic_city_name_not_empty CHECK (length(trim(name)) > 0),
    CONSTRAINT magic_city_area_positive CHECK (area > 0),
    CONSTRAINT magic_city_population_positive CHECK (population > 0),
    CONSTRAINT magic_city_governor_valid CHECK (governor IN ('HOBBIT','ELF','HUMAN','GOLLUM')),
    CONSTRAINT magic_city_density_positive CHECK (population_density > 0)
);

-- Индексы для ускорения фильтрации, сортировки и JOIN'ов на списке существ
CREATE INDEX IF NOT EXISTS idx_book_creature_name ON book_creature(name);
CREATE INDEX IF NOT EXISTS idx_book_creature_creature_type ON book_creature(creature_type);
CREATE INDEX IF NOT EXISTS idx_book_creature_location ON book_creature(creature_location_id);
CREATE INDEX IF NOT EXISTS idx_book_creature_ring ON book_creature(ring_id);
CREATE INDEX IF NOT EXISTS idx_book_creature_attack_level ON book_creature(attack_level);
CREATE INDEX IF NOT EXISTS idx_book_creature_age ON book_creature(age);

-- Книжное существо (основная сущность)
CREATE TABLE IF NOT EXISTS book_creature (
    id BIGINT PRIMARY KEY DEFAULT nextval('book_creature_seq'),
    name VARCHAR(255) NOT NULL,
    coordinates_id BIGINT NOT NULL UNIQUE,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    age INTEGER NOT NULL,
    creature_type VARCHAR(32),
    creature_location_id BIGINT,
    attack_level INTEGER NOT NULL,
    defense_level FLOAT,
    ring_id BIGINT,
    CONSTRAINT book_creature_name_not_empty CHECK (length(trim(name)) > 0),
    CONSTRAINT book_creature_age_positive CHECK (age > 0),
    CONSTRAINT book_creature_creature_type_valid CHECK (creature_type IS NULL OR creature_type IN ('HOBBIT','ELF','HUMAN','GOLLUM')),
    CONSTRAINT book_creature_attack_level_positive CHECK (attack_level > 0),
    CONSTRAINT book_creature_defense_level_positive CHECK (defense_level IS NULL OR defense_level > 0),
    CONSTRAINT fk_book_creature_coordinates FOREIGN KEY (coordinates_id) REFERENCES coordinates(id),
    CONSTRAINT fk_book_creature_location FOREIGN KEY (creature_location_id) REFERENCES magic_city(id) ON DELETE SET NULL,
    CONSTRAINT fk_book_creature_ring FOREIGN KEY (ring_id) REFERENCES ring(id) ON DELETE SET NULL
);
