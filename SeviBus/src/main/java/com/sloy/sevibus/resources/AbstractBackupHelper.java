package com.sloy.sevibus.resources;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractBackupHelper {
    
    public abstract String getTipo();
    
    /**
     * Plantilla para la exportación
     * 
     * @return realizado con éxito
     */
    public final void exportar() throws BackupException {
        try {
            // Genera el objeto base de backup
            JSONObject baseData = new JSONObject();
            baseData.put("tipo", getTipo());
            baseData.put("fechaCreacion", new Date().getTime());
            baseData.put("version", 0); // TODO poner versión de la aplicación
            baseData.put("datos", getDatosParaGuardar());
            
            guardarBackup(baseData);
            
        } catch (JSONException e) {
            throw new BackupException("Error al preparar los datos para el backup", e);
        }
    }
    
    public abstract JSONObject getDatosParaGuardar() throws BackupException;
    
    /**
     * Método abstracto encargado de guardar el backup de los datos en el medio correspondiente según la implementación
     * 
     * @param data
     *            objeto JSON con los datos que deben ser guardados.
     * @return true si se realizó con éxito, false en caso contrario.
     */
    public abstract void guardarBackup(JSONObject data) throws BackupException;
    
    public final void importar() throws BackupException {
        JSONObject data = getBackupData();
        restaurarBackup(data);
    }
    
    public abstract JSONObject getBackupData() throws BackupException;
    
    public abstract void restaurarBackup(JSONObject data) throws BackupException;
    
    public class BackupException extends Exception {
        
        private static final long serialVersionUID = 1L;
        
        public BackupException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
        
        public BackupException(String detailMessage) {
            super(detailMessage);
        }
        
    }
    
}
