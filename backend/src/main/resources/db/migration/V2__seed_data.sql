-- Seed data for local development.
-- Creates a demo tenant "acme-logistics" with one admin, one dispatcher, and one driver.
-- Password for all seed users is: Password123!
-- (BCrypt hash of "Password123!")

INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, role)
VALUES
    (uuid_generate_v4(), 'acme-logistics', 'njabulombele97@gmail.com',
     '$2a$12$3JfvdWf8MpOcnEhCaGqPkeevN4h8tXlh1e4YtgKb5IIhm4Doh.aSC',
     'Njabulo', 'Mbele', 'ADMIN'),
    (uuid_generate_v4(), 'acme-logistics', 'dispatcher@acme.co.za',
     '$2a$12$3JfvdWf8MpOcnEhCaGqPkeevN4h8tXlh1e4YtgKb5IIhm4Doh.aSC',
     'Sarah', 'Dispatcher', 'DISPATCHER'),
    (uuid_generate_v4(), 'acme-logistics', 'manager@acme.co.za',
     '$2a$12$3JfvdWf8MpOcnEhCaGqPkeevN4h8tXlh1e4YtgKb5IIhm4Doh.aSC',
     'James', 'Manager', 'MANAGER');

-- Seed vehicles
INSERT INTO vehicles (id, tenant_id, registration_number, type, capacity_kg, make_model, status)
VALUES
    (uuid_generate_v4(), 'acme-logistics', 'GP 12 ABC', 'Box Truck', 2000.0, 'Hino 300 Series', 'AVAILABLE'),
    (uuid_generate_v4(), 'acme-logistics', 'GP 34 DEF', 'Panel Van',  800.0, 'Toyota HiAce',    'AVAILABLE'),
    (uuid_generate_v4(), 'acme-logistics', 'GP 56 GHI', 'Flatbed',   5000.0, 'Isuzu FVZ',       'AVAILABLE'),
    (uuid_generate_v4(), 'acme-logistics', 'GP 78 JKL', 'Panel Van',  800.0, 'Ford Transit',    'MAINTENANCE');

-- Seed drivers
INSERT INTO drivers (id, tenant_id, name, phone_number, license_number, status)
VALUES
    (uuid_generate_v4(), 'acme-logistics', 'Sipho Dlamini',  '+27 82 111 1111', 'GP-DRV-001', 'AVAILABLE'),
    (uuid_generate_v4(), 'acme-logistics', 'Themba Nkosi',   '+27 82 222 2222', 'GP-DRV-002', 'AVAILABLE'),
    (uuid_generate_v4(), 'acme-logistics', 'Lungelo Mthembu','+27 82 333 3333', 'GP-DRV-003', 'AVAILABLE'),
    (uuid_generate_v4(), 'acme-logistics', 'Zanele Mokoena', '+27 82 444 4444', 'GP-DRV-004', 'AVAILABLE');

-- Seed customers
INSERT INTO customers (id, tenant_id, name, contact_name, phone, email, address_line1, city, latitude, longitude)
VALUES
    (uuid_generate_v4(), 'acme-logistics', 'Builders Warehouse Fourways',
     'Receiving Manager', '+27 11 555 0001', 'receiving@bw-fourways.co.za',
     '1 William Nicol Dr', 'Johannesburg', -26.0167, 28.0121),
    (uuid_generate_v4(), 'acme-logistics', 'Makro Centurion',
     'Receiving', '+27 12 555 0002', 'receiving@makro-centurion.co.za',
     '7 Akkerboom Rd', 'Pretoria', -25.8553, 28.1877),
    (uuid_generate_v4(), 'acme-logistics', 'Cash & Carry Rustenburg',
     'Thabo Sithole', '+27 14 555 0003', 'thabo@cashncarry.co.za',
     '14 Fatima Bhayat St', 'Rustenburg', -25.6700, 27.2473),
    (uuid_generate_v4(), 'acme-logistics', 'Pick n Pay Midrand',
     'Receiving Manager', '+27 11 555 0004', 'receiving@pnp-midrand.co.za',
     '555 New Road', 'Midrand', -25.9937, 28.1328);
