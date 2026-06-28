#!/bin/bash
# Cria múltiplos bancos de dados num único Postgres, a partir da variável
# POSTGRES_MULTIPLE_DATABASES (lista separada por vírgula). Cada microsserviço
# tem o seu próprio banco — eles nunca compartilham tabelas.
set -e

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
  echo "Criando bancos: $POSTGRES_MULTIPLE_DATABASES"
  for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
      SELECT 'CREATE DATABASE $db'
      WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
    echo "  -> banco '$db' pronto"
  done
fi
