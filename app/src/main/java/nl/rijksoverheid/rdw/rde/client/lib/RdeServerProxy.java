package nl.rijksoverheid.rdw.rde.client.lib;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import android.os.StrictMode;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import nl.rijksoverheid.rdw.rde.remoteapi.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RdeServerProxy
{
    //TODO inject settings/providers and use an identity/discovery endpoint on the server
    private static final String enrollmentUrl = "https://192.168.178.12:45455/api/mobiledevices/documents";
    //Urls in the list should are fully qualified.
    private static final String messageListUrl = "https://192.168.178.12:45455/api/mobiledevices/messages/received";
    private static final String messageUrl = "https://192.168.178.12:45455/api/mobiledevices/messages/received/";

    public RdeServerProxy()
    {

    }

    public HttpResponse<DocumentEnrolmentResponse> enrol(final DocumentEnrolmentRequestArgs enrollmentArgs, final String authToken)
    {
        if (enrollmentArgs == null)
            throw new IllegalArgumentException();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //TODO map RdeDocumentEnrollmentInfo to the API DTO...
        try {
            final var requestBodyContent = new Gson().toJson(enrollmentArgs);
            final var client = getOkHttpClient();
            final var body = RequestBody.create(requestBodyContent, MediaType.get("application/json"));
            final var request = new Request.Builder()
                    .header("authorize", "bearer " + authToken)
                    //.header("Accept", "application/json")

                    .url(enrollmentUrl)
                    .post(body)
                    .build();

            final var response = client.newCall(request).execute();

            if (!response.isSuccessful())
                return new HttpResponse<DocumentEnrolmentResponse>(response.code(), response.body().string());

            final var obj = new Gson().fromJson(response.body().string(), DocumentEnrolmentResponse.class);
            return new HttpResponse<DocumentEnrolmentResponse>(obj);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return new HttpResponse<DocumentEnrolmentResponse>(500, "Unexpected error.");
        }
    }

    //TODO async task
    public HttpResponse<ReceivedMessageList> getMessages(final String authToken) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final var client = getOkHttpClient();
        final var request = new Request.Builder()
                .header("authorize", "bearer " + authToken)
                .header("Accept", "application/json; utf-8")
                .url(messageListUrl)
                .get()
                .build();

        final var response = client.newCall(request).execute();

        if (!response.isSuccessful())
            return new HttpResponse<ReceivedMessageList>(response.code(), response.body().string());

        final var obj = new Gson().fromJson(response.body().string(), ReceivedMessageList.class);
        return new HttpResponse<ReceivedMessageList>(obj);

    }

    //TODO async task
    public HttpResponse<ReceivedMessage> getMessage(String messageId, String authToken) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final var client = getOkHttpClient();

        final var builder = new Request.Builder();
        final var request = builder
                .header("authorize", "bearer " + authToken)
                .header("Accept", "application/json; utf-8")
                .url(messageUrl+ messageId)
                .get()
                .build();

        final var response = client.newCall(request).execute();

        if (!response.isSuccessful())
            return new HttpResponse<ReceivedMessage>(response.code(), response.body().string());

        final var obj = new Gson().fromJson(response.body().string(), ReceivedMessage.class);
        return new HttpResponse<ReceivedMessage>(obj);
    }


    //TODO bean such that this is only debug mode, not production
    @NonNull
    private OkHttpClient getOkHttpClient() throws NoSuchAlgorithmException, KeyManagementException
    {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
