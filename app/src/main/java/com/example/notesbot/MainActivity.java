package com.example.notesbot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.text.HtmlCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Aplicación que conecta un asistente de IBM Watson con nuestra aplicación android.
 *
 * Vídeo explicativo: https://www.youtube.com/watch?v=G4rXpe9Wod8
 *
 * Necesario implementar la siguiente librería en el build.gradle (Module:app):
 * implementation 'com.amitshekhar.android:android-networking:1.0.0'
 *
 * Además debemos darle permiso de conexión a Internet en el Manifest:
 * <uses-permission android:name="android.permission.INTERNET"/>
 *
 * y en el mismo manifest establecemos la posición de la pantalla a sólo vertical:
 * android:screenOrientation="portrait"
 */


public class MainActivity extends AppCompatActivity {

    //Variables
    EditText et_mensaje;
    Button b_enviar;
    Context context;
    LinearLayout vista_mensajes;
    ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Asociamos los atributos con los componentes del layout.
        context=this;
        et_mensaje = findViewById(R.id.et_mensaje);
        b_enviar = findViewById(R.id.b_enviar);
        vista_mensajes = findViewById(R.id.vista_mensajes);
        scroll = findViewById(R.id.ScrollView);

        /*Mensaje de Bienvenida, para realizar en un futuro
        TextView miTextView = new TextView(getApplicationContext());
        miTextView.setPadding(30,0, 250, 0);
        miTextView.setGravity(Gravity.LEFT);
        //miTextView.setText(outputJsonObject);
        miTextView.append(HtmlCompat.fromHtml("<p><b> Notebot: </b></p><p>Hola, soy Notebot. ¿En qué te puedo ayudar? </p>", Html.FROM_HTML_MODE_COMPACT));
        miTextView.setBackgroundResource(R.drawable.borde_entrada);
        */

        b_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String input = et_mensaje.getText().toString();

                TextView miTextView = new TextView(getApplicationContext());

                /*
                int ancho=LinearLayout.LayoutParams.WRAP_CONTENT;
                int alto=LinearLayout.LayoutParams.WRAP_CONTENT;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ancho, alto);
                lp.setMargins(40, 0, 10, 5);
                miTextView.setLayoutParams(lp);*/

                miTextView.setPadding(250,0, 30, 0);
                miTextView.setGravity(Gravity.RIGHT);
                //miTextView.setText(input);
                miTextView.append(HtmlCompat.fromHtml("<p><b>Tú\n </b></p><p>"+input+"</p>", Html.FROM_HTML_MODE_COMPACT));
                //miTextView.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                miTextView.setBackgroundResource(R.drawable.borde_entrada);


                et_mensaje.setText("");
                vista_mensajes.addView(miTextView);

                getResponse(input);

            }
        });
    }

    /**
     * Método getResponse que recoge el mensaje del usuario y se lo envia al Asistente para que nos de la respuesta
     * @param input que es el mensaje del usuario
     * @throws JSONException se lanza para indicar un problema con la API JSON
     */
    private void getResponse(String input) {
        //Asignamos las credenciales de nuestro asistente
        //Para asignar el authentication (usuario + password) hay que hacerlo en base 64, en la siguiente página:
        //https://www.base64encode.org/

        String workspaceId = "Aquí va el ID de tu Workspace";
        String urlAssistant = "Aquí va la URL de tu Assistant"+workspaceId+"/message?version=2018-09-20"; //Añadir la versión
        String authentication = "Aquí va la autentificación {usuario : password}"; //Pasados a base 64

        //Creo la estructura JSON de input del usuario
        JSONObject inputjsonObject = new JSONObject();

        try {
            inputjsonObject.put("text", input);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("input", inputjsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**
         * Conexión de Android con el Asistente
         * @param urlAssistant es la URL de las credenciales de nuestro asistente
         * @throws JSONException se lanza para indicar un problema con la API JSON
         */
        AndroidNetworking.post(urlAssistant)
                //Establecemos las cabeceras y demás sacadas de Postman
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Basic "+authentication) //Acordarse de dejar un espacio despues de Basic
                .addJSONObjectBody(jsonBody)
                .setPriority(Priority.HIGH)
                .setTag(getString(R.string.app_name))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onResponse(JSONObject response) {
                        String outputAssistant = "";

                        //Parseo la respuesta del json
                        try{
                            //Esto es lo que me llega desde el Servicio del Asistente
                            String outputJsonObject = response.getJSONObject("output").getJSONArray("text").getString(0);
                            TextView miTextView = new TextView(getApplicationContext());
                            miTextView.setPadding(30,0, 250, 0);
                            miTextView.setGravity(Gravity.LEFT);
                            //miTextView.setText(outputJsonObject);
                            miTextView.append(HtmlCompat.fromHtml("<p><b> Notebot: </b></p><p>" +outputJsonObject+ "</p>", Html.FROM_HTML_MODE_COMPACT));
                            miTextView.setBackgroundResource(R.drawable.borde_entrada);



                            //Añado a la vista de mentajes un nuevo TextView con la información que le llega
                            vista_mensajes.addView(miTextView);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }

                    /**
                     * Método que muestra un Error en la conexión con el Asistente
                     */
                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_LONG).show();
                        Log.e("errorWatson", anError.toString());

                    }
                });

    }
}
