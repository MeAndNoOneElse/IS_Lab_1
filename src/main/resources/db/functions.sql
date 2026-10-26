-- Специальные операции как pl/pgsql-функции (вызываются из уровня бизнес-логики)
-- Выполнять от имени пользователя s465029 в базе studs на хосте pg
-- Схема: s465029

SET search_path TO s465029;

-- 1. Сумма значений поля attackLevel для всех объектов
CREATE OR REPLACE FUNCTION sum_attack_level()
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE total BIGINT;
BEGIN
    SELECT COALESCE(SUM(attack_level), 0) INTO total FROM s465029.book_creature;
    RETURN total;
END;
$$;

-- 2. Массив объектов, имя которых начинается с заданной подстроки
CREATE OR REPLACE FUNCTION find_by_name_substring(p_substring VARCHAR)
RETURNS TABLE (
    id BIGINT,
    name VARCHAR,
    age INTEGER,
    creature_type VARCHAR,
    attack_level INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT bc.id, bc.name, bc.age, bc.creature_type, bc.attack_level
    FROM s465029.book_creature bc
    WHERE bc.name LIKE p_substring || '%';
END;
$$;

-- 3. Массив уникальных значений поля attackLevel по всем объектам
CREATE OR REPLACE FUNCTION unique_attack_levels()
RETURNS TABLE (attack_level INTEGER)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT bc.attack_level
    FROM s465029.book_creature bc
    ORDER BY bc.attack_level;
END;
$$;

-- 4. Обмен кольцами между двумя персонажами (указать id персонажей)
CREATE OR REPLACE FUNCTION swap_rings(p_id1 BIGINT, p_id2 BIGINT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE r1 BIGINT; r2 BIGINT;
BEGIN
    SELECT ring_id INTO r1 FROM s465029.book_creature WHERE id = p_id1;
    SELECT ring_id INTO r2 FROM s465029.book_creature WHERE id = p_id2;
    UPDATE s465029.book_creature SET ring_id = r2 WHERE id = p_id1;
    UPDATE s465029.book_creature SET ring_id = r1 WHERE id = p_id2;
END;
$$;

-- 5. Уничтожить города эльфов
CREATE OR REPLACE FUNCTION destroy_elf_cities()
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE cnt INTEGER;
BEGIN
    SELECT COUNT(*) INTO cnt FROM s465029.magic_city WHERE governor = 'ELF';
    DELETE FROM s465029.magic_city WHERE governor = 'ELF';
    RETURN cnt;
END;
$$;
