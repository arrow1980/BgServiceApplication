package com.example.abneryu.bgserviceapplication;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MQIntentService extends IntentService {
    private static final String TAG = MQIntentService.class.getName();

    private NotificationManager notificationManager;
    private Notification notification;

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private Consumer consumer;

    public MQIntentService() {
        super("MQIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        new MQClient().execute();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
//        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        new MQClient().execute();
        return Service.START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @SuppressLint("StaticFieldLeak")
    private class MQClient extends AsyncTask<Void, String, Void> {
        private static final String QUEUE_NAME = "inQueue";

        @Override
        protected Void doInBackground(Void... voids) {
            factory = new ConnectionFactory();
            factory.setHost("172.16.3.203");
            factory.setVirtualHost("vh_TEST");
            factory.setUsername("abner");
            factory.setPassword("abnerPcc.123456");
            factory.setRequestedHeartbeat(10);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(10000);

            try {
                connection = factory.newConnection();
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(" [x] Received message: '" + message + "'");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            notification = new Notification.Builder(getApplicationContext())
                                    .setContentTitle("Hello Android")
                                    .setContentText(message)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setAutoCancel(true)
                                    .build();
                        }
                        notificationManager
                                .notify(1, notification);
                    }
                };
                channel.basicConsume(QUEUE_NAME, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            channel.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
