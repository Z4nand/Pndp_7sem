

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        long startTime = System.currentTimeMillis();

        List<String> listJavaFile = searchJavaFiles("/home/zanand/Study/JavaFolder/pndp/Pndp_7sem/lab2");
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

    public static Map<String, List<String>> clsHandler(List<String> listJavaFile) throws IOException {
        Map<String, List<String>> parent_children = new HashMap<>(); // создаем map для хранения родителя и его детей
        Pattern pattern_full = Pattern.compile("(\\binterface\\s\\w+\\s\\bextends\\s[\\w, ]+\\b)|(\\bclass\\s\\w+\\s\\bextends\\s\\w+\\b)");

        CountDownLatch latch = new CountDownLatch(listJavaFile.size());

        System.out.printf("Всего в листе Java файлов: "+String.valueOf(listJavaFile.size())+"\n");

        AtomicInteger n= new AtomicInteger();

        for (String i : listJavaFile) {
            Thread thread = new Thread(() -> {
                try {

                    String text = readFromFile(i);
                    Matcher matcher = pattern_full.matcher(text);
                    while (matcher.find()) {
                        String line = matcher.group();
                        String[] parts = line.split("\\s");
                        String className = parts[1];
                        String parentName = parts[parts.length - 1];
                        synchronized (parent_children) {
                            List<String> childClasses = parent_children.getOrDefault(parentName, new ArrayList<>());
                            childClasses.add(className);
                            parent_children.put(parentName, childClasses);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    n.getAndIncrement();
                    latch.countDown();
                }

            });
            thread.start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("Счётчик: "+String.valueOf(n.get())+"\n");
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





