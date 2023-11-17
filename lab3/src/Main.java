

//Лабораторная работа 2
//Поисковый робот для исходного текста программы
//Для Java проекта (локальной папки) построить и напечатать в консоли обратный индекс наследования классов.
//Для каждого класса необходимо найти (напечатать) классы, для которых он является базовым (родительским).
//Должны корректно обрабатываться ключевые слова class, interface, extends, implements.
//Необходимо использовать интерфейс Map, метод getOrDefault(). Желательно использовать Stream API.

//Лабораторная работа 3
//Усовершенствовать программу из задания 2, чтобы для обработки каждого файла исходного текста создавался отдельный
//поток (Thread). Взаимодействия потоков не требуется! Для ожидания завершения потоков можно использовать метод join(),
//желательно CountDownLatch. Работоспособность программы должна быть продемонстрирована на большом проекте с GitHub,
//например, Spring Framework.

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();

        List<String> listJavaFile = searchJavaFiles("/home/zanand/Study/JavaFolder/pndp/spring-framework");
        printer(clsHandler(listJavaFile));


        // Фиксируем конечное время выполнения программы
        long endTime = System.currentTimeMillis();
        System.out.println("Время выполнения программы: " + (endTime - startTime) + " миллисекунд");
    }


    //1 Поиск Java файлов
    public static List<String> searchJavaFiles(String directoryPath) throws IOException {
        try(Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            return paths.parallel()
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }
    //2 Чтение
    public static String readFromFile(String filePath) throws IOException {
        String content;
        content = Files.readString(Path.of(filePath));
        return content;
    }

    //3 Обработка

    public static  Map<String, List<String>> clsHandler(List<String> listJavaFile) throws IOException, InterruptedException {

        List<String> listSelections = new ArrayList<>(); // создаем массив для хранения выборочных данных

        Pattern pattern_full = Pattern.compile("(\\binterface\\s\\w+\\s\\bextends\\s[\\w, ]+\\b)|(\\bclass\\s\\w+\\s\\bextends\\s\\w+\\b)");

// Создаем счетчик потоков
        CountDownLatch latch = new CountDownLatch(listJavaFile.size());

        for (String i : listJavaFile) {
            Thread thread = new Thread(() -> {
                String text = null;
                try {
                    text = readFromFile(i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Matcher matcher = pattern_full.matcher(text);
                while (matcher.find()) {
                    listSelections.add(matcher.group());
                }
                latch.countDown(); // минусуем потоки, когда они отработали
            });
            thread.start();
        }

        latch.await(); // ждем, пока все потоки завершат свою работу


        Map<String, List<String>> parent_children = new HashMap<>();


        for (String line : listSelections) {
            String[] parts = line.split("\\s");
            String className = parts[1];
            String parentName = parts[parts.length - 1];
            List<String> childClasses = parent_children.getOrDefault(parentName, new ArrayList<>());
            childClasses.add(className);

            parent_children.put(parentName, childClasses);

        }


        return parent_children;
    }

    //4 print
    public static void printer(Map<String, List<String>> parent_children) throws IOException {
        for (Map.Entry<String, List<String>> entry : parent_children.entrySet()) {
            String parentClass = entry.getKey();
            List<String> childClasses = entry.getValue();

            System.out.println(parentClass + ": " + childClasses.toString());
        }

    }

}





