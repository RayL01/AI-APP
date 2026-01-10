-- Allow implicit cast from varchar to vector
-- This is needed because JPA AttributeConverter returns String,
-- but PostgreSQL needs explicit cast to vector type
CREATE CAST (varchar AS vector) WITH INOUT AS IMPLICIT;
