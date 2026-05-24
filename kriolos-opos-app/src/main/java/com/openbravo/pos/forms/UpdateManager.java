package com.openbravo.pos.forms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor de actualizaciones - Descarga y aplica actualizaciones sin borrar datos
 * @author Sebastian
 */
public class UpdateManager {
    
    private static final Logger LOGGER = Logger.getLogger(UpdateManager.class.getName());
    
    // URL base donde están los archivos JAR actualizados
    // Formato: https://github.com/USER/REPO/releases/download/vVERSION/kriolos-pos.jar
    private static final String UPDATE_BASE_URL = "https://github.com/Sebastian245675/bicis-elletricas/releases/download/";
    
    // URLs alternativas para descargar actualizaciones
    private static final String[] UPDATE_URLS = {
        UPDATE_BASE_URL + "v{version}/kriolos-pos-release.jar",
        UPDATE_BASE_URL + "v{version}/kriolos-pos.jar",
        "https://github.com/Sebastian245675/bicis-elletricas/releases/latest/download/kriolos-pos-release.jar",
        "https://github.com/Sebastian245675/bicis-elletricas/releases/latest/download/kriolos-pos.jar"
    };
    
    /**
     * Descarga y aplica una actualización
     * @param version Versión a descargar
     * @param progressCallback Callback para reportar progreso
     * @return true si la actualización fue exitosa
     */
    public static boolean applyUpdate(String version, ProgressCallback progressCallback) {
        try {
            progressCallback.onProgress(0, "Iniciando actualización...");
            
            // Obtener ruta del JAR actual
            String currentJarPath = getCurrentJarPath();
            if (currentJarPath == null) {
                progressCallback.onError("No se pudo determinar la ubicación del JAR actual");
                return false;
            }
            
            File currentJar = new File(currentJarPath);
            File backupJar = new File(currentJarPath + ".backup");
            File newJar = new File(currentJarPath + ".new");
            
            // 1. Crear respaldo del JAR actual
            progressCallback.onProgress(10, "Creando respaldo...");
            Files.copy(currentJar.toPath(), backupJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // 2. Descargar nuevo JAR
            progressCallback.onProgress(20, "Descargando nueva versión " + version + "...");
            boolean downloadSuccess = false;
            
            // Intentar descargar desde múltiples URLs
            for (String urlTemplate : UPDATE_URLS) {
                String downloadUrl = urlTemplate.replace("{version}", version);
                LOGGER.info("Intentando descargar desde: " + downloadUrl);
                
                if (downloadFile(downloadUrl, newJar, progressCallback)) {
                    downloadSuccess = true;
                    LOGGER.info("Descarga exitosa desde: " + downloadUrl);
                    break;
                } else {
                    LOGGER.warning("Falló descarga desde: " + downloadUrl);
                }
            }
            
            if (!downloadSuccess) {
                progressCallback.onError("No se pudo descargar la actualización desde ninguna fuente disponible.\n" +
                    "Por favor, verifica tu conexión a internet o descarga manualmente desde GitHub.");
                return false;
            }
            
            // 3. Verificar que el nuevo JAR es válido (tiene tamaño razonable)
            if (newJar.length() < 1000) {
                progressCallback.onError("El archivo descargado parece estar corrupto");
                newJar.delete();
                return false;
            }
            
            // 4. Reemplazar JAR actual con el nuevo
            progressCallback.onProgress(90, "Aplicando actualización...");
            
            // En Windows, no podemos reemplazar ni renombrar el archivo JAR en ejecución directamente
            // debido a que la JVM mantiene un bloqueo exclusivo sobre él.
            // Para resolver esto, escribimos un script .bat temporal, lo ejecutamos y salimos.
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                File parentDir = currentJar.getParentFile();
                if (parentDir == null) {
                    parentDir = new File(".");
                }
                File updaterScript = new File(parentDir, "update_helper.bat");
                String targetPath = currentJar.getAbsolutePath();
                String newPath = newJar.getAbsolutePath();
                
                StringBuilder script = new StringBuilder();
                script.append("@echo off\r\n");
                script.append("echo Aplicando actualizacion para KriolOS POS...\r\n");
                script.append(":loop\r\n");
                script.append("timeout /t 1 /nobreak >nul\r\n");
                script.append("move /y \"").append(newPath).append("\" \"").append(targetPath).append("\" >nul 2>&1\r\n");
                script.append("if errorlevel 1 goto loop\r\n");
                script.append("\r\n");
                script.append("echo Actualizacion aplicada con exito.\r\n");
                script.append("if exist \"").append(targetPath).append(".backup\" del /q \"").append(targetPath).append(".backup\"\r\n");
                script.append("if exist \"").append(targetPath).append(".old\" del /q \"").append(targetPath).append(".old\"\r\n");
                script.append("\r\n");
                script.append("echo Reiniciando la aplicacion...\r\n");
                script.append("start \"\" java -Xms256m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication -Dsun.java2d.d3d=false -Dsun.java2d.noddraw=true -Djava.awt.headless=false -Xverify:none -XX:TieredStopAtLevel=1 -XX:+TieredCompilation -Dfile.encoding=UTF-8 -Dsplash=true -jar \"").append(targetPath).append("\"\r\n");
                script.append("\r\n");
                script.append("(goto) 2>nul & del \"%~f0\"\r\n");
                
                // Guardar el script por lotes
                Files.writeString(updaterScript.toPath(), script.toString(), java.nio.charset.StandardCharsets.UTF_8);
                
                // Lanzar el script en un proceso independiente y desacoplado (detached)
                // Usamos 'start /min' para que el script continúe ejecutándose incluso si el proceso JVM padre termina
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start /min \"\" \"" + updaterScript.getAbsolutePath() + "\"");
                pb.directory(parentDir);
                pb.start();
                
                LOGGER.info("Script de actualizacion iniciado: " + updaterScript.getAbsolutePath());
            } else {
                // En Linux/Mac, usar move directamente ya que el SO permite renombrar archivos abiertos
                Files.move(newJar.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Limpiar archivo temporal si aún existe (solo en sistemas que no sean Windows,
            // ya que en Windows el script update_helper.bat se encarga de moverlo/reemplazarlo)
            if (!System.getProperty("os.name").toLowerCase().contains("win") && newJar.exists()) {
                newJar.delete();
            }
            
            progressCallback.onProgress(100, "¡Actualización completada!");
            LOGGER.info("Actualización aplicada exitosamente a versión " + version);
            
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error aplicando actualización", e);
            progressCallback.onError("Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Descarga un archivo desde una URL
     */
    private static boolean downloadFile(String urlString, File destination, ProgressCallback callback) {
        try {
            URL url = new java.net.URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.warning("No se pudo descargar: " + responseCode);
                return false;
            }
            
            long fileSize = conn.getContentLengthLong();
            try (InputStream inputStream = conn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(destination)) {
                
                byte[] buffer = new byte[4096];
                long totalBytesRead = 0;
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    if (fileSize > 0) {
                        int progress = 20 + (int) ((totalBytesRead * 70) / fileSize);
                        callback.onProgress(progress, "Descargando... " + 
                            (totalBytesRead / 1024 / 1024) + " MB / " + 
                            (fileSize / 1024 / 1024) + " MB");
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error descargando archivo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene la ruta del JAR actual
     * Funciona tanto si se ejecuta desde JAR como desde IDE
     */
    private static String getCurrentJarPath() {
        try {
            // Obtener la ruta del JAR desde la clase principal StartPOS
            java.net.URL location = StartPOS.class.getProtectionDomain()
                    .getCodeSource().getLocation();
            
            if (location == null) {
                // Intentar con UpdateManager
                location = UpdateManager.class.getProtectionDomain()
                        .getCodeSource().getLocation();
            }
            
            if (location != null) {
                String urlStr = location.toURI().toString();
                // Decodificar URL
                urlStr = java.net.URLDecoder.decode(urlStr, "UTF-8");
                
                LOGGER.info("Ubicación del código original: " + urlStr);
                
                // Si es un nested/jar URL de Spring Boot o similar, extraer el JAR/EXE externo
                // Ejemplos: 
                // jar:file:/C:/path/kriolos-pos.jar!/BOOT-INF/lib/...
                // nested:/C:/path/kriolos-pos.jar/BOOT-INF/lib/...
                String path = urlStr;
                
                // Remover prefijos comunes de protocolo
                if (path.startsWith("jar:file:")) {
                    path = path.substring(9);
                } else if (path.startsWith("file:")) {
                    path = path.substring(5);
                } else if (path.startsWith("nested:")) {
                    path = path.substring(7);
                }
                
                // Si contiene "!/", cortar allí
                int bangIndex = path.indexOf("!/");
                if (bangIndex != -1) {
                    path = path.substring(0, bangIndex);
                }
                
                // Si contiene ".jar" o ".exe" seguido de "/", cortar después de la extensión
                int jarIndex = path.toLowerCase().indexOf(".jar/");
                if (jarIndex != -1) {
                    path = path.substring(0, jarIndex + 4);
                }
                int exeIndex = path.toLowerCase().indexOf(".exe/");
                if (exeIndex != -1) {
                    path = path.substring(0, exeIndex + 4);
                }
                
                // En Windows, remover el "/" inicial si existe (ej: /C:/path -> C:/path)
                if (path.startsWith("/") && path.length() > 2 && path.charAt(2) == ':') {
                    path = path.substring(1);
                }
                
                File jarFile = new File(path);
                if (jarFile.exists() && (path.toLowerCase().endsWith(".jar") || path.toLowerCase().endsWith(".exe"))) {
                    if (path.toLowerCase().endsWith(".exe")) {
                        File jarInSameDir = new File(jarFile.getParent(), "kriolos-pos.jar");
                        if (jarInSameDir.exists()) {
                            return jarInSameDir.getAbsolutePath();
                        }
                    }
                    return jarFile.getAbsolutePath();
                }
            }
            
            // Si falló, intentar parsear java.class.path
            String classpath = System.getProperty("java.class.path");
            if (classpath != null && !classpath.isEmpty()) {
                String[] paths = classpath.split(File.pathSeparator);
                for (String p : paths) {
                    if (p.toLowerCase().contains("kriolos") && p.toLowerCase().endsWith(".jar")) {
                        File f = new File(p);
                        if (f.exists()) {
                            return f.getAbsolutePath();
                        }
                    }
                }
            }
            
            // Buscar en ubicaciones comunes
            String[] possiblePaths = {
                "kriolos-pos.jar",
                "target/kriolos-pos.jar",
                "kriolos-opos-app/target/kriolos-pos.jar"
            };
            
            for (String possiblePath : possiblePaths) {
                File possibleFile = new File(possiblePath);
                if (possibleFile.exists() && possibleFile.getName().endsWith(".jar")) {
                    return possibleFile.getAbsolutePath();
                }
            }
            
            throw new Exception("No se pudo resolver la ruta del JAR actual.");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al determinar ruta del JAR: " + e.getMessage(), e);
            // Último recurso: buscar en el directorio actual cualquier JAR con "kriolos"
            try {
                File currentDir = new File(System.getProperty("user.dir"));
                File[] jars = currentDir.listFiles((dir, name) -> name.endsWith(".jar") && name.contains("kriolos"));
                if (jars != null && jars.length > 0) {
                    return jars[0].getAbsolutePath();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Fallo último recurso de búsqueda de JAR: " + ex.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Restaura el respaldo en caso de error
     */
    public static boolean restoreBackup() {
        try {
            String currentJarPath = getCurrentJarPath();
            if (currentJarPath == null) {
                LOGGER.warning("No se pudo restaurar el respaldo porque la ruta del JAR es nula");
                return false;
            }
            File currentJar = new File(currentJarPath);
            File backupJar = new File(currentJarPath + ".backup");
            
            if (backupJar.exists()) {
                Files.copy(backupJar.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Respaldo restaurado exitosamente");
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error restaurando respaldo", e);
        }
        return false;
    }
    
    /**
     * Interfaz para reportar progreso de la actualización
     */
    public interface ProgressCallback {
        void onProgress(int percentage, String message);
        void onError(String error);
    }
}

