-- 1. CATEGORY Table
CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT,
                            icon_path VARCHAR(255),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. USER Table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       user_name VARCHAR(250) NOT NULL ,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL, -- BCrypt hash
                       phone VARCHAR(20),
                       role VARCHAR(20) NOT NULL, -- ORGANIZER, ATTENDEE, ADMIN
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. EVENT Table
CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(200) NOT NULL,
                        description TEXT,
                        start_time TIMESTAMP NOT NULL,
                        end_time TIMESTAMP NOT NULL,
                        event_type VARCHAR(20) NOT NULL, -- ONLINE, PHYSICAL, HYBRID
                        status VARCHAR(20) NOT NULL,     -- DRAFT, PUBLISHED, CANCELLED, COMPLETED
                        location VARCHAR(255),           -- Nullable if online
                        image_path VARCHAR(255),
                        max_capacity INTEGER NOT NULL,
                        category_id BIGINT REFERENCES categories(id),
                        organizer_id BIGINT REFERENCES users(id),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_event_times CHECK (end_time > start_time)
);

-- 4. TICKET_TIER Table
CREATE TABLE ticket_tiers (
                              id BIGSERIAL PRIMARY KEY,
                              event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                              name VARCHAR(100) NOT NULL, -- Early Bird, VIP, etc.
                              price DECIMAL(10, 2) NOT NULL,
                              total_quantity INTEGER NOT NULL,
                              sold_count INTEGER DEFAULT 0,
                              sale_start_date TIMESTAMP NOT NULL,
                              sale_end_date TIMESTAMP NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT chk_ticket_times CHECK (sale_end_date > sale_start_date)
);

-- 5. REGISTRATION Table
CREATE TABLE registrations (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL REFERENCES users(id),
                               event_id BIGINT NOT NULL REFERENCES events(id),
                               ticket_tier_id BIGINT NOT NULL REFERENCES ticket_tiers(id),
                               qr_code_uuid VARCHAR(100) NOT NULL UNIQUE,
                               registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               status VARCHAR(20) NOT NULL, -- CONFIRMED, CANCELLED
                               is_checked_in BOOLEAN DEFAULT FALSE,
                               check_in_time TIMESTAMP,
    -- Aynı kullanıcının aynı etkinliğe 2. kez kaydolmasını engellemek için:
                               CONSTRAINT unique_user_event_registration UNIQUE (user_id, event_id)
);

-- Indexing for Performance
CREATE INDEX idx_event_status ON events(status);
CREATE INDEX idx_event_category ON events(category_id);
CREATE INDEX idx_registration_qr ON registrations(qr_code_uuid);