-- Agrega la columna para almacenar el secreto TOTP (Google Authenticator)
ALTER TABLE PEOPLE ADD COLUMN TOTP_SECRET VARCHAR(255);
