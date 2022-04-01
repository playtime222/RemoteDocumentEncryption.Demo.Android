package nl.rijksoverheid.rdw.rde.clientlib.remoteapi;

import android.os.StrictMode;
import android.util.Base64;

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

public class RdeServerProxy
{
    //TODO inject settings/providers
    private static final String enrollmentUrl = "http://192.168.178.12:8080/api/document";
    private static final String messageListUrl = "http://192.168.178.12:8080/api/message/list";

    //Inject
    private static final String userName = "a1@mefitihe.com"; //Actually email
    private static final String password = "a1";

    private final static MessageListResult messageListErrorResult = new MessageListResult(true);
    private final static MessageGetResult messageGetErrorResult = new MessageGetResult(true);

    public RdeServerProxy()
    {

    }

    public DocumentAddResult send(final EnrollDocumentDto enrollmentArgs)
    {
        if (enrollmentArgs == null)
            throw new IllegalArgumentException();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //TODO map RdeDocumentEnrollmentInfo to the API DTO...
        try
        {
            final var connection = (HttpURLConnection) new URL(enrollmentUrl).openConnection();

            final var base64EncodedCredentials = "Basic " + Base64.encodeToString((userName + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", base64EncodedCredentials);

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            connection.setRequestProperty("Accept", "application/json; utf-8");

            try(final var requestWriter = new OutputStreamWriter(connection.getOutputStream()))
            {
                final var requestBody = new Gson().toJson(enrollmentArgs);
                requestWriter.write(requestBody);
                requestWriter.flush();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                return new DocumentAddResult(EnrollDocumentResult.Other);
            }

            try(final var responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
            {
                final var result = new StringBuilder();
                String buffer;
                while ((buffer = responseReader.readLine()) != null)
                {
                    result.append(buffer); //TODO .trim()?
                }
                return new Gson().fromJson(result.toString(), DocumentAddResult.class);
            }
        }
        catch(IOException ex)
        {
            //TODO log
            ex.printStackTrace();
            return new DocumentAddResult(EnrollDocumentResult.Other);
        }
    }

    //TODO async task
    public MessageListResult GetMessages()
    {
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpURLConnection connection;
        try
        {
            connection = (HttpURLConnection) new URL(messageListUrl).openConnection();
            final var base64EncodedCredentials = "Basic " + Base64.encodeToString((userName + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", base64EncodedCredentials);

            connection.setRequestMethod("GET");

            connection.setRequestProperty("Accept", "application/json; utf-8");
        }
        catch (ProtocolException e)
        {
            e.printStackTrace();
            return messageListErrorResult;
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            return messageListErrorResult;
        } catch (IOException e)
        {
            e.printStackTrace();
            return messageListErrorResult;
        }

        try(var responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
        {
            final var result = new StringBuilder();
            String responseLine;
            while ((responseLine = responseReader.readLine()) != null)
            {
                result.append(responseLine); //TODO .trim()?
            }
            return new Gson().fromJson(result.toString(), MessageListResult.class);
        }
        catch (IOException ex)
        {
            //TODO log
            ex.printStackTrace();
            return messageListErrorResult;
        }
    }



    //TODO async task
    public MessageGetResult GetMessage(String itemUrl)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpURLConnection connection;
        try
        {
            connection = (HttpURLConnection) new URL(itemUrl).openConnection();

            var base64EncodedCredentials = "Basic " + Base64.encodeToString((userName + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", base64EncodedCredentials);

            connection.setRequestMethod("GET");

            //connection.setRequestProperty("Accept", "application/json; utf-8");
        }
        catch (ProtocolException e)
        {
            e.printStackTrace();
            return messageGetErrorResult;
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            return messageGetErrorResult;
        } catch (IOException e)
        {
            e.printStackTrace();
            return messageGetErrorResult;
        }

        try(var responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
        {
            var result = new StringBuilder();
            String responseLine;
            while ((responseLine = responseReader.readLine()) != null)
            {
                result.append(responseLine); //TODO .trim()?
            }

            var jsonString = result.toString();

            var builder = new GsonBuilder();
            //builder.registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getEncoder().encodeToString(src)));
            builder.registerTypeAdapter(byte[].class, (JsonDeserializer<byte[]>) (json, typeOfT, context) -> Base64.decode(json.getAsString(), Base64.NO_WRAP));
            return builder.create().fromJson(jsonString, MessageGetResult.class);
        }
        catch (Exception ex)
        {
            //TODO log
            ex.printStackTrace();
            return messageGetErrorResult;
        }
    }
}
