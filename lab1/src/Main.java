import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Task#1
// Счетчик слов
//В произвольном текстовом документе посчитать и напечатать в консоли сколько раз встречается каждое слово.
// Текст можно сформировать генератором lorem ipsum. Необходимо использовать регулярные выражения (regexp),
// должны корректно обрабатываться любые знаки препинания в любом количестве.

public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = "text.txt";  // путь к файлу

        String text = readFromFile(filePath);
        text= toLowText(text);

//        System.out.println(text);

        System.out.printf(countWords(text).toString());

    }


    // 1) Считывание из файла
    public static String readFromFile(String filePath) throws IOException {
        String content;
        content = Files.readString(Path.of(filePath));
        return content;
    }

    // 2) Приводим текст в нижний регистр
    public static String toLowText(String text){
        text=text.toLowerCase();
        return text;
    }

    //3) Подсчитываем слова (map)
    public static Map<String, Integer> countWords(String text) {
//
        Map<String, Integer> wordCountMap = new HashMap<>();

        // Регулярное выражение для поиска слов
        Pattern pattern = Pattern.compile("\\b\\w+\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
        }

        return wordCountMap;
    }

}
