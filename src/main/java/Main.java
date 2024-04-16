import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private static final String ITEM_REGEX = "(?i)name:(?<name>[^:]+);price:(?<price>[^;]+);type:(?<type>[^;]+);expiration:(?<expiration>[^#]+)##";

    public String readRawDataToString() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        return IOUtils.toString(classLoader.getResourceAsStream("RawData.txt"));
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        String rawData = main.readRawDataToString();
        Map<String, Integer> errorCount = new HashMap<>();
        List<Items> itemsList = new ArrayList<>();

        // Parse using regex
        Pattern pattern = Pattern.compile(ITEM_REGEX);
        Matcher matcher = pattern.matcher(rawData);
        while (matcher.find()) {
            try {
                String name = matcher.group("name").trim();
                String price = matcher.group("price").trim();
                String type = matcher.group("type").trim();
                String expiration = matcher.group("expiration").trim();
                itemsList.add(new Items(name, price, type, expiration));
            } catch (Exception e) {
                // Increment error count for each exception
                errorCount.put(e.getClass().getSimpleName(), errorCount.getOrDefault(e.getClass().getSimpleName(), 0) + 1);
            }
        }

        // Printing parsed items
        Map<String, List<Items>> groupedItems = itemsList.stream()
                .collect(Collectors.groupingBy(Items::getName));

        groupedItems.forEach((name, items) -> {
            System.out.println("name: " + name + " ".repeat(Math.max(0, 9 - name.length())) + "seen: " + items.size() + " times");
            System.out.println("=".repeat(13) + " ".repeat(2) + "=".repeat(12));
            Map<String, Long> priceCount = items.stream()
                    .collect(Collectors.groupingBy(Items::getPrice, Collectors.counting()));
            priceCount.forEach((price, count) -> {
                System.out.println("Price: " + price + " ".repeat(Math.max(0, 9 - price.length())) + "seen: " + count + " times");
                if (!priceCount.keySet().stream().skip(1).findFirst().equals(price)) {
                    System.out.println("-".repeat(13) + " ".repeat(2) + "-".repeat(12));
                }
            });
            System.out.println();
        });

// Printing error counts
        System.out.println("Errors" + " ".repeat(Math.max(0, 12 - "Errors".length())) + "seen: " + errorCount.values().stream().mapToInt(Integer::intValue).sum() + " times");
        errorCount.forEach((error, count) -> System.out.println(error + " ".repeat(Math.max(0, 12 - error.length())) + "seen: " + count + " times"));
    }
}