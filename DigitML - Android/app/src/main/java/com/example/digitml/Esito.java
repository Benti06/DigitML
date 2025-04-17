package com.example.digitml;

public class Esito {
    private String status;
    private String message;

    private String prediction;
    Esito(String s, String m, String n){
        status = s;
        message = m;
        prediction =n;
    }

    Esito(){}

    public String getStatus() {
        return status;
    }

    // Getter e Setter
    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        if(status=="200")
            return ""+ prediction;
        else
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPrediction(String prediction){
        this.prediction = prediction;
    }

    public String getPrediction() {
        return prediction;
    }
}
