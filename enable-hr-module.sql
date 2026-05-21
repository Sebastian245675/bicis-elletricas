-- =====================================================================
-- Script para habilitar el módulo de Recursos Humanos
-- Ejecutar en la base de datos: kriolopos (MariaDB/MySQL)
-- =====================================================================

USE kriolopos;

-- 1. Eliminar el menú cacheado en la BD para que se recargue
--    desde el JAR con las nuevas entradas de RRHH
DELETE FROM resources WHERE NAME = 'Menu.Root';

-- 2. Agregar el permiso de RRHH al rol Administrator en la BD
--    El sistema guarda permisos por línea en la tabla 'permissions'
--    con el ID del rol
INSERT IGNORE INTO permissions (ID, PERMISSIONS)
SELECT r.ID, 'com.openbravo.pos.admin.JPanelHR'
FROM roles r
WHERE UPPER(r.NAME) = 'ADMINISTRATOR'
   OR r.NAME = '1';

-- 3. Actualizar el campo PERMISSIONS del rol Administrator en la 
--    tabla 'roles' agregando la clase de RRHH al XML de permisos
UPDATE roles
SET PERMISSIONS = CONCAT(
    LEFT(PERMISSIONS, LENGTH(PERMISSIONS) - LENGTH('</permissions>')),
    '\n    <class name="com.openbravo.pos.admin.JPanelHR"/>',
    '\n</permissions>'
)
WHERE (UPPER(NAME) = 'ADMINISTRATOR' OR NAME = '1')
  AND CONVERT(PERMISSIONS USING utf8) NOT LIKE '%JPanelHR%';

-- =====================================================================
-- Después de ejecutar este script, reinicia la aplicación.
-- El menú de Recursos Humanos aparecerá en la barra lateral.
-- =====================================================================
