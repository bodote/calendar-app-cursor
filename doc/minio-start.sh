brew services start minio
mc alias set myminio http://localhost:9000 minioadmin minioadmin
mc mb myminio/de.bas.bodo