CREATE ROLE stocks LOGIN;
ALTER USER stocks PASSWORD 'stocks';

GRANT ALL on SCHEMA public to stocks;
GRANT ALL on SCHEMA stocks to stocks;

GRANT ALL ON ALL TABLES IN SCHEMA public to stocks;
GRANT ALL ON ALL TABLES IN SCHEMA stocks to stocks;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public to stocks;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA stocks to stocks;
