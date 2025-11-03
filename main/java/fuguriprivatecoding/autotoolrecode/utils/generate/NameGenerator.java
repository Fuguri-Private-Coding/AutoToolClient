package fuguriprivatecoding.autotoolrecode.utils.generate;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@UtilityClass
public class NameGenerator {

    private List<String> prefixes = new ArrayList<>();
    private List<String> suffixes = new ArrayList<>();
    private List<String> fullNicks = new ArrayList<>();
    private final Random random = new Random();

    public void init(String resourcePath) throws IOException {
        fullNicks = loadNicknamesFromResource(resourcePath);

        if (fullNicks.isEmpty()) {
            throw new IllegalArgumentException("Файл с никнеймами пуст или не найден!");
        }

        prefixes = extractPrefixes(fullNicks);
        suffixes = extractSuffixes(fullNicks);
    }

    private static List<String> loadNicknamesFromResource(String resourcePath) throws IOException {
        List<String> nicks = new ArrayList<>();
        try (InputStream is = NameGenerator.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IOException("Файл не найден: " + resourcePath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    nicks.add(line);
                }
            }
        }
        return nicks;
    }

    private List<String> extractPrefixes(List<String> nicks) {
        List<String> prefixes = new ArrayList<>();
        for (String nick : nicks) {
            int splitPoint = Math.min(3 + random.nextInt(3), nick.length());
            prefixes.add(nick.substring(0, splitPoint));
        }
        return prefixes;
    }

    private List<String> extractSuffixes(List<String> nicks) {
        List<String> suffixes = new ArrayList<>();
        for (String nick : nicks) {
            int splitPoint = Math.max(0, nick.length() - (3 + random.nextInt(3)));
            suffixes.add(nick.substring(splitPoint));
        }
        return suffixes;
    }

    public String generate(int maxLength) {
        String nickname;
        int attempt = 0;
        do {
            int method = random.nextInt(3);
            nickname = switch (method) {
                case 0 -> getRandom(prefixes) + getRandom(suffixes);
                case 1 -> getRandom(fullNicks) + getPopularSuffix();
                case 2 -> getRandom(prefixes) + getRandom(prefixes) + getRandom(suffixes);
                default -> getRandom(fullNicks);
            };
            attempt++;
        } while (nickname.length() > maxLength && attempt < 10);

        if (nickname.length() > maxLength) {
            nickname = nickname.substring(0, maxLength);
        }

        return nickname;
    }

    private String getRandom(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }

    private String getPopularSuffix() {
        String[] popularSuffixes = {"YT", "Pro", "X", "Z", "GG", "HD", "FX", "TTV", "UA", "DE","RU","YO", "XD", "YTPro"};
        return popularSuffixes[random.nextInt(popularSuffixes.length)];
    }
}