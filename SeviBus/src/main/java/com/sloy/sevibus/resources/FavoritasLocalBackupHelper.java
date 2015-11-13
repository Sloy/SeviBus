package com.sloy.sevibus.resources;

import android.os.Environment;
import com.sloy.sevibus.bbdd.DBHelper;
import com.sloy.sevibus.bbdd.DBQueries;
import com.sloy.sevibus.model.tussam.Favorita;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavoritasLocalBackupHelper extends AbstractBackupHelper {
    
    private DBHelper mDBHelper;
    
    public FavoritasLocalBackupHelper(DBHelper dbhelper) {
        mDBHelper = dbhelper;
    }
    
    @Override
    public JSONObject getDatosParaGuardar() throws BackupException {
        try {
            List<Favorita> favoritas = DBQueries.getParadasFavoritas(mDBHelper);
            JSONObject datos = new JSONObject();
            JSONArray favoritasArray = new JSONArray();
            for (Favorita fav : favoritas) {
                JSONObject favObject = new JSONObject();
                favObject.put("numero", fav.getParadaAsociada().getNumero());
                favObject.put("nombrePropio", fav.getNombrePropio());
                favObject.put("color", fav.getColor());
                favoritasArray.put(favObject);
            }
            datos.put("favoritas", favoritasArray);
            return datos;
        } catch (Exception e) {
            throw new BackupException("Error al obtener las favoritas", e);
        }
    }
    
    @Override
    public void guardarBackup(JSONObject data) throws BackupException {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            throw new BackupException("Memoria externa no disponible");
        }
        File root = Environment.getExternalStorageDirectory();
        File sevibusDir = new File(root, "/Sevibus/");
        if (!sevibusDir.exists()) {
            sevibusDir.mkdir();
        }
        
        try {
            PrintWriter writer = new PrintWriter(new File(sevibusDir, "favoritas.txt"));
            writer.print(data.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            throw new BackupException("Error al escribir en la memoria externa", e);
        }
    }
    
    @Override
    public String getTipo() {
        return "favoritas";
    }
    
    @Override
    public JSONObject getBackupData() throws BackupException {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            throw new BackupException("Memoria externa no disponible");
        }
        
        File root = Environment.getExternalStorageDirectory();
        File favBackupFile = new File(root, "/Sevibus/favoritas.txt");
        if (!favBackupFile.exists()) {
            throw new BackupException("No se encontraron datos para importar");
        }
        
        JSONObject res = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(favBackupFile));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
            String everything = sb.toString();
            res = new JSONObject(everything);
        } catch (Exception e) {
            throw new BackupException("Error al leer la copia", e);
        }
        
        return res;
        
    }
    
    @Override
    public void restaurarBackup(JSONObject data) throws BackupException {
        // Comprueba que los datos son los que deben ser. El tipo, vamos
        if(!data.optString("tipo").equals("favortias")){
            throw new BackupException("Los datos de la copia no son correctos");
        }
        
        try {
            JSONArray arrayFavoritas = data.getJSONObject("datos").getJSONArray("favoritas");
            
        } catch (JSONException e) {
            throw new BackupException("Los datos de la copia no son correctos");
        }
        
    }
    
}
