import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import model.TaskItem;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SheetsExample {

    private static final String APPLICATION_NAME = "Richard App";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd MMM yyyy");
    private static Date currentDate = new Date();

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = SheetsExample.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException, ParseException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1iVqSy76TR4nWoEAS5eTa6yr5E2E8h_epPCjuXM61MyU";
        final String range = "Richard Sheet!A:C";

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();

        List<TaskItem> taskItemList = null;

        List<String> todoList = new ArrayList<>();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found");
        } else {
            taskItemList = new ArrayList<>();
            for (List row : values) {
                taskItemList.add(new TaskItem(FORMATTER.parse((String) row.get(0)), (String) row.get(1), Integer.parseInt((String) row.get(2))));
            }
        }

        if (taskItemList != null) {
            for (TaskItem item : taskItemList) {
                System.out.println(item.getStartDate());
                System.out.println(item.getTaskName());
                System.out.println(item.getFrequencyInDays());
                System.out.println();
                long diffInMillies = Math.abs(currentDate.getTime() - item.getStartDate().getTime());
                long diffInLong = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                int diff = (int) diffInLong;
                System.out.println("Difference in days (long): " + diffInLong);
                System.out.println("Difference in days (int): " + diff);

                if (diff % item.getFrequencyInDays() == 0) {
                    todoList.add(item.getTaskName());
                }
            }
        }

        System.out.println();

        if (todoList.isEmpty()) {
            System.out.println("Nothing to be done today~");
        } else {
            System.out.println("To-do for today:");
            for (String todoName : todoList) {
                System.out.println("- " + todoName);
            }
        }
    }
}
