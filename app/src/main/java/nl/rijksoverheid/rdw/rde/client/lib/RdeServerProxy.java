package nl.rijksoverheid.rdw.rde.client.lib;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import android.os.StrictMode;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import nl.rijksoverheid.rdw.rde.client.lib.data.DocumentEnrolmentRequestArgs;
import nl.rijksoverheid.rdw.rde.client.lib.data.DocumentEnrolmentResponse;
import nl.rijksoverheid.rdw.rde.client.lib.data.ReceivedMessage;
import nl.rijksoverheid.rdw.rde.client.lib.data.ReceivedMessageList;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class RdeServerProxy
{
    //TODO GET IDENTITY URL FROM THE TOKEN!!!!!

    private IdentityDocument identityDocument;

    private String getUrl(String name) {
        return Arrays.stream(identityDocument.getServices())
                .filter(x -> x.getId().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find service in identity.") )
                .getUrl();
    }

    public RdeServerProxy()
    {
    }

    public HttpResponse<DocumentEnrolmentResponse> enrol(final DocumentEnrolmentRequestArgs enrollmentArgs, final ServicesToken servicesToken) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        if (enrollmentArgs == null)
            throw new IllegalArgumentException();

        ensureIdentity(servicesToken.getIdentityUrl());

        //TODO Not best practice
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //TODO map RdeDocumentEnrollmentInfo to the API DTO...
        try {
            final var requestBodyContent = new Gson().toJson(enrollmentArgs);
            final var client = getOkHttpClient();
            final var body = RequestBody.create(requestBodyContent, MediaType.get("application/json"));
            final var request = new Request.Builder()
                    .header("authorize", "bearer " + servicesToken.getAuthToken())
                    //.header("Accept", "application/json")
                    .url(getUrl("documents.add"))
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

    private void ensureIdentity(String identityUrl) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        if (identityDocument != null)
            return;

        //TODO Not best practice
        final var policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final var client = getOkHttpClient();

        final var request = new Request.Builder()
                .header("Accept", "application/json; utf-8")
                .url(identityUrl)
                .get()
                .build();

        final var response = client.newCall(request).execute();

        if (!response.isSuccessful())
            throw new IllegalStateException("Cannot find identity.");

        identityDocument = new Gson().fromJson(response.body().string(), IdentityDocument.class);
    }

    //TODO async task
    public HttpResponse<ReceivedMessageList> getMessages(final ServicesToken servicesToken) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        ensureIdentity(servicesToken.getIdentityUrl());

        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final var client = getOkHttpClient();
        final var request = new Request.Builder()
                .header("authorize", "bearer " + servicesToken.getAuthToken())
                .header("Accept", "application/json; utf-8")
                .url(getUrl("messages.list"))
                .get()
                .build();

        final var response = client.newCall(request).execute();

        if (!response.isSuccessful())
            return new HttpResponse<ReceivedMessageList>(response.code(), response.body().string());

        final var obj = new Gson().fromJson(response.body().string(), ReceivedMessageList.class);
        return new HttpResponse<ReceivedMessageList>(obj);

    }


    //TODO async task
    public HttpResponse<ReceivedMessage> getMessage(String messageId, ServicesToken servicesToken) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        ensureIdentity(servicesToken.getIdentityUrl());

        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final var client = getOkHttpClient();

        final var builder = new Request.Builder();
        final var request = builder
                .header("authorize", "bearer " + servicesToken.getAuthToken())
                .header("Accept", "application/json; utf-8")
                .url(getUrl("messages.getById") + "/" + messageId)
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
