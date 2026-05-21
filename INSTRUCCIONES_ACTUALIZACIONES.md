# 📦 Instrucciones para Publicar Actualizaciones

## Opción 1: GitHub Releases (Recomendado - Más Fácil)

### Paso 1: Actualizar el archivo VERSION.txt
1. Edita el archivo `VERSION.txt` en la raíz del repositorio
2. Cambia la versión a la nueva (ej: `1.0.1-Sebastian`, `1.0.2-Sebastian`)
3. Haz commit y push a GitHub:
   ```bash
   git add VERSION.txt
   git commit -m "Actualizar versión a 1.0.1-Sebastian"
   git push
   ```

### Paso 2: Crear un Release en GitHub
1. Ve a tu repositorio en GitHub: https://github.com/Sebastian245675/bicis-elletricas
2. Haz clic en "Releases" (en el menú lateral derecho)
3. Haz clic en "Create a new release"
4. **Tag version**: Usa el formato `v1.0.1-Sebastian` (debe coincidir con VERSION.txt)
5. **Release title**: "Versión 1.0.1-Sebastian" o similar
6. **Description**: Describe los cambios de esta versión
7. **Attach binaries**: Arrastra el archivo `kriolos-opos-app/target/kriolos-pos.jar`
8. Haz clic en "Publish release"

### Paso 3: Verificar que funciona
- El sistema automáticamente:
  - Lee `VERSION.txt` desde: `https://raw.githubusercontent.com/Sebastian245675/bicis-elletricas/main/VERSION.txt`
  - Descarga el JAR desde: `https://github.com/Sebastian245675/bicis-elletricas/releases/download/v1.0.1-Sebastian/kriolos-pos.jar`

---

## Opción 2: Servidor Propio (Más Control)

Si prefieres usar tu propio servidor, necesitas modificar las URLs en el código:

### Archivo: `UpdateChecker.java`
```java
// Cambiar esta línea:
private static final String VERSION_CHECK_URL = "https://raw.githubusercontent.com/Sebastian245675/bicis-elletricas/main/VERSION.txt";

// Por tu URL, por ejemplo:
private static final String VERSION_CHECK_URL = "https://tudominio.com/updates/VERSION.txt";
```

### Archivo: `UpdateManager.java`
```java
// Cambiar esta línea:
private static final String UPDATE_BASE_URL = "https://github.com/Sebastian245675/bicis-elletricas/releases/download/";

// Por tu URL, por ejemplo:
private static final String UPDATE_BASE_URL = "https://tudominio.com/updates/";
```

### Estructura en tu servidor:
```
tudominio.com/updates/
├── VERSION.txt          (contiene: 1.0.1-Sebastian)
└── kriolos-pos.jar      (el archivo JAR actualizado)
```

**Nota**: El sistema intentará descargar desde: `{UPDATE_BASE_URL}kriolos-pos.jar`

---

## Opción 3: Servicio de Almacenamiento (Google Drive, Dropbox, etc.)

Si usas Google Drive o Dropbox, puedes:

1. Subir el JAR a Google Drive/Dropbox
2. Obtener el enlace directo de descarga
3. Modificar `UpdateManager.java` para usar ese enlace directo

**Ejemplo para Google Drive:**
```java
// En UpdateManager.java, modificar el método downloadFile:
// En lugar de usar UPDATE_BASE_URL, usar directamente:
String downloadUrl = "https://drive.google.com/uc?export=download&id=TU_FILE_ID";
```

---

## 📝 Formato de Versión

El formato de versión debe ser:
- `1.0.0-Sebastian`
- `1.0.1-Sebastian`
- `1.1.0-Sebastian`
- `2.0.0-Sebastian`

El sistema compara versiones numéricamente, así que:
- `1.0.1` > `1.0.0` ✅
- `1.1.0` > `1.0.9` ✅
- `2.0.0` > `1.9.9` ✅

---

## 🔧 Proceso Completo de Actualización

1. **Desarrollas** los cambios en tu código
2. **Compilas** el proyecto: `mvn clean install -DskipTests`
3. **Actualizas** `VERSION.txt` con la nueva versión
4. **Subes** `VERSION.txt` a GitHub (commit + push)
5. **Creas** un Release en GitHub con el JAR compilado
6. **El cliente** automáticamente detecta la actualización al iniciar
7. **El cliente** hace clic en "Actualizar Ahora"
8. **El sistema** descarga y aplica la actualización sin borrar datos

---

## ⚠️ Importante

- **Nunca cambies el nombre del archivo JAR**: debe ser siempre `kriolos-pos.jar`
- **El tag del release** debe coincidir con la versión en VERSION.txt (con el prefijo `v`)
- **Los datos del cliente se conservan** porque solo se actualiza el JAR, no la base de datos
- **Siempre prueba** la actualización en un entorno de prueba antes de publicarla

---

## 🆘 Solución de Problemas

### El cliente no detecta actualizaciones:
1. Verifica que `VERSION.txt` esté actualizado en GitHub
2. Verifica que el Release esté publicado (no como draft)
3. Verifica que el tag del release coincida con la versión

### Error al descargar:
1. Verifica que el JAR esté adjunto al Release
2. Verifica que la URL sea accesible desde el navegador
3. Verifica que el archivo no esté corrupto

### El cliente tiene problemas después de actualizar:
1. El sistema automáticamente restaura el respaldo si hay error
2. Si es necesario, puedes restaurar manualmente desde el archivo `.backup`

