package cpe.moi.hello.projetiot;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private String IP = "192.168.2.208";
    private final int PORT = 3000;
    private String ordre = "LTH";
    private DatagramSocket UDPSocket;
    private InetAddress address;
    private Button valider;
    private Button getter;
    private EditText ip;
    private RadioGroup group;
    private TextView textTemp, textHumi, textLumi,textPress,textIR,textUV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textTemp = findViewById(R.id.temp);
        textLumi = findViewById(R.id.lumi);
        textHumi = findViewById(R.id.humi);
        textPress = findViewById(R.id.press);
        textUV = findViewById(R.id.uv);
        textIR = findViewById(R.id.ir);
        this.getter = findViewById(R.id.getting);
        this.valider = findViewById(R.id.valid2);
        initNetwork(IP, PORT);


        this.getter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String message = "getValues()";
                sendNetworkMessage(message);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        this.notif("resume");


    }
    private void notif(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initNetwork(String ip, int port){
        try {
            this.UDPSocket = new DatagramSocket(port);
            this.address = InetAddress.getByName(ip);
            (new MessageReceiver()).execute();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

//    View.OnClickListener getValListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            sendNetworkMessage("getValues()");
//        }
//    };
//
//
//    View.OnClickListener validListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            IP = ip.getText().toString();
//            ordre = ((RadioButton) findViewById(group.getCheckedRadioButtonId())).getText().toString();
//            setContentView(R.layout.activity_main);
//            //
//            initNetwork(IP, PORT);
//            sendNetworkMessage(ordre);
//        }
//    };

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_edit:
                setContentView(R.layout.param);
                this.valider = findViewById(R.id.valid2);
                this.ip = findViewById(R.id.ip);
                this.group = findViewById(R.id.radgroup);
                this.valider.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        ordre = ((RadioButton) findViewById(group.getCheckedRadioButtonId())).getText().toString();
                        setContentView(R.layout.activity_main);
                        sendNetworkMessage(ordre);
                        findViewById(R.id.getting).setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                String message = "getValues()";
                                sendNetworkMessage(message);
                            }
                        });
                        IP = ip.getText().toString();
                        initNetwork(IP, PORT);
                    }
                });
                //this.valider.setOnClickListener(validListener);
                return true;
        }
        return onOptionsItemSelected(item);
    }

    // message : THL/TLH/
    protected void sendNetworkMessage(final String str) {
        (new Thread() {
            public void run() {
                try {
                    byte[] data = str.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
                    UDPSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

        private class MessageReceiver extends AsyncTask<Void, byte[], Void>{
            protected Void doInBackground(Void... rien){
                while(true){
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        UDPSocket.receive(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int size = packet.getLength();
                    publishProgress(java.util.Arrays.copyOf(data, size));
                }
            }

            protected void onProgressUpdate(byte[]... data){
                String message = new String(data[0], StandardCharsets.UTF_8);
                System.out.println("LOL BONJOUR DEBUG : "+message);
                JSONObject jsonData = null;
                try {
                    // TODO dégueulasse
                    jsonData = new JSONObject(message);
                    int temp = Integer.parseInt(jsonData.get("temperature").toString());
                    int hum = Integer.parseInt(jsonData.get("humidity").toString());
                    int lum = Integer.parseInt(jsonData.get("luminosity").toString());
                    int p = Integer.parseInt(jsonData.get("pressure").toString());
                    int u = Integer.parseInt(jsonData.get("uv").toString());
                    int i = Integer.parseInt(jsonData.get("ir").toString());
                    textPress.setText(p+" hPa");
                    textUV.setText(u+"");
                    textIR.setText(i+"");
                    textTemp.setText((temp/10)+","+(temp%10)+"°C");
                    textHumi.setText((hum/10)+","+(hum%10)+"%");
                    textLumi.setText(lum+" lux");
                } catch (JSONException e) {
                    System.out.println("invalid response");
                    e.printStackTrace();
                }
            }
        }
}
