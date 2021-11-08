-------------------------------------------------------------------
---- Identity Database User Setup script --------------------------
-------------------------------------------------------------------

    CREATE USER IF NOT EXISTS 'bbiam'@'%' IDENTIFIED BY 'bb1am:PW';
    
    GRANT ALL PRIVILEGES ON backbase_identity.* TO 'bbiam'@'%';