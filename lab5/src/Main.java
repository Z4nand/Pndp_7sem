

//Лабораторная работа 5
//Поисковый робот с Executor Service
//В отличии от программы из задания 4 количество потоков должно быть ограниченным. Следует использовать один из
//вариантов Java Executor Service. Формирование единого индекса должно осуществляться с использованием Future.
//Все объекты Future необходимо собрать в список и только когда последнее "задание" на обработку будет выдано
//начинать считывать результаты (метод get())


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    // 1 Поиск Java файлов
    public static List<String> searchJavaFiles(String directoryPath) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            return paths.parallel()
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    // 2 Чтение
    public static String readFromFile(String filePath) throws IOException {
        String content;
        content = Files.readString(Path.of(filePath));
        return content;
    }

    // 3 Обработка
    public static Map<String, List<String>> clsHandler(List<String> listJavaFile) throws IOException, InterruptedException {

        Pattern pattern_full = Pattern.compile("(\\binterface\\s\\w+\\s\\bextends\\s[\\w, ]+\\b)|(\\bclass\\s\\w+\\s\\bextends\\s\\w+\\b)");
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        List<Future<Map<String, List<String>>>> futures = new ArrayList<>();
        for (String i : listJavaFile) {
            Callable<Map<String, List<String>>> fun = () -> { //
                Map<String, List<String>> classes = new HashMap();
                try {

                    String text = readFromFile(i);
                    Matcher matcher = pattern_full.matcher(text);
                    while (matcher.find()) {
                        String line = matcher.group();
                        String[] parts = line.split("\\s");
                        String className = parts[1];
                        String parentName = parts[parts.length - 1];
                        List<String> childClasses = classes.getOrDefault(parentName, new ArrayList<>());
                        childClasses.add(className);
                        classes.put(parentName, childClasses);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return classes;
            };
            Future<Map<String, List<String>>> future = executorService.submit(fun);
            futures.add(future);
        }

        Map<String, List<String>> parent_children = new HashMap<>();

        for (Future<Map<String, List<String>>> future : futures) {
            try {
                parent_children.putAll(future.get());
                
            } catch (
                    ExecutionException e) {
                e.printStackTrace();
            }

        }
//        Map<String, List<String>> result = future.get();
//        for (String key : result.keySet()) {
//            if (parent_children.containsKey(key)) {
//                List<String> children = parent_children.get(key);
//                children.addAll(result.get(key));
//                parent_children.put(key, children);
//            } else {
//                parent_children.put(key, result.get(key));
//            }
            executorService.shutdown();
            return parent_children;
        }

        // 4 print
        public static void printer (Map < String, List < String >> parent_children) throws IOException {
            for (Map.Entry<String, List<String>> entry : parent_children.entrySet()) {
                String parentClass = entry.getKey();
                List<String> childClasses = entry.getValue();
                System.out.println(parentClass + ": " + childClasses.toString());
            }
        }
    }




