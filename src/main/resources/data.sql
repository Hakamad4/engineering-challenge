-- Seed data for the database
-- Contas
INSERT INTO accounts (name, type, balance) VALUES
    ('Conta do João', 'PROPERTY_OWNER', 5000.00),
    ('Conta da Imobiliaria Imoveis', 'REAL_ESTATE_AGENCY', 1000000.00),
    ('Conta do Morus', 'PLATFORM_REVENUE', 1000000.00);

-- Imobiliária
INSERT INTO real_estate_agencies (name, fee_percentage, account_id)
VALUES ('Imobiliaria Imoveis', 0.1, 2);

-- Proprietário
INSERT INTO property_owners (name, account_id, real_estate_agency_id)
VALUES ('João da Silva', 1, 1);

-- Pagamentos
INSERT INTO payments (external_reference, amount, property_owner_id, real_estate_agency_id, status, created_at)
VALUES
    ('PAY-001', 1000.00, 1, 1, 'COMPLETED', CURRENT_TIMESTAMP),
    ('PAY-002', 500.00, 1, 1, 'FAILED', CURRENT_TIMESTAMP);

-- Extratos (statements)
INSERT INTO statements (payment_id, account_id, amount, description, created_at)
VALUES
    (1, 1, 900.00, 'Recebimento proprietário', CURRENT_TIMESTAMP),
    (1, 2, 100.00, 'Taxa da imobiliária', CURRENT_TIMESTAMP),
    (1, 3, 50.00, 'Taxa da plataforma', CURRENT_TIMESTAMP);