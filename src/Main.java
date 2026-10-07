import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public class Main {

    // ---------- ЗАПУСК ПРОГРАМИ ----------
    public static void main(String[] args) throws Exception {
        // Хороший користувач
        User u1 = new User("Олена", "olena@mail.com", 25);
        // Поганий користувач: порожнє ім'я, email без @, вік 5
        User u2 = new User("", "olenamail.com", 5);
        // Поганий товар: немає назви, ціна мінус, залишок дуже великий
        Product p1 = new Product(null, -10, 99999);

        System.out.println("--- Користувач 1 ---");
        check(u1);
        System.out.println("--- Користувач 2 ---");
        check(u2);
        System.out.println("--- Товар ---");
        check(p1);

        System.out.println("--- ПІДСУМОК ---");
        System.out.println("Помилок: " + errors + ", попереджень: " + warnings);
    }

    // Лічильники для звіту
    static int errors = 0;
    static int warnings = 0;

    // ---------- ПЕРЕВІРКА ЧЕРЕЗ РЕФЛЕКСІЮ ----------
    // Бере будь-який об'єкт, дивиться на його поля і на анотації над ними
    static void check(Object obj) throws Exception {
        Field[] fields = obj.getClass().getDeclaredFields(); // усі поля класу

        for (Field f : fields) {
            Object value = f.get(obj); // значення поля

            // 1) Перевірка @Required: поле не має бути порожнім
            Required req = f.getAnnotation(Required.class);
            if (req != null) {
                if (value == null || value.toString().isEmpty()) {
                    report(f.getName(), req.message(), req.warning());
                    continue; // порожнє поле далі не перевіряємо
                }
            }

            // 2) Перевірка @Range: число має бути між min і max
            Range range = f.getAnnotation(Range.class);
            if (range != null && value != null) {
                int number = (Integer) value;
                if (number < range.min() || number > range.max()) {
                    report(f.getName(), range.message(), range.warning());
                }
            }

            // 3) Перевірка @Format: текст має відповідати шаблону
            Format format = f.getAnnotation(Format.class);
            if (format != null && value != null) {
                if (!value.toString().matches(format.regex())) {
                    report(f.getName(), format.message(), format.warning());
                }
            }
        }
    }

    // Друкує один рядок звіту і рахує його
    static void report(String field, String message, boolean isWarning) {
        if (isWarning) {
            warnings++;
            System.out.println("  [ПОПЕРЕДЖЕННЯ] поле " + field + ": " + message);
        } else {
            errors++;
            System.out.println("  [ПОМИЛКА] поле " + field + ": " + message);
        }
    }
}

// ---------- АНОТАЦІЇ ----------
// RUNTIME = анотація «живе» під час роботи програми, тому її можна прочитати

@Retention(RetentionPolicy.RUNTIME)
@interface Required {
    String message() default "Поле обов'язкове";
    boolean warning() default false; // false = помилка, true = попередження
}

@Retention(RetentionPolicy.RUNTIME)
@interface Range {
    int min() default 0;
    int max() default 100;
    String message() default "Число поза межами";
    boolean warning() default false;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Format {
    String regex();
    String message() default "Неправильний формат";
    boolean warning() default false;
}

// ---------- МОДЕЛІ (дані, які перевіряємо) ----------

class User {
    @Required(message = "Ім'я не може бути порожнім")
    String name;

    @Required
    @Format(regex = ".+@.+\\..+", message = "Email має бути виду a@b.c")
    String email;

    @Range(min = 14, max = 120, message = "Вік має бути від 14 до 120")
    Integer age;

    User(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}

class Product {
    @Required(message = "Назва товару обов'язкова")
    String title;

    @Range(min = 1, max = 1000000, message = "Ціна має бути додатною")
    Integer price;

    @Range(min = 0, max = 10000, warning = true, message = "Дуже великий залишок")
    Integer stock;

    Product(String title, Integer price, Integer stock) {
        this.title = title;
        this.price = price;
        this.stock = stock;
    }
}