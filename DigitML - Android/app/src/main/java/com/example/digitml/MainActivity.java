package com.example.digitml;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import okhttp3.*;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    private OkHttpClient client;
    private ImageView imageView;
    private DrawingView drawingView;
    private TextView resultEditText;

    final private int DimensioniImage = 400;

    private URL url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawingView = findViewById(R.id.drawingView);

        Button saveButton = findViewById(R.id.sendButton);
        saveButton.setOnClickListener(v -> {
            Bitmap b = drawingView.GetServerImage(DimensioniImage);
            if(b!=null) {
                Invia(b);
            }else
                resultEditText.setText(getString(R.string.err2));
        });

        // Trova l'ImageView
        imageView = findViewById(R.id.MatitaView);

        // Imposta il listener di clic per l'icona
        imageView.setOnClickListener(v -> showSeekBarDialog());

        //Trova il Result
        resultEditText = findViewById(R.id.result);

        findViewById(R.id.clear).setOnClickListener(v -> {
            clear();
        });

        TextView linkView = findViewById(R.id.linkView);
        linkView.setOnClickListener(v -> {
            setLinkSito();
        });

        String savedUrl = loadUrl();
        if (savedUrl == null) {
            showIPDialog();
        }else{
            try {
                url = new URL(savedUrl);
            } catch (MalformedURLException e){
                url = null;
            }
        }

        ImageView setting = (ImageView) findViewById(R.id.Setting);
        setting.setOnClickListener(v -> showIPDialog());

        setting.setOnLongClickListener(v -> {
            showURLDialog();
            return true;
        });

        /*
        Switch s = findViewById(R.id.switchA1);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            s.setChecked(s.isChecked());
        });
        */
        ToggleButton tbmodo = (ToggleButton)findViewById(R.id.TBModo);
        tbmodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //clear();
            if(isChecked){
                Toast.makeText(MainActivity.this, getString(R.string.disegnaSensori), Toast.LENGTH_SHORT).show();
                drawingView.setStrumento(DrawingView.SENSORI);
            }else{
                Toast.makeText(MainActivity.this, getString(R.string.disegnaMano), Toast.LENGTH_SHORT).show();
                drawingView.setStrumento(DrawingView.MANO);
            }
        });
    }

    //Per mostrare la SeekBarr
    private void showSeekBarDialog() {
        // Inflattare il layout personalizzato del dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_seekbar, null);

        // Trova la SeekBar e l'EditText
        SeekBar seekBar = dialogView.findViewById(R.id.seekBar);
        TextView text = dialogView.findViewById(R.id.seekBarValue);

        // Impostiamo un listener sulla SeekBar per aggiornare il valore nell'EditText
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText(String.valueOf(progress));  // Aggiorna il valore nell'EditText
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBar.setProgress(drawingView!=null? drawingView.GetSpessore() : 20);


        // Creiamo il dialog e impostiamo la vista personalizzata
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.seleziona))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    // Quando l'utente preme OK, otteniamo il valore selezionato dalla SeekBar e cambiamo lo spessore
                    drawingView.SetSpessore(seekBar.getProgress());

                })
                .setNegativeButton(getString(R.string.annulla), (dialog, which) -> dialog.dismiss());

        // Mostriamo il dialog
        builder.create().show();
    }

    public void Invia(Bitmap bitmap) {
        File f = GetFile(bitmap, "Img.png");
        drawingView.setAction(DrawingView.START);
        if(url==null){
            showIPDialog();
        }else {
            // Esegui la richiesta in modo sincrono (su un thread separato)
            new Thread(() -> {

                String msg = "{\"image\":\"data:image/png;base64," + drawingView.GetImage64(DimensioniImage) + "\"" + ", \"endpoint\":" + (((ToggleButton)findViewById(R.id.TBTipo)).isChecked() ? "letter" : "number") + "}";
                Log.e("MIO", "immaz: " + msg);
                String risposta = CreateRequestReadResponse(msg);

                // Mostra la risposta nell'UI
                runOnUiThread(() -> {
                    //Log.i("msg", risposta);
                    resultEditText.setText(risposta); // Imposta il testo della risposta nell'EditText
                });


            }).start(); // Esegui il thread separato
        }
    }




    //invia la richiesta e legge la risposta
    private String CreateRequestReadResponse(String s){
        // Creo un oggetto HttpURLConnection (per gestire le richieste)
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();


            // Definisco il metodo della richiesta
            connection.setRequestMethod("POST");

            // Aggiungo headers (il secondo header si imposta con tale valore se il body contiene parametri)
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            // nei casi in cui esiste un body bisogna indicarlo esplicitamente
            connection.setDoOutput(true);

            // Per scrivere un body bisogna usare un PrintWriter
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())));

            // non si usa println, ma si usa write (con flush)
            pw.print(s);
            pw.flush();

            try {

                // invia la richiesta e riceve la risposta, qui viene preso il codice di risposta
                int statusCode = connection.getResponseCode();

                if (statusCode == 200) {
                    // leggo la risposta, Ã¨ andata a buon fine
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line, msg = "";
                    while ((line = br.readLine()) != null) {
                        msg += line + "\n";
                    }
                    Esito e = readGson(msg);
                    switch (e.getStatus()){
                        case "500":
                            return e.getMessage();
                        case "200":
                            return e.getPrediction();
                        default:
                            return "Risposta: " + e.getStatus();
                    }

                }else{
                    return "Status code: "+statusCode;
                }


            } catch (IOException e) {
                e.printStackTrace();
                return getString(R.string.err5);
            }

        } catch (IOException e) {
            return getString(R.string.err4) + "\n" + url.toString();
        }
    }
    private Esito readGson(String s){
        return new Gson().fromJson(s, Esito.class);
    }
    private String getURL(String s){
        return "http://"+s+":8080/DigitML_API/image";
    }
    private String getURL(){
        return getURL(url.toString());
    }

    private void showLink(){
        runOnUiThread(() -> setLinkSito());
    }
    private void setLinkSito(){
        if(getSito().equals("")){
            Toast.makeText(this, getString(R.string.IP), Toast.LENGTH_SHORT);
        }else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getSito()));
            startActivity(browserIntent);
        }
    }
    private void setURL(String s) throws MalformedURLException{
        url = new URL(s);

    }
    private String getSito(){
        if(url!=null)
            return "http://"+url.getHost();
        return "";
    }

    private void showIPDialog(){
        runOnUiThread(() -> createIPDialog());
        this.getSito();
    }
    private void createIPDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.ip_dialog, null);

        if(url!=null){
            ((EditText)dialogView.findViewById(R.id.URLDialog)).setText(url.getHost());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.IP))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                        String ip = ((EditText)dialogView.findViewById(R.id.URLDialog)).getText().toString();
                        if(checkIp(ip)) {
                            try {
                                setURL(getURL(ip));
                                saveUrl(getURL(ip));
                                dialog1.dismiss();
                            } catch (MalformedURLException e) {
                                Toast.makeText(MainActivity.this, getString(R.string.err3), Toast.LENGTH_LONG).show();
                                createIPDialog();
                            }
                        }else{
                            createIPDialog();
                            Toast.makeText(MainActivity.this, getString(R.string.err3), Toast.LENGTH_LONG).show();
                        }


                })
                .setNegativeButton(getString(R.string.annulla), (dialog1, which) -> {

                    if(url==null){
                        Toast.makeText(MainActivity.this, getString(R.string.err3), Toast.LENGTH_LONG).show();
                        createIPDialog();
                    }else{
                        dialog1.dismiss();
                    }

                })
                        .create();
        dialog.show(); // Mostra il dialogo

    }
    private void showURLDialog(){
        runOnUiThread(() -> createURLDialog());
    }
    private void createURLDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.url_dialog, null);

        if(url!=null){
            ((EditText)dialogView.findViewById(R.id.URLDialog)).setText(url.toString());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.URL))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.ok), (dialog1, which) -> {
                    String location = ((EditText)dialogView.findViewById(R.id.URLDialog)).getText().toString();

                        try {
                            url = new URL(location);
                            saveUrl(location);
                            dialog1.dismiss();
                        } catch (MalformedURLException e) {
                            Toast.makeText(MainActivity.this, getString(R.string.err3), Toast.LENGTH_LONG).show();
                            createIPDialog();
                        }

                })
                .setNegativeButton(getString(R.string.annulla), (dialog1, which) -> {

                    if(url==null){
                        Toast.makeText(MainActivity.this, getString(R.string.err3), Toast.LENGTH_LONG).show();
                        createIPDialog();
                    }else{
                        dialog1.dismiss();
                    }

                })
                .create();
        dialog.show(); // Mostra il dialogo

    }
    private boolean checkIp(String ip){
        String[] split = ip.split("\\.");
        if(split.length!=4)
            return false;
        for (String s : split) {
            if(Integer.parseInt(s)>255 || Integer.parseInt(s)<0)
                return false;
        }
        return true;
    }

    private void saveUrl(String urlString) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("URL", urlString);
        editor.apply();
    }

    private String loadUrl() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("URL", null);  // Restituisce null se l'URL non è stato ancora salvato
    }

    private File GetFile(Bitmap bitmap, String name){
        File file = new File(getExternalFilesDir(null), name);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // Salva il bitmap come PNG
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void clear(){
        drawingView.clear();
        resultEditText.setText("");
    }
}