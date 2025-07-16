CREATE UNLOGGED TABLE payments (
    correlationId UUID PRIMARY KEY,
    amount DECIMAL NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    processor VARCHAR(8) NOT NULL
);

CREATE INDEX idx_payment ON payments (processor, requested_at);