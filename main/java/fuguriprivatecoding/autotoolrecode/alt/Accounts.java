package fuguriprivatecoding.autotoolrecode.alt;

import com.google.gson.*;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.file.FileUtils;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Accounts {

    File accountDirectory = new File(Client.INST.getName() + "/account");
    File accountFile = new File(accountDirectory, "accounts.json");

    public void init() {
        if (!accountDirectory.exists()) accountDirectory.mkdirs();
    }

    public void loadAccounts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(accountFile))) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            List<Account> accounts = parseAccountsFromJson(json);
            Client.INST.getAltScreen().accounts.clear();
            Client.INST.getAltScreen().accounts.addAll(accounts);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void saveAccounts() {
        FileUtils.createIfNotExists(accountFile);
        JsonObject accountsJson = createAccountsJson();

        try (PrintWriter writer = new PrintWriter(new FileWriter(accountFile))) {
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(accountsJson);
            writer.println(json);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private List<Account> parseAccountsFromJson(JsonObject json) {
        List<Account> accounts = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Account account = parseAccount(entry.getValue().getAsJsonObject());
            accounts.add(account);
        }

        return accounts;
    }

    private Account parseAccount(JsonObject accountData) {
        String name = accountData.get("name").getAsString();

        if (isPremiumAccount(accountData)) {
            String token = accountData.get("token").getAsString();
            String uuid = accountData.get("uuid").getAsString();
            return new Account(name, token, uuid);
        } else {
            return new Account(name);
        }
    }

    private boolean isPremiumAccount(JsonObject accountData) {
        return accountData.has("uuid") && accountData.has("token") &&
            accountData.get("uuid") != null && accountData.get("token") != null;
    }

    private JsonObject createAccountsJson() {
        JsonObject mainObject = new JsonObject();

        for (Account account : Client.INST.getAltScreen().accounts) {
            JsonObject accountJson = createAccountJson(account);
            mainObject.add(account.getName(), accountJson);
        }

        return mainObject;
    }

    private JsonObject createAccountJson(Account account) {
        JsonObject accountJson = new JsonObject();
        accountJson.addProperty("name", account.getName());

        if (isPremiumAccount(account)) {
            accountJson.addProperty("uuid", account.getUuid());
            accountJson.addProperty("token", account.getRefreshToken());
        }

        return accountJson;
    }

    private boolean isPremiumAccount(Account account) {
        return account.getUuid() != null && account.getRefreshToken() != null;
    }
}
