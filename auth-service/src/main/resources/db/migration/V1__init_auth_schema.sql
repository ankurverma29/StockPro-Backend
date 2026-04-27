CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    pending_email VARCHAR(255),
    password_hash VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(50),
    department VARCHAR(100),
    is_active BOOLEAN,
    otp_code VARCHAR(20),
    otp_expiry DATETIME,
    last_login_at DATETIME,
    created_at DATETIME
);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
