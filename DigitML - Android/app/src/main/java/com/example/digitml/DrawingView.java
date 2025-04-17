package com.example.digitml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.util.Base64;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.constraintlayout.helper.widget.MotionEffect;

import java.io.ByteArrayOutputStream;

public class DrawingView extends View {
    private Path disegno;
    // Per tracciare il percorso del disegno, lo schema

    private Paint matita;
    // Per definire le proprietà del disegno

    public static final boolean MOVETO=false;
    public static final boolean START=true;
    private boolean action;
    private boolean strumento;
    public static final boolean MANO=true;
    public static final boolean SENSORI=false;

    
    private SensorPath sensor;
    private boolean sensorSupported;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager==null){
            sensorSupported = false;

        }else{
            sensorSupported = true;
            sensor = new SensorPath(context, this);

        }
        action = START;
        strumento = MANO;
    }

    public void setStrumento(boolean b){
        strumento = b;
        sensor.stop();

    }

    private void init() {
        //Color c = new Color(12,12,12);
        this.setBackgroundColor(Color.WHITE);
        // Inizializza il percorso e la vernice
        disegno = new Path();
        matita = new Paint();
        matita.setColor(Color.BLACK);         // Colore del disegno
        matita.setAntiAlias(true);            // Smussatura
        matita.setStrokeWidth(20);             // Spessore del tratto
        matita.setStyle(Paint.Style.STROKE); // Tipo di disegno (linea)
    }

    @Override
    //metodo chiamato ad ogni onThouchEvent.invalidate()
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);
        //Disegna il percorso tracciato sulla Canvas della View
        canvas.drawPath(disegno, matita);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Gestisci l'input dell'utente (touch)
        
        if(strumento) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    disegno.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    disegno.lineTo(x,y);
                    break;
            }
            invalidate();  // Richiede il ridisegno della vista in modo da visualizzare mentre si disegna
            
        }else{
            if(sensorSupported && event.getAction()== MotionEvent.ACTION_DOWN) {
                if (!sensor.isDrawing()) {
                    if(action) {
                        Log.e("disegno", "start");
                        sensor.start(event.getX(), event.getY());
                        action = MOVETO;
                    }else{
                        sensor.moveTo(event.getX(), event.getY());
                    }
                }
                else {
                    Log.e("disegno", "stop");
                    disegno = sensor.stop();
                    refresh();

                }
            }
        }
        return true;
    }
    public Path getPath(){
        return disegno;
    }
    public void setAction(boolean b){
        action = b;
    }
    public void refresh(){
        invalidate();
    }

    public Bitmap GetServerImage(int n){
        // Crea una nuova Bitmap con la stessa larghezza e altezza della vista
        //ALPHA_8 perchè è il formato più leggero e funzionale per quello che ci serve ovvero una scala grigi
        Bitmap bitmap = Bitmap.createBitmap(n, n, Bitmap.Config.RGB_565);
        if(!disegno.isEmpty()) {
            // Crea un nuovo Canvas con la Bitmap appena creata, a quanto pare non si puo modificare diretta la bitmaè
            Canvas canvas = new Canvas(bitmap);

            //Inverto i colori
            Paint m2=new Paint(matita);
            m2.setColor(Color.WHITE);
            m2.setStrokeWidth( ((float)n) /14);
            //canvas.drawColor(Color.BLACK);

            Path p2 = new Path();
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(((float)n) /getWidth(), ((float)n)/getHeight());
            disegno.transform(scaleMatrix, p2);

            // Disegna il bitmap seguendo il disegno con la matita
            canvas.drawPath(p2, m2);



            return bitmap; // Ritorna la Bitmap con il disegno fatto
        }
        else
            return null;
    }


    public String GetImage64(int n) {


        Bitmap bitmap = GetServerImage(n);

        if (bitmap != null) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Comprimiamo la Bitmap in formato PNG (puoi usare anche JPEG o WebP a seconda delle tue necessità)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Restituisce la stringa Base64
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } else {
            return null; // Se la bitmap è null, ritorna null
        }
    }

    public void clear(){
        //creo un nuovo disegno
        disegno.reset();
        sensor.clear();
        invalidate();
    }

    public void SetSpessore(int n){
        //Resetto il disegno
        clear();
        sensor.clear();
        //Cambio lo spessore
        matita.setStrokeWidth(n);
    }

    public int GetSpessore(){
        return (int)matita.getStrokeWidth();
    }

}
